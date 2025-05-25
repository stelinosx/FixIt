import java.sql.*;
/**
 * Ενημερώνει το status ενός ραντεβού (πίνακας `appointment`).
 */
public class JdbcAppointmentRepository implements CustomerPayment.AppointmentRepository {
    private final Connection conn;

    public JdbcAppointmentRepository(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void updateStatus(int appointmentId, Object status) {
        try {
            String sql = "UPDATE appointment SET appointment_status = ? WHERE appointment_id = ?";
            PreparedStatement st = conn.prepareStatement(sql);
            // το status εδώ υποθέτω είναι Payment.AppointmentStatus
            st.setString(1, ((CustomerPayment.AppointmentStatus)status).name().toLowerCase());
            st.setInt(2, appointmentId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new CustomerPayment.RepositoryException("Failed to update appointment status", e);
        }
    }
}
