package itc.booking.mars;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ClassOfVehicle {

    public String classLogoLink;
    public String className;
    public int classID;
    public int availability;

    public ArrayList<String> vehicles = new ArrayList<String>();
    public ArrayList<String> vehicles_images = new ArrayList<String>();
    public ArrayList<String> affiliates = new ArrayList<String>();

    /*------------------------------------------------------Constructor()----------------------------------------------------------------------------*/
    public ClassOfVehicle(int _classID, String _className, String _classLogo, int _availbility) {

        classLogoLink = _classLogo;
        className = _className;
        classID = _classID;
        availability = _availbility;

        vehicles.add("Any");
        vehicles_images.add("SEDAN_WHITE");
        affiliates.add("Any Company");

    }

    /*------------------------------------------------------getVehiclesFromDB()----------------------------------------------------------------------------*/
    public void getVehiclesFromDB(Activity activty, String AffName, Boolean isNow, ArrayAdapter<String> vehAdaptor) {

        Cursor result;
        if (AffName.equalsIgnoreCase("")) {
            if (isNow)
                result = BookingApplication.db.rawQuery("SELECT DISTINCT VehName,VehTypeImage FROM Affiliates where bShowNow = 1 AND ClassID = " + classID, null);
            else
                result = BookingApplication.db.rawQuery("SELECT DISTINCT VehName,VehTypeImage FROM Affiliates where bShowLater = 1 AND ClassID = " + classID, null);
        } else if (isNow)
            result = BookingApplication.db.rawQuery("SELECT DISTINCT VehName,VehTypeImage FROM Affiliates where bShowNow = 1 AND AffiliateName = '" + AffName + "' AND ClassID = " + classID, null);
        else
            result = BookingApplication.db.rawQuery("SELECT DISTINCT VehName,VehTypeImage FROM Affiliates where bShowLater = 1 AND AffiliateName = '" + AffName + "' AND ClassID = " + classID, null);
        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        vehicles.clear();
        vehicles_images.clear();
        vehicles.add("Any");
        vehicles_images.add("SEDAN_WHITE");

        if (rowCount > 0) {
            result.moveToFirst();
            for (int j = 0; j < rowCount; j++) {
                vehicles.add(result.getString(0));
                vehicles_images.add(result.getString(1));
                result.moveToNext();
            }
        }
        if (vehAdaptor != null)
            vehAdaptor.notifyDataSetChanged();

    }// getVehiclesFromDB

    /*---------------------------------------------------------getAffiliatesFromDB()----------------------------------------------------------------------------*/
    public int getAffiliatesFromDB(Activity activty, String vehName, Boolean isNow, ArrayAdapter<String> affiAdaptor) {

        affiliates.clear();

        Cursor result;
        if (vehName.equalsIgnoreCase("")) {
            if (isNow)
                result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID,AffiliateName FROM Affiliates where bShowNow = 1 AND ClassID = " + classID, null);
            else
                result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID,AffiliateName FROM Affiliates where bShowLater = 1 AND ClassID = " + classID, null);
        } else if (isNow)
            result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID,AffiliateName FROM Affiliates where bShowNow = 1 AND VehName = '" + vehName + "' AND ClassID = " + classID, null);
        else
            result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID,AffiliateName FROM Affiliates where bShowLater = 1 AND VehName = '" + vehName + "' AND ClassID = " + classID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            for (int j = 0; j < rowCount; j++) {
                if (rowCount < 3 && result.getInt(0) == 0)
                    result.moveToNext();
                else {
                    affiliates.add(result.getString(1));
                    result.moveToNext();
                }
            }
        }
        if (affiAdaptor != null)
            affiAdaptor.notifyDataSetChanged();

        return affiliates.size();
    }// getAffiliatesFromDB

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.classID == ((ClassOfVehicle) obj).classID;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(this.classID);
    }

}
