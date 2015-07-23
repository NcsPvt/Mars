package itc.booking.mars;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import itcurves.mars.R;

public class NearbyVehicle {

    //{"iDriverID":2219,"iVehicleID":771,"DriverPicture":"","DriverName":"xyz","DriverNo":"9002","VehicleNo":"1234567","iVehTypeID":6,"AverageRating":1.0,
    // "TotalRatings":1,"Longitude":74.2975941,"Latitude":31.4691865,"Direction":"North","AffiliateID":6, WorkYears,VehicleMake}

    public int iDriverID;
    public int iVehicleID;
    public int iVehTypeID;
    public int iAffiliateID;
    public int iTotalRatings;
    public int iWorkYears;

    public double user_Rating;
    public double avg_Rating;

    public String driverPicture;
    public String driverName;
    public String driverPhone;
    public String vehicleMake;
    public String vehColor;
    public String vinNumber;
    public boolean isFavorite = false;
    public boolean bHailingAllowed = false;
    public boolean bPromoAllowed = false;
    public String distance;
    private LatLng latlong;
    public MarkerOptions vehMarker;
    public int marker = R.drawable.sedan_yellow;
    public String VehicleNo;
    public int PaymentType = 1;
    public String ourRates = "";
    public String ourFleet = "";
    public String ourTerms = "";
    public String aboutUs = "";
    public String companyLogo = "";
    private int direction = 0;

    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_HORIZONTAL = 2;
    public boolean IsPassCountReq = false;
    public int iClassID = 1;

    public NearbyVehicle(String _vehColor, LatLng _latlong) {
        driverPicture = "";
        iDriverID = 0;
        iVehicleID = -1;
        VehicleNo = "-1";
        iVehTypeID = 0;
        iAffiliateID = -1;
        iTotalRatings = 0;
        iWorkYears = 0;
        vehColor = _vehColor;
        vehicleMake = "";
        vinNumber = "";
        driverName = "";
        driverPhone = "";
        user_Rating = 0;
        avg_Rating = 2.5;
        distance = "";
        latlong = _latlong;

        marker = BookingApplication.getVehicleDrawable(vehColor);

        try {
            vehMarker = new MarkerOptions().position(latlong).title("Vehicle").snippet("").flat(true).icon(BitmapDescriptorFactory.fromResource(marker)).anchor(0.5f, 0.5f);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }

    public void setLatlong(LatLng ll) {
        try {
        latlong = ll;
        vehMarker = new MarkerOptions().position(latlong).title("Vehicle").snippet("").flat(true).icon(BitmapDescriptorFactory.fromResource(marker)).anchor(0.5f, 0.5f);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }

    public LatLng getLatlong() {
        return latlong;
    }

    public Bitmap flipImage(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        // if vertical
        if (type == FLIP_VERTICAL)
            // y = y * -1
            matrix.preScale(1.0f, -1.0f);
        else if (type == FLIP_HORIZONTAL)
            // x = x * -1
            matrix.preScale(-1.0f, 1.0f);
            // unknown type
        else
            return null;

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public void setDirection(Context ctx, String dirction) {
        if (dirction.equalsIgnoreCase("East") || dirction.equalsIgnoreCase("NE") || dirction.equalsIgnoreCase("SE"))
            this.direction = 0;
        else if (dirction.equalsIgnoreCase("West") || dirction.equalsIgnoreCase("NW") || dirction.equalsIgnoreCase("SW")) {
            this.direction = 0;
            vehMarker.icon(BitmapDescriptorFactory.fromBitmap(flipImage(BitmapFactory.decodeResource(ctx.getResources(), marker), FLIP_HORIZONTAL)));
        }

        //else if (dirction.equalsIgnoreCase("South") || dirction.equalsIgnoreCase("S"))
        //this.direction = 90;
        //else if (dirction.equalsIgnoreCase("North") || dirction.equalsIgnoreCase("N"))
        //this.direction = 270;
        //else if (dirction.equalsIgnoreCase("SouthEast") || dirction.equalsIgnoreCase("SE"))
        //this.direction = 45;
        //else if (dirction.equalsIgnoreCase("SouthWest") || dirction.equalsIgnoreCase("SW"))
        //this.direction = 135;
        //else if (dirction.equalsIgnoreCase("NorthWest") || dirction.equalsIgnoreCase("NW"))
        //this.direction = 225;
        //else if (dirction.equalsIgnoreCase("NorthEast") || dirction.equalsIgnoreCase("NE"))
        //this.direction = 315;

        vehMarker.rotation(this.direction);
    }

    @Override
    public String toString() {
        return Integer.toString(iVehicleID);
    }

    @Override
    public boolean equals(Object obj) {
        Boolean result = false;
        if (obj instanceof NearbyVehicle) {
            NearbyVehicle veh = (NearbyVehicle) obj;
            result = (veh.VehicleNo.equals(this.VehicleNo) && veh.iAffiliateID == this.iAffiliateID);
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this.VehicleNo);
    }
}
