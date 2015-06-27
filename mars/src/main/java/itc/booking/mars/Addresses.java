package itc.booking.mars;

import android.annotation.TargetApi;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

public class Addresses {
    public Boolean isRemoved = false;
    public int favId;
    public String caption;
    public String address;
    public String city;
    public String state;
    public String country;
    public String zip;
    LatLng latlong;

    public Addresses(String _caption, String _address, LatLng _latlong) {
        favId = 0;
        caption = _caption;
        latlong = _latlong;
        address = _address;
        city = "";
        state = "";
        country = "";
        zip = "0";
    }

    @Override
    public boolean equals(Object obj) {
        Boolean res = false;
        if (obj instanceof Addresses) {
            Addresses fav = (Addresses) obj;
            res = (fav.latlong.equals(this.latlong) || fav.favId == this.favId);
        }
        return res;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this.latlong);
    }
}
