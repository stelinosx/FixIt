import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Επιστρέφει το map από Type→Processor.
 * Θα χρειαστείς έναν CardGateway (π.χ. stub ή πραγματικό) και
 * έναν CustomerRepository (π.χ. JdbcCustomerRepository).
 */
public class DefaultProcessors {
    public static Map<Payment.Type, Payment.Processor> getDefaultProcessors(
            Payment.CardGateway gateway,
            Payment.CustomerRepository custRepo) {

        Map<Payment.Type, Payment.Processor> m = new HashMap<>();
        // κάρτα
        m.put(Payment.Type.CARD, new Payment.CardProcessor(gateway));
        // πόντοι
        m.put(Payment.Type.BONUS_POINT, new Payment.BonusProcessor(custRepo));
        // μετρητά
        m.put(Payment.Type.CASH, new Payment.Processor() {
            @Override public Payment.Type getSupportedType() { return Payment.Type.CASH; }
            @Override public Payment.Result process(Payment.Request req) {
                return new Payment.Result(true, "Πληρωμή με μετρητά κατά την παράδοση");
            }
        });
        return m;
    }
}
