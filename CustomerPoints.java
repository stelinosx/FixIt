
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerPoints extends JPanel {
    private static final int MAX_POINTS = 100;
    String text;

    private final Connection connection;
    private final int customerId;
    private int points;

    public CustomerPoints(Connection connection, CustomerFrame customerFrame, int customerId) {
        this.connection = connection;
        this.customerId = customerId;
        setPreferredSize(new Dimension(400, 150));
        setBackground(new Color(255, 250, 240));
        loadCustomerPoints();
        TopLeftLabelPanel(text);
    }

    // Ανάκτηση των πόντων από τη βάση δεδομένων
    private void loadCustomerPoints() {
        String sql = "SELECT customer_bonuspoints FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.points = Math.min(rs.getInt("customer_bonuspoints"), MAX_POINTS);
            } else {
                this.points = 0; // αν δεν βρεθεί ο χρήστης
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.points = 0;
        }
    }
    
public void TopLeftLabelPanel(String text) {
        setLayout(new BorderLayout());
        setOpaque(false); // Διαφανές για να φαίνεται το background από κάτω αν υπάρχει

        JLabel label = new JLabel("Οι πόντοι μου : ");
        label.setForeground(Color.BLACK); // Μαύρο χρώμα
        label.setFont(new Font("SansSerif", Font.BOLD, 24)); // Bold γράμματα, μέγεθος 14
        label.setBounds(10, 0, 300, 30); // Θέση και μέγεθος: (x, y, πλάτος, ύψος)

        // Προσθήκη πάνω αριστερά
        add(label, BorderLayout.WEST );
         
        // Προαιρετικά padding
        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
    }

    // Ζωγραφίζει την μπάρα πόντων στο κέντρο του πάνελ
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int barWidth = 300;
        int barHeight = 30;
        int barX = (panelWidth - barWidth) / 2;
        int barY = (panelHeight - barHeight) / 2;

        // Σχεδίαση κενού πλαισίου
        g.setColor(new Color(255, 255, 255));
        g.fillRect(barX, barY, barWidth, barHeight);

        // Σχεδίαση γεμίσματος ανάλογα με τους πόντους
        g.setColor(new Color(255, 165, 0)); // LIGHT_ORANGE
        int fillWidth = (int) ((points / (double) MAX_POINTS) * barWidth);
        g.fillRect(barX, barY, fillWidth, barHeight);

        // Πλαίσιο γύρω από τη μπάρα
        g.setColor(Color.DARK_GRAY);
        g.drawRect(barX, barY, barWidth, barHeight);

        // Κείμενο
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String label = " Οι Πόντοι Μου: " + points + " / " + MAX_POINTS;
        int strWidth = g.getFontMetrics().stringWidth(label);
        g.drawString(label, (panelWidth - strWidth) / 2, barY - 10);
    }
}