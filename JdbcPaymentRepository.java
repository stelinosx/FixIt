import java.sql.*;
/**
 * Αποθηκεύει ένα Payment στη βάση (πίνακας `payment`).
 */
public class JdbcPaymentRepository implements Payment.Repository {
    private final Connection conn;

    public JdbcPaymentRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Payment save(Payment payment) {
        try {
            String sql = "INSERT INTO payment(payment_type, customer_id, professional_id, appointment_id) "
                       + "VALUES (?,?,?,?)";
            PreparedStatement st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, payment.getType().getDbValue());
            st.setInt   (2, payment.getCustomerId());
            st.setInt   (3, payment.getProfessionalId());
            st.setInt   (4, payment.getAppointmentId());
            st.executeUpdate();

            ResultSet rk = st.getGeneratedKeys();
            if (rk.next()) payment.setId(rk.getInt(1));
            return payment;
        } catch (SQLException e) {
            throw new Payment.RepositoryException("Failed to save payment", e);
        }
    }
}
