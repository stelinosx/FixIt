import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CustomerSearch extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private JPanel resultsPanel;
    private JPanel specialtyButtonsPanel;
    private Connection connection;
    private JFrame parentFrame;
    private int customerId;

    private static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private static final Color BACKGROUND = new Color(250, 245, 240);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    private static final Map<String, String> greekToEnglishSpecialties = new HashMap<>();
    static {
        greekToEnglishSpecialties.put("Ηλεκτρολόγοι", "electrician");
        greekToEnglishSpecialties.put("Υδραυλικοί", "plumber");
        greekToEnglishSpecialties.put("Καθαριστές", "house cleaner");
        greekToEnglishSpecialties.put("Ψυκτικοί", "refrigerant");
        greekToEnglishSpecialties.put("Ξυλουργοί", "smith");
        greekToEnglishSpecialties.put("Ελαιοχρωματιστές", "painter");
        greekToEnglishSpecialties.put("Τεχνικοί PC", "pc technician");
        greekToEnglishSpecialties.put("Θερμοϋδραυλικοί", "thermohydraulic");
        greekToEnglishSpecialties.put("Σιδεράδες", "smith");
        greekToEnglishSpecialties.put("Καθαριστές Χαλιών", "Carpet Cleaner");
        greekToEnglishSpecialties.put("Τεχνικοί Αποφράξεων", "drain technician");
        greekToEnglishSpecialties.put("Ειδικοί Απεντομώσεων", "pest controller");
        greekToEnglishSpecialties.put("Γυψαδόροι", "Plaster Craftsman");
    }

    private static final Map<String, String> englishToGreekSpecialties = new HashMap<>();
    static {
        englishToGreekSpecialties.put("electrician", "Ηλεκτρολόγος");
        englishToGreekSpecialties.put("plumber", "Υδραυλικός");
        englishToGreekSpecialties.put("house cleaner", "Καθαριστής");
        englishToGreekSpecialties.put("refrigerant", "Ψυκτικός");
        englishToGreekSpecialties.put("smith", "Σιδεράς");
        englishToGreekSpecialties.put("painter", "Ελαιοχρωματιστής");
        englishToGreekSpecialties.put("pc technician", "Τεχνικός PC");
        englishToGreekSpecialties.put("thermohydraulic", "Θερμοϋδραυλικός");
        englishToGreekSpecialties.put("Carpet Cleaner", "Καθαριστής Χαλιών");
        englishToGreekSpecialties.put("drain technician", "Τεχνικός Αποφράξεων");
        englishToGreekSpecialties.put("pest controller", "Ειδικός Απεντομώσεων");
        englishToGreekSpecialties.put("Plaster Craftsman", "Γυψαδόρος");
    }

    public CustomerSearch(Connection connection, JFrame parentFrame, int customerId) {
        this.connection = connection;
        this.parentFrame = parentFrame;
        this.customerId = customerId;

        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND);

        searchField = new JTextField("Πληκτρολόγησε ειδικότητα ή όνομα...");
        searchField.setForeground(Color.GRAY);
        searchField.setFont(DEFAULT_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ORANGE_PRIMARY, 2),
            new EmptyBorder(8, 10, 8, 10)
        ));

        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Πληκτρολόγησε ειδικότητα ή όνομα...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
        });

        searchButton = new JButton("Αναζήτηση");
        styleButton(searchButton);

        searchButton.addActionListener(e -> showResults());

        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        specialtyButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        specialtyButtonsPanel.setBackground(BACKGROUND);

        String[] specialties = greekToEnglishSpecialties.keySet().toArray(new String[0]);

        for (String spec : specialties) {
            JButton btn = new JButton(spec);
            btn.setFont(DEFAULT_FONT);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(ORANGE_PRIMARY, 2));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(140, 36));
            btn.addActionListener(e -> {
                searchField.setText(spec);
                searchField.setForeground(Color.BLACK);
                showResults();
            });
            specialtyButtonsPanel.add(btn);
        }

        add(specialtyButtonsPanel, BorderLayout.CENTER);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setBackground(ORANGE_PRIMARY);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void showResults() {
        String searchText = typeOrChooseSpecialty();
        if (!validateSearch(searchText)) return;

        ArrayList<Professional> professionals = searchProfessionals(searchText);
        resultsPanel.removeAll();

        if (professionals.isEmpty()) {
            returnNoValidResults();
        } else {
            returnValidResults(professionals);
        }

        revalidate();
        repaint();
    }

    private String typeOrChooseSpecialty() {
        String text = searchField.getText().trim();
        if (greekToEnglishSpecialties.containsKey(text)) {
            return greekToEnglishSpecialties.get(text).toLowerCase();
        }
        return text.toLowerCase();
    }

    private boolean validateSearch(String searchText) {
        if (searchText.isEmpty() || searchText.equals("πληκτρολόγησε ειδικότητα ή όνομα...")) {
            JOptionPane.showMessageDialog(this, "Παρακαλώ εισάγετε μια ειδικότητα ή όνομα.");
            return false;
        }
        return true;
    }

    private ArrayList<Professional> searchProfessionals(String searchText) {
        ArrayList<Professional> list = new ArrayList<>();

        String query = "SELECT p.professional_id, u.user_email, p.professional_phone, p.professional_FirstName, " +
                       "p.professional_LastName, p.professional_speciality, p.professional_bio " +
                       "FROM professionals p " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE LOWER(p.professional_speciality) LIKE ? OR LOWER(p.professional_FirstName) LIKE ? OR LOWER(p.professional_LastName) LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String likeText = "%" + searchText + "%";
            stmt.setString(1, likeText);
            stmt.setString(2, likeText);
            stmt.setString(3, likeText);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Professional prof = new Professional(
                    rs.getInt("professional_id"),
                    "", "", rs.getString("user_email"),
                    rs.getString("professional_phone"),
                    rs.getString("professional_FirstName"),
                    rs.getString("professional_LastName"),
                    rs.getString("professional_speciality"),
                    rs.getString("professional_bio")
                );

                list.add(prof);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void returnValidResults(ArrayList<Professional> professionals) {
        for (Professional prof : professionals) {
            JPanel profCard = new JPanel();
            profCard.setLayout(new BoxLayout(profCard, BoxLayout.Y_AXIS));
            profCard.setBackground(CARD_COLOR);
            profCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(15, 15, 15, 15)
            ));
            profCard.setMaximumSize(new Dimension(650, 170));
            profCard.setAlignmentX(Component.CENTER_ALIGNMENT);

            String professionInGreek = englishToGreekSpecialties.getOrDefault(prof.getProfession(), prof.getProfession());

            profCard.add(new JLabel("👤 " + prof.getFirstName() + " " + prof.getLastName()));
            profCard.add(new JLabel("🛠 Ειδικότητα: " + professionInGreek));
            profCard.add(new JLabel("📞 Τηλέφωνο: " + prof.getPhoneNumber()));
            profCard.add(new JLabel("📧 Email: " + prof.getEmail()));
            profCard.add(new JLabel("📄 Βιογραφικό: " + prof.getDescription()));

            JButton bookButton = new JButton("Ραντεβού");
            styleButton(bookButton);
            bookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            bookButton.addActionListener(e -> {
                ShowBookingDialog dialog = new ShowBookingDialog(parentFrame, connection, customerId, prof.getProfessionalId());
            });

            profCard.add(Box.createVerticalStrut(10));
            profCard.add(bookButton);

            resultsPanel.add(Box.createVerticalStrut(15));
            resultsPanel.add(profCard);
        }
    }

    private void returnNoValidResults() {
        JLabel noResults = new JLabel("❌ Δεν βρέθηκαν αποτελέσματα από την αναζήτησή σας.");
        noResults.setForeground(Color.RED);
        noResults.setFont(new Font("SansSerif", Font.BOLD, 14));
        noResults.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(noResults);
    }
}


