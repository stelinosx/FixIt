import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AppointmentCard extends JPanel {
    /**
     * @param onCancel   ActionListener for cancellation (only if status is "pending")
     * @param onReview   ActionListener for review (only if status is "completed")
     */
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
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 150));

        // Content grid
        JPanel contentPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        contentPanel.add(createLabel("Ονοματεπώνυμο επαγγελματία:"));
        contentPanel.add(createLabel(professionalName));
        contentPanel.add(createLabel("Ημερομηνία:"));
        contentPanel.add(createLabel(appointmentDate));
        contentPanel.add(createLabel("Ώρα:"));
        contentPanel.add(createLabel(appointmentTime));
        contentPanel.add(createLabel("Υπηρεσία:"));
        contentPanel.add(createLabel(serviceType));
        contentPanel.add(createLabel("Διεύθυνση:"));
        contentPanel.add(createLabel(location));
        contentPanel.add(createLabel("Κατάσταση:"));

        JLabel statusValue = new JLabel();
        switch (status) {
            case "pending":   statusValue.setText("Εκκρεμεί");    statusValue.setForeground(Color.ORANGE); break;
            case "accepted":  statusValue.setText("Αποδεκτό");   statusValue.setForeground(new Color(0,128,0)); break;
            case "declined":  statusValue.setText("Απορριφθέν"); statusValue.setForeground(new Color(128,0,0)); break;
            case "cancelled": statusValue.setText("Ακυρωμένο"); statusValue.setForeground(Color.RED); break;
            case "completed": statusValue.setText("Ολοκληρωμένο"); statusValue.setForeground(new Color(0,0,255)); break;
            default: statusValue.setText(status); break;
        }
        contentPanel.add(statusValue);

        add(contentPanel, BorderLayout.CENTER);

        // Actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setBackground(Color.WHITE);

        if ("pending".equals(status) && onCancel != null) {
            JButton cancelButton = new JButton("Ακύρωση");
            cancelButton.setBackground(new Color(220, 53, 69));
            cancelButton.setForeground(Color.WHITE);
            cancelButton.setBorderPainted(false);
            cancelButton.addActionListener(onCancel);
            actionsPanel.add(cancelButton);
        } else if ("completed".equals(status) && onReview != null) {
            JButton reviewButton = new JButton("Αξιολόγηση");
            reviewButton.setBackground(new Color(92, 79, 150));
            reviewButton.setForeground(Color.WHITE);
            reviewButton.setBorderPainted(false);
            reviewButton.addActionListener(onReview);
            actionsPanel.add(reviewButton);
        }

        add(actionsPanel, BorderLayout.EAST);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));
        return lbl;
    }
}