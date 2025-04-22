import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

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
    
    // Register components
    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JComboBox<String> userTypeCombo;
    private JButton registerButton;

    // Database connection
    private Connection connection;

    public LoginFrame() {
        try {
            // Initialize database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC driver loaded successfully");
            
            String url = "jdbc:mysql://localhost:3306/fixitdb";
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
                "\nΕλέγξτε αν:\n1. Ο MySQL server είναι ενεργός\n2. Η βάση fixit_db υπάρχει\n3. Το username και password είναι σωστά");
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
                setBackground(ORANGE_PRIMARY);
            }
        };
        headerPanel.setPreferredSize(new Dimension(500, 200));
        
        JLabel titleLabel = new JLabel("FIXIT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
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
        
        return panel;
    }
    
    private JPanel createRegisterPanel() {
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
        regUsernameField = new JTextField(20);
        regUsernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        regUsernameField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(regUsernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        regPasswordField = new JPasswordField(20);
        regPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        regPasswordField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(regPasswordField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        regEmailField = new JTextField(20);
        regEmailField.setFont(new Font("Arial", Font.PLAIN, 14));
        regEmailField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(regEmailField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        regPhoneField = new JTextField(20);
        regPhoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        regPhoneField.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(regPhoneField, gbc);
        
        // User Type
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel userTypeLabel = new JLabel("User Type:");
        userTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(userTypeLabel, gbc);
        
        gbc.gridx = 1;
        String[] userTypes = {"Customer", "Professional"};
        userTypeCombo = new JComboBox<>(userTypes);
        userTypeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        userTypeCombo.setBackground(Color.WHITE);
        userTypeCombo.setBorder(BorderFactory.createLineBorder(ORANGE_LIGHT));
        panel.add(userTypeCombo, gbc);
        
        // Register button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        registerButton = new JButton("Register");
        styleButton(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
        panel.add(registerButton, gbc);
        
        return panel;
    }
    
    private void styleButton(JButton button) {
        button.setBackground(ORANGE_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
        
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
        String username = loginUsernameField.getText();
        String password = new String(loginPasswordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String query = "SELECT user_id, user_type FROM users WHERE user_username = ? AND user_password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String userType = rs.getString("user_type");
                JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Close this frame
                this.dispose();
                
                // Open appropriate frame based on user type
                if (userType.equals("customer")) {
                    // Get customer_id from customers table
                    query = "SELECT customer_id FROM customers WHERE user_id = ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setInt(1, userId);
                    rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        int customerId = rs.getInt("customer_id");
                        SwingUtilities.invokeLater(() -> {
                            new CustomerFrame(customerId).setVisible(true);
                        });
                    } else {
                        JOptionPane.showMessageDialog(this, "Customer record not found", "Error", JOptionPane.ERROR_MESSAGE);
                        new LoginFrame().setVisible(true);
                    }
                } else if (userType.equals("professional")) {
                    // Get professional_id from professionals table
                    query = "SELECT professional_id FROM professionals WHERE user_id = ?";
                    stmt = connection.prepareStatement(query);
                    stmt.setInt(1, userId);
                    rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        int professionalId = rs.getInt("professional_id");
                        SwingUtilities.invokeLater(() -> {
                            new ProfessionalFrame(professionalId).setVisible(true);
                        });
                    } else {
                        JOptionPane.showMessageDialog(this, "Professional record not found", "Error", JOptionPane.ERROR_MESSAGE);
                        new LoginFrame().setVisible(true);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegistration() {
        String username = regUsernameField.getText();
        String password = new String(regPasswordField.getPassword());
        String email = regEmailField.getText();
        String phone = regPhoneField.getText();
        String userType = (String) userTypeCombo.getSelectedItem();
    
        if (userType.equals("Customer")) {
            userType = "customer";
        } else if (userType.equals("Professional")) {
            userType = "professional";
        }
    
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        
        try {
            // Print debug info
            System.out.println("Attempting to register new user:");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phone);
            System.out.println("User Type: " + userType);
            
            // Begin transaction
            connection.setAutoCommit(false);
            
            // Insert user
            String query = "INSERT INTO users (user_username, user_password, user_email, user_type) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, userType);
            
            System.out.println("Executing user insert query: " + query);
            int userRowsAffected = stmt.executeUpdate();
            System.out.println("User insert affected " + userRowsAffected + " rows");
            
            // Get the generated user_id
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                System.out.println("Generated user_id: " + userId);
                
                // Insert into appropriate table based on user_type
                if (userType.equals("customer")) {
                    query = "INSERT INTO customers (customer_FirstName, customer_LastName, address, customer_phone, customer_bonuspoints, user_id) VALUES (?, ?, ?, ?, ?, ?)";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, "Default"); // Default values, can be updated later
                    stmt.setString(2, "User");
                    stmt.setString(3, "Default Address");
                    stmt.setString(4, phone);
                    stmt.setInt(5, 0);
                    stmt.setInt(6, userId);
                    
                    System.out.println("Executing customer insert query");
                    int customerRowsAffected = stmt.executeUpdate();
                    System.out.println("Customer insert affected " + customerRowsAffected + " rows");
                } else {
                    query = "INSERT INTO professionals (professional_FirstName, professional_LastName, professional_phone, professional_bio, user_id) VALUES (?, ?, ?, ?, ?)";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1, "Default"); // Default values, can be updated later
                    stmt.setString(2, "Professional");
                    stmt.setString(3, phone);
                    stmt.setString(4, "Default bio"); // Adding default value for professional_bio
                    stmt.setInt(5, userId);
                    
                    System.out.println("Executing professional insert query");
                    int professionalRowsAffected = stmt.executeUpdate();
                    System.out.println("Professional insert affected " + professionalRowsAffected + " rows");
                }
                
                // Commit transaction
                System.out.println("Committing transaction");
                connection.commit();
                
                JOptionPane.showMessageDialog(this, "Registration successful!\nUser ID: " + userId, "Success", JOptionPane.INFORMATION_MESSAGE);
                tabbedPane.setSelectedIndex(0); // Switch to login tab
                
                // Clear fields
                regUsernameField.setText("");
                regPasswordField.setText("");
                regEmailField.setText("");
                regPhoneField.setText("");
            }
        } catch (SQLException e) {
            try {
                System.out.println("Error during registration: " + e.getMessage());
                e.printStackTrace();
                connection.rollback();
                System.out.println("Transaction rolled back");
            } catch (SQLException ex) {
                // Ignore
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(this, "Registration error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                connection.setAutoCommit(true);
                System.out.println("Auto-commit restored to true");
            } catch (SQLException e) {
                // Ignore
                System.out.println("Error restoring auto-commit: " + e.getMessage());
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

