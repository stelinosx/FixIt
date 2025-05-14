import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * General Payment class encapsulating all payment-related types,
 * exceptions, processors, and service logic for appointment payments.
 */
public class Payment {
    private Integer id;
    private Type type;
    private int customerId;
    private int professionalId;
    private int appointmentId;
    private Status status;
    private LocalDateTime timestamp;

    /**
     * Creates a new Payment with PENDING status.
     */
    public Payment(Type type, int customerId, int professionalId, int appointmentId) {
        this.type = type;
        this.customerId = customerId;
        this.professionalId = professionalId;
        this.appointmentId = appointmentId;
        this.status = Status.PENDING;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getProfessionalId() { return professionalId; }
    public void setProfessionalId(int professionalId) { this.professionalId = professionalId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // --- Nested Enums ----------------------------------------------

    /** Types of payment supported */
    public enum Type {
        CARD("card"),
        CASH("cash"),
        BONUS_POINT("bonuspoint");

        private final String dbValue;
        Type(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }
        public static Type fromDbValue(String val) {
            for (Type t : values()) {
                if (t.dbValue.equalsIgnoreCase(val)) return t;
            }
            throw new IllegalArgumentException("Unknown payment type: " + val);
        }
    }

    /** Status of a payment */
    public enum Status {
        PENDING,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    // --- Nested Exceptions -----------------------------------------

    /** Base exception for payment errors */
    public static class PaymentException extends Exception {
        public PaymentException(String message) { super(message); }
    }

    /** Thrown when a card payment is declined */
    public static class CardDeclinedException extends PaymentException {
        public CardDeclinedException(String message) { super(message); }
    }

    /** Thrown when the customer has insufficient bonus points */
    public static class InsufficientPointsException extends PaymentException {
        public InsufficientPointsException(String message) { super(message); }
    }

    /** Runtime exception for repository failures */
    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) { super(message, cause); }
    }

    // --- Nested Supporting Types -----------------------------------

    /** Repository interface for persisting Payment records */
    public interface Repository {
        Payment save(Payment payment) throws RepositoryException;
    }

    /** Request DTO for processing a payment */
    public static class Request {
        public final int customerId;
        public final int professionalId;
        public final int appointmentId;
        public final Type type;
        public final Object details;

        public Request(int customerId,
                       int professionalId,
                       int appointmentId,
                       Type type,
                       Object details) {
            this.customerId = customerId;
            this.professionalId = professionalId;
            this.appointmentId = appointmentId;
            this.type = type;
            this.details = details;
        }
    }

    /** Result DTO for processing outcome */
    public static class Result {
        public final boolean success;
        public final String message;
        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /** Processor interface for different payment methods */
    public interface Processor {
        Type getSupportedType();
        Result process(Request request) throws PaymentException;
    }

    /** Card payment processor example */
    public static class CardProcessor implements Processor {
        private final CardGateway gateway;
        public CardProcessor(CardGateway gateway) { this.gateway = gateway; }
        @Override public Type getSupportedType() { return Type.CARD; }
        @Override
        public Result process(Request req) throws PaymentException {
            CardDetails cd = (CardDetails) req.details;
            boolean ok = gateway.charge(cd);
            if (ok) return new Result(true, "Επιτυχής συναλλαγή με κάρτα");
            else throw new CardDeclinedException("Η κάρτα απορρίφθηκε");
        }
    }

    /** Bonus points processor example */
    public static class BonusProcessor implements Processor {
        private final CustomerRepository custRepo;
        public BonusProcessor(CustomerRepository custRepo) { this.custRepo = custRepo; }
        @Override public Type getSupportedType() { return Type.BONUS_POINT; }
        @Override
        public Result process(Request req) throws PaymentException {
            int pts = (Integer) req.details;
            Customer c = custRepo.findById(req.customerId);
            if (c.getPoints() < pts) {
                throw new InsufficientPointsException("Δεν έχετε αρκετούς πόντους");
            }
            c.addPoints(-pts);
            custRepo.save(c);
            return new Result(true, "Επιτυχής εξαργύρωση πόντων");
        }
    }

    /** Service coordinating the payment flow */
    public static class Service {
        private final Repository repo;
        private final AppointmentRepository apptRepo;
        private final Map<Type, Processor> processors;
        private final Connection connection;

        /**
         * @param connection  η JDBC σύνδεση (για να διαβάσουμε τιμές & να βραβεύσουμε πόντους)
         * @param repo        η υλοποίηση του Payment.Repository
         * @param apptRepo    η υλοποίηση του AppointmentRepository
         * @param processors  το map Type→Processor
         */
        public Service(Connection connection,
                       Repository repo,
                       AppointmentRepository apptRepo,
                       Map<Type, Processor> processors) {
            this.connection = connection;
            this.repo       = repo;
            this.apptRepo   = apptRepo;
            this.processors = processors;
        }

        /**
         * Εκτελεί την πληρωμή. Σε περίπτωση CARD:
         *  • αποθηκεύει το payment
         *  • αλλάζει το status του ραντεβού σε COMPLETED
         *  • βραβεύει πόντους στον πελάτη (1€ = 1 πόντος, κόβοντας δεκαδικά)
         */
        public Result payAppointment(int customerId,
                                     int professionalId,
                                     int appointmentId,
                                     Type type,
                                     Object details) {
            Payment payment = new Payment(type, customerId, professionalId, appointmentId);
            try {
                Processor p = processors.get(type);
                Result res = p.process(
                    new Request(customerId, professionalId, appointmentId, type, details)
                );

                // επιτυχής
                payment.setStatus(Status.SUCCESS);
                repo.save(payment);
                apptRepo.updateStatus(appointmentId, AppointmentStatus.COMPLETED);

                // μετά από κάρτα, βράβευσε πόντους
                if (type == Type.CARD) {
                    awardPointsOnCardPayment(customerId, appointmentId);
                }

                return res;

            } catch (PaymentException ex) {
                payment.setStatus(Status.FAILED);
                try { repo.save(payment); } catch (Exception ignore) {}
                return new Result(false, ex.getMessage());
            }
        }

        /**
         * Διαβάζει την τιμή της υπηρεσίας από appointment→service,
         * υπολογίζει πόντους (1€ → 1 πόντος, κόβει τα δεκαδικά)
         * και τους προσθέτει στον πίνακα customers.
         */
        private void awardPointsOnCardPayment(int customerId, int appointmentId) {
            final String sqlPrice =
                "SELECT CAST(s.service_price AS DECIMAL(10,2)) AS price " +
                "FROM appointment a " +
                "JOIN service s ON a.service_id = s.service_id " +
                "WHERE a.appointment_id = ?";
            final String sqlUpdatePts =
                "UPDATE customers SET customer_bonuspoints = customer_bonuspoints + ? " +
                "WHERE customer_id = ?";

            try (
                PreparedStatement pst1 = connection.prepareStatement(sqlPrice);
                PreparedStatement pst2 = connection.prepareStatement(sqlUpdatePts)
            ) {
                pst1.setInt(1, appointmentId);
                try (ResultSet rs = pst1.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Δεν βρέθηκε ραντεβού με id=" + appointmentId);
                    }
                    BigDecimal price = rs.getBigDecimal("price");
                    int points       = price.setScale(0, RoundingMode.DOWN).intValue();

                    pst2.setInt(1, points);
                    pst2.setInt(2, customerId);
                    pst2.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RepositoryException("Αποτυχία βράβευσης πόντων", e);
            }
        }
    }

    // --- External dependencies / placeholders -----------------------

    /** Πύλη για χρεώσεις καρτών */
    public interface CardGateway { boolean charge(CardDetails details); }

    /** Πελάτης για εξαργύρωση πόντων */
    public interface CustomerRepository {
        Customer findById(int id);
        Customer save(Customer c);
    }

    /** Αλλαγή status ραντεβού */
    public interface AppointmentRepository {
        void updateStatus(int appointmentId, Object status);
    }

    /** Card details required for CARD payments. */
    public static class CardDetails {
        private final String number;
        private final String name;
        private final String expiry;
        private final String cvv;

        public CardDetails(String number, String name, String expiry, String cvv) {
            this.number = number;
            this.name   = name;
            this.expiry = expiry;
            this.cvv    = cvv;
        }

        public String getNumber() { return number; }
        public String getName()   { return name; }
        public String getExpiry() { return expiry; }
        public String getCvv()    { return cvv; }
    }

    /** Placeholder domain Customer for bonus points logic */
    public static class Customer {
        private int points;
        public int getPoints()           { return points; }
        public void addPoints(int delta) { points += delta; }
    }

    public enum AppointmentStatus { COMPLETED }
}
