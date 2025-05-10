import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ProfessionalProfile {

    private String firstName;
    private String lastName;
    private String phone;
    private String specialty;
    private String bio;
    private int professionalId;
    private Connection connection;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField phoneField;
    private JTextArea bioArea;
    private String originalFirstName;
    private String originalLastName;
    private String originalPhone;
    private String originalBio;
    private String originalSpecialty;
    private JComboBox<String> specialtyCombo;
    
    public ProfessionalProfile(Connection connection, int professionalId) {
        this.connection = connection;
        this.professionalId = professionalId;
    }

    public JPanel createProfilePanel() {
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(new Color(245, 245, 245));  // Ελαφρύ γκρίζο φόντο για πιο μοντέρνα αίσθηση
    
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(50, 50, 50));  // Σκούρο φόντο για header
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Πιο άνετο κενό
    
        JLabel titleLabel = new JLabel("Το Προφίλ Μου");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Μοντέρνα γραμματοσειρά και μεγαλύτερη
        titleLabel.setForeground(Color.WHITE); // Άσπρο χρώμα για τον τίτλο
        headerPanel.add(titleLabel, BorderLayout.WEST);
    
        profilePanel.add(headerPanel, BorderLayout.NORTH);
    
        // Profile Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(255, 255, 255));  // Λευκό φόντο για τη φόρμα
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),  // Λεπτό γκρίζο περίγραμμα
                BorderFactory.createEmptyBorder(10, 30, 20, 30))); // Άνετο κενό
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Μικρότερα κενά για πιο καθαρή εμφάνιση
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.1;
    
        // Fields
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 18);  // Χρησιμοποιούμε μοντέρνα γραμματοσειρά
    
        JTextField firstNameField = new JTextField(30);  // Μεγαλύτερο μέγεθος πεδίων
        firstNameField.setFont(fieldFont);
    
        JTextField lastNameField = new JTextField(30);  // Μεγαλύτερο μέγεθος πεδίων
        lastNameField.setFont(fieldFont);
    
        JTextField phoneField = new JTextField(30);  // Μεγαλύτερο μέγεθος πεδίων
        phoneField.setFont(fieldFont);
    
        JTextArea bioArea = new JTextArea(5, 30);  // Μεγαλύτερο μέγεθος περιοχής κειμένου
        bioArea.setFont(new Font("Arial", Font.PLAIN, 16));  // Κανονική γραμματοσειρά αρχικά
        bioArea.setText("Περιγράψτε τον εαυτό σας..."); // Προκαθορισμένο μήνυμα
        bioArea.setForeground(Color.GRAY); // Χρώμα για το placeholder κείμενο

        this.firstNameField = firstNameField;
        this.lastNameField = lastNameField;
        this.phoneField = phoneField;
        this.bioArea = bioArea;

        bioArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (bioArea.getText().equals("Περιγράψτε τον εαυτό σας...")) {
                    bioArea.setText("");
                    bioArea.setForeground(Color.BLACK);
                    bioArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Κανονική γραμματοσειρά όσο γράφει
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (bioArea.getText().trim().isEmpty()) {
                    bioArea.setText("Περιγράψτε τον εαυτό σας..."); // Επαναφορά placeholder
                    bioArea.setForeground(Color.GRAY);
                    bioArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Placeholder font
                } else {
                    // Αν έχει γραφτεί κάτι, κάνε το bold
                    bioArea.setFont(new Font("Arial", Font.BOLD, 16));
                }
            }
            });
        
        String[] specialties = {"Ηλεκτρολόγος", "Υδραυλικός", "Ψυκτικός", "Ελαιοχρωματιστής",
                "Τεχνικός PC", "Θερμοϋδραυλικός", "Σιδεράς", "Τεχνίτης Γύψου", "Καθαριστής Οικιών",
                "Καθαριστής Χαλιών", "Τεχνικός Αποφράξεων", "Ειδικός Απεντομώσεων/Μυοκτονιών"};
        JComboBox<String> specialtyCombo = new JComboBox<>(specialties);
        specialtyCombo.setFont(fieldFont);
        
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
    
        // Save Button
        JButton saveButton = new JButton("Αποθήκευση");
        saveButton.setBackground(Color.BLACK);  // Ορίζουμε το χρώμα φόντου
        saveButton.setForeground(Color.BLACK);     // Ορίζουμε το χρώμα γραμμάτων σε μαύρο
        saveButton.setPreferredSize(new Dimension(200, 50)); // Πιο μεγάλο κουμπί
        saveButton.setFont(new Font("Arial", Font.BOLD, 18));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

  saveButton.addActionListener(e -> {
    // Δημιουργία του JOptionPane με το μήνυμα και χωρίς εικονίδιο
    JOptionPane optionPane = new JOptionPane(
        "Είστε σίγουροι ότι θέλετε να αποθηκεύσετε τις αλλαγές?", 
        JOptionPane.PLAIN_MESSAGE, // Χρησιμοποιούμε PLAIN_MESSAGE για να μην εμφανίζεται εικονίδιο
        JOptionPane.YES_NO_OPTION
    );

    // Ρύθμιση εμφάνισης του JOptionPane με πορτοκαλί φόντο και άσπρα γράμματα
    UIManager.put("OptionPane.background", new Color(255, 165, 0));  // Πορτοκαλί φόντο
    UIManager.put("Panel.background", new Color(255, 165, 0));  // Πορτοκαλί φόντο για το Panel
    UIManager.put("OptionPane.messageForeground", Color.WHITE);  // Λευκά γράμματα
    UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));  // Μεγαλύτερη γραμματοσειρά (16px, bold)

    // Ρύθμιση για μαύρο φόντο και μαύρα γράμματα για τα κουμπιά
    UIManager.put("Button.background", Color.BLACK);  // Μαύρο φόντο για τα κουμπιά
    UIManager.put("Button.foreground", Color.BLACK);  // Μαύρα γράμματα για τα κουμπιά
    UIManager.put("Button.font", new Font("Arial", Font.BOLD, 14));  // Μεγαλύτερη γραμματοσειρά (14px, bold)
    UIManager.put("Button.border", BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Ρύθμιση περιθωρίων κουμπιού

    // Δημιουργία του JDialog από το JOptionPane
    JDialog dialog = optionPane.createDialog("Επιβεβαίωση Αποθήκευσης");

    // Ρύθμιση του δείκτη του ποντικιού σε χέρι όταν περνάει από πάνω τα κουμπιά
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowOpened(java.awt.event.WindowEvent windowEvent) {
            Component[] buttons =  optionPane.getComponents();
            for (Component button : buttons) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  // Χεράκι
            }
        }
    });


    // Εμφάνιση του dialog
    dialog.setVisible(true);

    // Ανάλογα με την απόφαση του χρήστη (Ναι ή Όχι)
    int confirm = optionPane.getValue() == null ? JOptionPane.CLOSED_OPTION : (Integer) optionPane.getValue();
    if (confirm == JOptionPane.YES_OPTION) {
        this.firstName = firstNameField.getText();
        this.lastName = lastNameField.getText();
        this.phone = phoneField.getText();
        this.bio = bioArea.getText();

        String specialtyGreek = (String) specialtyCombo.getSelectedItem();
        switch (specialtyGreek) {
            case "Ηλεκτρολόγος": this.specialty = "electrician"; break;
            case "Υδραυλικός": this.specialty = "plumber"; break;
            case "Ψυκτικός": this.specialty = "refrigerant"; break;
            case "Ελαιοχρωματιστής": this.specialty = "painter"; break;
            case "Τεχνικός PC": this.specialty = "pc technician"; break;
            case "Θερμοϋδραυλικός": this.specialty = "thermohydraulic"; break;
            case "Σιδεράς": this.specialty = "smith"; break;
            case "Τεχνίτης Γύψου": this.specialty = "Plaster Craftsman"; break;
            case "Καθαριστής Χαλιών": this.specialty = "Carpet Cleaner"; break;
            case "Καθαριστής Οικιών": this.specialty = "house cleaner"; break;
            case "Τεχνικός Αποφράξεων": this.specialty = "drain technician"; break;
            case "Ειδικός Απεντομώσεων/Μυοκτονιών": this.specialty = "pest controller"; break;
        }

        // Έλεγχος για τηλέφωνο
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(null,
                "Ο αριθμός τηλεφώνου πρέπει να περιέχει ακριβώς 10 ψηφία.",
                "Μη έγκυρο τηλέφωνο",
                JOptionPane.PLAIN_MESSAGE);

            // Επαναφορά παλιών δεδομένων στο GUI πριν το σφάλμα
            this.firstName = originalFirstName;
            this.lastName = originalLastName;
            this.phone = originalPhone;
            this.bio = originalBio;
            this.specialty = originalSpecialty;

            updateProfileUI(); // Επαναφορά δεδομένων GUI

            return;  // Μην προχωρήσουμε με την αποθήκευση
        }

        updateProfessionalProfile();

        // Νέα δεδομένα γίνονται τα "αρχικά" για μελλοντική ακύρωση
        this.originalFirstName = this.firstName;
        this.originalLastName = this.lastName;
        this.originalPhone = this.phone;
        this.originalBio = this.bio;
        this.originalSpecialty = this.specialty;

    } else if (confirm == JOptionPane.NO_OPTION) {
        JOptionPane.showMessageDialog(null,
            "Οι αλλαγές σας δεν αποθηκεύτηκαν.",
            "Ακύρωση Αποθήκευσης",
            JOptionPane.PLAIN_MESSAGE);

        // Επαναφορά των αρχικών τιμών
        this.firstName = originalFirstName;
        this.lastName = originalLastName;
        this.phone = originalPhone;
        this.bio = originalBio;
        this.specialty = originalSpecialty;

        updateProfileUI();  // Ενημέρωση GUI
    }
});
        
    
        buttonPanel.add(saveButton);
        formPanel.add(buttonPanel, gbc);
    
        // Scroll & add form
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        profilePanel.add(scrollPane, BorderLayout.CENTER);
    
        loadProfessionalProfile(firstNameField, lastNameField, phoneField, specialtyCombo, bioArea);
    
        return profilePanel;
    }

    private void loadProfessionalProfile(JTextField firstNameField, JTextField lastNameField,
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
            String firstName = rs.getString("professional_FirstName");
            String lastName = rs.getString("professional_LastName");
            String phone = rs.getString("professional_phone");
            String bio = rs.getString("professional_bio");
            String specialty = rs.getString("professional_speciality");

            // Set fields
            firstNameField.setText(firstName != null ? firstName : "");
            lastNameField.setText(lastName != null ? lastName : "");
            phoneField.setText(phone != null ? phone : "");
            bioArea.setText(bio != null ? bio : "");

            if (specialty != null) {
                switch (specialty) {
                    case "electrician": specialtyCombo.setSelectedItem("Ηλεκτρολόγος"); break;
                    case "plumber": specialtyCombo.setSelectedItem("Υδραυλικός"); break;
                    case "refrigerant": specialtyCombo.setSelectedItem("Ψυκτικός"); break;
                    case "painter": specialtyCombo.setSelectedItem("Ελαιοχρωματιστής"); break;
                    case "pc technician": specialtyCombo.setSelectedItem("Τεχνικός PC"); break;
                    case "thermohydraulic": specialtyCombo.setSelectedItem("Θερμοϋδραυλικός"); break;
                    case "smith": specialtyCombo.setSelectedItem("Σιδεράς"); break;
                    case "Plaster Craftsman": specialtyCombo.setSelectedItem("Τεχνίτης Γύψου"); break;
                    case "Carpet Cleaner": specialtyCombo.setSelectedItem("Καθαριστής Χαλιών"); break;
                    case "house cleaner": specialtyCombo.setSelectedItem("Καθαριστής Οικιών"); break;
                    case "drain technician": specialtyCombo.setSelectedItem("Τεχνικός Αποφράξεων"); break;
                    case "pest controller": specialtyCombo.setSelectedItem("Ειδικός Απεντομώσεων/Μυοκτονιών"); break;
                }
            } else {
                specialtyCombo.setSelectedItem("");
            }

            // Αποθήκευση τρεχουσών τιμών για την Ακύρωση
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.bio = bio;
            this.specialty = specialty;

            this.originalFirstName = firstName;
            this.originalLastName = lastName;
            this.originalPhone = phone;
            this.originalBio = bio;
            this.originalSpecialty = specialty;

            this.firstNameField = firstNameField;
            this.lastNameField = lastNameField;
            this.phoneField = phoneField;
            this.bioArea = bioArea;
            this.specialtyCombo = specialtyCombo;   
        }

        }   catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProfessionalProfile() {
        try {
            // Ξεκινάμε το transaction για αποθήκευση
            connection.setAutoCommit(false);

            String query = "UPDATE professionals SET professional_FirstName = ?, professional_LastName = ?, " +
                "professional_phone = ?, professional_bio = ?, professional_speciality = ? WHERE professional_id = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone);
            stmt.setString(4, bio);
            stmt.setString(5, specialty);
            stmt.setInt(6, professionalId); // Η ταυτότητα του επαγγελματία

            // Εκτελέσαμε το update
            stmt.executeUpdate();
            connection.commit();  // Ολοκλήρωση της αποθήκευσης

            // Μήνυμα επιτυχίας
            JOptionPane.showMessageDialog(null,
                "Τα δεδομένα σας αποθηκεύτηκαν επιτυχώς!",
                "Επιτυχία",
                JOptionPane.PLAIN_MESSAGE);

            // Ανανεώνουμε τα δεδομένα στο GUI
            updateProfileUI();

        } catch (SQLException e) {
            try {
                connection.rollback();  // Σε περίπτωση σφάλματος, επιστρέφουμε την προηγούμενη κατάσταση
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "Σφάλμα κατά την ενημέρωση του προφίλ.", "Σφάλμα", JOptionPane.PLAIN_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(true);  // Επαναφορά του commit mode
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProfileUI() {
        firstNameField.setText(this.firstName);
        lastNameField.setText(this.lastName);
        phoneField.setText(this.phone);
        bioArea.setText(this.bio);
    
        switch (this.specialty) {
            case "electrician": specialtyCombo.setSelectedItem("Ηλεκτρολόγος"); break;
            case "plumber": specialtyCombo.setSelectedItem("Υδραυλικός"); break;
            case "refrigerant": specialtyCombo.setSelectedItem("Ψυκτικός"); break;
            case "painter": specialtyCombo.setSelectedItem("Ελαιοχρωματιστής"); break;
            case "pc technician": specialtyCombo.setSelectedItem("Τεχνικός PC"); break;
            case "thermohydraulic": specialtyCombo.setSelectedItem("Θερμοϋδραυλικός"); break;
            case "smith": specialtyCombo.setSelectedItem("Σιδεράς"); break;
            case "Plaster Craftsman": specialtyCombo.setSelectedItem("Τεχνίτης Γύψου"); break;
            case "Carpet Cleaner": specialtyCombo.setSelectedItem("Καθαριστής Χαλιών"); break;
            case "house cleaner": specialtyCombo.setSelectedItem("Καθαριστής Οικιών"); break;
            case "drain technician": specialtyCombo.setSelectedItem("Τεχνικός Αποφράξεων"); break;
            case "pest controller": specialtyCombo.setSelectedItem("Ειδικός Απεντομώσεων/Μυοκτονιών"); break;
            default: specialtyCombo.setSelectedItem(""); break;
        }
    }

    // ---------- Βοηθητικές Μέθοδοι για Layout ----------
    private void addLabeledField(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));  // Μεγαλύτερη γραμματοσειρά για τις ετικέτες
        panel.add(label, gbc);
    
        gbc.gridx = 1;
        field.setFont(new Font("Arial", Font.PLAIN, 16)); // Μεγαλύτερη γραμματοσειρά για τα πεδία
        field.setPreferredSize(new Dimension(250, 30));   // Επαγγελματικό μέγεθος πεδίου
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));  // Καλύτερη εμφάνιση περιγράμματος
        panel.add(field, gbc);
    }
    
    // Προσθήκη Ετικέτας και ComboBox
    private void addLabeledCombo(JPanel panel, GridBagConstraints gbc, int y, String labelText, JComboBox<String> combo) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));  // Μεγαλύτερη γραμματοσειρά για τις ετικέτες
        panel.add(label, gbc);
    
        gbc.gridx = 1;
        combo.setFont(new Font("Arial", Font.PLAIN, 16)); // Μεγαλύτερη γραμματοσειρά για το ComboBox
        combo.setPreferredSize(new Dimension(250, 30));   // Επαγγελματικό μέγεθος combo box
        combo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));  // Καλύτερη εμφάνιση περιγράμματος
        panel.add(combo, gbc);
    }
    
    // Προσθήκη Ετικέτας και JTextArea
    private void addLabeledArea(JPanel panel, GridBagConstraints gbc, int y, String labelText, JTextArea area) {
        gbc.gridx = 0;
        gbc.gridy = y;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 16));  // Μεγαλύτερη γραμματοσειρά για τις ετικέτες
        panel.add(label, gbc);
    
        gbc.gridx = 0;
        gbc.gridy = y + 1;
        gbc.gridwidth = 2;  // Το JTextArea να καταλαμβάνει όλο το πλάτος
        JScrollPane scrollPane = new JScrollPane(area);
        area.setFont(new Font("Arial", Font.PLAIN, 16));  // Μεγαλύτερη γραμματοσειρά για το JTextArea
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));  // Καλύτερη εμφάνιση περιγράμματος
        panel.add(scrollPane, gbc);
    }
}
    


