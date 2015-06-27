package itc.booking.mars;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.Calendar;
import java.util.TimeZone;

public class Trip {

    public String tripType;        //CURR   FUT   REC
    public String PaymentType;    //cash   credit card
    public String CreditCardID;
    public String callbackName;
    public String callbackNumber;
    protected String PUaddress;
    protected String PUcity;
    protected String PUstate;
    protected String PUcountry;
    protected Double PUlat;
    protected Double PUlong;
    protected String PUZip;

    protected String DOaddress;
    protected Double DOlat;
    protected Double DOlong;
    protected String DOcity;
    protected String DOstate;
    protected String DOZip;
    protected String DOcountry;
    int companyID;
    int vehTypeID;
    int vehID;
    String vehNo;
    int etiquetteRating, taxiLateRating, cleanlinessRating, serviceRating;
    protected Calendar PUDateTime;

    protected String state;
    protected String iServiceID;
    protected String ConfirmNumber;
    protected String clientPhoneNumber;
    protected String estimatedCost;
    protected String estimatedDuration;
    protected boolean isWallRequested;
    public boolean isHailed;
    public boolean isPaymentDeclined;
    public String comments, otherInfo;
    public String signatureUrl;
    public String promoCode;
    public String currencySymbol;
    public int supportedPaymentType;
    public String tip;
    public String vehTypeName;
    public String companyName;
    public boolean exclusiveRide = false;
    public int childrenCount = 0;
    public int passengerCount = 1;
    public int bagsCount = 0;
    public String pickNotes;
    public String dropNotes;
    protected int estimatedDistance;
    public int classofserviceid;
    public String distanceUnit;

    // Constructor for Unshared Trips
    public Trip(Double picklat, Double picklong, String pickaddress, int companyid, int vehtypeid) {

        iServiceID = "";
        ConfirmNumber = "";
        vehTypeID = vehtypeid;
        companyID = companyid;
        vehTypeName = "Any";
        companyName = "Any Company";
        PUlat = picklat;
        PUlong = picklong;
        PUaddress = pickaddress;
        tripType = "CURR";
        classofserviceid = -1;
        PaymentType = "1";
        CreditCardID = "";
        state = "NONE";
        PUcity = "";
        PUZip = "";
        PUstate = "";
        PUcountry = "";
        DOaddress = "";
        DOcity = "";
        DOZip = "";
        DOstate = "";
        DOcountry = "";
        comments = "";
        estimatedCost = "0";
        estimatedDistance = 0;
        estimatedDuration = "0";
        vehID = -1;
        vehNo = "-1";
        DOlat = 0.0;
        DOlong = 0.0;
        etiquetteRating = 0;
        taxiLateRating = 0;
        cleanlinessRating = 0;
        serviceRating = 0;
        supportedPaymentType = 1;
        isWallRequested = false;
        isHailed = false;
        isPaymentDeclined = false;
        promoCode = "0";
        tip = "0";
        signatureUrl = "";
        pickNotes = "";
        dropNotes = "";
        otherInfo = "";
        callbackName = "";
        callbackNumber = "";
        currencySymbol = "$";
        distanceUnit = "km";
        PUDateTime = Calendar.getInstance(TimeZone.getDefault());

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.iServiceID.equalsIgnoreCase(((Trip) obj).iServiceID);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this.iServiceID);
    }

}
