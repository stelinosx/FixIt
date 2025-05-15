import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class CustomerFrame extends JFrame {
    // Main components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private CustomerPoints pointsPanel;

    // Panels
    private JPanel homePanel;

    // Database connection
    private Connection connection;
    private int customerId;

    // Colors
    public static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    public static final Color ORANGE_LIGHT   = new Color(255, 165, 0);
    public static final Color ORANGE_DARK    = new Color(255, 69, 0);
    public static final Color BACKGROUND     = new Color(255, 250, 240);
    public static final Color SIDEBAR_BG     = new Color(250, 250, 250);

    public CustomerFrame(int customerId) {
        this.customerId = customerId;
        initConnection();
        initFrame();
        initLayout();
        initSidebar();
        initContentPanels();
    }

    private void initConnection() {
        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/FixIt", "root", "2004Stelios2004"
            );
            System.out.println("Database connected");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Σφάλμα σύνδεσης με τη βάση δεδομένων: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void initFrame() {
        setTitle("FixIt - Πελάτης");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initLayout() {
        // Sidebar container
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(250, getHeight()));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        add(sidebarPanel, BorderLayout.WEST);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND);
        add(contentPanel, BorderLayout.CENTER);
    }

    public JPanel getContentPanel() {
    return contentPanel;
}

    private void initSidebar() {
        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(250, 100));
        JLabel logoLabel = new JLabel("FixIt");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30));
        logoLabel.setForeground(ORANGE_PRIMARY);
        logoPanel.add(logoLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Navigation buttons
        String[] navItems = {"Αρχική","Αναζήτηση","Ραντεβού","Οι Πόντοι μου","Κριτικές","Το προφίλ μου","Εξυπηρέτηση Πελατών"};
        String[] navKeys  = {"home","search","appointments","points","reviews","profile","support"};
        for (int i = 0; i < navItems.length; i++) {
            sidebarPanel.add(createNavButton(navItems[i], navKeys[i]));
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        sidebarPanel.add(Box.createVerticalGlue());
        // Logout
        JButton logoutBtn = createNavButton("Αποσύνδεση", "logout");
        logoutBtn.setBackground(ORANGE_LIGHT);
        logoutBtn.setForeground(Color.WHITE);
        sidebarPanel.add(logoutBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    }

    private JButton createNavButton(String text, String command) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(230, 40));
        btn.setFont(new Font("Arial", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(SIDEBAR_BG);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if ("logout".equals(command)) handleLogout();
            else switchTo(command);
        });
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ORANGE_LIGHT); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(SIDEBAR_BG); }
        });
        return btn;
    }

    // Expose panel switching 
    public void switchTo(String key) {
        cardLayout.show(contentPanel, key);
    }

    /**
     * Convenience to return to home screen
     */
    public void showHome() {
        switchTo("home");
    }

    private void initContentPanels() {
        // Home panel inline
        createHomePanel();
        contentPanel.add(homePanel, "home");
        // Other panels as separate classes
        contentPanel.add(new CustomerSearch(connection, this, customerId), "search");
        contentPanel.add(new CustomerAppointment(connection, this, customerId), "appointments");
        pointsPanel = new CustomerPoints(connection, this, customerId);
        contentPanel.add(pointsPanel, "points");
        

        switchTo("home");
    }

    private void createHomePanel() {
        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        homePanel.setBackground(BACKGROUND);
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("Δημοφιλείς υπηρεσίες");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        homePanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(BACKGROUND);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Popular services
        JPanel servicesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        servicesPanel.setBackground(BACKGROUND);
        
        String[] popularServices = {"Ηλεκτρολόγοι", "Ψυκτικοί", "Υδραυλικοί", "Τεχνικοί PC", "Ξυλουργοί", "Ελαιοχρωματουργοί"};
        
        for (String service : popularServices) {
            JButton serviceButton = new JButton(service);
            serviceButton.setPreferredSize(new Dimension(150, 40));
            serviceButton.setFont(new Font("Arial", Font.PLAIN, 14));
            serviceButton.setBackground(Color.WHITE);
            serviceButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
            
            serviceButton.addActionListener(e -> {
                cardLayout.show(contentPanel, "search");
                // TODO: Set search filter for the selected service
            });
            
            servicesPanel.add(serviceButton);
        }
        
        mainContentPanel.add(servicesPanel, BorderLayout.NORTH);
        
        // Calendar and next appointment
        JPanel calendarPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        calendarPanel.setBackground(BACKGROUND);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Calendar
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.setBackground(new Color(245, 245, 255));
        datePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        JLabel selectDateLabel = new JLabel("Select date");
        selectDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectDateLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        datePanel.add(selectDateLabel);
        
        JLabel dateLabel = new JLabel("Mon, Aug 17");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        datePanel.add(dateLabel);
        
        // Month navigation
        JPanel monthPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        monthPanel.setBackground(new Color(245, 245, 255));
        
        JLabel monthLabel = new JLabel("August 2025");
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        
        monthPanel.add(monthLabel);
        monthPanel.add(prevButton);
        monthPanel.add(nextButton);
        
        JPanel monthWrapper = new JPanel();
        monthWrapper.setLayout(new BoxLayout(monthWrapper, BoxLayout.X_AXIS));
        monthWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        monthWrapper.setBackground(new Color(245, 245, 255));
        monthWrapper.add(monthPanel);
        
        datePanel.add(monthWrapper);
        
        // Calendar grid
        JPanel calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
        calendarGrid.setBackground(new Color(245, 245, 255));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        // Days of week
        String[] daysOfWeek = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            calendarGrid.add(dayLabel);
        }
        
        // Day buttons (simplified - just show current month)
        for (int i = 1; i <= 31; i++) {
            if (i <= 31) { // Only days in August
                JButton dayButton = new JButton(String.valueOf(i));
                dayButton.setBackground(Color.WHITE);
                dayButton.setBorderPainted(false);
                
                if (i == 17) { // Highlight selected date
                    dayButton.setBackground(ORANGE_PRIMARY);
                    dayButton.setForeground(Color.WHITE);
                }
                
                calendarGrid.add(dayButton);
            } else {
                // Empty cell
                JLabel emptyLabel = new JLabel();
                calendarGrid.add(emptyLabel);
            }
        }
        
        datePanel.add(calendarGrid);
        
        // Next appointment panel
        JPanel appointmentPanel = new JPanel();
        appointmentPanel.setLayout(new BoxLayout(appointmentPanel, BoxLayout.Y_AXIS));
        appointmentPanel.setBackground(Color.WHITE);
        appointmentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        JPanel infoIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoIconPanel.setBackground(Color.WHITE);
        
        JLabel infoIcon = new JLabel("ⓘ");
        infoIcon.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel appointmentTitleLabel = new JLabel("Το επόμενο μου ραντεβού:");
        appointmentTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        infoIconPanel.add(infoIcon);
        infoIconPanel.add(appointmentTitleLabel);
        
        JPanel appointmentInfoPanel = new JPanel();
        appointmentInfoPanel.setLayout(new BoxLayout(appointmentInfoPanel, BoxLayout.Y_AXIS));
        appointmentInfoPanel.setBackground(Color.WHITE);
        appointmentInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JLabel infoLabel = new JLabel("(Πληροφορίες ραντεβού)");
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton editButton = new JButton("Επισκόπηση ραντεβού");
        editButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editButton.setBackground(new Color(240, 240, 255));
        editButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        editButton.addActionListener(e -> cardLayout.show(contentPanel, "appointments"));
        
        appointmentInfoPanel.add(infoLabel);
        appointmentInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        appointmentInfoPanel.add(editButton);
        
        appointmentPanel.add(infoIconPanel);
        appointmentPanel.add(appointmentInfoPanel);
        
        calendarPanel.add(datePanel);
        calendarPanel.add(appointmentPanel);
        
        mainContentPanel.add(calendarPanel, BorderLayout.CENTER);
        
        homePanel.add(mainContentPanel, BorderLayout.CENTER);
    }

    public void refreshPoints() {
        if (pointsPanel != null) {
            pointsPanel.reload();
        }
    }
    

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Είστε σίγουροι ότι θέλετε να αποσυνδεθείτε;",
            "Αποσύνδεση", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try { if (connection != null && !connection.isClosed()) connection.close(); }
            catch (SQLException ex) { ex.printStackTrace(); }
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}