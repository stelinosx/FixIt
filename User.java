public abstract class User {
    // Δήλωση των μεταβλητών της κλάσης User
    protected String username;
    protected String password;
    protected String email;
    protected String phoneNumber;
    
    // Δημιουργια αντικειμένου της κλάσης User για αρχικοποίηση των μεταβλητών 
    public User(String username, String password, String email, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    // Getters και Setters για τις μεταβλητές της κλάσης User
    // Επιστροφή του username
    public String getUsername() {
        return username;
    }
    // Αλλαγή του username
    public void setUsername(String username) {
        this.username = username;
    }
    // Επιστροφή του password
    public String getPassword() {
        return password;
    }
    // Αλλαγή του password
    public void setPassword(String password) {
        this.password = password;
    }
    // Επιστροφή του email
    public String getEmail() {
        return email;
    }
    // Αλλαγή του email
    public void setEmail(String email) {
        this.email = email;
    }
    // Επιστροφή του phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }
    // Αλλαγή του phoneNumber
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
} 