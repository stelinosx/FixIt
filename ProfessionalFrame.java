import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
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
    private int professionalId;

    // Custom colors
    private static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private static final Color ORANGE_LIGHT = new Color(255, 165, 0);
    private static final Color ORANGE_DARK = new Color(255, 69, 0);
    private static final Color BACKGROUND = new Color(255, 250, 240);

    public ProfessionalFrame(int professionalId, Connection connection) {
        this.professionalId = professionalId;
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
        sidebarPanel.setBackground(ORANGE_PRIMARY);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setBackground(ORANGE_PRIMARY);
        logoPanel.setMaximumSize(new Dimension(250, 100));

        JLabel logoLabel = new JLabel("FixIt");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);
        sidebarPanel.add(logoPanel);

        String[] navItems = {"Αρχική", "Ραντεβού", "Το προφίλ μου", "Εξυπηρέτηση Πελατών"};
        String[] navCommands = {"home", "appointments", "profile", "support"};

        for (int i = 0; i < navItems.length; i++) {
            JButton navButton = createNavButton(navItems[i], navCommands[i]);
            sidebarPanel.add(navButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.setBackground(ORANGE_PRIMARY);
        }

        JButton logoutButton = createNavButton("Αποσύνδεση", "logout");
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private JButton createNavButton(String text, String command) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(230, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(ORANGE_DARK);
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            if (command.equals("logout")) {
                handleLogout();
            } else {
                cardLayout.show(contentPanel, command);
            }
        });

        return button;
    }

    private void createHomePanel() {
    homePanel = new JPanel(new BorderLayout());
    homePanel.setBackground(BACKGROUND);

    // Τίτλος της σελίδας
    JLabel titleLabel = new JLabel("Καλωσήρθατε στο FixIt");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titleLabel.setForeground(ORANGE_PRIMARY);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    homePanel.add(titleLabel, BorderLayout.NORTH);

    // Προσθήκη κειμένου που εξηγεί την εφαρμογή
    JTextArea descriptionArea = new JTextArea(
        "Η εφαρμογή FixIt έχει σχεδιαστεί για να βοηθήσει τους επαγγελματίες\n" +
        "να διαχειρίζονται τα ραντεβού τους, να ενημερώνουν το προφίλ τους και να\n" +
        "παρέχουν υποστήριξη στους πελάτες τους εύκολα και αποτελεσματικά.\n\n" +
        "Με την εφαρμογή μας, μπορείτε να δείτε τα επερχόμενα ραντεβού σας,\n" +
        "να προσθέσετε νέες υπηρεσίες, να διαχειριστείτε τα δεδομένα του προφίλ σας\n" +
        "και να έχετε άμεση επικοινωνία με την εξυπηρέτηση πελατών για οποιοδήποτε πρόβλημα."
    );
    descriptionArea.setFont(new Font("Arial", Font.PLAIN, 16));
    descriptionArea.setForeground(Color.DARK_GRAY);
    descriptionArea.setBackground(BACKGROUND);
    descriptionArea.setEditable(false);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setLineWrap(true);
    descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    // Προσθήκη του descriptionArea στο κέντρο της σελίδας
    homePanel.add(descriptionArea, BorderLayout.CENTER);
}


    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Είστε σίγουροι ότι θέλετε να αποσυνδεθείτε?",
                "Αποσύνδεση",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }

            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                this.dispose();
            });
        }
    }
}
