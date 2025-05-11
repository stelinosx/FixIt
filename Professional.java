import java.util.ArrayList;
import java.util.List;

public class Professional extends User {
    // Δήλωση των μεταβλητών της κλάσης Professional
    private int professionalId;
    private String profession;
    private String description;
    private double rating;
    private String firstName;
    private String lastName;
    private final List<String> reviews;
    private final List<String> services;
    
   public Professional(int professionalId, String username, String password, String email, String phoneNumber, 
                   String firstName, String lastName, String profession, String description) {
    super(username, password, email, phoneNumber);  // Constructor της User
    this.professionalId = professionalId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.profession = profession;
    this.description = description;
    this.rating = 0.0;
    this.reviews = new ArrayList<>();
    this.services = new ArrayList<>();
}

    

    public int getProfessionalId() {
        return professionalId;
    }

    //Getters και Setters για τις μεταβλητές της κλάσης Professional 
    public String getFirstName() { 
        return firstName; 
    }
    public String getLastName() { 
            return lastName; 
        }
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
}
