package itc.booking.mars;

public class CreditCardProfile {

    String id, type, last4, expiry;

    public CreditCardProfile(String _id, String _type, String _last4, String _expiry) {
        id = _id.trim();
        type = _type.trim();
        last4 = _last4.trim();
        expiry = _expiry.trim();
    }

}
