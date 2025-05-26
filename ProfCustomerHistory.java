import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfCustomerHistory {

    private final Connection connection;
    private final int professionalId;

    public ProfCustomerHistory(Connection connection, int professionalId) {
        this.connection = connection;
        this.professionalId = professionalId;
    }

    public JPanel createProfCustomerPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel customersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JScrollPane scrollCustomers = new JScrollPane(customersPanel);
        scrollCustomers.setBorder(BorderFactory.createTitledBorder("Πελάτες με Ραντεβού"));
        mainPanel.add(scrollCustomers, BorderLayout.NORTH);

        JTextArea appointmentTextArea = new JTextArea();
        appointmentTextArea.setEditable(false);
        JScrollPane scrollAppointments = new JScrollPane(appointmentTextArea);
        scrollAppointments.setBorder(BorderFactory.createTitledBorder("Ιστορικό Ραντεβού"));
        mainPanel.add(scrollAppointments, BorderLayout.CENTER);

        try {
            String query = """
                SELECT * 
                FROM customers 
                WHERE customer_id IN (
                    SELECT customer_id 
                    FROM appointmenthistory 
                    WHERE professional_id = ?
                )
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, professionalId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String fullName = rs.getString("customer_FirstName") + " " + rs.getString("customer_LastName");
                int phone = rs.getInt("customer_phone");

                JButton customerBtn = new JButton(fullName + " (" + phone + ")");
                customerBtn.addActionListener(e -> loadAppointments(customerId, fullName, appointmentTextArea));
                customersPanel.add(customerBtn);
            }

        } catch (SQLException e) {
            showError("Σφάλμα φόρτωσης πελατών: " + e.getMessage());
        }

        return mainPanel;
    }

    private void loadAppointments(int customerId, String customerName, JTextArea textArea) {
        textArea.setText("");

        try {
            String query = """
                SELECT appointmenthistory_id, appointmenthistory_description, appointment_id
                FROM appointmenthistory
                WHERE customer_id = ? AND professional_id = ?
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, customerId);
            stmt.setInt(2, professionalId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Ραντεβού για " + customerName + ":\n\n");

            while (rs.next()) {
                int historyId = rs.getInt("appointmenthistory_id");
                int appointmentId = rs.getInt("appointment_id");
                String desc = rs.getString("appointmenthistory_description");

                sb.append("Appointment ID: ").append(appointmentId).append("\n");
                sb.append("Σχόλιο:\n").append(desc == null ? "—" : desc).append("\n");

                textArea.setText(sb.toString());

                // Προσθήκη κουμπιού για σχόλιο
                JButton addCommentBtn = new JButton("Προσθήκη Σχολίου για Appointment ID: " + appointmentId);
                addCommentBtn.addActionListener(e -> addComment(historyId, textArea, customerId, customerName));

                // Τοποθέτηση κουμπιού κάτω από το ιστορικό
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                buttonPanel.add(addCommentBtn);

                JFrame buttonFrame = new JFrame();
                buttonFrame.getContentPane().add(buttonPanel);
                buttonFrame.pack();
                buttonFrame.setVisible(true);

                sb.append("---------------------------\n");
            }

        } catch (SQLException e) {
            showError("Σφάλμα ανάκτησης ραντεβού: " + e.getMessage());
        }
    }

    private void addComment(int appointmentHistoryId, JTextArea textArea, int customerId, String customerName) {
        while (true) {
            String newComment = JOptionPane.showInputDialog(null, "Γράψε νέο σχόλιο:");

            if (newComment == null) {
                int choice = JOptionPane.showConfirmDialog(
                        null,
                        "Είστε σίγουρος/σίγουρη πώς θέλετε να ακυρώσετε την σημείωση σας;",
                        "Επιβεβαίωση Ακύρωσης",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    loadAppointments(customerId, customerName, textArea);
                    return;
                } else {
                    continue;
                }
            }

            if (!newComment.trim().isEmpty()) {
                try {
                    String selectOld = "SELECT appointmenthistory_description FROM appointmenthistory WHERE appointmenthistory_id = ?";
                    PreparedStatement selStmt = connection.prepareStatement(selectOld);
                    selStmt.setInt(1, appointmentHistoryId);
                    ResultSet rs = selStmt.executeQuery();

                    String previous = "";
                    if (rs.next()) {
                        previous = rs.getString("appointmenthistory_description");
                    }

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String timestamp = "[" + now.format(formatter) + "] ";

                    String updated = (previous == null || previous.isEmpty() ? "" : previous + "\n") + timestamp + newComment;

                    String update = "UPDATE appointmenthistory SET appointmenthistory_description = ? WHERE appointmenthistory_id = ?";
                    PreparedStatement stmt = connection.prepareStatement(update);
                    stmt.setString(1, updated);
                    stmt.setInt(2, appointmentHistoryId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Το σχόλιο προστέθηκε.");
                    loadAppointments(customerId, customerName, textArea);
                    return;

                } catch (SQLException e) {
                    showError("Σφάλμα αποθήκευσης σχολίου: " + e.getMessage());
                    return;
                }
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
    }
}

