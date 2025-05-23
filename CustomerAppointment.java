import java.awt.*;
import java.sql.*;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerAppointment extends JPanel {
    private Connection connection;
    private CustomerFrame parent;
    private int customerId;
    private JPanel appointmentsListPanel;
    private Timer refreshTimer;

    public CustomerAppointment(Connection connection, CustomerFrame parent, int customerId) {
        this.connection = connection;
        this.parent = parent;
        this.customerId = customerId;
        init();
        
        // Ελέγχουμε και ενημερώνουμε τα ραντεβού που έχουν περάσει
        updatePastAppointments();
        
        // Ρυθμίζω ένα timer για αυτόματη ανανέωση κάθε 30 δευτερόλεπτα
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Εκτελούμε την ανανέωση και τον έλεγχο στο Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    updatePastAppointments();
                    loadAppointments();
                });
            }
        }, 30000, 30000); // Αρχική καθυστέρηση 30 δευτερόλεπτα, επανάληψη κάθε 30 δευτερόλεπτα
    }
    
    // Μέθοδος που ελέγχει και ενημερώνει τα ραντεβού που έχουν περάσει
    private void updatePastAppointments() {
        try {
            // Βρίσκουμε όλα τα ραντεβού με κατάσταση "accepted" που έχουν περάσει
            String query = "UPDATE appointment " +
                          "SET appointment_status = 'completed' " +
                          "WHERE appointment_status = 'accepted' " +
                          "AND EndDate < NOW() " +
                          "AND customer_id = ?";
            
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, customerId);
            int updatedRows = stmt.executeUpdate();
            
            if (updatedRows > 0) {
                System.out.println(updatedRows + " ραντεβού ενημερώθηκαν σε κατάσταση 'completed'");
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την ενημέρωση των παλαιών ραντεβού: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Μέθοδος για να σταματήσει ο χρονοδιακόπτης όταν κλείνει το παράθυρο
    public void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(CustomerFrame.BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CustomerFrame.BACKGROUND);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("Λίστα Ραντεβού:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Κουμπί ανανέωσης
        JButton refreshButton = new JButton("Ανανέωση");
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(CustomerFrame.ORANGE_PRIMARY);
        refreshButton.setForeground(Color.black);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            updatePastAppointments();
            loadAppointments();
        });
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Appointments list container
        appointmentsListPanel = new JPanel();
        appointmentsListPanel.setLayout(new BoxLayout(appointmentsListPanel, BoxLayout.Y_AXIS));
        appointmentsListPanel.setBackground(CustomerFrame.BACKGROUND);
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));    

        JScrollPane scrollPane = new JScrollPane(appointmentsListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsListPanel.removeAll();
        boolean hasAppointments = false;
        try {
            String query = "SELECT a.appointment_id, s.service_type, a.address, a.appointment_status, " +
                           "DATE_FORMAT(a.BeginDate, '%d/%m/%Y') as appointment_date, " +
                           "DATE_FORMAT(a.BeginDate, '%H:%i') as appointment_time, " +
                           "p.professional_FirstName, p.professional_LastName, " +
                           "p.professional_id " +
                           "FROM appointment a " +
                           "JOIN professionals p ON a.professional_id = p.professional_id " +
                           "JOIN service s ON a.service_id = s.service_id " +
                           "WHERE a.customer_id = ? " +
                           "ORDER BY a.appointment_status = 'pending' DESC, a.appointment_status = 'accepted' DESC, a.appointment_id DESC";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                hasAppointments = true;
                int id = rs.getInt("appointment_id");
                String serviceType = rs.getString("service_type");
                String status = rs.getString("appointment_status");
                String date = rs.getString("appointment_date");
                String time = rs.getString("appointment_time");
                String profName = rs.getString("professional_FirstName") + " " + rs.getString("professional_LastName");
                String location = rs.getString("address");
                int professionalId = rs.getInt("professional_id");

                // Δημιουργία κάρτας με listeners
                AppointmentCard card = new AppointmentCard(
                    id,
                    profName,
                    date,
                    time,
                    serviceType,
                    location,
                    status,
                    e -> { // onCancel
                        cancelAppointment(id);
                        loadAppointments();
                    },
                    e -> { // onReview
                        openReviewDialog(id, professionalId);
                    }
                );
                
                // Προσθήκη οπτικής ένδειξης για νέες ενημερώσεις
                if ("accepted".equals(status) || "declined".equals(status)) {
                    JPanel badgePanel = new JPanel(new BorderLayout());
                    badgePanel.setBackground(CustomerFrame.BACKGROUND);
                    
                    JPanel cardWithBadge = new JPanel(new BorderLayout());
                    cardWithBadge.setBackground(CustomerFrame.BACKGROUND);
                    
                    JLabel badgeLabel = new JLabel(
                        "accepted".equals(status) ? "Νέο αποδεκτό ραντεβού!" : "Το ραντεβού απορρίφθηκε",
                        SwingConstants.CENTER
                    );
                    badgeLabel.setOpaque(true);
                    badgeLabel.setBackground("accepted".equals(status) ? new Color(40, 167, 69) : new Color(220, 53, 69));
                    badgeLabel.setForeground(Color.WHITE);
                    badgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
                    badgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                    
                    badgePanel.add(badgeLabel, BorderLayout.NORTH);
                    badgePanel.add(card, BorderLayout.CENTER);
                    appointmentsListPanel.add(badgePanel);
                } else if ("completed".equals(status)) {
                    // Προσθήκη ειδικής ένδειξης για ραντεβού που έχουν ολοκληρωθεί
                    JPanel badgePanel = new JPanel(new BorderLayout());
                    badgePanel.setBackground(CustomerFrame.BACKGROUND);
                    
                    // Έλεγχος αν έχει ήδη αξιολογηθεί
                    boolean hasReview = checkIfReviewed(id);
                    
                    JLabel badgeLabel = new JLabel(
                        hasReview ? "Ολοκληρωμένο - Έχετε αξιολογήσει" : "Ολοκληρωμένο - Μπορείτε να αξιολογήσετε!",
                        SwingConstants.CENTER
                    );
                    badgeLabel.setOpaque(true);
                    badgeLabel.setBackground(new Color(0, 123, 255)); // Μπλε για ολοκληρωμένα
                    badgeLabel.setForeground(Color.WHITE);
                    badgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
                    badgeLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
                    
                    badgePanel.add(badgeLabel, BorderLayout.NORTH);
                    badgePanel.add(card, BorderLayout.CENTER);
                    appointmentsListPanel.add(badgePanel);
                } else {
                    appointmentsListPanel.add(card);
                }
                
                appointmentsListPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            if (!hasAppointments) {
                appointmentsListPanel.add(Box.createVerticalGlue());
                JLabel none = new JLabel("Δεν έχετε κανένα ραντεβού");
                none.setFont(new Font("Arial", Font.BOLD, 16));
                none.setAlignmentX(Component.CENTER_ALIGNMENT);
                appointmentsListPanel.add(none);
                appointmentsListPanel.add(Box.createVerticalGlue());
            }
        } catch (SQLException e) {
            JLabel err = new JLabel("Σφάλμα φόρτωσης ραντεβού: " + e.getMessage());
            err.setForeground(Color.RED);
            appointmentsListPanel.add(err);
            e.printStackTrace();
        }

        appointmentsListPanel.revalidate();
        appointmentsListPanel.repaint();
    }
    
    // Μέθοδος για να ελέγξει αν ένα ραντεβού έχει ήδη αξιολογηθεί
    private boolean checkIfReviewed(int appointmentId) {
        try {
            String query = "SELECT COUNT(*) FROM review WHERE appointment_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Μέθοδος για άνοιγμα παραθύρου αξιολόγησης
    private void openReviewDialog(int appointmentId, int professionalId) {
        // Έλεγχος αν έχει ήδη αξιολογηθεί
        if (checkIfReviewed(appointmentId)) {
            JOptionPane.showMessageDialog(this, 
                "Έχετε ήδη αξιολογήσει αυτό το ραντεβού.", 
                "Πληροφορία", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Δημιουργία οθόνης αξιολόγησης σύμφωνα με το διάγραμμα
        JDialog reviewDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Αξιολόγηση Ραντεβού", true);
        reviewDialog.setLayout(new BorderLayout());
        reviewDialog.setSize(400, 350);
        reviewDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Τίτλος
        JLabel titleLabel = new JLabel("Αξιολογήστε τον επαγγελματία");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Εμφάνιση πληροφοριών ραντεβού
        try {
            String query = "SELECT p.professional_FirstName, p.professional_LastName, " +
                          "s.service_type, DATE_FORMAT(a.BeginDate, '%d/%m/%Y %H:%i') as appointment_date " +
                          "FROM appointment a " +
                          "JOIN professionals p ON a.professional_id = p.professional_id " +
                          "JOIN service s ON a.service_id = s.service_id " +
                          "WHERE a.appointment_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String profName = rs.getString("professional_FirstName") + " " + rs.getString("professional_LastName");
                String serviceType = rs.getString("service_type");
                String appointmentDate = rs.getString("appointment_date");
                
                JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                infoPanel.setBorder(BorderFactory.createTitledBorder("Πληροφορίες ραντεβού"));
                
                infoPanel.add(new JLabel("Επαγγελματίας:"));
                infoPanel.add(new JLabel(profName));
                infoPanel.add(new JLabel("Υπηρεσία:"));
                infoPanel.add(new JLabel(serviceType));
                infoPanel.add(new JLabel("Ημερομηνία:"));
                infoPanel.add(new JLabel(appointmentDate));
                
                contentPanel.add(infoPanel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Βαθμολογία
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ratingLabel = new JLabel("Βαθμολογία (1-5): ");
        Integer[] ratings = {1, 2, 3, 4, 5};
        JComboBox<Integer> ratingComboBox = new JComboBox<>(ratings);
        ratingComboBox.setSelectedIndex(4); // Προεπιλογή: 5
        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingComboBox);
        contentPanel.add(ratingPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Σχόλια
        JLabel commentsLabel = new JLabel("Σχόλια:");
        commentsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(commentsLabel);
        
        JTextArea commentsArea = new JTextArea(5, 20);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        JScrollPane commentsScrollPane = new JScrollPane(commentsArea);
        commentsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(commentsScrollPane);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Κουμπιά ενεργειών
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // Κουμπί επιβεβαίωσης
        JButton confirmButton = new JButton("Επιβεβαίωση");
        confirmButton.setBackground(CustomerFrame.ORANGE_PRIMARY);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> {
            // Εμφάνιση παραθύρου επιβεβαίωσης σύμφωνα με το διάγραμμα
            int rating = (Integer) ratingComboBox.getSelectedItem();
            String comments = commentsArea.getText();
            
            // Αν η βαθμολογία δεν έχει συμπληρωθεί σωστά ή τα σχόλια είναι κενά
            if (rating < 1 || comments.trim().isEmpty()) {
                // Εμφάνιση παραθύρου "Incomplete Review"
                JOptionPane.showMessageDialog(reviewDialog,
                    "Παρακαλώ συμπληρώστε τόσο τη βαθμολογία όσο και τα σχόλια για να ολοκληρωθεί η αξιολόγηση.",
                    "Ελλιπής Αξιολόγηση",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Επιβεβαίωση σύμφωνα με το διάγραμμα "Completed Review"
            int confirmResult = JOptionPane.showConfirmDialog(reviewDialog,
                "Είστε βέβαιοι ότι θέλετε να υποβάλετε την αξιολόγηση;\n" +
                "Βαθμολογία: " + rating + "/5\n" +
                "Σχόλια: " + comments,
                "Επιβεβαίωση Αξιολόγησης",
                JOptionPane.YES_NO_OPTION);
            
            if (confirmResult == JOptionPane.YES_OPTION) {
                if (submitReview(appointmentId, professionalId, rating, comments)) {
                    // Δημιουργία οθόνης επιβεβαίωσης (Conf Screen)
                    JDialog confirmationScreen = new JDialog((Frame)SwingUtilities.getWindowAncestor(reviewDialog), "Επιτυχής Υποβολή", true);
                    confirmationScreen.setLayout(new BorderLayout());
                    confirmationScreen.setSize(350, 200);
                    confirmationScreen.setLocationRelativeTo(reviewDialog);
                    
                    JPanel confPanel = new JPanel();
                    confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
                    confPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                    
                    JLabel successLabel = new JLabel("Η αξιολόγησή σας υποβλήθηκε με επιτυχία!");
                    successLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    
                    JLabel thanksLabel = new JLabel("Σας ευχαριστούμε για την ανατροφοδότηση!");
                    thanksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    
                    JButton okButton = new JButton("OK");
                    okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    okButton.addActionListener(okEvent -> {
                        confirmationScreen.dispose();
                        reviewDialog.dispose();
                        loadAppointments(); // Ανανέωση λίστας ραντεβού
                    });
                    
                    confPanel.add(successLabel);
                    confPanel.add(Box.createRigidArea(new Dimension(0, 15)));
                    confPanel.add(thanksLabel);
                    confPanel.add(Box.createRigidArea(new Dimension(0, 20)));
                    confPanel.add(okButton);
                    
                    confirmationScreen.add(confPanel, BorderLayout.CENTER);
                    confirmationScreen.setVisible(true);
                }
            }
        });
        
        // Κουμπί ακύρωσης
        JButton cancelButton = new JButton("Ακύρωση");
        cancelButton.addActionListener(e -> {
            // Διαδικασία ακύρωσης αξιολόγησης σύμφωνα με το διάγραμμα "Review cancellation"
            int cancelConfirm = JOptionPane.showConfirmDialog(reviewDialog,
                "Είστε βέβαιοι ότι θέλετε να ακυρώσετε την αξιολόγηση;",
                "Ακύρωση Αξιολόγησης",
                JOptionPane.YES_NO_OPTION);
                
            if (cancelConfirm == JOptionPane.YES_OPTION) {
                reviewDialog.dispose();
            }
        });
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        contentPanel.add(buttonPanel);
        
        reviewDialog.add(contentPanel, BorderLayout.CENTER);
        reviewDialog.setVisible(true);
    }
    
    // Μέθοδος για την υποβολή αξιολόγησης στη βάση δεδομένων
    private boolean submitReview(int appointmentId, int professionalId, int rating, String comments) {
        try {
            // Εισαγωγή αξιολόγησης στον πίνακα review
            String insertQuery = "INSERT INTO review (rating, comments, customer_id, professional_id, appointment_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            stmt.setInt(1, rating);
            stmt.setString(2, comments);
            stmt.setInt(3, customerId);
            stmt.setInt(4, professionalId);
            stmt.setInt(5, appointmentId);
            
            int result = stmt.executeUpdate();
            
            // Ενημέρωση μέσης βαθμολογίας του επαγγελματία
            if (result > 0) {
                updateProfessionalRating(professionalId);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Σφάλμα κατά την υποβολή της αξιολόγησης: " + e.getMessage(), 
                "Σφάλμα", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    // Μέθοδος για την ενημέρωση της μέσης βαθμολογίας του επαγγελματία
    private void updateProfessionalRating(int professionalId) {
        try {
            // Υπολογισμός μέσης βαθμολογίας από όλες τις αξιολογήσεις
            String query = "SELECT AVG(rating) FROM review WHERE professional_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, professionalId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double avgRating = rs.getDouble(1);
                
                // Ενημέρωση του πίνακα professionals με τη νέα μέση βαθμολογία
                String updateQuery = "UPDATE professionals SET professional_rating = ? WHERE professional_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                updateStmt.setDouble(1, avgRating);
                updateStmt.setInt(2, professionalId);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cancelAppointment(int appointmentId) {
        try {
            String update = "UPDATE appointment SET appointment_status = 'cancelled' WHERE appointment_id = ?";
            PreparedStatement stmt = connection.prepareStatement(update);
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this,
                "Το ραντεβού ακυρώθηκε επιτυχώς!",
                "Επιτυχία",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Σφάλμα βάσης δεδομένων: " + ex.getMessage(),
                "Σφάλμα",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
