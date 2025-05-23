import javax.swing.*;
import java.awt.*;



public class SupportPanelCus extends JPanel {
    public SupportPanelCus() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Δημιουργία Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 238, 230));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)), // κάτω γραμμή
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Χρήσιμες πληροφορίες");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Δημιουργία του κύριου περιεχομένου
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(new Color(245, 238, 230));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Πληροφορίες επικοινωνίας
        JPanel contactPanel = new JPanel();
        contactPanel.setLayout(new BoxLayout(contactPanel, BoxLayout.Y_AXIS));
        contactPanel.setBackground(new Color(245, 238, 230));
        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel emailLabel = new JLabel("📧 Email: fixit@gmail.com");
        emailLabel.setFont(new Font("Noto Color Emoji", Font.PLAIN, 16));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phoneLabel = new JLabel("📞 Τηλέφωνο: 210-1234567");
        phoneLabel.setFont(new Font("Noto Color Emoji", Font.PLAIN, 16));
        phoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hoursLabel = new JLabel("🕒 Ώρες λειτουργίας: Δευτέρα - Παρασκευή, 9:00 - 17:00");
        hoursLabel.setFont(new Font("Noto Color Emoji", Font.PLAIN, 16));
        hoursLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contactPanel.add(emailLabel);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contactPanel.add(phoneLabel);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contactPanel.add(hoursLabel);

        mainContentPanel.add(contactPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Ενότητα Συχνών Ερωτήσεων
        JPanel faqPanel = new JPanel();
        faqPanel.setLayout(new BoxLayout(faqPanel, BoxLayout.Y_AXIS));
        faqPanel.setBackground(new Color(245, 238, 230));
        faqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel faqTitle = new JLabel("❓ Συχνές Ερωτήσεις");
        faqTitle.setFont(new Font("Noto Color Emoji", Font.BOLD, 18));
        faqTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Πίνακας για τις ερωτήσεις και απαντήσεις
        String[] faqs = {
            "Πώς μπορώ να κλείσω ραντεβού;",
            "Ποιες είναι οι διαθέσιμες μέθοδοι πληρωμής;",
            "Πώς μπορώ να ενημερώσω τα στοιχεία του προφίλ μου;",
            "Πώς μπορώ να δω τις κριτικές για τις υπηρεσίες που έχω λάβει;"
        };

        String[] answers = {
            "Για να κλείσετε ραντεβού μεταβείτε στη καρτέλα 'Αναζήτηση' και επιλέξτε την υπηρεσία και τον επαγγελματία που επιθυμείτε.",
            "Το σύστημα πληρωμών υποστηρίζει χρεωστικές/πιστωτικές κάρτες και αντικαταβολή.",
            "Για να ενημερώσετε το προφίλ σας, κάντε κλικ στην ενότητα 'Το προφίλ μου'.",
            "Οι κριτικές εμφανίζονται στην καρτέλα 'Κριτικές'."
        };

        faqPanel.add(faqTitle);
        faqPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Δημιουργία κουμπιών για τις ερωτήσεις
        for (int i = 0; i < faqs.length; i++) {
            String question = faqs[i];
            String answer = answers[i];

            JButton faqButton = new JButton(question);
            faqButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            faqButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            faqButton.setFont(new Font("Arial", Font.PLAIN, 18));
            faqButton.setBackground(Color.WHITE);
            faqButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            faqButton.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));

            // Αντιδράσεις κουμπιών για εμφάνιση της απάντησης
            faqButton.addActionListener(e -> {
                JPanel messagePanel = new JPanel();
                JLabel messageLabel = new JLabel(answer);
                messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
                messageLabel.setForeground(Color.black); 

                // Προσθήκη του JLabel στο message panel
                messagePanel.add(messageLabel);

            // Εμφάνιση του message panel στο JOptionPane
            JOptionPane.showMessageDialog(this, messagePanel, "Απάντηση", JOptionPane.PLAIN_MESSAGE);
});

            faqPanel.add(faqButton);
            faqPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        mainContentPanel.add(faqPanel);
        add(mainContentPanel, BorderLayout.CENTER);
    }
}
