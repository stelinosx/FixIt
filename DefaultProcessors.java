import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Επιστρέφει το map από Type→Processor.
 * Θα χρειαστείς έναν CardGateway (π.χ. stub ή πραγματικό), 
 * έναν CustomerRepository (π.χ. JdbcCustomerRepository)
 * και τη JDBC Connection για το BonusProcessor.
 */
public class DefaultProcessors {
    public static Map<CustomerPayment.Type, CustomerPayment.Processor> getDefaultProcessors(
            Connection conn,
            CustomerPayment.CardGateway gateway,
            CustomerPayment.CustomerRepository custRepo) {

        Map<CustomerPayment.Type, CustomerPayment.Processor> m = new HashMap<>();

        // 1) Κάρτα
        m.put(
            CustomerPayment.Type.CARD,
            new CustomerPayment.CardProcessor(gateway)
        );

        // 2) Πόντοι
        m.put(
            CustomerPayment.Type.BONUS_POINT,
            new CustomerPayment.BonusProcessor(custRepo, conn)
        );

        // 3) Μετρητά
        m.put(
            CustomerPayment.Type.CASH,
            new CustomerPayment.Processor() {
                @Override
                public CustomerPayment.Type getSupportedType() {
                    return CustomerPayment.Type.CASH;
                }
                @Override
                public CustomerPayment.Result process(CustomerPayment.Request req) {
                    return new CustomerPayment.Result(
                        true,
                        "Πληρωμή με μετρητά κατά την παράδοση"
                    );
                }
            }
        );

        return m;
    }
}
