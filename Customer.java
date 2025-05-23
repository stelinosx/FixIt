import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private int points;
    private final List<String> appointments;
    private int customerId;
    // Δημιουργία αντικειμένου της κλάσης Customer για αρχικοποίηση των μεταβλητών
    public Customer(String username, String password, String email, String phoneNumber, int customerId) {
        super(username, password, email, phoneNumber);
        this.customerId = customerId;
        this.points = 0;
        this.appointments = new ArrayList<>();
    }
    // Getters και Setters για τις μεταβλητές της κλάσης Customer
    // Επιστροφή των πόντων του πελάτη
    public int getcustomerId()
    {
        return customerId;
    }
    public int getPoints() {
        return points;
    }
    // Αλλαγή των πόντων του πελάτη
    public void addPoints(int points) {
        this.points += points;
    }
    // Επιστροφή των ραντεβού του πελάτη
    public List<String> getAppointments() {
        return appointments;
    }
    // Αλλαγή των ραντεβού του πελάτη
    public void addAppointment(String appointment) {
        appointments.add(appointment);
    }
}