import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class ProfessionalProfile {

    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
    private String bio;
    private int professionalId;
    private Connection connection;

    private static final Color BACKGROUND = new Color(240, 240, 240);
    private static final Color ORANGE_PRIMARY = new Color(255, 153, 51);

    public ProfessionalProfile(Connection connection, int professionalId) {
        this.connection = connection;
        this.professionalId = professionalId;
    }

    public JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Το Προφίλ Μου");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        profilePanel.add(headerPanel, BorderLayout.NORTH);

        // Profile Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.1;

        // Fields
        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextArea bioArea = new JTextArea(5, 20);
        String[] specialties = {"Ηλεκτρολόγος", "Υδραυλικός", "Καθαριστής"};
        JComboBox<String> specialtyCombo = new JComboBox<>(specialties);

        // Add Fields
        addLabeledField(formPanel, gbc, 0, "Όνομα:", firstNameField);
        addLabeledField(formPanel, gbc, 1, "Επώνυμο:", lastNameField);
        addLabeledField(formPanel, gbc, 2, "Τηλέφωνο:", phoneField);
        addLabeledCombo(formPanel, gbc, 3, "Ειδικότητα:", specialtyCombo);
        addLabeledArea(formPanel, gbc, 4, "Βιογραφικό:", bioArea);

        // Save Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        JButton saveButton = new JButton("Αποθήκευση");
        saveButton.setBackground(ORANGE_PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(150, 40));
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> {
            this.firstName = firstNameField.getText();
            this.lastName = lastNameField.getText();
            this.phone = phoneField.getText();
            this.bio = bioArea.getText();

            String specialtyGreek = (String) specialtyCombo.getSelectedItem();
            switch (specialtyGreek) {
                case "Ηλεκτρολόγος": this.specialty = "electrician"; break;
                case "Υδραυλικός": this.specialty = "plumber"; break;
                case "Καθαριστής": this.specialty = "house cleaner"; break;
                default: this.specialty = ""; break;
            }

            if (checkValidity()) {
                showConfirmationScreen();
            }
        });

        buttonPanel.add(saveButton);
        formPanel.add(buttonPanel, gbc);

        // Scroll & add form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        profilePanel.add(scrollPane, BorderLayout.CENTER);

        searchProfileDetails(firstNameField, lastNameField, phoneField, specialtyCombo, bioArea);

        return profilePanel;
    }

    private void searchProfileDetails(JTextField firstNameField, JTextField lastNameField,
                                      JTextField phoneField, JComboBox<String> specialtyCombo,
                                      JTextArea bioArea) {
        try {
            String query = "SELECT professional_FirstName, professional_LastName, " +
                    "professional_phone, professional_bio, professional_speciality " +
                    "FROM professionals WHERE professional_id = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, professionalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                firstNameField.setText(rs.getString("professional_FirstName"));
                lastNameField.setText(rs.getString("professional_LastName"));
                phoneField.setText(rs.getString("professional_phone"));
                bioArea.setText(rs.getString("professional_bio"));

                String specialty = rs.getString("professional_speciality");
                switch (specialty) {
                    case "electrician": specialtyCombo.setSelectedItem("Ηλεκτρολόγος"); break;
                    case "plumber": specialtyCombo.setSelectedItem("Υδραυλικός"); break;
                    case "house cleaner": specialtyCombo.setSelectedItem("Καθαριστής"); break;
                    default: specialtyCombo.setSelectedItem(""); break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkValidity() {
        if (!validateNewProfileDetails()) {
            JOptionPane.showMessageDialog(null, "Παρακαλώ συμπληρώστε όλα τα πεδία.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateNewProfileDetails() {
        return !firstName.trim().isEmpty() &&
               !lastName.trim().isEmpty() &&
               !phone.trim().isEmpty() &&
               !bio.trim().isEmpty() &&
               !specialty.trim().isEmpty();
    }

    private void showConfirmationScreen() {
        int result = JOptionPane.showConfirmDialog(null,
                "Θέλετε σίγουρα να αποθηκεύσετε τις αλλαγές;",
                "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            customerConfirmation();
        } else {
            customerCancelledProfile();
        }
    }

    private void customerConfirmation() {
        modifyProfile();
        showNewProfileDetails();
    }

    private void customerCancelledProfile() {
        JOptionPane.showMessageDialog(null, "Η αποθήκευση ακυρώθηκε.", "Ακύρωση", JOptionPane.INFORMATION_MESSAGE);
    }

    private void modifyProfile() {
        try {
            connection.setAutoCommit(false);

            String query = "UPDATE professionals SET professional_FirstName = ?, professional_LastName = ?, " +
                    "professional_phone = ?, professional_bio = ?, professional_speciality = ? WHERE professional_id = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone);
            stmt.setString(4, bio);
            stmt.setString(5, specialty);
            stmt.setInt(6, professionalId);

            stmt.executeUpdate();
            connection.commit();
            JOptionPane.showMessageDialog(null, "Το προφίλ ενημερώθηκε επιτυχώς.", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Σφάλμα κατά την ενημέρωση του προφίλ.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showNewProfileDetails() {
        System.out.println("Νέο προφίλ:");
        System.out.println("Όνομα: " + firstName);
        System.out.println("Επώνυμο: " + lastName);
        System.out.println("Τηλέφωνο: " + phone);
        System.out.println("Ειδικότητα: " + specialty);
        System.out.println("Βιογραφικό: " + bio);
    }

    // ---------- Βοηθητικές Μέθοδοι για Layout ----------
    private void addLabeledField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(field, gbc);
    }

    private void addLabeledCombo(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComboBox<String> combo) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        combo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(combo, gbc);
    }

    private void addLabeledArea(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextArea area) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = 0;
        gbc.gridy = y + 1;
        gbc.gridwidth = 2;
        JScrollPane scrollPane = new JScrollPane(area);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(scrollPane, gbc);
    }
}
