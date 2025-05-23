import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AppointmentCard extends JPanel {

    public AppointmentCard(int appointmentId,
                           String professionalName,
                           String appointmentDate,
                           String appointmentTime,
                           String serviceType,
                           String location,
                           String status,
                           ActionListener onCancel,
                           ActionListener onReview) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0), 2, true), // Πορτοκαλί border
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setMinimumSize(new Dimension(850, 200));
        setMaximumSize(new Dimension(850, 200));
                            
        // Κύριο panel με BoxLayout για αριστερή στοίχιση
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(createInfoRow("Ονοματεπώνυμο:", professionalName));
        contentPanel.add(createInfoRow("Ημερομηνία:", appointmentDate));
        contentPanel.add(createInfoRow("Ώρα:", appointmentTime));
        contentPanel.add(createInfoRow("Διεύθυνση:", location));
        contentPanel.add(createInfoRow("Υπηρεσία:", serviceType));

        // Status
        JLabel statusLabel = new JLabel("Κατάσταση: ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel statusValue = new JLabel();
        statusValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        switch (status) {
            case "pending":
                statusValue.setText("Εκκρεμεί");
                statusValue.setForeground(new Color(255, 165, 0));
                break;
            case "accepted":
                statusValue.setText("Αποδεκτό");
                statusValue.setForeground(new Color(76, 175, 80));
                break;
            case "cancelled":
                statusValue.setText("Ακυρωμένο");
                statusValue.setForeground(Color.GRAY);
                break;
            case "declined":
                statusValue.setText("Απορρίφθηκε");
                statusValue.setForeground(new Color(244, 67, 54));
                break;
            case "completed":
                statusValue.setText("Ολοκληρωμένο");
                statusValue.setForeground(new Color(33, 150, 243));
                break;
            default:
                statusValue.setText(status);
        }

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);
        statusPanel.add(statusValue);
        contentPanel.add(statusPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(Color.WHITE);

        if ("pending".equals(status) && onCancel != null) {
            JButton cancelButton = createStyledButton("Ακύρωση", new Color(244, 67, 54));
            cancelButton.addActionListener(onCancel);
            actionsPanel.add(cancelButton);
        } else if ("completed".equals(status) && onReview != null) {
            JButton reviewButton = createStyledButton("Αξιολόγηση", new Color(92, 79, 150));
            reviewButton.addActionListener(onReview);
            actionsPanel.add(reviewButton);
        }

        add(actionsPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(lbl);
        panel.add(val);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}