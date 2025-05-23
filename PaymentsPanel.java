import javax.swing.*;
import javax.swing.SpinnerNumberModel;
import java.awt.*;

public class PaymentsPanel extends JPanel {
    private final Payment.Service paymentService;
    private final int customerId;
    private final int professionalId;
    private final int appointmentId;
    private final CustomerFrame parent;

    private JComboBox<Payment.Type> typeCombo;
    private CardLayout cardLayout;
    private JPanel detailsPanel;
    private JTextField cardNumberField;
    private JTextField cardNameField;
    private JTextField cardExpiryField;
    private JTextField cardCvvField;
    private JSpinner pointsSpinner;

    public PaymentsPanel(Payment.Service paymentService,
                         int customerId,
                         int professionalId,
                         int appointmentId,
                         CustomerFrame parent) {
        this.paymentService  = paymentService;
        this.customerId      = customerId;
        this.professionalId  = professionalId;
        this.appointmentId   = appointmentId;
        this.parent          = parent;
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBackground(CustomerFrame.BACKGROUND);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CustomerFrame.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        JLabel title = new JLabel("Επιλογή Τρόπου Πληρωμής");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Main content
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(CustomerFrame.BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        // Payment type selection
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBackground(CustomerFrame.BACKGROUND);
        typePanel.add(new JLabel("Τύπος: "));
        typeCombo = new JComboBox<>(Payment.Type.values());
        typeCombo.addActionListener(e -> switchDetailsPanel());
        typePanel.add(typeCombo);
        center.add(typePanel, BorderLayout.NORTH);

        // Details panel
        cardLayout   = new CardLayout();
        detailsPanel = new JPanel(cardLayout);
        detailsPanel.setBackground(CustomerFrame.BACKGROUND);

        // CARD
        JPanel cardPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        cardNumberField = new JTextField();
        cardNameField   = new JTextField();
        cardExpiryField = new JTextField();
        cardCvvField    = new JTextField();
        cardPanel.add(new JLabel("Αρ. Κάρτας:"));      cardPanel.add(cardNumberField);
        cardPanel.add(new JLabel("Όνομα Κάτοχου:"));    cardPanel.add(cardNameField);
        cardPanel.add(new JLabel("Λήξη (MM/YY):"));     cardPanel.add(cardExpiryField);
        cardPanel.add(new JLabel("CVV:"));              cardPanel.add(cardCvvField);
        detailsPanel.add(cardPanel, Payment.Type.CARD.name());

        // BONUS_POINT
        JPanel bonusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bonusPanel.setBackground(CustomerFrame.BACKGROUND);
        bonusPanel.add(new JLabel("Πόντοι προς εξαργύρωση:"));
        pointsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        bonusPanel.add(pointsSpinner);
        detailsPanel.add(bonusPanel, Payment.Type.BONUS_POINT.name());

        // CASH
        JPanel cashPanel = new JPanel();
        cashPanel.setBackground(CustomerFrame.BACKGROUND);
        cashPanel.add(new JLabel("Πληρωμή με μετρητά κατά την παράδοση."));
        detailsPanel.add(cashPanel, Payment.Type.CASH.name());

        center.add(detailsPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        switchDetailsPanel();

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(CustomerFrame.BACKGROUND);
        JButton payBtn    = new JButton("Πληρωμή");
        JButton cancelBtn = new JButton("Ακύρωση");
        payBtn.addActionListener(e -> onPay());
        cancelBtn.addActionListener(e -> parent.show());
        bottom.add(cancelBtn);
        bottom.add(payBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void switchDetailsPanel() {
        Payment.Type selected = (Payment.Type) typeCombo.getSelectedItem();
        cardLayout.show(detailsPanel, selected.name());
    }

    private void onPay() {
        Payment.Type type = (Payment.Type) typeCombo.getSelectedItem();
        Object details   = null;

        if (type == Payment.Type.CARD) {
            String number = cardNumberField.getText().trim();
            String name   = cardNameField.getText().trim();
            String expiry = cardExpiryField.getText().trim();
            String cvv    = cardCvvField.getText().trim();

            // 1) Έλεγχος για κενά
            if (number.isEmpty() || name.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Παρακαλώ συμπληρώστε όλα τα πεδία της κάρτας.",
                    "Σφάλμα",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2) 16 ψηφία
            String digitsOnly = number.replaceAll("\\D", "");
            if (!digitsOnly.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this,
                    "Ο αριθμός της κάρτας πρέπει να αποτελείται από 16 ψηφία.",
                    "Σφάλμα",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3) MM/YY + όχι ληγμένη
            if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(this,
                    "Η ημερομηνία λήξης πρέπει να είναι στη μορφή MM/YY.",
                    "Σφάλμα",
                    JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                String[] parts = expiry.split("/");
                int month = Integer.parseInt(parts[0]);
                int year  = 2000 + Integer.parseInt(parts[1]);
                java.time.YearMonth exp = java.time.YearMonth.of(year, month);
                if (exp.isBefore(java.time.YearMonth.now())) {
                    JOptionPane.showMessageDialog(this,
                        "Η κάρτα έχει λήξει. Παρακαλώ εισάγετε έγκυρη ημερομηνία λήξης.",
                        "Σφάλμα",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 4) CVC (3 ψηφία)
            if (!cvv.matches("\\d{3}")) {
                JOptionPane.showMessageDialog(this,
                    "Το CVC πρέπει να αποτελείται από 3 ψηφία.",
                    "Σφάλμα",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Έτοιμα τα στοιχεία
            details = new Payment.CardDetails(number, name, expiry, cvv);

        } else if (type == Payment.Type.BONUS_POINT) {
            details = (Integer) pointsSpinner.getValue();
        } else {
            details = null;
        }

        // Κάνουμε την πληρωμή
        Payment.Result res = paymentService.payAppointment(
            customerId,
            professionalId,
            appointmentId,
            type,
            details
        );

        if (res.success) {
            JOptionPane.showMessageDialog(this, res.message, "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            parent.refreshPoints();   // φρεσκάρισμα των πόντων
            parent.show();
        } else {
            JOptionPane.showMessageDialog(this, res.message, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }
}
