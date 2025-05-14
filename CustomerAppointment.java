import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class CustomerAppointment extends JPanel {
    private Connection connection;
    private CustomerFrame parent;
    private int customerId;
    private JPanel appointmentsListPanel;
    private Timer refreshTimer;

    public CustomerAppointment(Connection connection, CustomerFrame parent, int customerId) {
        this.connection = connection;
        this.parent     = parent;
        this.customerId = customerId;
        init();
        updatePastAppointments();
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> {
                    updatePastAppointments();
                    loadAppointments();
                });
            }
        }, 30000, 30000);
    }

    private void updatePastAppointments() {
        try {
            String sql = "UPDATE appointment SET appointment_status='completed' "
                       + "WHERE appointment_status='accepted' AND EndDate< NOW() AND customer_id=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, customerId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(CustomerFrame.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CustomerFrame.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JLabel lbl = new JLabel("Λίστα Ραντεβού:");
        lbl.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(lbl, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Ανανέωση");
        btnRefresh.setBackground(CustomerFrame.ORANGE_PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> { updatePastAppointments(); loadAppointments(); });
        header.add(btnRefresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Body
        appointmentsListPanel = new JPanel();
        appointmentsListPanel.setLayout(new BoxLayout(appointmentsListPanel, BoxLayout.Y_AXIS));
        appointmentsListPanel.setBackground(CustomerFrame.BACKGROUND);
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(0,20,20,20));
        JScrollPane sp = new JScrollPane(appointmentsListPanel);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsListPanel.removeAll();
        try {
            String sql =
                "SELECT a.appointment_id, s.service_type, a.address, a.appointment_status, " +
                "DATE_FORMAT(a.BeginDate,'%d/%m/%Y') AS dt, DATE_FORMAT(a.BeginDate,'%H:%i') AS tm, " +
                "p.professional_id, p.professional_FirstName, p.professional_LastName " +
                "FROM appointment a " +
                " JOIN professionals p ON a.professional_id=p.professional_id " +
                " JOIN service s ON a.service_id=s.service_id " +
                "WHERE a.customer_id=? " +
                "ORDER BY a.appointment_status='pending' DESC, a.appointment_id DESC";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int apptId = rs.getInt("appointment_id");
                String svc   = rs.getString("service_type");
                String st    = rs.getString("appointment_status");
                String date  = rs.getString("dt");
                String time  = rs.getString("tm");
                int profId   = rs.getInt("professional_id");
                String profName = rs.getString("professional_FirstName") + " " + rs.getString("professional_LastName");
                String addr  = rs.getString("address");

                AppointmentCard card = new AppointmentCard(
                    apptId, profName, date, time, svc, addr, st,
                    e -> { cancelAppointment(apptId); loadAppointments(); },
                    e -> openReviewDialog(apptId, profId)
                );

                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(CustomerFrame.BACKGROUND);
                wrapper.add(card, BorderLayout.CENTER);

                if ("accepted".equals(st)) {
                    JButton pay = new JButton("Πληρωμή");
                    pay.setBackground(CustomerFrame.ORANGE_PRIMARY);
                    pay.setForeground(Color.WHITE);
                    pay.addActionListener(ev -> {
                        // 1) CardGateway stub
                        Payment.CardGateway gateway = details -> true;

                        // 2) CustomerRepository με findById + save
                        Payment.CustomerRepository custRepo = new Payment.CustomerRepository() {
                            @Override
                            public Payment.Customer findById(int id) {
                                try {
                                    PreparedStatement p = connection.prepareStatement(
                                        "SELECT customer_bonuspoints FROM customers WHERE customer_id=?"
                                    );
                                    p.setInt(1, id);
                                    ResultSet r = p.executeQuery();
                                    Payment.Customer c = new Payment.Customer();
                                    if (r.next()) {
                                        c.addPoints(r.getInt("customer_bonuspoints"));
                                    }
                                    r.close();
                                    p.close();
                                    return c;
                                } catch (SQLException ex) {
                                    throw new Payment.RepositoryException("findById πελάτη απέτυχε", ex);
                                }
                            }
                            @Override
                            public Payment.Customer save(Payment.Customer c) {
                                try {
                                    int pts = c.getPoints();
                                    PreparedStatement p = connection.prepareStatement(
                                        "UPDATE customers SET customer_bonuspoints=? WHERE customer_id=?"
                                    );
                                    p.setInt(1, pts);
                                    p.setInt(2, customerId);
                                    p.executeUpdate();
                                    p.close();
                                    return c;
                                } catch (SQLException ex) {
                                    throw new Payment.RepositoryException("save πελάτη απέτυχε", ex);
                                }
                            }
                        };

                        // 3) Repository για persist Payment
                        Payment.Repository payRepo = new Payment.Repository() {
                            @Override
                            public Payment save(Payment payment) {
                                try {
                                    PreparedStatement p = connection.prepareStatement(
                                        "INSERT INTO payment(payment_type,customer_id,professional_id,appointment_id) VALUES(?,?,?,?)",
                                        Statement.RETURN_GENERATED_KEYS
                                    );
                                    p.setString(1, payment.getType().getDbValue());
                                    p.setInt(2, payment.getCustomerId());
                                    p.setInt(3, payment.getProfessionalId());
                                    p.setInt(4, payment.getAppointmentId());
                                    p.executeUpdate();
                                    ResultSet gk = p.getGeneratedKeys();
                                    if (gk.next()) payment.setId(gk.getInt(1));
                                    gk.close();
                                    p.close();
                                    return payment;
                                } catch (SQLException ex) {
                                    throw new Payment.RepositoryException("Αποθήκευση payment απέτυχε", ex);
                                }
                            }
                        };

                        // 4) AppointmentRepository για αλλαγή status
                        Payment.AppointmentRepository apptRepo = new Payment.AppointmentRepository() {
                            @Override
                            public void updateStatus(int id, Object status) {
                                try {
                                    PreparedStatement p = connection.prepareStatement(
                                        "UPDATE appointment SET appointment_status='completed' WHERE appointment_id=?"
                                    );
                                    p.setInt(1, id);
                                    p.executeUpdate();
                                    p.close();
                                } catch (SQLException ex) {
                                    throw new Payment.RepositoryException("updateStatus απέτυχε", ex);
                                }
                            }
                        };

                        // 5) Ορισμός όλων των Processors
                        Map<Payment.Type, Payment.Processor> procs = new HashMap<>();
                        procs.put(Payment.Type.CARD, new Payment.CardProcessor(gateway));
                        procs.put(Payment.Type.BONUS_POINT, new Payment.BonusProcessor(custRepo));
                        procs.put(Payment.Type.CASH, new Payment.Processor() {
                            @Override public Payment.Type getSupportedType() { return Payment.Type.CASH; }
                            @Override public Payment.Result process(Payment.Request req) {
                                return new Payment.Result(true, "Πληρωμή με μετρητά επιτυχής");
                            }
                        });

                        // 6) Δημιουργία Service με custom dependencies
                        Payment.Service paymentService = new Payment.Service(
                            connection,
                            payRepo,
                            apptRepo,
                            procs
                        );

                        // 7) Μετάβαση στο PaymentsPanel
                        PaymentsPanel pp = new PaymentsPanel(
                            paymentService,
                            customerId,
                            profId,
                            apptId,
                            parent
                        );
                        parent.getContentPanel().add(pp, "payment");
                        parent.switchTo("payment");
                    });

                    JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    btnP.setBackground(CustomerFrame.BACKGROUND);
                    btnP.add(pay);
                    wrapper.add(btnP, BorderLayout.SOUTH);
                }

                appointmentsListPanel.add(wrapper);
                appointmentsListPanel.add(Box.createRigidArea(new Dimension(0,15)));
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        appointmentsListPanel.revalidate();
        appointmentsListPanel.repaint();
    }

    // stubs...
    private boolean checkIfReviewed(int appointmentId) { return false; }
    private void openReviewDialog(int a,int p)     {}
    private boolean submitReview(int a,int p,int r,String c) { return true; }
    private void updateProfessionalRating(int p)    {}
    private void cancelAppointment(int apptId)      {}

}
