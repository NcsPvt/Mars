package itc.booking.mars;

import android.annotation.TargetApi;
import android.os.Build;

public class Promotion {

    public String promoDescription;
    public String companyLogoLink;
    public String companyName;
    public int companyID;
    public String promoCode;

    // Constructor
    public Promotion(String promo_Code, String promo_Description, String companyLogo, String cmpanyName, int cmpanyID) {
        promoCode = promo_Code;
        promoDescription = promo_Description;
        companyLogoLink = companyLogo;
        companyName = cmpanyName;
        companyID = cmpanyID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.promoDescription.equalsIgnoreCase(((Promotion) obj).promoDescription);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this.promoDescription);
    }

}
