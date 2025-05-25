import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.util.Timer;

public class CustomerAppointment extends JPanel {
    private final Connection connection;
    private final CustomerFrame parent;
    private final int customerId;
    private final JPanel appointmentsListPanel;
    private Timer refreshTimer;

    public CustomerAppointment(Connection connection, CustomerFrame parent, int customerId) {
        this.connection  = connection;
        this.parent      = parent;
        this.customerId  = customerId;

        setLayout(new BorderLayout());
        setBackground(CustomerFrame.BACKGROUND);

        // === HEADER με κουμπί Ανανέωσης ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CustomerFrame.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        JLabel lblTitle = new JLabel("Λίστα Ραντεβού:");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnRefresh = new JButton("Ανανέωση");
        btnRefresh.setBackground(CustomerFrame.ORANGE_PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            updatePastAppointments();
            loadAppointments();
            JOptionPane.showMessageDialog(
                this,
                "Η λίστα ραντεβού ανανεώθηκε με επιτυχία.",
                "Ανανέωση",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        header.add(btnRefresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // === BODY με scrollable panel ===
        appointmentsListPanel = new JPanel();
        appointmentsListPanel.setLayout(new BoxLayout(appointmentsListPanel, BoxLayout.Y_AXIS));
        appointmentsListPanel.setBackground(CustomerFrame.BACKGROUND);
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(0,20,20,20));
        JScrollPane scroll = new JScrollPane(appointmentsListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // === Αρχικό load + αυτόματη ανανέωση κάθε 30" ===
        updatePastAppointments();
        loadAppointments();
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
        String sql =
            "UPDATE appointment " +
            "SET appointment_status='completed' " +
            "WHERE appointment_status='accepted' AND EndDate< NOW() AND customer_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
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

    public void loadAppointments() {
        appointmentsListPanel.removeAll();
        boolean hasAppointments = false;

        String sql =
            "SELECT a.appointment_id, s.service_type, a.address, a.appointment_status, " +
            "DATE_FORMAT(a.BeginDate, '%d/%m/%Y') AS appointment_date, " +
            "DATE_FORMAT(a.BeginDate, '%H:%i') AS appointment_time, " +
            "p.professional_id, p.professional_FirstName, p.professional_LastName " +
            "FROM appointment a " +
            "JOIN professionals p ON a.professional_id = p.professional_id " +
            "JOIN service s ON a.service_id = s.service_id " +
            "WHERE a.customer_id = ? " +
            "ORDER BY " +
            "  a.appointment_status = 'pending' DESC, " +
            "  a.appointment_status = 'accepted' DESC, " +
            "  a.appointment_id DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasAppointments = true;

                    // --- Εξαγωγή μεταβλητών από το ResultSet ---
                    final int apptId    = rs.getInt("appointment_id");
                    final int profId    = rs.getInt("professional_id");
                    String svcType      = rs.getString("service_type");
                    String status       = rs.getString("appointment_status");
                    String date         = rs.getString("appointment_date");
                    String time         = rs.getString("appointment_time");
                    String profName     = rs.getString("professional_FirstName")
                                         + " " + rs.getString("professional_LastName");
                    String addr         = rs.getString("address");

                    // --- Δημιουργία του AppointmentCard ---
                    AppointmentCard card = new AppointmentCard(
                        apptId, profName, date, time, svcType, addr, status,
                        e -> { cancelAppointment(apptId); loadAppointments(); },
                        e -> openReviewDialog(apptId, profId)
                    );

                    // --- Banners & Actions ανά status ---
                    if ("accepted".equals(status)) {
                        JPanel banner = new JPanel(new BorderLayout());
                        banner.setBackground(CustomerFrame.BACKGROUND);

                        JLabel lab = new JLabel("Νέο αποδεκτό ραντεβού!", SwingConstants.CENTER);
                        lab.setOpaque(true);
                        lab.setBackground(new Color(40,167,69));
                        lab.setForeground(Color.WHITE);
                        lab.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
                        lab.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        banner.add(lab, BorderLayout.NORTH);

                        banner.add(card, BorderLayout.CENTER);

                        // κουμπί Πληρωμής
                        JButton payBtn = new JButton("Πληρωμή");
                        payBtn.setBackground(CustomerFrame.ORANGE_PRIMARY);
                        payBtn.setForeground(Color.WHITE);
                        payBtn.setFocusPainted(false);
                        payBtn.addActionListener(ev -> {
                            // --- Εδώ βάζεις το δικό σου gateway/repo/service όπως πριν ---
                            CustomerPayment.CardGateway gateway = details -> true;
                            CustomerPayment.CustomerRepository custRepo = new CustomerPayment.CustomerRepository(){
                                @Override public CustomerPayment.Customer findById(int id) {
                                    // όπως στην προηγούμενη υλοποίηση
                                    try (PreparedStatement p = connection.prepareStatement(
                                            "SELECT customer_bonuspoints FROM customers WHERE customer_id=?")) {
                                        p.setInt(1,id);
                                        try (ResultSet r = p.executeQuery()) {
                                            CustomerPayment.Customer c = new CustomerPayment.Customer();
                                            if(r.next()) c.addPoints(r.getInt("customer_bonuspoints"));
                                            return c;
                                        }
                                    } catch(SQLException ex){
                                        throw new CustomerPayment.RepositoryException("findById failed", ex);
                                    }
                                }
                                @Override public CustomerPayment.Customer save(CustomerPayment.Customer c) {
                                    try (PreparedStatement p = connection.prepareStatement(
                                            "UPDATE customers SET customer_bonuspoints=? WHERE customer_id=?")) {
                                        p.setInt(1,c.getPoints());
                                        p.setInt(2,customerId);
                                        p.executeUpdate();
                                        return c;
                                    } catch(SQLException ex){
                                        throw new CustomerPayment.RepositoryException("save failed", ex);
                                    }
                                }
                            };
                            CustomerPayment.Repository payRepo = payment -> {
                                try (PreparedStatement p = connection.prepareStatement(
                                        "INSERT INTO payment(payment_type,customer_id,professional_id,appointment_id) VALUES(?,?,?,?)",
                                        Statement.RETURN_GENERATED_KEYS)) {
                                    p.setString(1,payment.getType().getDbValue());
                                    p.setInt(2,payment.getCustomerId());
                                    p.setInt(3,payment.getProfessionalId());
                                    p.setInt(4,payment.getAppointmentId());
                                    p.executeUpdate();
                                    try(ResultSet gk=p.getGeneratedKeys()){
                                        if(gk.next()) payment.setId(gk.getInt(1));
                                    }
                                    return payment;
                                } catch(SQLException ex){
                                    throw new CustomerPayment.RepositoryException("save payment failed", ex);
                                }
                            };
                            CustomerPayment.AppointmentRepository apptRepo = (id, st) -> {
                                try (PreparedStatement p = connection.prepareStatement(
                                        "UPDATE appointment SET appointment_status='completed' WHERE appointment_id=?")) {
                                    p.setInt(1,id);
                                    p.executeUpdate();
                                } catch(SQLException ex){
                                    throw new CustomerPayment.RepositoryException("updateStatus failed", ex);
                                }
                            };
                            Map<CustomerPayment.Type,CustomerPayment.Processor> procs = new HashMap<>();
                            procs.put(CustomerPayment.Type.CARD, new CustomerPayment.CardProcessor(gateway));
                            procs.put(CustomerPayment.Type.BONUS_POINT, new CustomerPayment.BonusProcessor(custRepo, connection));
                            procs.put(CustomerPayment.Type.CASH, new CustomerPayment.Processor(){
                                @Override public CustomerPayment.Type getSupportedType(){ return CustomerPayment.Type.CASH; }
                                @Override public CustomerPayment.Result process(CustomerPayment.Request req){
                                    return new CustomerPayment.Result(true,"Πληρωμή με μετρητά επιτυχής");
                                }
                            });
                            CustomerPayment.Service paymentService =
                                new CustomerPayment.Service(connection, payRepo, apptRepo, procs);

                            CustomerPayment paymentPanel =
                                new CustomerPayment(paymentService, customerId, profId, apptId, parent);
                            parent.getContentPanel().add(paymentPanel, "payment");
                            parent.switchTo("payment");
                        });
                        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        btnP.setBackground(CustomerFrame.BACKGROUND);
                        btnP.add(payBtn);
                        banner.add(btnP, BorderLayout.SOUTH);

                        appointmentsListPanel.add(banner);

                    } else if ("declined".equals(status)) {
                        JPanel banner = new JPanel(new BorderLayout());
                        banner.setBackground(CustomerFrame.BACKGROUND);

                        JLabel lab = new JLabel("Το ραντεβού απορρίφθηκε", SwingConstants.CENTER);
                        lab.setOpaque(true);
                        lab.setBackground(new Color(220,53,69));
                        lab.setForeground(Color.WHITE);
                        lab.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
                        lab.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        banner.add(lab, BorderLayout.NORTH);

                        banner.add(card, BorderLayout.CENTER);

                        appointmentsListPanel.add(banner);

                    } else if ("completed".equals(status)) {
                        JPanel banner = new JPanel(new BorderLayout());
                        banner.setBackground(CustomerFrame.BACKGROUND);

                        boolean reviewed = checkIfReviewed(apptId);
                        JLabel lab = new JLabel(
                            reviewed
                              ? "Ολοκληρωμένο – Έχει αξιολογηθεί"
                              : "Ολοκληρωμένο – Μπορείτε να αξιολογήσετε!",
                            SwingConstants.CENTER
                        );
                        lab.setOpaque(true);
                        lab.setBackground(new Color(0,123,255));
                        lab.setForeground(Color.WHITE);
                        lab.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
                        lab.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        banner.add(lab, BorderLayout.NORTH);

                        banner.add(card, BorderLayout.CENTER);
                        // το κουμπί αξιολόγησης είναι ήδη μέσα στο AppointmentCard
                        appointmentsListPanel.add(banner);

                    } else {
                        // pending κ.λπ.
                        appointmentsListPanel.add(card);
                    }

                    appointmentsListPanel.add(Box.createRigidArea(new Dimension(0,15)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!hasAppointments) {
            appointmentsListPanel.add(Box.createVerticalGlue());
            JLabel none = new JLabel("Δεν έχετε κανένα ραντεβού");
            none.setFont(new Font("Arial", Font.BOLD, 16));
            none.setAlignmentX(Component.CENTER_ALIGNMENT);
            appointmentsListPanel.add(none);
            appointmentsListPanel.add(Box.createVerticalGlue());
        }

        appointmentsListPanel.revalidate();
        appointmentsListPanel.repaint();
    }

    private boolean checkIfReviewed(int appointmentId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM review WHERE appointment_id = ?"
        )) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openReviewDialog(int appointmentId, int professionalId) {
    // Αν έχει ήδη αξιολογηθεί, ενημέρωση
    if (checkIfReviewed(appointmentId)) {
        JOptionPane.showMessageDialog(this,
            "Έχετε ήδη αξιολογήσει αυτό το ραντεβού.",
            "Πληροφορία",
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // Δημιουργία modal διαλόγου
    JDialog dialog = new JDialog(
        (Frame) SwingUtilities.getWindowAncestor(this),
        "Αξιολόγηση Ραντεβού",
        true
    );
    dialog.setSize(400, 350);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    // Κύριο panel
    JPanel main = new JPanel();
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

    // Τίτλος
    JLabel title = new JLabel("Αξιολογήστε τον επαγγελματία");
    title.setFont(new Font("Arial", Font.BOLD, 16));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    main.add(title);
    main.add(Box.createRigidArea(new Dimension(0,10)));

    // Πληροφορίες ραντεβού (όνομα, υπηρεσία, ημερομηνία)
    try (PreparedStatement ps = connection.prepareStatement(
            "SELECT p.professional_FirstName, p.professional_LastName, " +
            "s.service_type, DATE_FORMAT(a.BeginDate, '%d/%m/%Y %H:%i') AS dt " +
            "FROM appointment a " +
            "JOIN professionals p ON a.professional_id = p.professional_id " +
            "JOIN service s ON a.service_id = s.service_id " +
            "WHERE a.appointment_id = ?"
        )) {
        ps.setInt(1, appointmentId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String profName = rs.getString("professional_FirstName") + " " + rs.getString("professional_LastName");
                String svc      = rs.getString("service_type");
                String dt       = rs.getString("dt");
                JPanel info = new JPanel(new GridLayout(3,2,5,5));
                info.setBorder(BorderFactory.createTitledBorder("Πληροφορίες"));
                info.add(new JLabel("Επαγγελματίας:")); info.add(new JLabel(profName));
                info.add(new JLabel("Υπηρεσία:"));       info.add(new JLabel(svc));
                info.add(new JLabel("Ημερομηνία:"));     info.add(new JLabel(dt));
                main.add(info);
                main.add(Box.createRigidArea(new Dimension(0,10)));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    // Panel για βαθμολογία
    JPanel ratingPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
    ratingPan.setAlignmentX(Component.LEFT_ALIGNMENT);
    ratingPan.add(new JLabel("Βαθμολογία (1-5):"));
    JComboBox<Integer> ratingCmb = new JComboBox<>(new Integer[]{1,2,3,4,5});
    ratingCmb.setSelectedIndex(4);
    ratingPan.add(ratingCmb);
    main.add(ratingPan);
    main.add(Box.createRigidArea(new Dimension(0,10)));

    // Panel για σχόλια
    main.add(new JLabel("Σχόλια:"));
    JTextArea comments = new JTextArea(5,20);
    comments.setLineWrap(true);
    comments.setWrapStyleWord(true);
    main.add(new JScrollPane(comments));
    main.add(Box.createRigidArea(new Dimension(0,10)));

    // Κουμπιά Επιβεβαίωσης/Ακύρωσης
    JPanel btnPan = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton btnSubmit = new JButton("Επιβεβαίωση");
    JButton btnCancel = new JButton("Ακύρωση");
    btnPan.add(btnSubmit);
    btnPan.add(btnCancel);
    main.add(btnPan);

    dialog.add(main, BorderLayout.CENTER);

    // Handler για το κουμπί Επιβεβαίωση
    btnSubmit.addActionListener(e -> {
        int rating = (Integer) ratingCmb.getSelectedItem();
        String comm = comments.getText().trim();
        if (comm.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Παρακαλώ γράψτε σχόλια.",
                "Σφάλμα", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(dialog,
            "Υποβολή αξιολόγησης;\nΒαθμολογία: " + rating + "\nΣχόλια: " + comm,
            "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        // Εισαγωγή στην βάση
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO review (rating, comments, customer_id, professional_id, appointment_id) " +
                "VALUES (?,?,?,?,?)"
            )) {
            ps.setInt(1, rating);
            ps.setString(2, comm);
            ps.setInt(3, customerId);
            ps.setInt(4, professionalId);
            ps.setInt(5, appointmentId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog,
                "Σφάλμα: " + ex.getMessage(),
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // Ενημέρωση μέσης βαθμολογίας επαγγελματία
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE professionals SET professional_rating = (" +
                "SELECT AVG(rating) FROM review WHERE professional_id = ?) " +
                "WHERE professional_id = ?"
            )) {
            ps.setInt(1, professionalId);
            ps.setInt(2, professionalId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JOptionPane.showMessageDialog(dialog,
            "Η αξιολόγηση υποβλήθηκε με επιτυχία!",
            "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

        dialog.dispose();
        loadAppointments();  // ανανέωση λίστας
    });

    // Handler για το κουμπί Ακύρωση
    btnCancel.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
}

    private void cancelAppointment(int appointmentId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE appointment SET appointment_status='cancelled' WHERE appointment_id=?"
        )) {
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Σφάλμα στην ακύρωση: " + e.getMessage(),
                "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
