import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowBookingDialog extends JDialog {
    private JTextField dateField;
    private JTextField timeField;
    private JTextField addressField;  // Δημιουργία πεδίου για τη διεύθυνση
    private JButton confirmButton;
    private JButton cancelButton;
    private int customerId;
    private int professionalId;  // Προσθήκη της μεταβλητής για τον επαγγελματία
    private Connection connection;
    private JFrame parentFrame;

    public ShowBookingDialog(JFrame parentFrame, Connection connection, int customerId, int professionalId) {
        this.parentFrame = parentFrame;
        this.connection = connection;
        this.customerId = customerId;
        this.professionalId = professionalId;  // Αποθήκευση του professionalId
        setTitle("Κράτηση Ραντεβού");
        setLayout(new BorderLayout());

        // Προσθήκη πεδίων για ημερομηνία, ώρα και διεύθυνση
        JPanel panel = new JPanel(new GridLayout(4, 2));  // Αφαιρέσαμε τις "Λεπτομέρειες"

        panel.add(new JLabel("Ημερομηνία:"));
        dateField = new JTextField();
        panel.add(dateField);

        panel.add(new JLabel("Ώρα:"));
        timeField = new JTextField();
        panel.add(timeField);

        panel.add(new JLabel("Διεύθυνση:"));
        addressField = new JTextField();  // Δημιουργία πεδίου για την διεύθυνση
        panel.add(addressField);

        add(panel, BorderLayout.CENTER);

        // Κουμπιά για επιβεβαίωση και ακύρωση
        confirmButton = new JButton("Επιβεβαίωση Ραντεβού");
        cancelButton = new JButton("Ακύρωση");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Ενέργεια επιβεβαίωσης
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAppointmentDetails()) {
                    customerConfirms();
                }
            }
        });

        // Ενέργεια ακύρωσης
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerRejects();
            }
        });

        pack();
        setLocationRelativeTo(parentFrame);
        setModal(true);
        setVisible(true);
    }

    // Method to display and return appointment details
    private void typeAppointmentDetails() {
        String date = dateField.getText();
        String time = timeField.getText();
        String address = addressField.getText();  // Παίρνουμε τη διεύθυνση
        JOptionPane.showMessageDialog(this, 
            "Ημερομηνία: " + date + "\n" + 
            "Ώρα: " + time + "\n" +
            "Διεύθυνση: " + address);
    }

    // Method to validate appointment details
    private boolean validateAppointmentDetails() {
        String date = dateField.getText();
        String time = timeField.getText();
        String address = addressField.getText();  // Παίρνουμε τη διεύθυνση

        if (date.isEmpty() || time.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Παρακαλώ συμπληρώστε όλα τα πεδία.");
            return false;  // Σταματάμε τη διαδικασία αν κάποιο πεδίο είναι κενό
        }
        return true;
    }

    // Method to return valid appointment
    private boolean returnValidAppointment() {
        String date = dateField.getText();
        String time = timeField.getText();
        return checkAvailability(professionalId, date, time);
    }

    // Method for customer confirmation
    private void customerConfirms() {
        if (returnValidAppointment()) {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Επιβεβαίωση Ραντεβού;", "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                saveAppointment(customerId, professionalId, dateField.getText(), timeField.getText(), addressField.getText());
                JOptionPane.showMessageDialog(null,
                        "Ο επαγγελματίας θα ενημερωθεί άμεσα για το επερχόμενο ραντεβού σας, αναμένεται επιβεβαίωση.");
                dispose();  // Κλείνουμε το παράθυρο μετά την αποδοχή
            }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Παρακαλώ πληκτρολογήστε διαθέσιμη ημερομηνία και ώρα.");
        }
    }

    // Method for customer rejection
    private void customerRejects() {
        int cancel = JOptionPane.showConfirmDialog(null,
                "Είστε σίγουροι ότι θέλετε να ακυρώσετε την διαδικασία;", "Ακύρωση", JOptionPane.YES_NO_OPTION);
        if (cancel == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, "Ακύρωση κράτησης. Επιστροφή στην αρχική οθόνη.");
            dispose();  // Κλείσιμο του παραθύρου
        }
    }

    // Method to handle confirmation of rejection
    private void customerConfirmsRejection() {
        int cancel = JOptionPane.showConfirmDialog(null,
                "Η διαδικασία ακυρώθηκε. Είστε σίγουροι ότι θέλετε να επιστρέψετε στην αρχική οθόνη;", 
                "Επιβεβαίωση Ακύρωσης", JOptionPane.YES_NO_OPTION);
        if (cancel == JOptionPane.YES_OPTION) {
            dispose();  // Κλείσιμο του παραθύρου
        }
    }

    // Check availability of the professional for the specified date and time
    private boolean checkAvailability(int professionalId, String date, String time) {
        String query = "SELECT COUNT(*) FROM appointment WHERE professional_id = ? AND BeginDate = STR_TO_DATE(?, '%d/%m/%Y %H:%i')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, professionalId);
            stmt.setString(2, date + " " + time);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;  // true if no appointments found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Save the appointment details to the database
    private void saveAppointment(int customerId, int professionalId, String date, String time, String address) {
        // Βήμα 1: Αναζητούμε την υπηρεσία για τον επαγγελματία
        try {
            String serviceQuery = "SELECT service_id FROM service WHERE professional_id = ?";
            PreparedStatement serviceStmt = connection.prepareStatement(serviceQuery);
            serviceStmt.setInt(1, professionalId);  // Χρήση του `professionalId`
            ResultSet serviceRs = serviceStmt.executeQuery();

            if (serviceRs.next()) {
                int serviceId = serviceRs.getInt("service_id");

                // Βήμα 2: Εισαγωγή στο appointment
                String insertQuery = "INSERT INTO appointment (customer_id, professional_id, service_id, BeginDate, EndDate, address, appointment_status) " +
                                     "VALUES (?, ?, ?, STR_TO_DATE(?, '%d/%m/%Y %H:%i'), DATE_ADD(STR_TO_DATE(?, '%d/%m/%Y %H:%i'), INTERVAL 1 HOUR), ?, 'pending')";
                PreparedStatement stmt = connection.prepareStatement(insertQuery);
                stmt.setInt(1, customerId);
                stmt.setInt(2, professionalId);  // Χρήση του `professionalId`
                stmt.setInt(3, serviceId);
                stmt.setString(4, date + " " + time);
                stmt.setString(5, date + " " + time);
                stmt.setString(6, address);  // Προσθήκη της διεύθυνσης
                stmt.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(null, "Δεν βρέθηκε υπηρεσία για τον επαγγελματία.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}