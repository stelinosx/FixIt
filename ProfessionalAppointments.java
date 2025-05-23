import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfessionalAppointments extends JPanel {
    private int professionalId;
    private Connection connection;
    private final Color BACKGROUND = new Color(250, 250, 250);
    private final Color CARD_BG = new Color(255, 255, 255);
    private final Color PRIMARY_ORANGE = new Color(255, 140, 0);
    private final Color ACCEPT_GREEN = new Color(76, 175, 80);
    private final Color DECLINE_RED = new Color(244, 67, 54);
    private JPanel listPanel;
    private JScrollPane scrollPane;

    public ProfessionalAppointments(int professionalId, Connection connection) {
        this.professionalId = professionalId;
        this.connection = connection;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        initUI();
        updatePastAppointments();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📅 Ραντεβού");
        titleLabel.setFont(new Font("Noto Color Emoji", Font.BOLD, 26));
        titleLabel.setForeground(new Color(45, 45, 45));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = new JButton("Ανανέωση");
        styleButton(refreshButton, PRIMARY_ORANGE);
        refreshButton.setForeground(Color.black);
        refreshButton.addActionListener(e -> {
            updatePastAppointments();
            loadAppointments();
        });
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BACKGROUND);
        listPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadAppointments();
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void updatePastAppointments() {
        try {
            String query = "UPDATE appointment " +
                    "SET appointment_status = 'completed' " +
                    "WHERE appointment_status = 'accepted' " +
                    "AND EndDate < NOW() " +
                    "AND professional_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, professionalId);
            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println(updatedRows + " ραντεβού ενημερώθηκαν σε 'completed'");
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα ενημέρωσης παλαιών ραντεβού: " + e.getMessage());
        }
    }

    private void loadAppointments() {
        listPanel.removeAll();
        try {
            String query = "SELECT a.appointment_id, s.service_type, a.address, a.appointment_status, " +
                    "DATE_FORMAT(a.BeginDate, '%d/%m/%Y') as appointment_date, " +
                    "DATE_FORMAT(a.BeginDate, '%H:%i') as appointment_time, " +
                    "c.customer_FirstName, c.customer_LastName " +
                    "FROM appointment a " +
                    "JOIN customers c ON a.customer_id = c.customer_id " +
                    "JOIN service s ON a.service_id = s.service_id " +
                    "WHERE a.professional_id = ? " +
                    "ORDER BY a.appointment_status = 'pending' DESC, a.appointment_id DESC";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, professionalId);
            ResultSet rs = stmt.executeQuery();

            boolean hasAppointments = false;

            while (rs.next()) {
                hasAppointments = true;
                int appointmentId = rs.getInt("appointment_id");
                String serviceType = rs.getString("service_type");
                String status = rs.getString("appointment_status");
                String appointmentDate = rs.getString("appointment_date");
                String appointmentTime = rs.getString("appointment_time");
                String customerName = rs.getString("customer_FirstName") + " " + rs.getString("customer_LastName");
                String location = rs.getString("address");

                JPanel cardPanel = new JPanel(new BorderLayout());
                cardPanel.setBackground(CARD_BG);
                cardPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                        new EmptyBorder(15, 15, 15, 15)
                ));

                AppointmentCard card = new AppointmentCard(
                        appointmentId,
                        customerName,
                        appointmentDate,
                        appointmentTime,
                        serviceType,
                        location,
                        status,
                        e -> {}, // cancel
                        e -> {}  // review
                );
                cardPanel.add(card, BorderLayout.CENTER);

                if ("pending".equals(status)) {
                    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                    actionPanel.setBackground(CARD_BG);

                    JButton acceptButton = new JButton("Αποδοχή");
                    styleButton(acceptButton, ACCEPT_GREEN);
                    acceptButton.setForeground(Color.BLACK);
                    acceptButton.addActionListener(e -> updateAppointmentStatus(appointmentId, "accepted"));

                    JButton rejectButton = new JButton("Απόρριψη");
                    styleButton(rejectButton, DECLINE_RED);
                    rejectButton.setForeground(Color.BLACK); 
                    rejectButton.addActionListener(e -> updateAppointmentStatus(appointmentId, "declined"));

                    actionPanel.add(acceptButton);
                    actionPanel.add(rejectButton);
                    cardPanel.add(actionPanel, BorderLayout.SOUTH);

                
                } else if ("completed".equals(status)) {
                    JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    statusPanel.setBackground(CARD_BG);

                    boolean hasReview = checkIfReviewed(appointmentId);

                    JLabel statusLabel = new JLabel(
                            hasReview ? "Ολοκληρωμένο - Έχει αξιολογηθεί" : "Ολοκληρωμένο - Αναμένεται αξιολόγηση"
                    );
                    statusLabel.setForeground(new Color(33, 150, 243));
                    statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    statusPanel.add(statusLabel);

                    cardPanel.add(statusPanel, BorderLayout.SOUTH);
                }

                listPanel.add(cardPanel);
                listPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (!hasAppointments) {
                JLabel noAppointmentsLabel = new JLabel("Δεν έχετε κανένα ραντεβού");
                noAppointmentsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                noAppointmentsLabel.setForeground(new Color(130, 130, 130));
                noAppointmentsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel emptyPanel = new JPanel();
                emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
                emptyPanel.setBackground(BACKGROUND);
                emptyPanel.add(Box.createVerticalGlue());
                emptyPanel.add(noAppointmentsLabel);
                emptyPanel.add(Box.createVerticalGlue());

                listPanel.add(emptyPanel);
            }

        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Σφάλμα φόρτωσης ραντεβού: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            listPanel.add(errorLabel);
            e.printStackTrace();
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private boolean checkIfReviewed(int appointmentId) {
        try {
            String query = "SELECT COUNT(*) FROM review WHERE appointment_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateAppointmentStatus(int appointmentId, String status) {
        try {
            String update = "UPDATE appointment SET appointment_status = ? WHERE appointment_id = ?";
            PreparedStatement stmt = connection.prepareStatement(update);
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                String message = "accepted".equals(status) ?
                        "Το ραντεβού έγινε αποδεκτό με επιτυχία!" :
                        "Το ραντεβού απορρίφθηκε με επιτυχία!";

                JOptionPane.showMessageDialog(this,
                        message,
                        "Επιτυχία",
                        JOptionPane.INFORMATION_MESSAGE);

                loadAppointments();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Δεν ήταν δυνατή η ενημέρωση του ραντεβού.",
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Σφάλμα βάσης δεδομένων: " + e.getMessage(),
                    "Σφάλμα",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}

