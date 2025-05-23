import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

public class ProfessionalFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private JPanel homePanel;
    private JPanel appointmentsPanel;
    private JPanel profilePanel;
    private JPanel supportPanel;
    private Connection connection;
    private JButton activeButton = null;

    // Custom colors
    private static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private static final Color BACKGROUND = new Color(255, 250, 240);

    public ProfessionalFrame(int professionalId, Connection connection) {
        this.connection = connection;

        setTitle("FixIt - Επαγγελματίας");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        createSidebar();

        // Main content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Panels
        createHomePanel();
        profilePanel = new ProfessionalProfile(connection, professionalId).createProfilePanel();
        
        createHomePanel();
        appointmentsPanel = new JPanel(); // Προσωρινή λύση
        supportPanel = new SupportPanelPro();      // Προσωρινή λύση
        profilePanel = new ProfessionalProfile(connection, professionalId).createProfilePanel();

        contentPanel.add(homePanel, "home");
        contentPanel.add(appointmentsPanel, "appointments");
        contentPanel.add(profilePanel, "profile");
        contentPanel.add(supportPanel, "support");

        cardLayout.show(contentPanel, "home");

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        ImageIcon logoIcon = new ImageIcon("images\\FixIt.png");

// Μπορείς να κάνεις resize αν είναι μεγάλη:
        Image scaledImage = logoIcon.getImage().getScaledInstance(290, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);

// Δημιουργία JLabel με την εικόνα
        JLabel logoLabel = new JLabel(resizedIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Κέντρο στο sidebar

// Προσθήκη στο sidebarPanel
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Κενό πάνω από την εικόνα
        sidebarPanel.add(logoLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        

        String[] navItems = {"🏠 Αρχική", "📅 Ραντεβού", "👤 Το προφίλ μου"};
        String[] navCommands = {"home", "appointments", "profile"};
        
        for (int i = 0; i < navItems.length; i++) {
            JButton navButton = createNavButton(navItems[i], navCommands[i]);
            sidebarPanel.add(navButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        sidebarPanel.add(Box.createGlue());  // Χρησιμοποιούμε Box.createGlue για να σπρώξουμε το κουμπί προς τα κάτω
        JLabel dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateTimeLabel.setForeground(Color.GRAY);
        dateTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateDateTimeLabel(dateTimeLabel);
        sidebarPanel.add(dateTimeLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  
        // Timer για αυτόματη ανανέωση κάθε λεπτό
        Timer timer = new Timer(60000, e -> updateDateTimeLabel(dateTimeLabel));
        timer.start();

        JButton logoutButton = createNavButton("🔓 Αποσύνδεση", "logout");
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }
        private void updateDateTimeLabel(JLabel label) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        label.setText(now.format(formatter));
    }
    private JButton createNavButton(String text, String command) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(290, 40));
        button.setFont(new Font("Noto Color Emoji", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setActionCommand(command);
    
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != activeButton) {
                    button.setOpaque(true);
                    button.setBackground(ORANGE_PRIMARY);
                    button.setForeground(Color.WHITE);
                }
            }
    
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != activeButton) {
                    button.setOpaque(false);
                    button.setBackground(null);
                    button.setForeground(Color.BLACK);
                }
            }
        });
    
        // Όταν πατιέται, αλλάζει ενεργό
        button.addActionListener(e -> {
            if (command.equals("logout")) {
                handleLogout();
            } else {
                cardLayout.show(contentPanel, command);
                setActiveButton(button); // Εδώ καλούμε τη μέθοδο για να το ορίσουμε ως ενεργό
            }
        });
    
        return button;
    }
    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            activeButton.setOpaque(false);
            activeButton.setBackground(null);
            activeButton.setForeground(Color.BLACK);
        }
    
        activeButton = button;
        activeButton.setOpaque(true);
        activeButton.setBackground(ORANGE_PRIMARY);
        activeButton.setForeground(Color.WHITE);
    }

    private void createHomePanel() {
        homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(BACKGROUND);
    
        // Τίτλος της σελίδας
        JLabel titleLabel = new JLabel("Καλωσήρθατε στο FixIt 😄");
        titleLabel.setFont(new Font("Noto Color Emoji", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE); // Άσπρα γράμματα
        titleLabel.setBackground(ORANGE_PRIMARY); // Πορτοκαλί φόντο
        titleLabel.setOpaque(true); // Χρειάζεται για να φαίνεται το background 
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        homePanel.add(titleLabel, BorderLayout.NORTH);
    
        // Προσθήκη κειμένου που εξηγεί την εφαρμογή
        JTextArea descriptionArea = new JTextArea(
        "🔧 Η εφαρμογή FixIt έχει σχεδιαστεί για να βοηθήσει τους επαγγελματίες" +
        "να διαχειρίζονται ραντεβού, να ενημερώνουν το προφίλ τους και να" +
        "παρέχουν υποστήριξη στους πελάτες εύκολα και αποτελεσματικά.\n\n" +
        "Με την υπηρεσία μας μπορείτε να:\n\n" +
        "📅 Δείτε τα επερχόμενα ραντεβού σας\n" +
        "💼 Προσθέσετε νέες υπηρεσίες\n" +
        "👤 Διαχειριστείτε το προφίλ σας\n" +
        "📞 Επικοινωνήσετε άμεσα με τον πελάτη με την 'Εξυπηρέτηση Πελατών'"
        );
    
        descriptionArea.setFont(new Font("Sans serif", Font.PLAIN, 20)); // Πιο smooth γραμματοσειρά
        descriptionArea.setForeground(new Color(50, 50, 50)); // Σκούρο γκρι
        descriptionArea.setBackground(new Color(245, 238, 230)); // Άσπρο ή ό,τι ταιριάζει με BACKGROUND
        descriptionArea.setEditable(false);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setOpaque(true); // Κάνει το background διάφανο, αν θέλεις
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Πιο "ανάλαφρο"
    
        // Προσθήκη του descriptionArea στο κέντρο της σελίδας
        homePanel.add(descriptionArea, BorderLayout.CENTER);

       // Δημιουργία του κουμπιού
        JButton helpButton = new JButton("?");
        helpButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        helpButton.setBorder(new RoundBorder(20));  // Εφαρμογή στρογγυλεμένων γωνιών
        helpButton.setFont(new Font("Arial", Font.BOLD, 20));
        helpButton.setForeground(Color.BLACK);
        helpButton.setBackground(Color.ORANGE);
        helpButton.setPreferredSize(new Dimension(40, 40)); // Μικρό μέγεθος
        helpButton.setFocusPainted(false);
        helpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpButton.setBorder(BorderFactory.createLineBorder(Color.orange, 2));

        helpButton.setToolTipText("Help");
        ToolTipManager.sharedInstance().setInitialDelay(0);

          // Ενέργεια όταν το κουμπί Help πατηθεί
        helpButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "support");
        });

        // Δημιουργία του buttonPanel και προσθήκη του κουμπιού
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Χρησιμοποιούμε FlowLayout για αριστερή τοποθέτηση
        buttonPanel.setOpaque(true);
        buttonPanel.add(helpButton); // Προσθήκη του κουμπιού
        homePanel.add(buttonPanel, BorderLayout.SOUTH);
    
        
    }
    


private void handleLogout() {
    // Δημιουργία custom JDialog
    JDialog dialog = new JDialog(this, "Αποσύνδεση", true);
    dialog.setSize(400, 100);
    dialog.setLayout(new BorderLayout());
    dialog.setLocationRelativeTo(this); // Κέντρο στο τρέχον παράθυρο

    // Μήνυμα με στυλ
    JLabel messageLabel = new JLabel("Είστε σίγουροι ότι θέλετε να αποσυνδεθείτε;");
    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
    messageLabel.setForeground(Color.WHITE); // Άσπρα γράμματα
    messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
    
    // Πάνελ για μήνυμα
    JPanel messagePanel = new JPanel();
    messagePanel.setBackground(new Color(255, 140, 0)); // Πορτοκαλί φόντο
    messagePanel.add(messageLabel);

    // Πάνελ για κουμπιά
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(255, 140, 0)); // Πορτοκαλί φόντο

    // Κουμπί Ναι
    JButton yesButton = new JButton("Ναι");
    yesButton.setForeground(Color.black);
    yesButton.setFocusPainted(false);
    yesButton.setBackground(Color.red);
    yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    yesButton.addActionListener(e -> {
        dialog.dispose(); // Κλείνει το παράθυρο
        logoutConfirmed(); // Εκτελεί την αποσύνδεση
    });

    // Κουμπί Όχι
    JButton noButton = new JButton("Όχι");
    noButton.setForeground(Color.black);
    noButton.setFocusPainted(false);
    noButton.setBackground(Color.green);
    noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    noButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(yesButton);
    buttonPanel.add(noButton);

    dialog.add(messagePanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
}

private void logoutConfirmed() {
    try {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Database connection closed");
        }
    } catch (SQLException e) {
        System.out.println("Error closing database connection: " + e.getMessage());
    }

    SwingUtilities.invokeLater(() -> {
        new LoginFrame().setVisible(true); // Ή το δικό σου frame
        this.dispose();
    });
}

}

