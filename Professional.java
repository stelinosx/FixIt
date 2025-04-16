import java.util.ArrayList;
import java.util.List;

public class Professional extends User {
    // Δήλωση των μεταβλητών της κλάσης Professional
    private String profession;
    private String description;
    private double rating;
    private final List<String> reviews;
    private final List<String> services;
    
    
    // Δημιουργία του constructor της κλάσης Professional, σε συνδυασμό με τον constructor της κλάσης User για να κληρονομήσει τα πεδία    της κλάσης User
    public Professional(String username, String password, String email, String phoneNumber, 
                       String profession, String description) {
        super(username, password, email, phoneNumber);
        this.profession = profession;
        this.description = description;
        this.rating = 0.0;
        this.reviews = new ArrayList<>();
        this.services = new ArrayList<>();
    }
    
    //Getters και Setters για τις μεταβλητές της κλάσης Professional 
    
    // Επιστροφή την ειδιικότητα του επαγγελματία
    public String getProfession() {
        return profession;
    }
    // Αλλαγή της ειδικότητας του επαγγελματία
    public void setProfession(String profession) {
        this.profession = profession;
    }
    // Επιστροφή της περιγραφής του επαγγελματία
    public String getDescription() {
        return description;
    }
   // Αλλαγή της περιγραφής του επαγγελματία
    public void setDescription(String description) {
        this.description = description;
    }
    // Επιστροφή της βαθμολογίας του επαγγελματία
    public double getRating() {
        return rating;
    }
    // Πρόσθεση της βαθμολογίας του επαγγελματία
    public void addReview(String review, double rating) {
        reviews.add(review);
        this.rating = (this.rating * (reviews.size() - 1) + rating) / reviews.size();
    }
    // Επιστροφή των κριτικών του επαγγελματία
    public List<String> getServices() {
        return services;
    }
    // Προσθήκη υπηρεσίας του επαγγελματία
    public void addService(String service) {
        services.add(service);
    }
}