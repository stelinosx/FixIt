import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;


public class LoginFrame extends JFrame {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JPanel loginPanel;
    private JPanel registerPanel;
    
    // Colors
    private static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private static final Color ORANGE_LIGHT = new Color(255, 165, 0);
    private static final Color ORANGE_DARK = new Color(255, 69, 0);
    private static final Color BACKGROUND = new Color(255, 250, 240);
    
    // Login components
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JLabel loginErrorLabel;
    
    // Register components
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JComboBox<String> userTypeCombo;
    private JButton registerButton;
    private JLabel registrationErrorLabel;

    // Database connection
    private Connection connection;
    public LoginFrame() {
        try {
            // Initialize database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC driver loaded successfully");
            
            String url = "jdbc:mysql://localhost:3306/FixItDB";
            String username = "root";
            String password = "NikolasMicro21!";
            
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully");
            
            // Test query
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Available tables in the database:");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found. Make sure the JAR file is in your classpath");
            JOptionPane.showMessageDialog(null, "Σφάλμα φόρτωσης του οδηγού JDBC: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Σφάλμα σύνδεσης με τη βάση δεδομένων: " + e.getMessage() + 
                "\nΕλέγξτε αν:\n1. Ο MySQL server είναι ενεργός\n2. Η βάση fixi υπάρχει\n3. Το username και password είναι σωστά");
            System.exit(1);
        }

        setTitle("FixIt - Login/Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        
        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);
            }
        };
        headerPanel.setPreferredSize(new Dimension(500, 200));

        ImageIcon logoIcon = new ImageIcon("images\\FixIt.png");

        Image scaledImage = logoIcon.getImage().getScaledInstance(280, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);
        JLabel logoLabel = new JLabel(resizedIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(logoLabel, BorderLayout.CENTER);
    
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
    
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND);
        tabbedPane.setForeground(ORANGE_DARK);
        
        // Create login panel
        loginPanel = createLoginPanel();
        tabbedPane.addTab("Login", loginPanel);
        
        // Create register panel
        registerPanel = createRegisterPanel();
        tabbedPane.addTab("Register", registerPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:"); 
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        loginUsernameField = new JTextField(20);
        loginUsernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginUsernameField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(loginUsernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPasswordField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(loginPasswordField, gbc);
    
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        styleButton(loginButton);
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);

        gbc.gridy = 3;
        loginErrorLabel = new JLabel(" ", SwingConstants.CENTER);
        loginErrorLabel.setForeground(Color.RED);
        loginErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(loginErrorLabel, gbc);
    
        return panel;
    }
    
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);
        javax.swing.border.Border orangeBorder = BorderFactory.createLineBorder(ORANGE_LIGHT);
    
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        panel.add(usernameLabel, gbc);
    
        gbc.gridx = 1;
        regUsernameField = new JTextField(20);
        regUsernameField.setFont(fieldFont);
        regUsernameField.setBorder(orangeBorder);
        panel.add(regUsernameField, gbc);
    
        // Password
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        panel.add(passwordLabel, gbc);
    
        gbc.gridx = 1;
        regPasswordField = new JPasswordField(20);
        regPasswordField.setFont(fieldFont);
        regPasswordField.setBorder(orangeBorder);
        panel.add(regPasswordField, gbc);
    
        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        panel.add(emailLabel, gbc);
    
        gbc.gridx = 1;
        regEmailField = new JTextField(20);
        regEmailField.setFont(fieldFont);
        regEmailField.setBorder(orangeBorder);
        panel.add(regEmailField, gbc);
    
        // Phone
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(labelFont);
        panel.add(phoneLabel, gbc);
    
        gbc.gridx = 1;
        regPhoneField = new JTextField(20);
        regPhoneField.setFont(fieldFont);
        regPhoneField.setBorder(orangeBorder);
        panel.add(regPhoneField, gbc);
    
        // User Type
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel userTypeLabel = new JLabel("User Type:");
        userTypeLabel.setFont(labelFont);
        panel.add(userTypeLabel, gbc);
    
        gbc.gridx = 1;
        userTypeCombo = new JComboBox<>(new String[] {"Customer", "Professional"});
        userTypeCombo.setFont(fieldFont);
        userTypeCombo.setBorder(orangeBorder);
        panel.add(userTypeCombo, gbc);
    
        // Register button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        registerButton = new JButton("Register");
        styleButton(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
        panel.add(registerButton, gbc);
    
        // Error label κάτω από το κουμπί
        gbc.gridy++;
        registrationErrorLabel = new JLabel(" ", SwingConstants.CENTER);
        registrationErrorLabel.setForeground(Color.RED);
        registrationErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(registrationErrorLabel, gbc);
    
        return panel;
    }
        
    private void styleButton(JButton button) {
        button.setBackground(ORANGE_PRIMARY);        
        button.setFocusPainted(false);               
        button.setFont(new Font("Arial", Font.BOLD, 14)); 
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
    
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ORANGE_DARK);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ORANGE_PRIMARY);
            }
        });
    }

    private void handleLogin() {
    String username = loginUsernameField.getText().trim();
    String password = new String(loginPasswordField.getPassword()).trim();

    if (username.isEmpty() || password.isEmpty()) {
        loginErrorLabel.setText("Please fill in all fields");
        return;
    }

    loginErrorLabel.setText("");

    try {
        String query = "SELECT user_id, user_type FROM users WHERE user_username = ? AND user_password = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int userId = rs.getInt("user_id");
            String userType = rs.getString("user_type");

            System.out.println("Login successful: user_id = " + userId + ", user_type = " + userType);
            CustomDialog dialog = new CustomDialog(this, "Login successful!");
            dialog.setVisible(true);

            if (userType.equals("customer")) {
                query = "SELECT customer_id FROM customers WHERE user_id = ?";
                stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                rs = stmt.executeQuery();
    
                if (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    System.out.println("Customer found: customer_id = " + customerId);
                    SwingUtilities.invokeLater(() -> new CustomerFrame(customerId, connection).setVisible(true));
                    this.dispose();
                } else {
                    System.out.println("Customer record not found for user_id: " + userId);
                    JOptionPane.showMessageDialog(this, "Customer record not found", "Error", JOptionPane.ERROR_MESSAGE);
                    new LoginFrame().setVisible(true);
                    this.dispose();
                }

            } else if (userType.equals("professional")) {
                query = "SELECT professional_id FROM professionals WHERE user_id = ?";
                stmt = connection.prepareStatement(query);
                stmt.setInt(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    int professionalId = rs.getInt("professional_id");
                    System.out.println("Professional found: professional_id = " + professionalId);
                    SwingUtilities.invokeLater(() -> new ProfessionalFrame(professionalId, connection).setVisible(true));
                    this.dispose();
                } else {
                    System.out.println("Professional record not found for user_id: " + userId);
                    JOptionPane.showMessageDialog(this, "Professional record not found", "Error", JOptionPane.ERROR_MESSAGE);
                    new LoginFrame().setVisible(true);
                    this.dispose();
                }
            }

        } else {
            System.out.println("Login failed: wrong username or password");
            loginErrorLabel.setText("Wrong username or password");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        loginErrorLabel.setText("Database error: " + e.getMessage());
    }
}

    private void handleRegistration() {
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String userType = (String) userTypeCombo.getSelectedItem();
    
        // Έλεγχος αν κάποιο πεδίο είναι κενό
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            registrationErrorLabel.setForeground(Color.RED);
            registrationErrorLabel.setText("Please fill in all the fields");
            return;
        }
    
        try {
            // Έλεγχος αν υπάρχει ήδη το username
            String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE user_username = ?";
            try (PreparedStatement checkUsernameStmt = connection.prepareStatement(checkUsernameQuery)) {
                checkUsernameStmt.setString(1, username);
                try (ResultSet checkUsernameRs = checkUsernameStmt.executeQuery()) {
                    if (checkUsernameRs.next() && checkUsernameRs.getInt(1) > 0) {
                        registrationErrorLabel.setForeground(Color.RED);
                        registrationErrorLabel.setText("Username already exists.");
                        return;
                    }
                }
            }
    
            // Έλεγχος αν υπάρχει ήδη το email
            String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE user_email = ?";
            try (PreparedStatement checkEmailStmt = connection.prepareStatement(checkEmailQuery)) {
                checkEmailStmt.setString(1, email);
                try (ResultSet checkEmailRs = checkEmailStmt.executeQuery()) {
                    if (checkEmailRs.next() && checkEmailRs.getInt(1) > 0) {
                        registrationErrorLabel.setForeground(Color.RED);
                        registrationErrorLabel.setText("Email already exists.");
                        return;
                    }
                }
            }
    
            // Όλα καλά -> καθαρισμός μηνύματος λάθους
            registrationErrorLabel.setText("");
    
            // Εισαγωγή νέου χρήστη
            connection.setAutoCommit(false); // Ξεκινάμε transaction
    
            String insertUserQuery = "INSERT INTO users (user_username, user_password, user_email, user_type) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertUserStmt = connection.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertUserStmt.setString(1, username);
                insertUserStmt.setString(2, password);
                insertUserStmt.setString(3, email);
                insertUserStmt.setString(4, userType);
                insertUserStmt.executeUpdate();
    
                // Παίρνουμε το παραγόμενο user_id
                try (ResultSet generatedKeys = insertUserStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
    
                        if (userType.equalsIgnoreCase("customer")) {
                            String insertCustomerQuery = "INSERT INTO customers (customer_FirstName, customer_LastName, address, customer_phone, customer_bonuspoints, user_id) VALUES (?, ?, ?, ?, ?, ?)";
                            try (PreparedStatement insertCustomerStmt = connection.prepareStatement(insertCustomerQuery)) {
                                insertCustomerStmt.setString(1, ""); // Default FirstName
                                insertCustomerStmt.setString(2, ""); // Default LastName
                                insertCustomerStmt.setString(3, ""); // Default address
                                insertCustomerStmt.setString(4, phone);
                                insertCustomerStmt.setInt(5, 0); // bonus points = 0
                                insertCustomerStmt.setInt(6, userId);
                                insertCustomerStmt.executeUpdate();
                            }
                        } else if (userType.equalsIgnoreCase("professional")) {
                            String insertProfessionalQuery = "INSERT INTO professionals (professional_FirstName, professional_LastName, professional_phone, professional_bio, user_id) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement insertProfessionalStmt = connection.prepareStatement(insertProfessionalQuery)) {
                                insertProfessionalStmt.setString(1, ""); // Default FirstName
                                insertProfessionalStmt.setString(2, ""); // Default LastName
                                insertProfessionalStmt.setString(3, phone);
                                insertProfessionalStmt.setString(4, ""); // Default bio
                                insertProfessionalStmt.setInt(5, userId);
                                insertProfessionalStmt.executeUpdate();
                            }
                        }
                    } else {
                        throw new SQLException("User ID not generated.");
                    }
                }
            }
    
            connection.commit(); // Επιτυχής καταχώρηση
            registrationErrorLabel.setForeground(new Color(0, 128, 0)); // Πράσινο χρώμα
            registrationErrorLabel.setText("Registration successful!");
    
            // Καθαρισμός πεδίων
            regUsernameField.setText("");
            regPasswordField.setText("");
            regEmailField.setText("");
            regPhoneField.setText("");
    
            // Επιστροφή στην καρτέλα login
            tabbedPane.setSelectedIndex(0);
    
        } catch (SQLException e) {
            try {
                connection.rollback(); // Αν κάτι πάει στραβά κάνουμε rollback
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            registrationErrorLabel.setForeground(Color.RED);
            registrationErrorLabel.setText("Registration error: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Ξαναενεργοποιούμε το autocommit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}