package itc.booking.mars;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import itc.downloader.image.ImageLoader;
import itcurves.mars.R;

public class BookingApplication extends Application {

    public class Campaign {
        public String CampaignName;
        public String PromoCode;
        public String Balance;
        public String dtExpiry;
        public String PromoURL;
        public String PerTripAmount;

        public Campaign() {
            CampaignName = "";
            PromoCode = "";
            Balance = "";
            dtExpiry = "";
            PromoURL = "";
            PerTripAmount = "";
        }
    }

    public class PromoPartner {
        public String CompanyID;
        public String CompanyName;
        public String MainLogoLink;
        public String PromotionMessage;

        public void PromoPartner() {
            CompanyID = "";
            CompanyName = "";
            MainLogoLink = "";
            PromotionMessage = "";
        }
    }

    public static Activity callerContext;
    public static ArrayList<NearbyVehicle> nearByVehicles = new ArrayList<NearbyVehicle>();
    public static ArrayList<Marker> nearbyVehiclesMarkers = new ArrayList<Marker>();
    public static ArrayList<CreditCardProfile> ccProfiles = new ArrayList<CreditCardProfile>();
    public static ArrayList<Addresses> favorites = new ArrayList<Addresses>();
    public static ArrayList<Addresses> recents = new ArrayList<Addresses>();
    public static ArrayList<ClassOfVehicle> classOfVehicles = new ArrayList<ClassOfVehicle>();
    public static ArrayList<Trip> recentTrips = new ArrayList<Trip>();
    public static ArrayList<Promotion> promotions = new ArrayList<Promotion>();
    public static ArrayList<Campaign> activePromotions = new ArrayList<Campaign>();
    public static ArrayList<String> invitees = new ArrayList<String>();

    public static ArrayList<String> possibleEmail;
    public static LatLng currentLatLong;
    public static Address currentAddress = new Address(Locale.US);
    public static SharedPreferences userInfoPrefs;
    public static SQLiteDatabase db;
    public static DatabaseHelper dbHelper;
    public static TelephonyManager tMgr;
    public static WifiManager wifiManager;
    public static ConnectivityManager cm;
    public static boolean isMinimized = false;
    public static boolean syncRequired = false;
    public static boolean isNetworkConnected = false;
    public static boolean dbNotFound = false;
    public static boolean ridesFetched = false;
    public static boolean bsendsms = false;
    public static boolean isAutoLogin = false;
    public static boolean bShowNearbyPlaces = false;
    public static boolean bHandshakeSuccess = false;
    public static ImageLoader imagedownloader;
    public static int apiCalled = -1;
    public static String SupportedPaymentMethod = "1";
    public static int MaxLaterHours = 48;
    public static int theme_color = R.color.mars_red;
    public static int font_color = R.color.white;
    public static int textView_Background = R.drawable.mars_text_view_red;
    public static int button_Background = R.drawable.button_red;
    public static int gray_textView_Background = R.drawable.mars_text_view_gray_red;
    static int registeredVersionCode = 0;
    static int currentVersionCode = 0;
    public static int screenWidth, screenHeight;
    public static String phoneNumberFetched = "";
    public static String appID, packageName, appVersion, phoneNumber, code, password, colorScheme, senderEmailPassword, receiverEmail, availableBalance = "0.0";
    public static String userName = "";
    public static String marsID = "";
    public static String regID = "";
    public static String CompanyID = "1";
    public static String AppShareText = "";
    public static String ReferralRegistrationURL = "";
    public static String airport_hours = "4";
    public static String SERVER_IP = "http://outload.mars.itcurves.us";
    public static String SENDER_ID = "";
    public static String selected_lang = "en";
    public static String trip_type = "CURR";
    public static Dialog customProgressDialog;
    public static CallbackResponseListener currentCallbackListener = null;
    public static long currentDBTimeStamp = 0;
    protected static int unPerformedTripsCount = 0;
    public static DecimalFormat df = new DecimalFormat("#0.00");

    static SimpleDateFormat inFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    static SimpleDateFormat inFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
    static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
    static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-dd-yyyy", Locale.US);

    static InstanceID gcm;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /*-------------------------------------------------------------- isDialerAvailable -------------------------------------------------------------------------------------*/
    public static boolean isDialerAvailable(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return !(manager == null || manager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE);
    }

    /*------------------------------------------------------ SortTrips -------------------------------------------------------------------------------------*/
    public static void SortTrips() {
        Collections.sort(recentTrips, new Comparator<Trip>() {

            @Override
            public int compare(Trip trip1, Trip trip2) {
                return trip2.PUDateTime.compareTo(trip1.PUDateTime);
            }
        });
    }

    /*-------------------------------------------------------------getVehicleType-----------------------------------------------------------------------------*/
    public static int getVehicleDrawable(String vehName) {

        int veh_icon = R.drawable.sedan_yellow;

        if (vehName.startsWith("SEDAN")) {

            veh_icon = R.drawable.sedan_white;

            if (vehName.equalsIgnoreCase("SEDAN_YELLOW"))
                veh_icon = R.drawable.sedan_yellow;
            if (vehName.equalsIgnoreCase("SEDAN_BLACK"))
                veh_icon = R.drawable.sedan_black;
            else if (vehName.equalsIgnoreCase("SEDAN_GRAY"))
                veh_icon = R.drawable.sedan_gray;
            else if (vehName.equalsIgnoreCase("SEDAN_ORANGE"))
                veh_icon = R.drawable.sedan_orange;
            else if (vehName.equalsIgnoreCase("SEDAN_BLUE"))
                veh_icon = R.drawable.sedan_blue;
            else if (vehName.equalsIgnoreCase("SEDAN_PURPLE"))
                veh_icon = R.drawable.sedan_purple;
            else if (vehName.equalsIgnoreCase("SEDAN_PINK"))
                veh_icon = R.drawable.sedan_pink;
            else if (vehName.equalsIgnoreCase("SEDAN_GREEN"))
                veh_icon = R.drawable.sedan_green;
        } else if (vehName.startsWith("VAN")) {

            veh_icon = R.drawable.van_white;

            if (vehName.equalsIgnoreCase("VAN_YELLOW"))
                veh_icon = R.drawable.van_yellow;
            else if (vehName.equalsIgnoreCase("VAN_GRAY"))
                veh_icon = R.drawable.van_gray;
            else if (vehName.equalsIgnoreCase("VAN_ORANGE"))
                veh_icon = R.drawable.van_orange;
            else if (vehName.equalsIgnoreCase("VAN_BLUE"))
                veh_icon = R.drawable.van_blue;
            else if (vehName.equalsIgnoreCase("VAN_PURPLE"))
                veh_icon = R.drawable.van_purple;
            else if (vehName.equalsIgnoreCase("VAN_PINK"))
                veh_icon = R.drawable.van_pink;
            else if (vehName.equalsIgnoreCase("VAN_GREEN"))
                veh_icon = R.drawable.van_green;
        } else if (vehName.startsWith("LIFT"))
            veh_icon = R.drawable.lift;
        else if (vehName.startsWith("SUV"))
            veh_icon = R.drawable.suv;
        else if (vehName.equalsIgnoreCase("LIMO_WHITE"))
            veh_icon = R.drawable.limo_white;
        else if (vehName.equalsIgnoreCase("LIMO_BLACK"))
            veh_icon = R.drawable.limo_black;

        return veh_icon;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    /*----------------------------------------------------------checkPlayServices-----------------------------------------------------------------------------*/
    public static boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(callerContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, callerContext, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                showCustomToast(0, "GooglePlayServices Not Found.", false);
                callerContext.finish();
            }
            return false;
        }
        return true;
    }

    /*----------------------------------------------------------showCustomToast-----------------------------------------------------------------------------*/
    public static void showCustomToast(int resId, String toastMsg, Boolean isError) {
        LayoutInflater inflater = callerContext.getLayoutInflater();
        View layout = inflater.inflate(R.layout.waitlayout, (ViewGroup) callerContext.findViewById(R.id.toast_layout_root));

        if (isError)
            layout.setBackgroundResource(R.drawable.mars_text_view_red);

        layout.findViewById(R.id.customprogress_progress).setVisibility(View.GONE);

        TextView message = (TextView) layout.findViewById(R.id.customprogress_text);
        if (toastMsg != null && toastMsg.length() > 0)
            message.setText(toastMsg);
        else if (resId > 0)
            message.setText(resId);
        else
            message.setText("");

        Toast toast = new Toast(callerContext);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        if (message.length() > 0)
            toast.show();
    }

    /*----------------------------------------------------------showCustomProgress-----------------------------------------------------------------------------*/
    public static void showCustomProgress(final Context context, String Msg, Boolean isCancelable) {
        if (customProgressDialog != null)
            while (customProgressDialog.isShowing())
                customProgressDialog.dismiss();

        customProgressDialog = new Dialog(context, R.style.customDialogTheme);
        customProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customProgressDialog.setContentView(R.layout.waitlayout);
        customProgressDialog.setCancelable(isCancelable);

        if (Msg.length() > 1) {
            TextView tv = (TextView) customProgressDialog.findViewById(R.id.customprogress_text);
            tv.setVisibility(View.VISIBLE);
            tv.setText(Msg);
        }

        customProgressDialog.show();
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email String value
     * @return boolean true for valid false for invalid
     */
    /*--------------------------------------------------------------isEmailValid-----------------------------------------------------------------------------*/
    public static boolean isEmailValid(String email) {

        //CharSequence inputStr = email;
        //Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(inputStr);
        //return matcher.matches();

        return email.matches("^[a-z0-9._%+-]+@(?:[a-z0-9-]+.)+[a-z]{2,4}$");

    }

    /**
     * method is used for checking valid phone number format.
     *
     * @param phone phone number without country code
     * @return boolean true for valid, false for invalid
     */
    /*--------------------------------------------------------------isValidPhone-----------------------------------------------------------------------------*/
    public static boolean isValidPhone(String countryCode, String phone) {
        if (countryCode.matches("^[0-9]{1,3}$") && phone.matches("^(0+[0-9]{9})|([1-9]{1}+[0-9]{8,9})$") || phone.matches("^[+][0-9]{11,13}$"))
            return true;
        else
            return false;
    }

    /*--------------------------------------------------------------getUserCountry-----------------------------------------------------------------------------*/
    public static String getUserCountry() {
        try {
            final String simCountry = tMgr.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2)
                return simCountry.toLowerCase(Locale.US);
            else if (tMgr.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tMgr.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2)
                    return networkCountry.toLowerCase(Locale.US);
            }
        } catch (Exception e) {

        }
        return null;
    }

    /*------------------------------------------------------------getUserCountryCode-----------------------------------------------------------------------------*/
    public static int getUserCountryCode() {
        int cc = 1;
        try {
            if (BookingApplication.isValidPhone("", phoneNumber))
                cc = PhoneNumberUtil.getInstance().parse(phoneNumber, "US").getCountryCode();
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return cc;
    }

    /*--------------------------------------------------------------getUserSimNumber-----------------------------------------------------------------------------*/
    public static String getUserSimNumber() {
        String ph = "";
        if (BookingApplication.isValidPhone("", phoneNumber))
            try {
                ph = Long.toString(PhoneNumberUtil.getInstance().parse(phoneNumber, "US").getNationalNumber());
            } catch (NumberParseException e) {
                try {
                    ph = phoneNumber.substring(Integer.toString(PhoneNumberUtil.getInstance().parse(phoneNumber, "US").getCountryCode()).length());
                } catch (NumberParseException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        return ph;
    }

    /*--------------------------------------------------------------getLocation-----------------------------------------------------------------------------*/
    public static LatLng getLocation(double lat, double lng, int radius) {
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(lat);

        double foundLongitude = new_x + lng;
        double foundLatitude = y + lat;

        return new LatLng(foundLatitude, foundLongitude);
    }

    /*--------------------------------------------getStraightLineDistance-----------------------------------------------------------------------------*/

    /**
     * Returns the approximate distance in meters between this
     * location and the given location.  Distance is defined using
     * the WGS84 ellipsoid.
     *
     * @param point_A the start LatLng
     * @param point_B the End LatLng
     * @return the approximate distance in meters
     */
    public static double getStraightLineDistance(LatLng point_A, LatLng point_B) {
        double distance = 0;

        Location locationA = new Location("Point_A");
        locationA.setLatitude(point_A.latitude);
        locationA.setLongitude(point_A.longitude);

        Location locationB = new Location("Point_B");
        locationB.setLatitude(point_B.latitude);
        locationB.setLongitude(point_B.longitude);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

    // Convert a view to bitmap
    /*------------------------------------------------createDrawableFromView-----------------------------------------------------------------------------*/
    public static Bitmap createDrawableFromView(Context context, View view) {

        //View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        //TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
        //numTxt.setText("27");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /*-----------------------------------------------------showTrackingScreen-----------------------------------------------------------------------------*/
    public static void showTrackingScreen(Activity callingActivity, Trip tripToTrack) {
        Intent tracking = new Intent(callingActivity, ActivityTrack.class);
        Bundle bndle = new Bundle();
        bndle.putString("ServiceID", tripToTrack.iServiceID);
        bndle.putString("ConfirmationNumber", tripToTrack.ConfirmNumber);
        bndle.putDouble("PULat", tripToTrack.PUlat);
        bndle.putDouble("PULng", tripToTrack.PUlong);
        bndle.putString("PUAddress", tripToTrack.PUaddress);

        bndle.putInt("iEtiquetteQuality", tripToTrack.etiquetteRating);
        bndle.putInt("iTaxiLate", tripToTrack.taxiLateRating);
        bndle.putInt("iCleanQuality", tripToTrack.cleanlinessRating);
        bndle.putInt("iServiceQuality", tripToTrack.serviceRating);
        bndle.putString("vComments", tripToTrack.comments);

        if (tripToTrack.DOlat > 0) {
            bndle.putDouble("DOLat", tripToTrack.DOlat);
            bndle.putDouble("DOLng", tripToTrack.DOlong);
            bndle.putString("DOAddress", tripToTrack.DOaddress);
        }
        tracking.putExtras(bndle);
        callingActivity.startActivity(tracking);
        callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
    }

    /*--------------------------------------------------ShowPPV--------------------------------------------------------------------------------------*/
    public static void ShowPPV(Activity callingActivity) {
        try {
            if (SupportedPaymentMethod.contains("3")) {
                Intent profile = new Intent(callingActivity, ActivityPPV.class);
                callingActivity.startActivity(profile);
                callingActivity.overridePendingTransition(R.anim.slide_in_right, 0);
            } else
                showCustomToast(0, callingActivity.getString(R.string.PaymentTypeNotSupportedByApp, callingActivity.getString(R.string.PrepaidAccount)), false);
        } catch (Exception e) {
            showCustomToast(0, e.getLocalizedMessage(), true);
        }
    }

    /*-----------------------------------------------------showWebScreen-----------------------------------------------------------------------------*/
    public static void showWebScreen(Activity callingActivity, String url) {
        Intent webIntent = new Intent(callingActivity, ActivityWeb.class);
        Bundle bndle = new Bundle();
        bndle.putString("url", url);
        webIntent.putExtras(bndle);
        callingActivity.startActivityForResult(webIntent, CODES.WEB_ACTIVITY);
        callingActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.fade_out);
    }

    /*--------------------------------------------------showFavoritesScreen----------------------------------------------------------------------------------*/
    public static void showFavoritesScreen(Activity callingActivity, Double lat, Double lon) {
        Intent search = new Intent(callingActivity, ActivityMyAddresses.class);
        Bundle bndle = new Bundle();
        bndle.putDouble("Latitude", lat);
        bndle.putDouble("Longitude", lon);
        search.putExtras(bndle);
        callingActivity.startActivity(search);
        callingActivity.overridePendingTransition(R.anim.slide_in_right, 0);
    }

    /*--------------------------------------------------showProfileScreen----------------------------------------------------------------------------------*/
    public static void showProfileScreen(Activity callingActivity) {
        Intent profile = new Intent(callingActivity, ActivityProfile.class);
        //callingActivity.startActivity(profile);
        callingActivity.startActivityForResult(profile, APIs.UPDATEPROFILE);
        callingActivity.overridePendingTransition(R.anim.slide_in_right, 0);
    }//showProfileScreen

    /*-----------------------------------------------------showPromotions-----------------------------------------------------------------------------*/
    public static void showPromotions(Activity callingActivity) {
        callingActivity.startActivity(new Intent(callingActivity, ActivityPromotions.class));
        callingActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /*------------------------------------------------------restartAPP()----------------------------------------------------------------------------*/
    public static void restartAPP(Activity callingActivity) {
        Intent intent = new Intent(callingActivity, ActivitySplash.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(callingActivity.getApplicationContext(), 0, intent, 0);
        AlarmManager mgr = (AlarmManager) callingActivity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);
        System.exit(0);
    }

    /*------------------------------------------------------exitAPP()----------------------------------------------------------------------------*/
    public static void exitAPP() {
        db.close();
        System.exit(0);
    }

    /*-----------------------------------------------------showPaymentOptions-----------------------------------------------------------------------------*/
    public static void showPaymentOptions(String skipButtonLabel, String fareEstimate, String currencySymbol, Activity callingActivity, int requestCode, Boolean killParent) {
        Intent activityIntent = new Intent(callingActivity, ActivityPaymentOptions.class);

        Bundle bndle = new Bundle();
        bndle.putString("SkipBtnLabel", skipButtonLabel);
        bndle.putString("fareEstimate", fareEstimate);
        bndle.putString("currencySymbol", currencySymbol);
        bndle.putInt("requestCode", requestCode);
        activityIntent.putExtras(bndle);
        if (killParent) {
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            callingActivity.startActivity(activityIntent);
            callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
            callingActivity.finish();
        } else {
            callingActivity.startActivityForResult(activityIntent, requestCode);
            callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        }
    }

    /*-----------------------------------------------------showSignatureScreen-----------------------------------------------------------------------------*/
    public static void showSignatureScreen(String fareEstimate, Activity callingActivity) {

        Intent activityIntent = new Intent(callingActivity, ActivitySignature.class);

        Bundle bndle = new Bundle();
        bndle.putString("fareEstimate", fareEstimate);
        activityIntent.putExtras(bndle);

        callingActivity.startActivityForResult(activityIntent, CODES.SIGNATURE_REQUIRED);
        callingActivity.overridePendingTransition(R.anim.slide_in_top, 0);
    }

    /*-----------------------------------------------------showVerificationScreen-----------------------------------------------------------------------------*/
    public static void showVerificationScreen(Activity callingActivity) {
        callingActivity.startActivity(new Intent(callingActivity, ActivityVerifyNumber.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        callingActivity.finish();
    }

    /*-----------------------------------------------------showVerificationScreen-----------------------------------------------------------------------------*/
    public static void showSplashScreen(Activity callingActivity) {
        callingActivity.startActivity(new Intent(callingActivity, ActivitySplash.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        callingActivity.finish();
    }

    /*-----------------------------------------------------showLoginScreen-----------------------------------------------------------------------------*/
    public static void showLoginScreen(Activity callingActivity) {
        if (!callerContext.getLocalClassName().endsWith(ActivityLogin.class.getSimpleName())) {
            callingActivity.startActivity(new Intent(callingActivity, ActivityLogin.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
            callingActivity.finish();
        }
    }

    /*------------------------------------------------------showMainScreen-----------------------------------------------------------------------------*/
    public static void showMainScreen(Activity callingActivity, Boolean isQuickBooking) {
        Intent activityIntent = new Intent(callingActivity, ActivityMain.class);

        Bundle bndle = new Bundle();
        bndle.putBoolean("isQuickBooking", isQuickBooking);
        activityIntent.putExtras(bndle);

        callingActivity.startActivity(activityIntent);
        callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
    }

    /*------------------------------------------------------showReferedbyScreen-----------------------------------------------------------------------------*/
    public static void showReferedbyScreen(Activity callingActivity) {
        callingActivity.startActivity(new Intent(callingActivity, ActivityReferedby.class));
        callingActivity.overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        callingActivity.finish();
    }

    /*------------------------------------------------------share_App-----------------------------------------------------------------------------*/
    public static void share_App() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        if (AppShareText.length() == 0)
            sendIntent.putExtra(Intent.EXTRA_TEXT, callerContext.getResources().getString(R.string.InviteFriend, getApplicationName(callerContext)) + "\n" + ReferralRegistrationURL + "AppID=" + appID + "&RefMarsUserID=" + marsID);
        else
            sendIntent.putExtra(Intent.EXTRA_TEXT, AppShareText + "\n" + ReferralRegistrationURL + "AppID=" + appID + "&RefMarsUserID=" + marsID);
        sendIntent.setType("text/plain");
        callerContext.startActivity(sendIntent);
    }

    /*--------------------------------------------------------performLogin-----------------------------------------------------------------------------*/
    public static void performLogin(String numberToSave, String pin, String code, CallbackResponseListener loginlistner) throws NumberParseException {

        currentCallbackListener = loginlistner;
        phoneNumber = numberToSave;
        password = pin;

        if (bsendsms && phoneNumberFetched != null && phoneNumberFetched.length() > 7)
            bsendsms = !phoneNumberFetched.endsWith(phoneNumber);

        String numberToSend = Long.toString(PhoneNumberUtil.getInstance().parse(numberToSave, "US").getNationalNumber());
        numberToSend = numberToSend.length() < 10 ? "0" + numberToSend : numberToSend;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("phonenumber", new StringBody(numberToSend, Charset.forName("UTF-8")));
            outEntity.addPart("password", new StringBody(pin, Charset.forName("UTF-8")));
            outEntity.addPart("code", new StringBody(code, Charset.forName("UTF-8")));
            outEntity.addPart("bsendsms", new StringBody(Boolean.toString(bsendsms), Charset.forName("UTF-8")));

            ITCWebService loginTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.LOGIN;
            loginTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------performRegister-----------------------------------------------------------------------------*/
    public static void performRegister(String numberToSave, String showNumberToDriver, String pin, String fullName, String email, String secretQuestion, String secretAnswer, CallbackResponseListener registerlistner) throws NumberParseException {

        currentCallbackListener = registerlistner;
        phoneNumber = numberToSave;
        password = pin;

        if (bsendsms && phoneNumberFetched != null && phoneNumberFetched.length() > 7)
            bsendsms = !phoneNumberFetched.endsWith(phoneNumber);

        PhoneNumber phNumber = PhoneNumberUtil.getInstance().parse(numberToSave, "US");
        String numberToSend = Long.toString(phNumber.getNationalNumber());
        numberToSend = Long.toString(phNumber.getCountryCode()) + "-" + (numberToSend.length() < 10 ? "0" + numberToSend : numberToSend);

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("phonenumber", new StringBody(numberToSend, Charset.forName("UTF-8")));
            outEntity.addPart("bShowToDriver", new StringBody(showNumberToDriver, Charset.forName("UTF-8")));
            outEntity.addPart("password", new StringBody(pin, Charset.forName("UTF-8")));
            outEntity.addPart("username", new StringBody(fullName, Charset.forName("UTF-8")));
            outEntity.addPart("bsendsms", new StringBody(Boolean.toString(bsendsms), Charset.forName("UTF-8")));
            outEntity.addPart("Email", new StringBody(email, Charset.forName("UTF-8")));
            outEntity.addPart("SecretQuestion", new StringBody(secretQuestion, Charset.forName("UTF-8")));
            outEntity.addPart("SecretAnswer", new StringBody(secretAnswer, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.REGISTER;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------performUpdateProfile-----------------------------------------------------------------------------*/
    public static void performUpdateProfile(String showNumberToDriver, String pin, String newPassword, String fullName, String email, String secretQuestion, String secretAnswer, String tspPreference, String tspID, CallbackResponseListener updatelistner) {

        currentCallbackListener = updatelistner;
        if (newPassword.equalsIgnoreCase(""))
            password = pin;
        else
            password = newPassword;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("password", new StringBody(pin, Charset.forName("UTF-8")));
            outEntity.addPart("newpassword", new StringBody(newPassword, Charset.forName("UTF-8")));
            outEntity.addPart("username", new StringBody(fullName, Charset.forName("UTF-8")));
            outEntity.addPart("Email", new StringBody(email, Charset.forName("UTF-8")));
            outEntity.addPart("SecretQuestion", new StringBody(secretQuestion, Charset.forName("UTF-8")));
            outEntity.addPart("SecretAnswer", new StringBody(secretAnswer, Charset.forName("UTF-8")));
            outEntity.addPart("tsppreference", new StringBody(tspPreference, Charset.forName("UTF-8")));
            outEntity.addPart("tspid", new StringBody(tspID, Charset.forName("UTF-8")));
            outEntity.addPart("bshowtodriver", new StringBody(showNumberToDriver, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.UPDATEPROFILE;
            registerTask.execute(outEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------fetchProfile-----------------------------------------------------------------------------*/
    public static void fetchProfile(String password, CallbackResponseListener fetchProfileListener) {
        currentCallbackListener = fetchProfileListener;
        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("password", new StringBody(password, Charset.forName("UTF-8")));
            outEntity.addPart("zip", new StringBody(currentAddress.getPostalCode() == null ? "" : currentAddress.getPostalCode(), Charset.forName("UTF-8")));
            outEntity.addPart("city", new StringBody(currentAddress.getLocality() == null ? "" : currentAddress.getLocality(), Charset.forName("UTF-8")));
            outEntity.addPart("state", new StringBody(currentAddress.getAdminArea() == null ? "" : currentAddress.getAdminArea(), Charset.forName("UTF-8")));
            outEntity.addPart("country", new StringBody(currentAddress.getCountryCode() == null ? "" : currentAddress.getCountryCode(), Charset.forName("UTF-8")));
            outEntity.addPart("latitude", new StringBody(Double.toString(currentAddress.hasLatitude() ? currentAddress.getLatitude() : 0), Charset.forName("UTF-8")));
            outEntity.addPart("longitude", new StringBody(Double.toString(currentAddress.hasLongitude() ? currentAddress.getLongitude() : 0), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.FETCHPROFILE;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------performHandshake-----------------------------------------------------------------------------*/
    public static void performHandshake(CallbackResponseListener handshakeListener) {

        currentCallbackListener = handshakeListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("appversion", new StringBody(appVersion, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.HANDSHAKE;
        registerTask.execute(outEntity);
    }

    /*---------------------------------------------------AddC2DHistoryInOutLoad-----------------------------------------------------------------------------*/
    public static void AddC2DHistoryInOutLoad(CallbackResponseListener addC2DListener) {

        currentCallbackListener = addC2DListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("tripid", new StringBody(appVersion, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.ADDC2D;
        registerTask.execute(outEntity);
    }

    /*------------------------------------------------------AvailPromotion-----------------------------------------------------------------------------*/
    public static void AvailPromotion(String promotioncode, String companyid, CallbackResponseListener availPromoListener) {

        currentCallbackListener = availPromoListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("promotioncode", new StringBody(promotioncode, Charset.forName("UTF-8")));
            outEntity.addPart("companyid", new StringBody(companyid, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.AvailPromotion;
        registerTask.execute(outEntity);
    }

    /*------------------------------------------------------sendRegistrationIdToBackend---------------------------------------------------------------------*/
    public static void sendRegistrationIdToBackend() {

        //currentCallbackListener =(CallbackResponseListener) callerContext;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("registrationid", new StringBody(regID, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.RegisterForPushMessages;
        registerTask.execute(outEntity);
    }

    /*------------------------------------------------------getUserPromoDetail---------------------------------------------------------------------*/
    public static void getUserPromoDetail(String tspID) {

        currentCallbackListener = (CallbackResponseListener) callerContext;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("companyid", new StringBody(tspID, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.GETUSERPROMOTIONDETAIL;
        registerTask.execute(outEntity);
    }

    /*------------------------------------------------------fetchCustomerRides-----------------------------------------------------------------------------*/
    public static void fetchCustomerRides(CallbackResponseListener fetchCustomerRidesListener, String rideType) {

        currentCallbackListener = fetchCustomerRidesListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("password", new StringBody(password, Charset.forName("UTF-8")));
            outEntity.addPart("ridetype", new StringBody(rideType, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.GETCUSTOMERRIDES;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /*---------------------------------------------------performPreActivation-----------------------------------------------------------------------------*/
    public static void performPreActivation(String phoneNumber, CallbackResponseListener preActivationListener) {

        currentCallbackListener = preActivationListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("phonenumber", new StringBody(phoneNumber, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.PREACTIVATE;
        registerTask.execute(outEntity);

    }

    /*----------------------------------------------performPostActivation-----------------------------------------------------------------------------*/
    public static void performPostActivation(String verificationcode, String newPassword, String phoneNumber) {

        code = verificationcode;
        currentCallbackListener = (CallbackResponseListener) callerContext;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("verificationcode", new StringBody(verificationcode, Charset.forName("UTF-8")));
            outEntity.addPart("osversion", new StringBody(Build.VERSION.RELEASE, Charset.forName("UTF-8")));
            outEntity.addPart("devicemodel", new StringBody(getDeviceName(), Charset.forName("UTF-8")));
            outEntity.addPart("appversion", new StringBody(appVersion, Charset.forName("UTF-8")));
            outEntity.addPart("newpassword", new StringBody(newPassword, Charset.forName("UTF-8")));
            outEntity.addPart("phonenumber", new StringBody(phoneNumber, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.POSTACTIVATE;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*---------------------------------------------------------------------getRatesFromDB()----------------------------------------------------------------------------*/
    public static String[] getRatesFromDB(Activity activty, String vehName, String affiliateName, String className) {

        Cursor result = BookingApplication.db.rawQuery("SELECT InitialFare,CostPerUnit,VehRateUnit,CurrencyUnit,ADU,AddPassengerCost FROM Affiliates where AffiliateName='" + affiliateName + "' and ClassName='" + className + "'", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return new String[]{result.getString(0), result.getString(1), result.getString(2), result.getString(3), result.getString(4)};
        }
        return null;
    }// getRatesFromDB

    /*---------------------------------------------------------------------getRatesFromDB()----------------------------------------------------------------------------*/
    public static String[] getRatesFromDB(Activity activty, int vehTypeID, int affiliateID, int classID) {

        Cursor result = BookingApplication.db.rawQuery("SELECT InitialFare,CostPerUnit,VehRateUnit,CurrencyUnit,ADU,AddPassengerCost FROM Affiliates where AffiliateID=" + affiliateID + " and ClassID=" + classID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return new String[]{result.getString(0), result.getString(1), result.getString(2), result.getString(3), result.getString(4)};
        }
        return null;
    }// getRatesFromDB

    /*---------------------------------------------------------getVehicleClassesFromDB()----------------------------------------------------------------------------*/
    public static int getVehicleClassesFromDB(Activity activty, Boolean isNow, ArrayAdapter<ClassOfVehicle> classOfVehicleAdaptor) {

        classOfVehicles.clear();

        Cursor result;
        if (isNow)
            result = BookingApplication.db.rawQuery("SELECT DISTINCT ClassID,ClassName,COSVehImage,ClassAvailability FROM Affiliates where bShowNow = 1", null);
        else
            result = BookingApplication.db.rawQuery("SELECT DISTINCT ClassID,ClassName,COSVehImage,ClassAvailability FROM Affiliates where bShowLater = 1", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            for (int j = 0; j < rowCount; j++) {
                classOfVehicles.add(new ClassOfVehicle(result.getInt(0), result.getString(1), result.getString(2), result.getInt(3)));
                result.moveToNext();
            }

            for (ClassOfVehicle cov : classOfVehicles) {
                if (cov.getAffiliatesFromDB(activty, "", isNow, null) > 0)
                    cov.getVehiclesFromDB(activty, "", isNow, null);
                else
                    classOfVehicles.remove(cov);
            }
        }
        classOfVehicleAdaptor.notifyDataSetChanged();

        return rowCount;

    }// getVehicleClassesFromDB

    /*---------------------------------------------------------getAffiliateIDFromDB()----------------------------------------------------------------------------*/
    public static int getAffiliateIDFromDB(Activity activty, String affName) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID FROM Affiliates where AffiliateName = '" + affName + "'", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getInt(0);
        }
        return -1;
    }// getAffiliateIDFromDB

    /*---------------------------------------------------------getAffiliateIDFromDB()----------------------------------------------------------------------------*/
    public static ArrayList<String> getAffiliatesIDFromDB(Activity activty) {

        ArrayList<String> affiliatesIDs = new ArrayList<String>();
        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateID FROM Affiliates", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            affiliatesIDs.add(result.getString(0));
        }
        return affiliatesIDs;
    }// getAffiliatesIDFromDB

    /*---------------------------------------------------------getAffiliateIDFromDB()----------------------------------------------------------------------------*/
    public static Boolean isPassengerCountReq(Activity activty, int affID) {

        Cursor result = BookingApplication.db.rawQuery("SELECT IsPassCountReq FROM Affiliates where AffiliateID = " + affID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getInt(0) != 0;
        }
        return false;
    }// isPassengerCountReq

    /*---------------------------------------------------------getAffiliatePaymentType()----------------------------------------------------------------------------*/
    public static int getAffiliatePaymentType(Activity activty, String affName) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT PaymentType FROM Affiliates where AffiliateName = '" + affName + "'", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getInt(0);
        }
        return -1;
    }// getAffiliatePaymentType

    /*---------------------------------------------------------getAffiliatePaymentType()----------------------------------------------------------------------------*/
    public static int getAffiliatePaymentType(Activity activty, int affID) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT PaymentType FROM Affiliates where AffiliateID = " + affID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getInt(0);
        }
        return -1;
    }// getAffiliatePaymentType

    /*---------------------------------------------------------getAffiliateDistanceUnit()----------------------------------------------------------------------------*/
    public static String getAffiliateDistanceUnit(Activity activty, int companyID) {
        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT VehRateUnit FROM Affiliates where AffiliateID = " + companyID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getString(0);
        }
        return "mile";
    }

    /*---------------------------------------------------------getAffiliateNameFromDB()----------------------------------------------------------------------------*/
    public static String getAffiliateNameFromDB(Activity activty, int iAffiliateID) {
        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT AffiliateName FROM Affiliates where AffiliateID = " + iAffiliateID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getString(0);
        }
        return "Any Company";
    }//getAffiliateNameFromDB

    /*---------------------------------------------------------getFavoritesFromDB()----------------------------------------------------------------------------*/
    public static void getFavoritesFromDB(Activity activty) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT favId,favName,streetAddress,lat,lng FROM Favorites", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        favorites.clear();

        Addresses favAddress;

        if (rowCount > 0) {
            result.moveToFirst();
            for (int j = 0; j < rowCount; j++) {
                favAddress = new Addresses(result.getString(1), result.getString(2), new LatLng(Double.parseDouble(result.getString(3)), Double.parseDouble(result.getString(4))));
                favAddress.favId = Integer.parseInt(result.getString(0));
                favorites.add(favAddress);
                result.moveToNext();
            }
        }
    }// getFavoritesFromDB

    /*---------------------------------------------------------getVehicleIDFromDB()----------------------------------------------------------------------------*/
    public static int getVehicleIDFromDB(Activity activty, String vehName) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT VehTypeID FROM Affiliates where VehName = '" + vehName + "'", null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getInt(0);
        }
        return -1;
    }// getAffiliateIDFromDB

    /*---------------------------------------------------------getVehicleIDFromDB()----------------------------------------------------------------------------*/
    public static String getVehicleNameFromDB(Activity activty, int vehID) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT VehName FROM Affiliates where VehTypeID = " + vehID, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getString(0);
        }
        return "Any";
    }// getAffiliateIDFromDB

    /*---------------------------------------------------------getVehicleNameFromDB()----------------------------------------------------------------------------*/
    public static String getVehicleImageFromDB(Activity activty, int vehType) {

        Cursor result = BookingApplication.db.rawQuery("SELECT DISTINCT VehTypeImage FROM Affiliates where VehTypeID = " + vehType, null);

        activty.startManagingCursor(result);
        int rowCount = result.getCount();

        if (rowCount > 0) {
            result.moveToFirst();
            return result.getString(0);
        }
        return "SEDAN_WHITE";
    }// getAffiliateIDFromDB

    /*----------------------------------------------------------makeReservation()----------------------------------------------------------------------------*/
    public static void makeReservation(Trip p, CallbackResponseListener bookingResponseListener, Boolean bPutOnWall) {

        if (p.isPaymentDeclined)
            updatePaymentInfo(p, bookingResponseListener);
        else {
            currentCallbackListener = bookingResponseListener;

            MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
            try {
                outEntity.addPart("picklat", new StringBody(Double.toString(p.PUlat), Charset.forName("UTF-8")));
                outEntity.addPart("picklong", new StringBody(Double.toString(p.PUlong), Charset.forName("UTF-8")));
                outEntity.addPart("pickaddress", new StringBody(p.PUaddress, Charset.forName("UTF-8")));
                outEntity.addPart("pickcity", new StringBody(p.PUcity, Charset.forName("UTF-8")));
                outEntity.addPart("pickstate", new StringBody(p.PUstate, Charset.forName("UTF-8")));
                outEntity.addPart("pickzip", new StringBody(p.PUZip, Charset.forName("UTF-8")));
                outEntity.addPart("pickcountry", new StringBody(p.PUcountry, Charset.forName("UTF-8")));
                outEntity.addPart("droplat", new StringBody(Double.toString(p.DOlat), Charset.forName("UTF-8")));
                outEntity.addPart("droplong", new StringBody(Double.toString(p.DOlong), Charset.forName("UTF-8")));
                outEntity.addPart("dropaddress", new StringBody(p.DOaddress, Charset.forName("UTF-8")));
                outEntity.addPart("dropcity", new StringBody(p.DOcity, Charset.forName("UTF-8")));
                outEntity.addPart("dropstate", new StringBody(p.DOstate, Charset.forName("UTF-8")));
                outEntity.addPart("dropzip", new StringBody(p.DOZip, Charset.forName("UTF-8")));
                outEntity.addPart("dropcountry", new StringBody(p.DOcountry, Charset.forName("UTF-8")));
                outEntity.addPart("bwallrequest", new StringBody(bPutOnWall.toString(), Charset.forName("UTF-8")));
                outEntity.addPart("vehtype", new StringBody(Integer.toString(p.vehTypeID), Charset.forName("UTF-8")));
                outEntity.addPart("companyid", new StringBody(Integer.toString(p.companyID), Charset.forName("UTF-8")));
                outEntity.addPart("vehno", new StringBody(p.vehNo, Charset.forName("UTF-8")));
                outEntity.addPart("tripcost", new StringBody(p.estimatedCost, Charset.forName("UTF-8")));
                outEntity.addPart("tripdistance", new StringBody(Integer.toString(p.estimatedDistance), Charset.forName("UTF-8")));
                outEntity.addPart("tripduration", new StringBody(p.estimatedDuration, Charset.forName("UTF-8")));
                outEntity.addPart("paymenttype", new StringBody(p.PaymentType, Charset.forName("UTF-8")));
                outEntity.addPart("userccprofileuniqueid", new StringBody(p.CreditCardID, Charset.forName("UTF-8")));
                outEntity.addPart("promocode", new StringBody(p.promoCode, Charset.forName("UTF-8")));
                outEntity.addPart("tip", new StringBody(p.tip, Charset.forName("UTF-8")));
                outEntity.addPart("pickupperson", new StringBody(p.callbackName, Charset.forName("UTF-8")));
                outEntity.addPart("callbacknumber", new StringBody(p.callbackNumber, Charset.forName("UTF-8")));
                outEntity.addPart("extrainfo", new StringBody(p.pickNotes + "\n" + p.dropNotes + "\n" + p.otherInfo, Charset.forName("UTF-8")));
                outEntity.addPart("noofpassengers", new StringBody(Integer.toString(p.passengerCount), Charset.forName("UTF-8")));
                outEntity.addPart("noofkids", new StringBody(Integer.toString(p.childrenCount), Charset.forName("UTF-8")));
                outEntity.addPart("noofluggage", new StringBody(Integer.toString(p.bagsCount), Charset.forName("UTF-8")));
                outEntity.addPart("bisexclusive", new StringBody(Boolean.toString(p.exclusiveRide), Charset.forName("UTF-8")));
                outEntity.addPart("classofserviceid", new StringBody(Integer.toString(p.classofserviceid), Charset.forName("UTF-8")));

                if (p.tripType.equalsIgnoreCase("FUT"))
                    //inFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
                    outEntity.addPart("pickdatetime", new StringBody(inFormat1.format(p.PUDateTime.getTime()), Charset.forName("UTF-8")));

                if (p.PaymentType.equals("cash") || p.signatureUrl.length() == 0)
                    outEntity.addPart("signature", new StringBody(""));
                else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    options.inPurgeable = true;

                    Bitmap bm = BitmapFactory.decodeFile(p.signatureUrl, options);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 40, baos);
                    // generate base64 string of image
                    String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                    outEntity.addPart("signature", new StringBody(encodedImage));
                }

                ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
                BookingApplication.apiCalled = APIs.MAKERESERVATION;
                registerTask.execute(outEntity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /*----------------------------------------------------------updatePaymentInfo()----------------------------------------------------------------------------*/
    public static void updatePaymentInfo(Trip p, CallbackResponseListener bookingResponseListener) {

        currentCallbackListener = bookingResponseListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("serviceid", new StringBody(p.iServiceID, Charset.forName("UTF-8")));
            outEntity.addPart("paymenttype", new StringBody(p.PaymentType, Charset.forName("UTF-8")));
            outEntity.addPart("userccprofileuniqueid", new StringBody(p.CreditCardID, Charset.forName("UTF-8")));

            if (p.PaymentType.equals("cash") || p.signatureUrl.length() == 0)
                outEntity.addPart("signature", new StringBody(""));
            else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.inPurgeable = true;

                Bitmap bm = BitmapFactory.decodeFile(p.signatureUrl, options);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 40, baos);
                // generate base64 string of image
                String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                outEntity.addPart("signature", new StringBody(encodedImage));
            }

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.UPDATEPAYMENTINFO;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------signatureUpload()----------------------------------------------------------------------------*/
    public static void signatureUpload(String confirmNumber, String signatureUrl, CallbackResponseListener requestStatusListener) {

        currentCallbackListener = requestStatusListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("confirmationno", new StringBody(confirmNumber, Charset.forName("UTF-8")));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inPurgeable = true;

            Bitmap bm = BitmapFactory.decodeFile(signatureUrl, options);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 40, baos);
            // generate base64 string of image
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            outEntity.addPart("signature", new StringBody(encodedImage));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.SIGNATUREUPLOAD;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------getRequestStatus()----------------------------------------------------------------------------*/
    public static void getRequestStatus(String iServiceID, Boolean isLastCall, CallbackResponseListener requestStatusListener) {

        currentCallbackListener = requestStatusListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("serviceid", new StringBody(iServiceID, Charset.forName("UTF-8")));
            outEntity.addPart("bLastCall", new StringBody(Boolean.toString(isLastCall), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.GETREQUESTSTATUS;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------sendNoShowResponse()----------------------------------------------------------------------------*/
    public static void sendNoShowResponse(String iServiceID, Boolean isapproved, CallbackResponseListener requestStatusListener) {

        currentCallbackListener = requestStatusListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("serviceid", new StringBody(iServiceID, Charset.forName("UTF-8")));
            outEntity.addPart("isapproved", new StringBody(Boolean.toString(isapproved), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.SendNoShowResponse;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------putOnWall()----------------------------------------------------------------------------*/
    public static void putOnWall(String iServiceID, CallbackResponseListener putOnWallListener, Boolean putOnWall) {

        currentCallbackListener = putOnWallListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("serviceid", new StringBody(iServiceID, Charset.forName("UTF-8")));
            outEntity.addPart("bwallrequest", new StringBody(putOnWall.toString(), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.PUTTRIPONWALL;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------addUpdateFavorite()----------------------------------------------------------------------------*/
    public static void addUpdateFavorite(Addresses myFav) {
        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            if (myFav.favId > 0)
                outEntity.addPart("id", new StringBody(Integer.toString(myFav.favId), Charset.forName("UTF-8")));
            outEntity.addPart("address", new StringBody(myFav.caption, Charset.forName("UTF-8")));
            outEntity.addPart("streetaddress", new StringBody(myFav.address, Charset.forName("UTF-8")));
            outEntity.addPart("city", new StringBody(myFav.city, Charset.forName("UTF-8")));
            outEntity.addPart("zip", new StringBody(myFav.zip, Charset.forName("UTF-8")));
            outEntity.addPart("state", new StringBody(myFav.state, Charset.forName("UTF-8")));
            outEntity.addPart("country", new StringBody(myFav.country, Charset.forName("UTF-8")));
            outEntity.addPart("latitude", new StringBody(Double.toString(myFav.latlong.latitude), Charset.forName("UTF-8")));
            outEntity.addPart("longitude", new StringBody(Double.toString(myFav.latlong.longitude), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.ADDUPDATEFAVORITE;

            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------removeFavorite()----------------------------------------------------------------------------*/
    public static void removeFavorite(Addresses myFav, CallbackResponseListener requestStatusListener) {

        currentCallbackListener = requestStatusListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {
            outEntity.addPart("id", new StringBody(Integer.toString(myFav.favId), Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.REMOVEFAVORITE;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------cancelTrip()----------------------------------------------------------------------------*/
    public static void cancelTrip(String confirmNumber, String cancelReason, String serviceid, String requesttype, CallbackResponseListener requestStatusListener) {

        currentCallbackListener = requestStatusListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {

            outEntity.addPart("serviceid", new StringBody(serviceid, Charset.forName("UTF-8")));
            outEntity.addPart("confirmationno", new StringBody(confirmNumber, Charset.forName("UTF-8")));
            outEntity.addPart("cancelreason", new StringBody(cancelReason, Charset.forName("UTF-8")));
            outEntity.addPart("requesttype", new StringBody(requesttype, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.CANCELTRIP;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*-----------------------------------------------------getDeviceName()------------------------------------------------------------------------*/
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer))
            return model;
        else
            return manufacturer + " " + model;
    }

    /*--------------------------------------------------getApplicationName()------------------------------------------------------------------------*/
    public static String getApplicationName(Context context) {
        return callerContext.getApplicationInfo().nonLocalizedLabel.toString();
    }

    /*------------------------------------------------------getDensityName()------------------------------------------------------------------------*/
    public static String displayDensityName() {
        float density = callerContext.getResources().getDisplayMetrics().density;
        if (density >= 4.0)
            return "xxxhdpi";
        if (density >= 3.0)
            return "xxhdpi";
        if (density >= 2.0)
            return "xhdpi";
        if (density >= 1.5)
            return "hdpi";
        if (density >= 1.0)
            return "mdpi";
        return "ldpi";
    }

    /*------------------------------------------------------setMyTheme()----------------------------------------------------------------------------*/
    public static void setMyTheme(Context callerActivity) {

        String myTheme = userInfoPrefs.getString("ColorScheme", "Yellow");

        if (myTheme.equalsIgnoreCase("Red")) {
            theme_color = R.color.mars_red;
            font_color = R.color.black;
            button_Background = R.drawable.button_red;
            textView_Background = R.drawable.mars_text_view_red;
            gray_textView_Background = R.drawable.mars_text_view_gray_red;
            callerActivity.setTheme(R.style.MarsRed);
        } else if (myTheme.equalsIgnoreCase("Blue")) {
            theme_color = R.color.mars_blue;
            font_color = R.color.white;
            button_Background = R.drawable.marsblue_btn_default_holo_light;
            textView_Background = R.drawable.mars_text_view_blue;
            gray_textView_Background = R.drawable.mars_text_view_gray_blue;
            callerActivity.setTheme(R.style.MarsBlue);
        } else if (myTheme.equalsIgnoreCase("Green")) {
            theme_color = R.color.mars_green;
            font_color = R.color.black;
            button_Background = R.drawable.marsgreen_btn_default_holo_light;
            textView_Background = R.drawable.mars_text_view_green;
            gray_textView_Background = R.drawable.mars_text_view_gray_green;
            callerActivity.setTheme(R.style.MarsGreen);
        } else if (myTheme.equalsIgnoreCase("Yellow")) {
            theme_color = R.color.mars_yellow;
            font_color = R.color.black;
            button_Background = R.drawable.button_yellow;
            textView_Background = R.drawable.mars_text_view_yellow;
            gray_textView_Background = R.drawable.mars_text_view_gray_yellow;
            callerActivity.setTheme(R.style.MarsYellow);
        } else if (myTheme.equalsIgnoreCase("Purple")) {
            theme_color = R.color.mars_purple;
            font_color = R.color.black;
            button_Background = R.drawable.marspurple_btn_default_holo_light;
            textView_Background = R.drawable.mars_text_view_purple;
            gray_textView_Background = R.drawable.mars_text_view_gray_purple;
            callerActivity.setTheme(R.style.MarsPurple);
        } else if (myTheme.equalsIgnoreCase("Cyan")) {
            theme_color = R.color.mars_cyan;
            font_color = R.color.black;
            button_Background = R.drawable.marscyan_btn_default_holo_light;
            textView_Background = R.drawable.mars_text_view_cyan;
            gray_textView_Background = R.drawable.mars_text_view_gray_cyan;
            callerActivity.setTheme(R.style.MarsCyan);
        }
    }

    /*------------------------------------------------------setMyLanguage()----------------------------------------------------------------------------*/
    public static void setMyLanguage(String lang) {
        try {
            String languageToLoad = lang; // your language
            userInfoPrefs.edit().putString("lang", lang).commit();
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            callerContext.getResources().updateConfiguration(config, callerContext.getResources().getDisplayMetrics());
        } catch (Exception e) {
            showCustomToast(0, e.getLocalizedMessage(), true);
        }
    }

    /*------------------------------------------------------getFavorites()----------------------------------------------------------------------------*/
    public static void getFavorites() {
        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        ITCWebService favoriteTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.GETFAVORITES;
        favoriteTask.execute(outEntity);
    }

    /*------------------------------------------------------getCCProfiles()----------------------------------------------------------------------------*/
    public static void getCCProfiles(CallbackResponseListener getCCProfileListener) {

        currentCallbackListener = getCCProfileListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        ITCWebService getTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.GETCCPROFILES;
        getTask.execute(outEntity);
    }

    /*------------------------------------------------------createCCSession()----------------------------------------------------------------------------*/
    public static void createCCSession(CallbackResponseListener createCCSessionListener) {

        currentCallbackListener = createCCSessionListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        ITCWebService sessionTask = (new BookingApplication()).new ITCWebService();
        BookingApplication.apiCalled = APIs.CREATECCSESSION;
        sessionTask.execute(outEntity);
    }

    /*------------------------------------------------------checkCCSession()----------------------------------------------------------------------------*/
    public static void checkCCSession(String sessionid, CallbackResponseListener checkCCSessionListener) {

        currentCallbackListener = checkCCSessionListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("waittime", new StringBody("0", Charset.forName("UTF-8")));
            outEntity.addPart("sessionid", new StringBody(sessionid, Charset.forName("UTF-8")));

            ITCWebService checkTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.CHECKCCSESSION;
            checkTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------RemoveUserCCProfile()----------------------------------------------------------------------------*/
    public static void RemoveUserCCProfile(String uniqueID, CallbackResponseListener removeCCListener) {

        currentCallbackListener = removeCCListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("uniqueid", new StringBody(uniqueID, Charset.forName("UTF-8")));

            ITCWebService removeTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.REMOVECCPROFILE;
            removeTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------GetEstimatedFare()----------------------------------------------------------------------------*/
    public static void getFareInfo(Trip p, CallbackResponseListener removeCCListener) {

        currentCallbackListener = removeCCListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("tripdistance", new StringBody(Integer.toString(p.estimatedDistance), Charset.forName("UTF-8")));
            outEntity.addPart("distanceunit", new StringBody("meters", Charset.forName("UTF-8")));
            outEntity.addPart("tripduration", new StringBody(p.estimatedDuration, Charset.forName("UTF-8")));
            outEntity.addPart("durationunit", new StringBody("seconds", Charset.forName("UTF-8")));
            outEntity.addPart("picklat", new StringBody(Double.toString(p.PUlat), Charset.forName("UTF-8")));
            outEntity.addPart("picklong", new StringBody(Double.toString(p.PUlong), Charset.forName("UTF-8")));
            outEntity.addPart("pickaddress", new StringBody(p.PUaddress, Charset.forName("UTF-8")));
            outEntity.addPart("pickcity", new StringBody(p.PUcity, Charset.forName("UTF-8")));
            outEntity.addPart("pickstate", new StringBody(p.PUstate, Charset.forName("UTF-8")));
            outEntity.addPart("pickzip", new StringBody(p.PUZip, Charset.forName("UTF-8")));
            outEntity.addPart("pickcountry", new StringBody(p.PUcountry, Charset.forName("UTF-8")));
            outEntity.addPart("droplat", new StringBody(Double.toString(p.DOlat), Charset.forName("UTF-8")));
            outEntity.addPart("droplong", new StringBody(Double.toString(p.DOlong), Charset.forName("UTF-8")));
            outEntity.addPart("dropaddress", new StringBody(p.DOaddress, Charset.forName("UTF-8")));
            outEntity.addPart("dropcity", new StringBody(p.DOcity, Charset.forName("UTF-8")));
            outEntity.addPart("dropstate", new StringBody(p.DOstate, Charset.forName("UTF-8")));
            outEntity.addPart("dropzip", new StringBody(p.DOZip, Charset.forName("UTF-8")));
            outEntity.addPart("dropcountry", new StringBody(p.DOcountry, Charset.forName("UTF-8")));
            outEntity.addPart("companyid", new StringBody(Integer.toString(p.companyID), Charset.forName("UTF-8")));
            outEntity.addPart("vehtype", new StringBody(Integer.toString(p.vehTypeID), Charset.forName("UTF-8")));
            outEntity.addPart("tripType", new StringBody(p.tripType, Charset.forName("UTF-8")));
            outEntity.addPart("promocode", new StringBody(p.promoCode, Charset.forName("UTF-8")));
            outEntity.addPart("tip", new StringBody(p.tip, Charset.forName("UTF-8")));
            outEntity.addPart("noofpassengers", new StringBody(Integer.toString(p.passengerCount), Charset.forName("UTF-8")));
            outEntity.addPart("noofkids", new StringBody(Integer.toString(p.childrenCount), Charset.forName("UTF-8")));
            outEntity.addPart("noofluggage", new StringBody(Integer.toString(p.bagsCount), Charset.forName("UTF-8")));
            outEntity.addPart("bisexclusive", new StringBody(Boolean.toString(p.exclusiveRide), Charset.forName("UTF-8")));
            outEntity.addPart("classofserviceid", new StringBody(Integer.toString(p.classofserviceid), Charset.forName("UTF-8")));

            ITCWebService removeTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.GETFAREINFO;
            removeTask.execute(outEntity);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------PostRating()----------------------------------------------------------------------------*/
    public static void submitRating(String serviceid, String serviceQuality, String cleanliness, String etiquettes, String taxiOntime, String comments, CallbackResponseListener requestStatusListener) {
        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
        try {

            outEntity.addPart("serviceid", new StringBody(serviceid, Charset.forName("UTF-8")));
            outEntity.addPart("servicequality", new StringBody(serviceQuality, Charset.forName("UTF-8")));
            outEntity.addPart("cleanliness", new StringBody(cleanliness, Charset.forName("UTF-8")));
            outEntity.addPart("etiquette", new StringBody(etiquettes, Charset.forName("UTF-8")));
            outEntity.addPart("taxilate", new StringBody(taxiOntime, Charset.forName("UTF-8")));
            outEntity.addPart("comments", new StringBody(comments, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.SUBMITRATING;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------performForgotPassword-----------------------------------------------------------------------------*/
    public static void performForgotPassword(String phNo, CallbackResponseListener forgotPasswordListener) {

        currentCallbackListener = forgotPasswordListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("phonenumber", new StringBody(phNo, Charset.forName("UTF-8")));

            ITCWebService forgotTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.FORGOTPASSWORD;
            forgotTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------performResetPassword-----------------------------------------------------------------------------*/
    public static void performResetPassword(String phNo, String secretAnswer, String newPassword, CallbackResponseListener resetPasswordListener) {

        currentCallbackListener = resetPasswordListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("phonenumber", new StringBody(phNo, Charset.forName("UTF-8")));
            outEntity.addPart("secretanswer", new StringBody(secretAnswer, Charset.forName("UTF-8")));
            outEntity.addPart("newpassword", new StringBody(newPassword, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.RESETPASSWORD;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*-----------------------------------------getNearbyVehicles----------------------------------------------------------------------------------------*/
    public static void getNearbyVehicles(Address pickAddress, Address dropAddress, Boolean bVehiclesOnly) {
        if (pickAddress != null) {


            try {

                currentCallbackListener = (CallbackResponseListener) callerContext;

                MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

                outEntity.addPart("Latitude", new StringBody(Double.toString(pickAddress.getLatitude())));
                outEntity.addPart("Longitude", new StringBody(Double.toString(pickAddress.getLongitude())));
                outEntity.addPart("zip", new StringBody(pickAddress.getPostalCode() == null ? "" : pickAddress.getPostalCode(), Charset.forName("UTF-8")));
                outEntity.addPart("city", new StringBody(pickAddress.getLocality() == null ? "" : pickAddress.getLocality(), Charset.forName("UTF-8")));
                outEntity.addPart("state", new StringBody(pickAddress.getAdminArea() == null ? "" : pickAddress.getAdminArea(), Charset.forName("UTF-8")));
                outEntity.addPart("country", new StringBody(pickAddress.getCountryCode() == null ? "" : pickAddress.getCountryCode(), Charset.forName("UTF-8")));
                outEntity.addPart("bVehiclesOnly", new StringBody(Boolean.toString(bVehiclesOnly), Charset.forName("UTF-8")));

                if (dropAddress != null) {
                    outEntity.addPart("droplat", new StringBody(Double.toString(dropAddress.getLatitude())));
                    outEntity.addPart("droplong", new StringBody(Double.toString(dropAddress.getLongitude())));
                    outEntity.addPart("dropzip", new StringBody(dropAddress.getPostalCode() == null ? "" : dropAddress.getPostalCode(), Charset.forName("UTF-8")));
                    outEntity.addPart("dropcity", new StringBody(dropAddress.getLocality() == null ? "" : dropAddress.getLocality(), Charset.forName("UTF-8")));
                    outEntity.addPart("dropstate", new StringBody(dropAddress.getAdminArea() == null ? "" : dropAddress.getAdminArea(), Charset.forName("UTF-8")));
                    outEntity.addPart("dropcountry", new StringBody(dropAddress.getCountryCode() == null ? "" : dropAddress.getCountryCode(), Charset.forName("UTF-8")));
                }

                ITCWebService getVehiclesTask = (new BookingApplication()).new ITCWebService();
                BookingApplication.apiCalled = APIs.GETNEARBYVEHICLES;
                getVehiclesTask.execute(outEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /*------------------------------------------------------topUpBalance-----------------------------------------------------------------------------*/
    public static void topUpBalance(String cardNo, String phNo, String companyId, CallbackResponseListener topUpListener) {

        currentCallbackListener = topUpListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {
            outEntity.addPart("cardno", new StringBody(cardNo, Charset.forName("UTF-8")));
            outEntity.addPart("drivernoorcustomerphone", new StringBody(phNo, Charset.forName("UTF-8")));
            outEntity.addPart("persontype", new StringBody("0", Charset.forName("UTF-8")));
            outEntity.addPart("companyid", new StringBody(companyId, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.TOPUPBALANCE;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------topUpBalanceByCC-----------------------------------------------------------------------------*/
    public static void topUpBalanceByCC(String uniqueId, String companyId, String amount, CallbackResponseListener topUpByCCListener) {

        currentCallbackListener = topUpByCCListener;

        MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

        try {

            outEntity.addPart("uniqueid", new StringBody(uniqueId, Charset.forName("UTF-8")));
            outEntity.addPart("itspid", new StringBody(companyId, Charset.forName("UTF-8")));
            outEntity.addPart("amount", new StringBody(amount, Charset.forName("UTF-8")));

            ITCWebService registerTask = (new BookingApplication()).new ITCWebService();
            BookingApplication.apiCalled = APIs.TOPUPBALANCEBYCC;
            registerTask.execute(outEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------getDateTime-----------------------------------------------------------------------------*/
    public static String getDateTime(String dateTimeFormat) {

        SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat, Locale.US);

        Calendar c = Calendar.getInstance();
        return format.format(c.getTime());

    }

    /*------------------------------------------------------formatDateTime-----------------------------------------------------------------------------*/
    public static String formatDateTime(Calendar c, String dateTimeFormat) {

        SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat, Locale.US);
        return format.format(c.getTime());

    }

    /*-------------------------------------------------------------getVehicleType-----------------------------------------------------------------------------*/
    public String getVehicleType(int i) {
        switch (i % 7) {
            case 1:
                return "SEDAN";
            case 2:
                return "SUV";
            case 3:
                return "LIMO";
            case 4:
                return "BlackCab";
            case 5:
                return "LONDON";
            case 6:
                return "LIFT";
        }
        return null;
    }

    /*----------------------------------------------------------------onCreate--------------------------------------------------------------------------------*/
    @Override
    public void onCreate() {

        try {
            userInfoPrefs = getSharedPreferences("CUSTOMER_DATA_PREF", MODE_PRIVATE);
            packageName = getApplicationContext().getPackageName();
            tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1)
                display.getSize(size);
            else {
                screenWidth = display.getWidth();
                screenHeight = display.getHeight();
            }
            screenWidth = size.x;
            screenHeight = size.y;
            appID = userInfoPrefs.getString("appID", packageName.substring(packageName.lastIndexOf('.') + 1) + "_android");
            SERVER_IP = userInfoPrefs.getString("ServerIP", SERVER_IP).length() > 7 ? userInfoPrefs.getString("ServerIP", SERVER_IP) : SERVER_IP;
            appVersion = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0).versionName;

            synchronized (userInfoPrefs) {

                colorScheme = userInfoPrefs.getString("ColorScheme", "Red");
                receiverEmail = userInfoPrefs.getString("ReceiverEmail", "");
                senderEmailPassword = userInfoPrefs.getString("SenderEmailPass", "");
                selected_lang = userInfoPrefs.getString("lang", "en");
                password = userInfoPrefs.getString("pin", "");
                code = userInfoPrefs.getString("code", "0");
                regID = userInfoPrefs.getString("GCM_REG_ID", "");
                registeredVersionCode = userInfoPrefs.getInt("APP_VERSION_CODE", Integer.MIN_VALUE);
                phoneNumberFetched = tMgr.getLine1Number();
                if (userInfoPrefs.getString("phone", "").equalsIgnoreCase("")) {
                    if ((tMgr.getLine1Number() == null) || tMgr.getLine1Number().equalsIgnoreCase("Unknown") || (tMgr.getLine1Number().length() < 10))
                        phoneNumber = "";
                    else
                        phoneNumber = phoneNumberFetched;

                    //if (!phoneNumber.startsWith("+"))
                    //phoneNumber = String.format("+966-%s-%s%s", phoneNumber.substring(phoneNumber.length() - 10, phoneNumber.length() - 7), phoneNumber.substring(phoneNumber.length() - 7, phoneNumber.length() - 4), phoneNumber.substring(phoneNumber.length() - 4, phoneNumber.length()));

                } else
                    phoneNumber = userInfoPrefs.getString("phone", "");
            }//synchronized

            possibleEmail = new ArrayList<String>();
            Account[] accounts = ((AccountManager) getApplicationContext().getSystemService(Context.ACCOUNT_SERVICE)).getAccountsByType("com.google");
            for (Account account : accounts)
                if (isEmailValid(account.name))
                    possibleEmail.add(account.name);

            wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            isNetworkConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            dbHelper = new DatabaseHelper(this);
            db = dbHelper.getWritableDatabase();
            imagedownloader = new ImageLoader(getApplicationContext());

            //states.put("Alabama", "AL");
            //states.put("Alaska", "AK");
            //states.put("Alberta", "AB");
            //states.put("American Samoa", "AS");
            //states.put("Arizona", "AZ");
            //states.put("Arkansas", "AR");
            //states.put("Armed Forces (AE)", "AE");
            //states.put("Armed Forces Americas", "AA");
            //states.put("Armed Forces Pacific", "AP");
            //states.put("British Columbia", "BC");
            //states.put("California", "CA");
            //states.put("Colorado", "CO");
            //states.put("Connecticut", "CT");
            //states.put("Delaware", "DE");
            //states.put("District Of Columbia", "DC");
            //states.put("Florida", "FL");
            //states.put("Georgia", "GA");
            //states.put("Guam", "GU");
            //states.put("Hawaii", "HI");
            //states.put("Idaho", "ID");
            //states.put("Illinois", "IL");
            //states.put("Indiana", "IN");
            //states.put("Iowa", "IA");
            //states.put("Kansas", "KS");
            //states.put("Kentucky", "KY");
            //states.put("Louisiana", "LA");
            //states.put("Maine", "ME");
            //states.put("Manitoba", "MB");
            //states.put("Maryland", "MD");
            //states.put("Massachusetts", "MA");
            //states.put("Michigan", "MI");
            //states.put("Minnesota", "MN");
            //states.put("Mississippi", "MS");
            //states.put("Missouri", "MO");
            //states.put("Montana", "MT");
            //states.put("Nebraska", "NE");
            //states.put("Nevada", "NV");
            //states.put("New Brunswick", "NB");
            //states.put("New Hampshire", "NH");
            //states.put("New Jersey", "NJ");
            //states.put("New Mexico", "NM");
            //states.put("New York", "NY");
            //states.put("Newfoundland", "NF");
            //states.put("North Carolina", "NC");
            //states.put("North Dakota", "ND");
            //states.put("Northwest Territories", "NT");
            //states.put("Nova Scotia", "NS");
            //states.put("Nunavut", "NU");
            //states.put("Ohio", "OH");
            //states.put("Oklahoma", "OK");
            //states.put("Ontario", "ON");
            //states.put("Oregon", "OR");
            //states.put("Pennsylvania", "PA");
            //states.put("Prince Edward Island", "PE");
            //states.put("Puerto Rico", "PR");
            //states.put("Quebec", "PQ");
            //states.put("Rhode Island", "RI");
            //states.put("Saskatchewan", "SK");
            //states.put("South Carolina", "SC");
            //states.put("South Dakota", "SD");
            //states.put("Tennessee", "TN");
            //states.put("Texas", "TX");
            //states.put("Utah", "UT");
            //states.put("Vermont", "VT");
            //states.put("Virgin Islands", "VI");
            //states.put("Virginia", "VA");
            //states.put("Washington", "WA");
            //states.put("West Virginia", "WV");
            //states.put("Wisconsin", "WI");
            //states.put("Wyoming", "WY");
            //states.put("Yukon Territory", "YT");

        } catch (Exception e) {
            showCustomToast(0, e.getLocalizedMessage(), true);
            Log.e("Navigator OnCreate()", e.toString());
        }
    }// onCreate

    /*---------------------------------------------------------CustomDialog-----------------------------------------------------------------------------*/
    public static class CustomDialog extends Dialog {
        public CustomDialog(Context context, View view) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(view);
            getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);

        }
    }

    public static class CODES {
        public static final int EXIT = 0;
        public static final int BOOKING_SUCCESS = 1;
        public static final int RESERVATION_SUCCESS = 2;
        public static final int WEB_ACTIVITY = 3;
        public static final int PAYMENT_OPTION_ACTIVITY = 4;
        public static final int SIGNATURE_REQUIRED = 5;
        public static final int SOFTWARE_UPDATE = 6;
        public static final int CC_ID_REQUIRED = 7;

        public static final int BOOKING_FAILED = -1;
        public static final int NOSHOWREQUESTED = -2;
        public static final int ACTIVATION_REQUIRED = -3;
        public static final int TRIP_SERVED = -4;

        public static final int SEARCHADDRESS = 96;
        public static final int START_MAPS = 97;
        public static final int WIFI_TOGGLE = 98;
        public static final int GPS_TOGGLE = 99;
        public static final int NONE = 100;
        public static final int PPV_RESPONSE = 101;
        public static final int BLACKLIST_LIMIT_REACHED = -100;
        public static final int RESERVATION_MULTIPLE_REQUESTS = -200;
        public static final int RESERVATION_NOVEHICLE = -300;
    }

    public static class APIs {
        public static final int HANDSHAKE = 0;
        public static final int REGISTER = 1;
        public static final int LOGIN = 2;
        public static final int GETFAREINFO = 3;
        public static final int GETNEARBYVEHICLES = 4;
        public static final int PREACTIVATE = 5;
        public static final int POSTACTIVATE = 6;
        public static final int UPDATEPROFILE = 7;
        public static final int FETCHPROFILE = 8;
        public static final int ADDUPDATEFAVORITE = 9;
        public static final int REMOVEFAVORITE = 10;
        public static final int MAKERESERVATION = 11;
        public static final int GETCUSTOMERRIDES = 12;
        public static final int GETREQUESTSTATUS = 13;
        public static final int PUTTRIPONWALL = 14;
        public static final int GETFAVORITES = 15;
        public static final int CANCELTRIP = 16;
        public static final int SUBMITRATING = 17;
        public static final int FORGOTPASSWORD = 18;
        public static final int RESETPASSWORD = 19;
        public static final int GETCCPROFILES = 20;
        public static final int CREATECCSESSION = 21;
        public static final int CHECKCCSESSION = 22;
        public static final int REMOVECCPROFILE = 23;
        public static final int SIGNATUREUPLOAD = 24;
        public static final int UPDATEPAYMENTINFO = 25;
        public static final int RegisterForPushMessages = 26;
        public static final int GETUSERPROMOTIONDETAIL = 27;
        public static final int GeoCoderResponse = 28;
        public static final int TOPUPBALANCE = 29;
        public static final int TOPUPBALANCEBYCC = 30;
        public static final int AvailPromotion = 31;
        public static final int SendNoShowResponse = 32;
        public static final int ADDC2D = 33;

        //outload.mars.itcurves.us, 86.51.177.115:8085 (MyTaxi)  outload.mytaxi.itcurves.us:82 (mytaxi-beta)  outload.demo.itcurves.us (Demo)

        public static String GetURIFor(int api) {

            switch (api) {
                case HANDSHAKE:
                    return SERVER_IP + "/API/Account/Handshake/";
                case REGISTER:
                    return SERVER_IP + "/API/Account/Register/";
                case RegisterForPushMessages:
                    return SERVER_IP + "/API/Account/RegisterForPushMessages/";
                case LOGIN:
                    return SERVER_IP + "/API/Account/Login/";
                case GETFAREINFO:
                    return SERVER_IP + "/API/Reservation/GetFareInfo/";
                case GETNEARBYVEHICLES:
                    return SERVER_IP + "/API/Reservation/GetNearbyVehicles/";
                case PREACTIVATE:
                    return SERVER_IP + "/API/Account/PreActivation/";
                case POSTACTIVATE:
                    return SERVER_IP + "/API/Account/PostActivation/";
                case UPDATEPROFILE:
                    return SERVER_IP + "/API/Account/UpdateProfile/";
                case FETCHPROFILE:
                    return SERVER_IP + "/API/Account/GetUserInfo/";
                case ADDUPDATEFAVORITE:
                    return SERVER_IP + "/API/reservation/AddUpdateFavorite/";
                case REMOVEFAVORITE:
                    return SERVER_IP + "/API/reservation/RemoveFavorite/";
                case MAKERESERVATION:
                    return SERVER_IP + "/API/reservation/MakeReservation/";
                case CANCELTRIP:
                    return SERVER_IP + "/API/reservation/CancelRequest/";
                case GETCUSTOMERRIDES:
                    return SERVER_IP + "/API/reservation/GetCustomerRides/";
                case GETFAVORITES:
                    return SERVER_IP + "/API/reservation/GetFavorites/";
                case GETREQUESTSTATUS:
                    return SERVER_IP + "/API/reservation/GetRequestStatus/";
                case PUTTRIPONWALL:
                    return SERVER_IP + "/API/reservation/PutReservationOnWall/";
                case SUBMITRATING:
                    return SERVER_IP + "/API/reservation/RateDriverVehicle/";
                case SIGNATUREUPLOAD:
                    return SERVER_IP + "/API/reservation/SignatureUpload/";
                case UPDATEPAYMENTINFO:
                    return SERVER_IP + "/API/reservation/UpdatePaymentInfo/";
                case GETUSERPROMOTIONDETAIL:
                    return SERVER_IP + "/API/reservation/GetUserPromotionDetails/";
                case AvailPromotion:
                    return SERVER_IP + "/API/reservation/AvailPromotion/";
                case ADDC2D:
                    return SERVER_IP + "/API/reservation/AddC2DHistoryInOutLoad/";
                case SendNoShowResponse:
                    return SERVER_IP + "/API/reservation/UpdateNoShowApprovedStatus/";
                case FORGOTPASSWORD:
                    return SERVER_IP + "/API/Account/ForgotPassword/";
                case RESETPASSWORD:
                    return SERVER_IP + "/API/Account/ResetPassword/";
                case GETCCPROFILES:
                    return SERVER_IP + "/API/Payment/GetAllUserCCProfiles/";
                case CREATECCSESSION:
                    return SERVER_IP + "/API/Payment/CreateSession/";
                case CHECKCCSESSION:
                    return SERVER_IP + "/API/Payment/CheckSession/";
                case REMOVECCPROFILE:
                    return SERVER_IP + "/API/Payment/RemoveUserCCProfile/";
                case TOPUPBALANCE:
                    return SERVER_IP + "/API/PPV/TopupBalance/";
                case TOPUPBALANCEBYCC:
                    return SERVER_IP + "/Api/Payment/TopUpBalanceFromCC/";
                default:
                    return "http://";
            }
        }

        public static String getApiName(int api) {

            switch (api) {
                case HANDSHAKE:
                    return "Handshake";
                case REGISTER:
                    return "Register";
                case RegisterForPushMessages:
                    return "RegisterForPushMessages";
                case LOGIN:
                    return "Login";
                case GETFAREINFO:
                    return "GetFareInfo";
                case GETNEARBYVEHICLES:
                    return "GetNearbyVehicles";
                case PREACTIVATE:
                    return "PreActivation";
                case POSTACTIVATE:
                    return "PostActivation";
                case UPDATEPROFILE:
                    return "UpdateProfile";
                case FETCHPROFILE:
                    return "GetUserInfo";
                case ADDUPDATEFAVORITE:
                    return "AddUpdateFavorite";
                case REMOVEFAVORITE:
                    return "RemoveFavorite";
                case MAKERESERVATION:
                    return "MakeReservation";
                case CANCELTRIP:
                    return "CancelRequest";
                case GETCUSTOMERRIDES:
                    return "GetCustomerRides";
                case GETFAVORITES:
                    return "GetFavorites";
                case GETREQUESTSTATUS:
                    return "GetRequestStatus";
                case PUTTRIPONWALL:
                    return "putReservationOnWall";
                case SUBMITRATING:
                    return "RateDriverVehicle";
                case SIGNATUREUPLOAD:
                    return "SignatureUpload";
                case UPDATEPAYMENTINFO:
                    return "UpdatePaymentInfo";
                case GETUSERPROMOTIONDETAIL:
                    return "GetUserPromotionDetails";
                case AvailPromotion:
                    return "AvailPromotion";
                case ADDC2D:
                    return "AddC2DHistoryInOutLoad";
                case SendNoShowResponse:
                    return "UpdateNoShowApprovedStatus";
                case FORGOTPASSWORD:
                    return "ForgotPassword";
                case RESETPASSWORD:
                    return "ResetPassword";
                case GETCCPROFILES:
                    return "GetAllUserCCProfiles";
                case CREATECCSESSION:
                    return "CreateSession";
                case CHECKCCSESSION:
                    return "CheckSession";
                case REMOVECCPROFILE:
                    return "RemoveUserCCProfile";
                case TOPUPBALANCE:
                    return "TopupBalance";
                case TOPUPBALANCEBYCC:
                    return "TopUpBalanceFromCC";
                default:
                    return "http://";
            }
        }

        public static int GetApiCalled(String apiName) {

            if (apiName.equalsIgnoreCase("HandshakeResponse"))
                return HANDSHAKE;
            else if (apiName.equalsIgnoreCase("RegisterResponse"))
                return REGISTER;
            else if (apiName.equalsIgnoreCase("RegisterForPushMessagesResponse"))
                return RegisterForPushMessages;
            else if (apiName.equalsIgnoreCase("LoginResponse"))
                return LOGIN;
            else if (apiName.equalsIgnoreCase("GetNearbyVehiclesResponse"))
                return GETNEARBYVEHICLES;
            else if (apiName.equalsIgnoreCase("PreActivationResponse"))
                return PREACTIVATE;
            else if (apiName.equalsIgnoreCase("PostActivationResponse"))
                return POSTACTIVATE;
            else if (apiName.equalsIgnoreCase("UpdateProfileResponse"))
                return UPDATEPROFILE;
            else if (apiName.equalsIgnoreCase("GetUserInfoResponse"))
                return FETCHPROFILE;
            else if (apiName.equalsIgnoreCase("AddUpdateFavoriteResponse"))
                return ADDUPDATEFAVORITE;
            else if (apiName.equalsIgnoreCase("RemoveFavoriteResponse"))
                return REMOVEFAVORITE;
            else if (apiName.equalsIgnoreCase("MakeReservationResponse"))
                return MAKERESERVATION;
            else if (apiName.equalsIgnoreCase("CancelRequestResponse"))
                return CANCELTRIP;
            else if (apiName.equalsIgnoreCase("GetCustomerRidesResponse"))
                return GETCUSTOMERRIDES;
            else if (apiName.equalsIgnoreCase("GetFavoritesResponse"))
                return GETFAVORITES;
            else if (apiName.equalsIgnoreCase("GetRequestStatusResponse"))
                return GETREQUESTSTATUS;
            else if (apiName.equalsIgnoreCase("putReservationOnWallResponse"))
                return PUTTRIPONWALL;
            else if (apiName.equalsIgnoreCase("RateDriverVehicleResponse"))
                return SUBMITRATING;
            else if (apiName.equalsIgnoreCase("SignatureUploadResponse"))
                return SIGNATUREUPLOAD;
            else if (apiName.equalsIgnoreCase("UpdatePaymentInfoResponse"))
                return UPDATEPAYMENTINFO;
            else if (apiName.equalsIgnoreCase("GetUserPromotionDetailsResponse"))
                return GETUSERPROMOTIONDETAIL;
            else if (apiName.equalsIgnoreCase("AvailPromotionResponse"))
                return AvailPromotion;
            else if (apiName.equalsIgnoreCase("ADDC2DHistoryINOUTLoadResponse"))
                return ADDC2D;
            else if (apiName.equalsIgnoreCase("UpdateNoShowApprovedStatus"))
                return SendNoShowResponse;
            else if (apiName.equalsIgnoreCase("ForgotPasswordResponse"))
                return FORGOTPASSWORD;
            else if (apiName.equalsIgnoreCase("ResetPasswordResponse"))
                return RESETPASSWORD;
            else if (apiName.equalsIgnoreCase("GetAllUserCCProfilesResponse"))
                return GETCCPROFILES;
            else if (apiName.equalsIgnoreCase("CreateSessionResponse"))
                return CREATECCSESSION;
            else if (apiName.equalsIgnoreCase("CheckSessionResponse"))
                return CHECKCCSESSION;
            else if (apiName.equalsIgnoreCase("RemoveUserCCProfileResponse"))
                return REMOVECCPROFILE;
            else if (apiName.equalsIgnoreCase("GetFareInfoResponse"))
                return GETFAREINFO;
            else if (apiName.equalsIgnoreCase("TopupBalanceResponse"))
                return TOPUPBALANCE;
            else if (apiName.equalsIgnoreCase("TopupBalanceFromCCResponse"))
                return TOPUPBALANCEBYCC;

            return -1;

        }
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId(Context context) {
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.

        currentVersionCode = getAppVersionCode(context);
        if (registeredVersionCode != currentVersionCode) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return regID;
    }

    public static void registerGCMInBackground(String project_id) {
        SENDER_ID = project_id;
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = InstanceID.getInstance(callerContext);
                    }
                    regID = gcm.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    msg = "Device registered\nID = " + regID;

                    // Persist the regID - no need to register again.
                    userInfoPrefs.edit().putString("GCM_REG_ID", regID).putInt("APP_VERSION_CODE", currentVersionCode).putBoolean("GCMIdSentToBackend", false).apply();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    userInfoPrefs.edit().putBoolean("GCMIdSentToBackend", true).commit();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    /*-----------------------------------------------------------------------------------------------------------------------------------------------
     *-------------------------------------------------- ITCWebService AsyncTask --------------------------------------------------------------------
	 *-----------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Note: Always set BookingApplication.callerContext value before calling this AsyncTask.
	 */
    public class ITCWebService extends AsyncTask<MultipartEntity, Integer, JSONObject> {

        private JSONObject tempObj, response;
        private JSONArray jsonArray;

        @Override
        protected JSONObject doInBackground(MultipartEntity... params) throws NullPointerException {

            try {
                if (isNetworkConnected)
                    return JSONhandler.getJSONfromURL(params);
                else {
                    response = new JSONObject();
                    try {
                        response.put("Fault", true);
                        response.put("ReasonPhrase", callerContext.getResources().getString(R.string.No_Internet_Connectivity));

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    return response;
                }
            } catch (Exception e) {
                response = new JSONObject();
                try {
                    response.put("Fault", true);
                    response.put("ResponseType", "Fault");
                    if ((e instanceof IOException) || (e instanceof TimeoutException))
                        response.put("ReasonPhrase", callerContext.getResources().getString(R.string.Network_Error));
                    else
                        response.put("ReasonPhrase", ((e.getMessage() == null) || (e.getMessage().length() == 0)) ? e.toString() : e.getMessage());

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                return response;
            }
        }


        @Override
        protected void onPostExecute(JSONObject JSONResp) {
            try {
                if (customProgressDialog != null)
                    while (customProgressDialog.isShowing())
                        customProgressDialog.dismiss();
            } catch (Exception e) {

            }

            try {

                if (JSONResp != null) {
                    if (JSONResp.has("Fault") && !isMinimized)
                        showCustomToast(0, JSONResp.getString("ReasonPhrase"), true);

                    if (JSONResp.has("ResponseType"))
                        switch (APIs.GetApiCalled(JSONResp.getString("ResponseType"))) {
                            case APIs.GETNEARBYVEHICLES:
                                if (JSONResp.has("Vehicles")) {
                                    jsonArray = JSONResp.getJSONArray("Vehicles");
                                    nearByVehicles.clear();
                                    if (jsonArray.length() > 0)
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            tempObj = jsonArray.getJSONObject(i);
                                            NearbyVehicle nbv = new NearbyVehicle(tempObj.getString("VehicleColor"), new LatLng(tempObj.getDouble("Latitude"), tempObj.getDouble("Longitude")));
                                            nbv.vehMarker.snippet(Integer.toString(i));
                                            nbv.vinNumber = tempObj.getString("VinNo");
                                            nbv.avg_Rating = tempObj.getDouble("AverageRating");
                                            nbv.iTotalRatings = tempObj.getInt("TotalRatings");
                                            nbv.iVehicleID = tempObj.getInt("iVehicleID");
                                            nbv.VehicleNo = tempObj.getString("VehicleNo");
                                            nbv.iVehTypeID = tempObj.getInt("iVehTypeID");
                                            nbv.iAffiliateID = tempObj.getInt("AffiliateID");
                                            nbv.iWorkYears = tempObj.getInt("WorkYears");
                                            nbv.vehicleMake = tempObj.getString("VehicleMake");
                                            nbv.driverName = tempObj.getString("DriverName");
                                            nbv.driverPicture = tempObj.getString("DriverPicture");
                                            nbv.bHailingAllowed = tempObj.getBoolean("bHailingAllowed");
                                            nbv.PaymentType = tempObj.getInt("PaymentType");
                                            nbv.setDirection(callerContext, tempObj.has("Direction") ? tempObj.getString("Direction") : "North");
                                            nbv.ourRates = tempObj.has("RatesLink") ? tempObj.getString("RatesLink") : "";
                                            nbv.ourFleet = tempObj.has("FleetInfoLink") ? tempObj.getString("FleetInfoLink") : "";
                                            nbv.ourTerms = tempObj.has("TCLink") ? tempObj.getString("TCLink") : "";
                                            nbv.aboutUs = tempObj.has("AboutUsLink") ? tempObj.getString("AboutUsLink") : "";
                                            nbv.companyLogo = tempObj.has("BottomLogoLink") ? tempObj.getString("BottomLogoLink") : "";
                                            nbv.bPromoAllowed = tempObj.has("bPromoAllowed") ? tempObj.getBoolean("bPromoAllowed") : false;
                                            nbv.IsPassCountReq = tempObj.has("IsPassCountReq") ? tempObj.getBoolean("IsPassCountReq") : false;
                                            nbv.iClassID = tempObj.getInt("ClassID");

                                            nearByVehicles.add(nbv);
                                        }
                                        //drawNearByVehicles();
                                    else
                                        showCustomToast(R.string.No_nearbyvehicle_available, "", false);
                                }

                                if (JSONResp.has("bVehiclesOnly"))
                                    if (!JSONResp.getBoolean("bVehiclesOnly")) {
                                        if (JSONResp.has("Affiliates")) {

                                            jsonArray = JSONResp.getJSONArray("Affiliates");
                                            db.delete("Affiliates", null, null);
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tempObj = jsonArray.getJSONObject(i);

                                                ContentValues values = new ContentValues(12);
                                                values.put("AffiliateID", tempObj.getInt("AffiliateID"));
                                                values.put("AffiliateName", tempObj.getString("AffiliateName"));
                                                values.put("VehTypeID", tempObj.getInt("VehTypeID"));
                                                values.put("ClassID", tempObj.getInt("ClassID"));
                                                values.put("ClassName", tempObj.getString("ClassName"));
                                                values.put("COSVehImage", tempObj.getString("COSVehImage"));
                                                values.put("VehName", tempObj.getString("VehType"));
                                                values.put("VehTypeImage", tempObj.getString("VehicleColor"));
                                                values.put("ADU", tempObj.getDouble("DistUnit"));
                                                values.put("MinLaterHours", tempObj.getDouble("MinLaterHours"));
                                                values.put("MaxLaterHours", tempObj.getDouble("MaxLaterHours"));
                                                values.put("InitialFare", tempObj.getDouble("InitialFare"));
                                                values.put("CostPerUnit", tempObj.getDouble("CostPerUnit"));
                                                values.put("AddPassengerCost", tempObj.getDouble("AddPassengerCost"));
                                                values.put("VehRateUnit", tempObj.getString("dVehTypeRateUnit"));
                                                values.put("bQueuedAllowed", tempObj.getBoolean("bQueuedAllowed") ? 1 : 0);
                                                values.put("bShowNow", tempObj.getBoolean("bShowNow") ? 1 : 0);
                                                values.put("bShowLater", tempObj.getBoolean("bShowLater") ? 1 : 0);
                                                values.put("bHailingAllowed", tempObj.getBoolean("bHailingAllowed") ? 1 : 0);
                                                values.put("TotalWheelChairs", tempObj.getInt("TotalWheelChairs"));
                                                values.put("TotalCapacity", tempObj.getInt("TotalCapacity"));
                                                values.put("CurrencyUnit", tempObj.getString("CurrencyUnit"));
                                                values.put("OtherInfo", tempObj.getString("OtherInfo"));
                                                values.put("PaymentType", tempObj.getInt("PaymentType"));
                                                values.put("bPromoAllowed", tempObj.getBoolean("bPromoAllowed") ? 1 : 0);
                                                values.put("IsPassCountReq", tempObj.getBoolean("IsPassCountReq") ? 1 : 0);
                                                values.put("ClassAvailability", tempObj.getDouble("COSAvailability"));

                                                db.insert("Affiliates", null, values);
                                            }
                                        }
                                        if (JSONResp.has("Promotions")) {
                                            promotions.clear();
                                            jsonArray = JSONResp.getJSONArray("Promotions");
                                            if (jsonArray.length() > 0) {
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    tempObj = jsonArray.getJSONObject(i);
                                                    if (tempObj.has("PromotionDescription"))
                                                        promotions.add(new Promotion(tempObj.getString("PromoCode"), tempObj.getString("PromotionDescription"), tempObj.getString("CompanyLogo"), tempObj.getString("CompanyName"), tempObj.getInt("CompanyID")));
                                                }

                                                //showCustomToast(R.string.Promotions_Available, "", false);
                                            }
                                        }
                                    }
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.GETNEARBYVEHICLES, JSONResp, null, true);
                                else {
                                    currentCallbackListener.callbackResponseReceived(APIs.GETNEARBYVEHICLES, JSONResp, null, false);
                                }
                                break;
                            case APIs.HANDSHAKE:
                                bsendsms = JSONResp.getBoolean("SendSMS");
                                CompanyID = JSONResp.getString("CompanyID");
                                if (JSONResp.has("AppShareText"))
                                    AppShareText = JSONResp.getString("AppShareText");
                                if (JSONResp.has("SupportedPaymentMethod"))
                                    SupportedPaymentMethod = JSONResp.getString("SupportedPaymentMethod");
                                airport_hours = JSONResp.getString("MinLaterHours");
                                MaxLaterHours = JSONResp.getInt("MaxLaterHours");
                                ReferralRegistrationURL = JSONResp.getString("ReferralRegistrationURL");
                                if (JSONResp.has("bShowNearbyPlaces"))
                                    bShowNearbyPlaces = JSONResp.getBoolean("bShowNearbyPlaces");
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.HANDSHAKE, JSONResp, null, true);
                                else
                                    currentCallbackListener.callbackResponseReceived(APIs.HANDSHAKE, JSONResp, null, false);
                                break;
                            case APIs.LOGIN:
                                if (!JSONResp.has("Fault")) {
                                    userName = JSONResp.getString("userName");
                                    if (JSONResp.has("MarsID"))
                                        marsID = JSONResp.getString("MarsID");
                                    if (userName == null)
                                        userName = "";
                                    userInfoPrefs.edit().putString("pin", password).putString("UserID", JSONResp.getString("UserID")).putString("phone", phoneNumber).putString("code", code).commit();
                                    currentCallbackListener.callbackResponseReceived(APIs.LOGIN, JSONResp, null, true);
                                } else
                                    currentCallbackListener.callbackResponseReceived(APIs.LOGIN, JSONResp, null, false);
                                break;
                            case APIs.REGISTER:
                                if (!JSONResp.has("Fault")) {
                                    code = JSONResp.getString("VerificationCode");
                                    userInfoPrefs.edit().putString("UserID", JSONResp.getString("UserID")).putString("phone", phoneNumber).putString("pin", password).putString("code", code).commit();
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.REGISTER, JSONResp, null, true);
                                }
                                break;
                            case APIs.RegisterForPushMessages:
                                if (!JSONResp.has("Fault")) {
                                    userInfoPrefs.edit().putBoolean("GCMIdSentToBackend", true).commit();
                                }
                                break;
                            case APIs.PREACTIVATE:
                                if (!JSONResp.has("Fault"))
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                currentCallbackListener.callbackResponseReceived(APIs.PREACTIVATE, JSONResp, null, true);
                                break;
                            case APIs.POSTACTIVATE:
                                if (!JSONResp.has("Fault")) {
                                    if (JSONResp.has("MarsID"))
                                        marsID = JSONResp.getString("MarsID");
                                    userInfoPrefs.edit().putString("pin", password).putString("code", code).putString("phone", phoneNumber).commit();
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.POSTACTIVATE, JSONResp, null, true);
                                }
                                break;
                            case APIs.UPDATEPROFILE:
                                if (!JSONResp.has("Fault")) {
                                    userInfoPrefs.edit().putString("pin", password).commit();
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.UPDATEPROFILE, JSONResp, null, true);
                                }
                                break;
                            case APIs.GETUSERPROMOTIONDETAIL:
                                if (!JSONResp.has("Fault")) {

                                    if (JSONResp.has("ActivePromotionsList")) {
                                        activePromotions.clear();
                                        jsonArray = JSONResp.getJSONArray("ActivePromotionsList");
                                        if (jsonArray.length() > 0) {
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tempObj = jsonArray.getJSONObject(i);
                                                Campaign activePromotion = new Campaign();
                                                activePromotion.CampaignName = tempObj.getString("CampaignName");
                                                activePromotion.PromoCode = tempObj.getString("PromoCode");
                                                activePromotion.Balance = tempObj.getString("Balance");
                                                activePromotion.dtExpiry = tempObj.getString("dtExpiry");
                                                activePromotion.PerTripAmount = tempObj.getString("PerTripAmount");
                                                activePromotion.PromoURL = tempObj.getString("PromoURL");
                                                activePromotions.add(activePromotion);
                                            }
                                        }
                                    }
                                    if (JSONResp.has("Invitees")) {
                                        invitees.clear();
                                        jsonArray = JSONResp.getJSONArray("Invitees");
                                        if (jsonArray.length() > 0) {
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tempObj = jsonArray.getJSONObject(i);
                                                invitees.add(tempObj.getString("CustomerName"));
                                            }
                                        }
                                    }
                                    currentCallbackListener.callbackResponseReceived(APIs.GETUSERPROMOTIONDETAIL, JSONResp, null, true);
                                }
                                break;
                            case APIs.AvailPromotion:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.AvailPromotion, JSONResp, null, true);
                                break;
                            case APIs.FETCHPROFILE:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.FETCHPROFILE, JSONResp, null, true);
                                break;
                            case APIs.MAKERESERVATION:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.MAKERESERVATION, JSONResp, null, true);
                                else
                                    currentCallbackListener.callbackResponseReceived(APIs.MAKERESERVATION, JSONResp, null, false);
                                break;
                            case APIs.SIGNATUREUPLOAD:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.SIGNATUREUPLOAD, JSONResp, null, true);
                                break;
                            case APIs.PUTTRIPONWALL:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.PUTTRIPONWALL, JSONResp, null, true);
                                break;
                            case APIs.GETCUSTOMERRIDES:
                                ridesFetched = true;
                                if (JSONResp.has("HistoricRides"))
                                    recentTrips.clear();

                                if (JSONResp.has("CurrentFuture")) {

                                    jsonArray = JSONResp.getJSONArray("CurrentFuture");
                                    unPerformedTripsCount = jsonArray.length();

                                    for (int i = 0; i < unPerformedTripsCount; i++) {
                                        tempObj = jsonArray.getJSONObject(i);

                                        Trip bookedtrip = new Trip(tempObj.getDouble("PLatitude"), tempObj.getDouble("PLongitude"), tempObj.getString("PickAddress"), 0, 0);
                                        bookedtrip.iServiceID = tempObj.getString("ID");
                                        bookedtrip.ConfirmNumber = tempObj.getString("ConfirmationNo");
                                        bookedtrip.PUZip = tempObj.getString("PZIP");
                                        bookedtrip.PUcity = tempObj.getString("PCity");
                                        bookedtrip.PUstate = tempObj.getString("PState");
                                        bookedtrip.PUcountry = tempObj.getString("PCountry");
                                        bookedtrip.DOaddress = tempObj.getString("DropAddress");
                                        bookedtrip.DOZip = tempObj.getString("DZIP");
                                        bookedtrip.DOcity = tempObj.getString("DCity");
                                        bookedtrip.DOstate = tempObj.getString("DState");
                                        bookedtrip.DOcountry = tempObj.getString("DCountry");
                                        bookedtrip.DOlat = tempObj.getDouble("DLatitude");
                                        bookedtrip.DOlong = tempObj.getDouble("DLongitude");
                                        bookedtrip.state = tempObj.getString("TripRequestStatus");
                                        bookedtrip.isWallRequested = tempObj.getBoolean("bIsWall");
                                        try {
                                            bookedtrip.PUDateTime.setTime(inFormat1.parse(tempObj.getString("dtPickupDate")));
                                        } catch (Exception e) {
                                            bookedtrip.PUDateTime.setTime(inFormat2.parse(tempObj.getString("dtPickupDate")));
                                        }

                                        if (recentTrips.contains(bookedtrip))
                                            recentTrips.set(recentTrips.indexOf(bookedtrip), bookedtrip);
                                        else
                                            recentTrips.add(bookedtrip);
                                    }//for

                                    if (JSONResp.has("HistoricRides")) {
                                        jsonArray = JSONResp.getJSONArray("HistoricRides");
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            tempObj = jsonArray.getJSONObject(i);

                                            Trip bookedtrip = new Trip(tempObj.getDouble("PLatitude"), tempObj.getDouble("PLongitude"), tempObj.getString("PickAddress"), 0, 0);
                                            bookedtrip.iServiceID = tempObj.getString("ID");
                                            bookedtrip.ConfirmNumber = tempObj.getString("ConfirmationNo");
                                            bookedtrip.PUZip = tempObj.getString("PZIP");
                                            bookedtrip.PUcity = tempObj.getString("PCity");
                                            bookedtrip.PUstate = tempObj.getString("PState");
                                            bookedtrip.PUcountry = tempObj.getString("PCountry");
                                            bookedtrip.DOaddress = tempObj.getString("DropAddress");
                                            bookedtrip.DOZip = tempObj.getString("DZIP");
                                            bookedtrip.DOcity = tempObj.getString("DCity");
                                            bookedtrip.DOstate = tempObj.getString("DState");
                                            bookedtrip.DOcountry = tempObj.getString("DCountry");
                                            bookedtrip.DOlat = tempObj.getDouble("DLatitude");
                                            bookedtrip.DOlong = tempObj.getDouble("DLongitude");
                                            bookedtrip.state = tempObj.getString("TripRequestStatus");
                                            bookedtrip.isWallRequested = tempObj.getBoolean("bIsWall");
                                            bookedtrip.etiquetteRating = tempObj.getInt("iEtiquetteQuality");
                                            bookedtrip.taxiLateRating = tempObj.getInt("iTaxiLate");
                                            bookedtrip.cleanlinessRating = tempObj.getInt("iCleanQuality");
                                            bookedtrip.serviceRating = tempObj.getInt("iServiceQuality");
                                            bookedtrip.comments = tempObj.getString("vComments");

                                            try {
                                                bookedtrip.PUDateTime.setTime(inFormat1.parse(tempObj.getString("dtPickupDate")));
                                            } catch (Exception e) {
                                                bookedtrip.PUDateTime.setTime(inFormat2.parse(tempObj.getString("dtPickupDate")));
                                            }

                                            recentTrips.add(bookedtrip);
                                        }
                                    }

                                    recents.clear();
                                    for (Trip crrTrip : recentTrips)
                                        try {
                                            Addresses recentAddress = new Addresses(crrTrip.PUaddress, crrTrip.PUaddress, new LatLng(crrTrip.PUlat, crrTrip.PUlong));
                                            recentAddress.zip = crrTrip.PUZip;
                                            recentAddress.city = crrTrip.PUcity;
                                            recentAddress.state = crrTrip.PUstate;
                                            recentAddress.country = crrTrip.PUcountry;
                                            if (!recents.contains(recentAddress))
                                                recents.add(recentAddress);

                                            if (crrTrip.DOlat > 0) {
                                                Addresses recntAddress = new Addresses(crrTrip.DOaddress, crrTrip.DOaddress, new LatLng(crrTrip.DOlat, crrTrip.DOlong));
                                                recntAddress.zip = crrTrip.DOZip;
                                                recntAddress.city = crrTrip.DOcity;
                                                recntAddress.state = crrTrip.DOstate;
                                                recntAddress.country = crrTrip.DOcountry;
                                                if (!recents.contains(recntAddress))
                                                    recents.add(recntAddress);
                                            }
                                        } catch (Exception e) {
                                            showCustomToast(0, e.getLocalizedMessage(), true);
                                        }
                                    SortTrips();
                                }
                                currentCallbackListener.callbackResponseReceived(APIs.GETCUSTOMERRIDES, JSONResp, null, true);
                                break;
                            case APIs.GETREQUESTSTATUS:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.GETREQUESTSTATUS, JSONResp, null, true);
                                break;
                            case APIs.FORGOTPASSWORD:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.FORGOTPASSWORD, JSONResp, null, true);
                                break;
                            case APIs.RESETPASSWORD:
                                if (!JSONResp.has("Fault"))
                                    currentCallbackListener.callbackResponseReceived(APIs.RESETPASSWORD, JSONResp, null, true);
                                break;
                            case APIs.REMOVEFAVORITE:
                                if (!JSONResp.has("Fault")) {
                                    //Addresses fav = new Addresses("", "", new LatLng(0.0, 0.0));
                                    //fav.favId = JSONResp.getInt("ID");
                                    //favorites.remove(fav);
                                    currentCallbackListener.callbackResponseReceived(APIs.REMOVEFAVORITE, JSONResp, null, true);
                                }
                                break;
                            case APIs.ADDUPDATEFAVORITE:
                                if (!JSONResp.has("Fault")) {
                                    Addresses favAddress = new Addresses(JSONResp.getString("FavoriteName"), JSONResp.getString("StreetAddress"), new LatLng(Double.parseDouble(JSONResp.getString("Latitude")), Double.parseDouble(JSONResp.getString("Longitude"))));
                                    favAddress.favId = JSONResp.getInt("ID");
                                    favAddress.zip = JSONResp.getString("ZIP");
                                    favAddress.city = JSONResp.getString("City");
                                    favAddress.state = JSONResp.getString("State");
                                    favAddress.country = JSONResp.getString("Country");

                                    if (!favorites.contains(favAddress))
                                        favorites.add(favAddress);
                                }
                                break;
                            case APIs.GETFAVORITES:
                                if (JSONResp.has("Favorites")) {
                                    jsonArray = JSONResp.getJSONArray("Favorites");
                                    //db.delete("Favorites", null, null);
                                    favorites.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        tempObj = jsonArray.getJSONObject(i);

                                        Addresses favAddress = new Addresses(tempObj.getString("FavoriteName"), tempObj.getString("streetaddress"), new LatLng(Double.parseDouble(tempObj.getString("Latitude")), Double.parseDouble(tempObj.getString("Longitude"))));
                                        favAddress.favId = tempObj.getInt("ID");
                                        favAddress.zip = tempObj.getString("ZIP");
                                        favAddress.city = tempObj.getString("City");
                                        favAddress.state = tempObj.getString("State");
                                        favAddress.country = tempObj.getString("Country");

                                        favorites.add(favAddress);

                                        //ContentValues values = new ContentValues(12);
                                        //values.put("favId", tempObj.getInt("ID"));
                                        //values.put("favName", tempObj.getString("FavoriteName"));
                                        //values.put("streetAddress", tempObj.getString("streetaddress"));
                                        //values.put("lat", Double.parseDouble(tempObj.getString("Latitude")));
                                        //values.put("lng", Double.parseDouble(tempObj.getString("Longitude")));
                                        //if (tempObj.has("ZIP")) {
                                        //values.put("zip", tempObj.getString("ZIP"));
                                        //values.put("City", tempObj.getString("City"));
                                        //values.put("State", Double.parseDouble(tempObj.getString("State")));
                                        //values.put("Country", Double.parseDouble(tempObj.getString("Country")));
                                        //} else {
                                        //values.put("zip", tempObj.getString("0"));
                                        //values.put("City", tempObj.getString(""));
                                        //values.put("State", Double.parseDouble(tempObj.getString("")));
                                        //values.put("Country", Double.parseDouble(tempObj.getString("")));
                                        //}

                                        //db.insert("Favorites", null, values);
                                    }
                                    currentCallbackListener.callbackResponseReceived(APIs.GETFAVORITES, JSONResp, null, true);
                                }
                                break;
                            case APIs.CANCELTRIP:
                                if (!JSONResp.has("Fault")) {
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.CANCELTRIP, JSONResp, null, true);
                                }
                                break;
                            case APIs.SUBMITRATING:
                                if (!JSONResp.has("Fault")) {
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.SUBMITRATING, JSONResp, null, true);
                                }
                                break;
                            case APIs.GETFAREINFO:
                                currentCallbackListener.callbackResponseReceived(APIs.GETFAREINFO, JSONResp, null, !JSONResp.has("Fault"));
                                break;
                            case APIs.GETCCPROFILES:
                                if (JSONResp.has("UserCCProfileList")) {
                                    jsonArray = JSONResp.getJSONArray("UserCCProfileList");
                                    ccProfiles.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        tempObj = jsonArray.getJSONObject(i);
                                        CreditCardProfile ccProfile = new CreditCardProfile(tempObj.getString("UniqueID"), tempObj.getString("CardType"), tempObj.getString("Last4Digits"), tempObj.getString("ExpiryDate"));
                                        ccProfiles.add(ccProfile);
                                    }
                                }
                                currentCallbackListener.callbackResponseReceived(APIs.GETCCPROFILES, JSONResp, null, true);
                                break;
                            case APIs.CREATECCSESSION:
                                if (!JSONResp.has("Fault")) {
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.CREATECCSESSION, JSONResp, null, true);
                                }
                                break;
                            case APIs.CHECKCCSESSION:
                                //{"ResponseCode":0,"responseMessage":"Profile Created Successfully","Language":null,"UserID":31,
                                //"ResponseType":"CheckSessionResponse","Sessionid":"9D8F5363415A1F29DD83879C3B637B09B6D25D5F","Sessionstatus":"success","Cardexpmonth":null,"Cardexpyear":null,
                                //"Cardtype":"Visa","Cardlast4digits":"0002","UniqueID":6,"ExpiryDate":"12/25"}

                                if (!JSONResp.has("Fault")) {
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    CreditCardProfile ccProfile = new CreditCardProfile(JSONResp.getString("UniqueID"), JSONResp.getString("CardType"), JSONResp.getString("Last4Digits"), JSONResp.getString("ExpiryDate"));
                                    ccProfiles.add(ccProfile);
                                    currentCallbackListener.callbackResponseReceived(APIs.CHECKCCSESSION, JSONResp, null, true);
                                } else
                                    currentCallbackListener.callbackResponseReceived(APIs.CHECKCCSESSION, JSONResp, null, false);
                                break;
                            case APIs.REMOVECCPROFILE:
                                if (!JSONResp.has("Fault")) {
                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.REMOVECCPROFILE, JSONResp, null, true);
                                }
                                break;
                            case APIs.TOPUPBALANCE:
                                if (!JSONResp.has("Fault")) {
//                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.TOPUPBALANCE, JSONResp, null, true);
                                }
                                break;
                            case APIs.TOPUPBALANCEBYCC:
                                if (!JSONResp.has("Fault")) {
//                                    showCustomToast(0, JSONResp.getString("responseMessage"), false);
                                    currentCallbackListener.callbackResponseReceived(APIs.TOPUPBALANCEBYCC, JSONResp, null, true);
                                }
                                break;
                        }//switch
                    else
                        showCustomToast(0, "Unknown Response\n\n" + JSONResp.toString(), true);
                }
            } catch (Exception e) {
                showCustomToast(0, e.getLocalizedMessage(), true);
            }
        }

        @Override
        protected void onPreExecute() {
            if (apiCalled == APIs.HANDSHAKE)
                showCustomProgress(callerContext, "", false);
            else if (apiCalled == APIs.GETFAREINFO)
                showCustomProgress(callerContext, "Getting Fare Estimate...", true);
            else if (apiCalled != APIs.PREACTIVATE && apiCalled != APIs.GETCCPROFILES && apiCalled != APIs.GETNEARBYVEHICLES && apiCalled != APIs.FETCHPROFILE
                    && !callerContext.getClass().getName().equalsIgnoreCase(ActivitySplash.class.getName()))
                showCustomProgress(callerContext, "", true);
            super.onPreExecute();
            Log.d("JSONRequest", APIs.getApiName(apiCalled));
        }

    }// AsyncTask ITCWebService

}
