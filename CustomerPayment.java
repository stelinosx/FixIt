import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Ενοποιημένη κλάση που περιλαμβάνει τόσο το Payment logic
 * (τύποι, επεξεργαστές, service) όσο και το UI panel πληρωμών.
 */
public class CustomerPayment extends JPanel {

    // =====================
    // --- Payment Logic ---
    // =====================

    public enum Type {
        CARD("card"),
        CASH("cash"),
        BONUS_POINT("bonuspoint");

        private final String dbValue;
        Type(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }
    }

    public enum Status {
        PENDING, SUCCESS, FAILED, CANCELLED
    }

    public static class PaymentException extends Exception {
        public PaymentException(String m) { super(m); }
    }
    public static class CardDeclinedException extends PaymentException {
        public CardDeclinedException(String m) { super(m); }
    }
    public static class InsufficientPointsException extends PaymentException {
        public InsufficientPointsException(String m) { super(m); }
    }
    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String m, Throwable c) { super(m, c); }
    }

    public interface Processor {
        Type getSupportedType();
        Result process(Request req) throws PaymentException;
    }

    public static class Request {
        public final int customerId, professionalId, appointmentId;
        public final Type type;
        public final Object details;
        public Request(int c, int p, int a, Type t, Object d) {
            customerId = c;
            professionalId = p;
            appointmentId = a;
            type = t;
            details = d;
        }
    }

    public static class Result {
        public final boolean success;
        public final String message;
        public Result(boolean s, String m) {
            success = s;
            message = m;
        }
    }

    /** Κάρτα: χωρίς άλλη αλλαγή */
    public static class CardProcessor implements Processor {
        private final CardGateway gateway;
        public CardProcessor(CardGateway gw) { gateway = gw; }
        @Override public Type getSupportedType() { return Type.CARD; }
        @Override public Result process(Request req) throws PaymentException {
            CardDetails cd = (CardDetails) req.details;
            if (gateway.charge(cd)) {
                return new Result(true, "Η συναλλαγή ολοκληρώθηκε με επιτυχία");
            }
            throw new CardDeclinedException("Η κάρτα απορρίφθηκε");
        }
    }

    /**
     * Bonus points: εδώ το κόστος σε πόντους = floor(price (€))
     * (1 πόντος = 1 €). Δεν υπάρχει επιλογή — απλώς αφαιρούνται αυτόματα.
     */
    public static class BonusProcessor implements Processor {
        private final CustomerRepository repo;
        private final Connection conn;

        public BonusProcessor(CustomerRepository repo, Connection conn) {
            this.repo = repo;
            this.conn = conn;
        }

        @Override public Type getSupportedType() { return Type.BONUS_POINT; }

        @Override public Result process(Request req) throws PaymentException {
            // 1) Βρες την τιμή σε € (floor)
            final String sqlPrice =
                "SELECT CAST(s.service_price AS DECIMAL(10,2)) AS price " +
                "FROM appointment a " +
                "JOIN service s ON a.service_id = s.service_id " +
                "WHERE a.appointment_id = ?";
            int requiredPts;
            try (PreparedStatement ps = conn.prepareStatement(sqlPrice)) {
                ps.setInt(1, req.appointmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new InsufficientPointsException(
                            "Δεν βρέθηκε το ραντεβού για εξαργύρωση πόντων"
                        );
                    }
                    BigDecimal price = rs.getBigDecimal("price");
                    // 1 πόντος = 1 €
                    requiredPts = price.setScale(0, RoundingMode.DOWN).intValue();
                }
            } catch (SQLException e) {
                throw new RepositoryException("Αποτυχία ανάγνωσης τιμής υπηρεσίας", e);
            }

            // 2) Έλεγχος & αφαίρεση πόντων
            Customer c = repo.findById(req.customerId);
            if (c.getPoints() < requiredPts) {
                throw new InsufficientPointsException(
                    "Δεν έχετε αρκετούς πόντους (χρειάζεστε " + requiredPts + ")"
                );
            }
            c.addPoints(-requiredPts);
            repo.save(c);

            // 3) Επιστροφή μηνύματος
            return new Result(
                true,
                "Επιτυχής εξαργύρωση πόντων: αφαιρέθηκαν " + requiredPts + " πόντοι."
            );
        }
    }

    public static class Service {
        private final Repository payRepo;
        private final AppointmentRepository apptRepo;
        private final Map<Type, Processor> processors;
        private final Connection conn;

        public Service(Connection conn,
                       Repository pr,
                       AppointmentRepository ar,
                       Map<Type, Processor> procs) {
            this.conn = conn;
            this.payRepo = pr;
            this.apptRepo = ar;
            this.processors = procs;
        }

        public Result payAppointment(int customerId,
                                     int professionalId,
                                     int appointmentId,
                                     Type type,
                                     Object details) {
            Payment p = new Payment(type, customerId, professionalId, appointmentId);
            try {
                Processor proc = processors.get(type);
                Result res = proc.process(
                    new Request(customerId, professionalId, appointmentId, type, details)
                );

                // αποθήκευση & status
                p.status = Status.SUCCESS;
                payRepo.save(p);
                apptRepo.updateStatus(appointmentId, AppointmentStatus.COMPLETED);

                // award πόντους 1/5€
                String msg = res.message;
                if (type == Type.CARD) {
                    int awarded = awardPointsOnCardPayment(customerId, appointmentId);
                    msg += "\nΣυγχαρητήρια, κερδίσατε " + awarded + " πόντους!";
                }
                return new Result(true, msg);

            } catch (PaymentException ex) {
                p.status = Status.FAILED;
                try { payRepo.save(p); } catch (Exception ignore) {}
                return new Result(false, ex.getMessage());
            }
        }

        private int awardPointsOnCardPayment(int customerId, int appointmentId) {
            // 1 πόντος / 5€
            final String sqlPrice =
                "SELECT CAST(s.service_price AS DECIMAL(10,2)) AS price " +
                "FROM appointment a " +
                "JOIN service s ON a.service_id = s.service_id " +
                "WHERE a.appointment_id = ?";
            final String sqlUpdatePts =
                "UPDATE customers SET customer_bonuspoints = customer_bonuspoints + ? " +
                "WHERE customer_id = ?";

            try (
                PreparedStatement pst1 = conn.prepareStatement(sqlPrice);
                PreparedStatement pst2 = conn.prepareStatement(sqlUpdatePts)
            ) {
                pst1.setInt(1, appointmentId);
                try (ResultSet rs = pst1.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Δεν βρέθηκε ραντεβού με id=" + appointmentId);
                    }
                    BigDecimal price = rs.getBigDecimal("price");
                    int points = price
                        .divide(BigDecimal.valueOf(5), 0, RoundingMode.DOWN)
                        .intValue();

                    pst2.setInt(1, points);
                    pst2.setInt(2, customerId);
                    pst2.executeUpdate();
                    return points;
                }
            } catch (SQLException e) {
                throw new RepositoryException("Αποτυχία βράβευσης πόντων", e);
            }
        }
    }

    // === εξαρτήσεις / DTO ===

    public static class Payment {
        private Integer id;
        public Type type;
        public int customerId, professionalId, appointmentId;
        public Status status;
        public LocalDateTime timestamp = LocalDateTime.now();

        public Payment(Type t, int c, int p, int a) {
            type = t; customerId = c; professionalId = p; appointmentId = a;
            status = Status.PENDING;
        }
        public void setId(int i)      { this.id = i; }
        public Integer getId()        { return this.id; }
        public Type getType()         { return type; }
        public int getCustomerId()    { return customerId; }
        public int getProfessionalId(){ return professionalId; }
        public int getAppointmentId() { return appointmentId; }
    }

    public interface Repository {
        Payment save(Payment p) throws RepositoryException;
    }

    public interface AppointmentRepository {
        void updateStatus(int a, Object s);
    }

    public interface CardGateway {
        boolean charge(CardDetails d);
    }

    public interface CustomerRepository {
        Customer findById(int id);
        Customer save(Customer c);
    }

    public static class CardDetails {
        public final String number, name, expiry, cvv;
        public CardDetails(String num,String n,String e,String c){
            number=num; name=n; expiry=e; cvv=c;
        }
    }

    public static class Customer {
        private int pts;
        public int getPoints(){ return pts; }
        public void addPoints(int d){ pts += d; }
    }

    public enum AppointmentStatus { COMPLETED }

    // =======================
    // --- Payments UI Code ---
    // =======================

    private final Service paymentService;
    private final int customerId, professionalId, appointmentId;
    private final CustomerFrame parent;

    private JComboBox<Type>   typeCombo;
    private CardLayout        cardLayout;
    private JPanel            detailsPanel;
    private JTextField        cardNumberField;
    private JTextField        cardNameField;
    private JTextField        cardExpiryField;
    private JTextField        cardCvvField;

    public CustomerPayment(Service svc,
                           int customerId,
                           int professionalId,
                           int appointmentId,
                           CustomerFrame parent) {
        this.paymentService = svc;
        this.customerId     = customerId;
        this.professionalId = professionalId;
        this.appointmentId  = appointmentId;
        this.parent         = parent;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        setBackground(CustomerFrame.BACKGROUND);

        // HEADER
        JLabel title = new JLabel("Επιλογή Τρόπου Πληρωμής");
        title.setFont(new Font("Arial",Font.BOLD,24));
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(CustomerFrame.BACKGROUND);
        hdr.setBorder(BorderFactory.createEmptyBorder(20,20,0,20));
        hdr.add(title,BorderLayout.WEST);
        add(hdr,BorderLayout.NORTH);

        // CENTER
        JPanel center = new JPanel(new BorderLayout(10,10));
        center.setBorder(BorderFactory.createEmptyBorder(0,20,20,20));
        center.setBackground(CustomerFrame.BACKGROUND);

        // επιλογή τύπου
        JPanel typePan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePan.setBackground(CustomerFrame.BACKGROUND);
        typePan.add(new JLabel("Τύπος:"));
        typeCombo = new JComboBox<>(Type.values());
        typeCombo.addActionListener(e-> switchDetails());
        typePan.add(typeCombo);
        center.add(typePan,BorderLayout.NORTH);

        // λεπτομέρειες
        cardLayout = new CardLayout();
        detailsPanel = new JPanel(cardLayout);
        detailsPanel.setBackground(CustomerFrame.BACKGROUND);

        // CARD
        JPanel cardPan = new JPanel(new GridLayout(4,2,5,5));
        cardNumberField = new JTextField();
        cardNameField   = new JTextField();
        cardExpiryField = new JTextField();
        cardCvvField    = new JTextField();
        cardPan.add(new JLabel("Αρ. Κάρτας:"));   cardPan.add(cardNumberField);
        cardPan.add(new JLabel("Όνομα Κατόχου:"));cardPan.add(cardNameField);
        cardPan.add(new JLabel("Λήξη (MM/YY):")); cardPan.add(cardExpiryField);
        cardPan.add(new JLabel("CVV:"));          cardPan.add(cardCvvField);
        detailsPanel.add(cardPan, Type.CARD.name());

        // BONUS_POINT (μόνο μήνυμα)
        JPanel bonusPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bonusPan.setBackground(CustomerFrame.BACKGROUND);
        bonusPan.add(new JLabel("Πατήστε Πληρωμή για αυτόματη εξαργύρωση πόντων"));
        detailsPanel.add(bonusPan, Type.BONUS_POINT.name());

        // CASH
        JPanel cashPan = new JPanel();
        cashPan.setBackground(CustomerFrame.BACKGROUND);
        cashPan.add(new JLabel("Πληρωμή με μετρητά κατά την παράδοση."));
        detailsPanel.add(cashPan, Type.CASH.name());

        center.add(detailsPanel,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);

        // BOTTOM
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setBackground(CustomerFrame.BACKGROUND);
        JButton cancel = new JButton("Ακύρωση");
        cancel.addActionListener(e-> parent.showHome());
        JButton pay    = new JButton("Πληρωμή");
        pay.addActionListener(e-> doPay());
        bot.add(cancel);
        bot.add(pay);
        add(bot,BorderLayout.SOUTH);

        switchDetails();
    }

    private void switchDetails() {
        Type t = (Type)typeCombo.getSelectedItem();
        cardLayout.show(detailsPanel, t.name());
    }

    private void doPay() {
        Type type = (Type)typeCombo.getSelectedItem();
        Object details = null;

        if (type == Type.CARD) {
            // όλοι οι έλεγχοι κάρτας όπως είχες
            String number = cardNumberField.getText().trim();
            String name   = cardNameField.getText().trim();
            String expiry = cardExpiryField.getText().trim();
            String cvv    = cardCvvField.getText().trim();
            if (number.isEmpty()||name.isEmpty()||expiry.isEmpty()||cvv.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Παρακαλώ συμπληρώστε όλα τα πεδία της κάρτας.",
                    "Σφάλμα",JOptionPane.ERROR_MESSAGE);
                return;
            }
            String digitsOnly = number.replaceAll("\\D","");
            if (!digitsOnly.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this,
                    "Ο αριθμός της κάρτας πρέπει να αποτελείται από 16 ψηφία.",
                    "Σφάλμα",JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Η ημερομηνία λήξης πρέπει να είναι στη μορφή MM/YY.",
                    "Σφάλμα",JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                String[] parts = expiry.split("/");
                int month = Integer.parseInt(parts[0]);
                int year  = 2000 + Integer.parseInt(parts[1]);
                java.time.YearMonth exp = java.time.YearMonth.of(year, month);
                if (exp.isBefore(java.time.YearMonth.now())) {
                    JOptionPane.showMessageDialog(this,
                        "Η κάρτα έχει λήξει.", "Σφάλμα",JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (!cvv.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(this,
                    "Το CVC πρέπει να αποτελείται από 3 ψηφία.",
                    "Σφάλμα",JOptionPane.ERROR_MESSAGE);
                return;
            }
            details = new CardDetails(number,name,expiry,cvv);
        }
        // BONUS_POINT & CASH στέλνουν details = null
        Result res = paymentService.payAppointment(
            customerId, professionalId, appointmentId, type, details
        );

        if (res.success) {
            JOptionPane.showMessageDialog(this, res.message, "Επιτυχία",
                                          JOptionPane.INFORMATION_MESSAGE);
            parent.refreshPoints();
            parent.switchTo("appointments");
            parent.refreshAppointments();
        } else {

            JOptionPane.showMessageDialog(this, res.message, "Σφάλμα",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}
