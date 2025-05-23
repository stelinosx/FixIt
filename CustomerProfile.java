import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerProfile {

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private int customerId;
    private Connection connection;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextField addressField;
    private String originalFirstName;
    private String originalLastName;
    private String originalPhone;
    private String originalAddress;

    public CustomerProfile(Connection connection, int customerId) {
        this.connection = connection;
        this.customerId = customerId;
    }

    public JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(new Color(245, 245, 245)); 

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(50, 50, 50));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Το Προφίλ Μου");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        profilePanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 30, 20, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.1;

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 18);

        firstNameField = new JTextField(30);
        firstNameField.setFont(fieldFont);

        lastNameField = new JTextField(30);
        lastNameField.setFont(fieldFont);

        phoneField = new JTextField(30);
        phoneField.setFont(fieldFont);

        addressField = new JTextField(30);
        addressField.setFont(fieldFont);

        addLabeledField(formPanel, gbc, 0, "Όνομα:", firstNameField);
        addLabeledField(formPanel, gbc, 1, "Επώνυμο:", lastNameField);
        addLabeledField(formPanel, gbc, 2, "Τηλέφωνο:", phoneField);
        addLabeledField(formPanel, gbc, 3, "Διεύθυνση:", addressField);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Αποθήκευση");
        saveButton.setBackground(Color.BLACK);
        saveButton.setForeground(Color.BLACK);
        saveButton.setPreferredSize(new Dimension(200, 50));
        saveButton.setFont(new Font("Arial", Font.BOLD, 18));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Είστε σίγουροι ότι θέλετε να αποθηκεύσετε τις αλλαγές?",
                    "Επιβεβαίωση Αποθήκευσης",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                String newFirstName = firstNameField.getText().trim();
                String newLastName = lastNameField.getText().trim();
                String newPhone = phoneField.getText().trim();
                String newAddress = addressField.getText().trim();

                // Έλεγχος τηλεφώνου: 10 ψηφία
                if (!newPhone.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(null,
                        "Ο αριθμός τηλεφώνου πρέπει να περιέχει ακριβώς 10 ψηφία.",
                        "Μη έγκυρο τηλέφωνο",
                        JOptionPane.PLAIN_MESSAGE);

                    // Επαναφορά τιμών
                    updateProfileUI();
                    return;
                }

                // Ενημέρωση πεδίων
                this.firstName = newFirstName;
                this.lastName = newLastName;
                this.phone = newPhone;
                this.address = newAddress;

                // Αποθήκευση στο DB
                updateCustomerProfile();

                // Ενημέρωση original τιμών
                this.originalFirstName = firstName;
                this.originalLastName = lastName;
                this.originalPhone = phone;
                this.originalAddress = address;

            } else {
                JOptionPane.showMessageDialog(null,
                        "Οι αλλαγές σας δεν αποθηκεύτηκαν.",
                        "Ακύρωση Αποθήκευσης",
                        JOptionPane.PLAIN_MESSAGE);

                // Επαναφορά αρχικών τιμών
                this.firstName = originalFirstName;
                this.lastName = originalLastName;
                this.phone = originalPhone;
                this.address = originalAddress;
                updateProfileUI();
            }
        });

        buttonPanel.add(saveButton);
        formPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        profilePanel.add(scrollPane, BorderLayout.CENTER);

        loadCustomerProfile();

        return profilePanel;
    }

    private void loadCustomerProfile() {
        try {
            String query = "SELECT customer_FirstName, customer_LastName, customer_phone, address FROM customers WHERE customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                firstName = rs.getString("customer_FirstName");
                lastName = rs.getString("customer_LastName");
                phone = rs.getString("customer_phone");
                address = rs.getString("address");

                originalFirstName = firstName;
                originalLastName = lastName;
                originalPhone = phone;
                originalAddress = address;

                updateProfileUI();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateCustomerProfile() {
        try {
            connection.setAutoCommit(false);
            String query = "UPDATE customers SET customer_FirstName = ?, customer_LastName = ?, customer_phone = ?, address = ? WHERE customer_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            stmt.setInt(5, customerId);

            stmt.executeUpdate();
            connection.commit();

            JOptionPane.showMessageDialog(null,
                    "Τα δεδομένα σας αποθηκεύτηκαν επιτυχώς!",
                    "Επιτυχία",
                    JOptionPane.PLAIN_MESSAGE);

            updateProfileUI();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null,
                    "Σφάλμα κατά την ενημέρωση του προφίλ.",
                    "Σφάλμα",
                    JOptionPane.PLAIN_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProfileUI() {
        firstNameField.setText(firstName != null ? firstName : "");
        lastNameField.setText(lastName != null ? lastName : "");
        phoneField.setText(phone != null ? phone : "");
        addressField.setText(address != null ? address : "");
    }

    private void addLabeledField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(250, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        panel.add(field, gbc);
    }
}