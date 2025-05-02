import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomDialog extends JDialog {
    public CustomDialog(Frame parent, String message) {
        super(parent, true);  // true makes it modal
        setTitle("Login Successful");
        setSize(250, 120);
        setLocationRelativeTo(parent);  // Center it on the parent frame

        // Set the layout
        setLayout(new BorderLayout());

        // Set the background color to orange
        getContentPane().setBackground(new Color(255, 140, 0));  // Orange color

        // Message label with white font color
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setForeground(Color.WHITE);  // White text color
        add(messageLabel, BorderLayout.CENTER);

        // Create "OK" button with black background and white text
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(Color.BLACK);  // Black background for the button
        okButton.setForeground(Color.BLACK);  // Black text color for the button

        // Change cursor to hand when hovering over the button
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Action listener for the "OK" button
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();  // Close the dialog when "OK" is clicked
            }
        });

        // Add "OK" button to the bottom of the dialog
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 140, 0));  // Match the button panel with the dialog background
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
