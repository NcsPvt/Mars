package itc.booking.mars;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import itc.booking.mars.BookingApplication.APIs;
import itc.booking.mars.BookingApplication.CODES;
import itc.google.places.GooglePlaces;
import itc.google.places.Param;
import itc.google.places.Place;
import itcurves.mars.R;

public class ActivityMain extends FragmentActivity implements LocationListener, CallbackResponseListener {

    private final long countDownLimit = 60000;
    private MarkerOptions pickMarkerOptions, dropMarkerOptions;
    private GoogleMap mapFragment;
    private NearbyVehicle currVehicle;
    private TimerTask tt, tt_nbv;
    private Marker lastLocationMarker, pickMarker, dropMarker;
    private Address pickAddress, dropAddress;
    private List<Address> addressList;
    private ArrayList<Place> placesList;
    private String[] rates, hailRates;
    private Trip trip_requested;
    private Bundle extras;
    private GoogleApiClient googleApiClient;
    private GooglePlaces google;
    private Polyline path;
    private LatLngBounds.Builder boundsBuilder;
    private LinearLayout ll_bottom_views, vehicle_balloon, ll_FairCashCredit, ll_cancelConfirm, overlayTrip, timer_view, layout_later, ll_pick_address, ll_drop_address, ll_selected_time, ll_selected_cab, ll_getting_nearby_progress;
    private RelativeLayout ll_pn_pl, rl_send_nearest, history, app_action_bar;
    private ScrollView menu;
    private View cancel_drop_seperator;
    private RelativeLayout rl_vehicle_company_fare;
    private GMapV2Direction mapDirections;
    private Document routeDoc;
    private Geocoder geocoder;
    private GoogleApiClient.ConnectionCallbacks connectionCallbacks;
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener;
    private LocationManager locationManager;
    private Location lastLocation;
    private TextView tv_app_version, tv_promo_available, tv_timer, tv_pick_drop, tv_address, tv_address_title, tv_pick_address, tv_drop_address, tv_sendme, tv_nearestcab, tv_selectedcab, tv_fare_estimate, tv_mile_estimate, menu_header, tv_refresh, tv_airport_note, tv_skip, tv_payWithVoucher;
    private Button cb_fav_pick, cb_fav_drop;
    private TextView tv_selected_time_title, tv_selected_time, tv_payWith, tv_endingWih;
    private EditText et_pickup_person_name, et_callback_number, et_driver_notes;
    private ImageView pointer, iv_menu, iv_home, iv_company_logo;
    //private ImageButton cancel_drop;
    private Button btn_drop_notes;
    private ListView tripsListView, lv_nearby_places, lv_vehicle_classes;
    private CountDownTimer mCountDownTimer;
    private TimePicker timePicker1;
    private DatePicker datePicker1;
    private TripAdapter trips_adapter;
    private VehicleListAdapter vehAdaptor;
    private AffiliateListAdapter affAdaptor;
    private NearbyPlacesAdapter placesAdaptor;
    private ClassOfVehicleAdapter classOfVehicleAdaptor;
    private boolean isQuickBooking;
    private boolean selecting_pickup = true;
    private boolean selecting_drop = false;
    private boolean dragging_pickup = false;
    private boolean auto_zoom = true;
    private boolean pickNow = true;
    private boolean addressFound = false;
    private boolean pressed = false;
    private Timer requestStatustimer = new Timer();
    private String ppvBalance = "0.0";

    /*----------------------------------------------------- onCreate -------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        BookingApplication.setMyLanguage(BookingApplication.userInfoPrefs.getString("lang", "en"));
        BookingApplication.setMyTheme(ActivityMain.this);

        extras = getIntent().getExtras();
        isQuickBooking = extras.getBoolean("isQuickBooking", true);

        setContentView(R.layout.activity_main);

        app_action_bar = (RelativeLayout) findViewById(R.id.app_action_bar);
        rl_send_nearest = (RelativeLayout) findViewById(R.id.rl_send_nearest);
        rl_send_nearest.setBackgroundResource(BookingApplication.textView_Background);
        ll_cancelConfirm = (LinearLayout) findViewById(R.id.ll_cancelConfirm);
        ll_bottom_views = (LinearLayout) findViewById(R.id.ll_bottom_views);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (BookingApplication.screenHeight / 2.5));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.ll_cancelConfirm);
        ll_bottom_views.setLayoutParams(layoutParams);
        //ll_bottom_views.setTop(BookingApplication.screenHeight - BookingApplication.screenHeight / 2);
        ll_FairCashCredit = (LinearLayout) findViewById(R.id.ll_FairCashCredit);
        ll_FairCashCredit.setBackgroundResource(BookingApplication.textView_Background);
        ll_pn_pl = (RelativeLayout) findViewById(R.id.rl_Now_Later);
        ll_pn_pl.setBackgroundResource(BookingApplication.textView_Background);
        ll_pick_address = (LinearLayout) findViewById(R.id.ll_selected_pick_address);
        ll_pick_address.setBackgroundResource(BookingApplication.gray_textView_Background);
        ll_drop_address = (LinearLayout) findViewById(R.id.ll_selected_drop_address);
        ll_drop_address.setBackgroundResource(BookingApplication.gray_textView_Background);
        ll_selected_time = (LinearLayout) findViewById(R.id.ll_selected_time);
        ll_selected_time.setBackgroundResource(BookingApplication.gray_textView_Background);
        ll_selected_cab = (LinearLayout) findViewById(R.id.ll_selected_cab);
        ll_selected_cab.setBackgroundResource(BookingApplication.gray_textView_Background);
        tv_refresh = (TextView) findViewById(R.id.tv_refresh);
        tv_refresh.setBackgroundResource(BookingApplication.textView_Background);
        tv_refresh.setTextColor(getResources().getColor(BookingApplication.theme_color));
        tv_pick_drop = (TextView) findViewById(R.id.tv_pick_drop);
        tv_pick_drop.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        menu_header = (TextView) findViewById(R.id.menu_header);
        menu_header.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        iv_home = (ImageView) findViewById(R.id.iv_home);
        iv_home.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        lv_nearby_places = (ListView) findViewById(R.id.lv_nearby_places);

        lv_vehicle_classes = (ListView) findViewById(R.id.list_vehicle_classes);
        lv_vehicle_classes.setSelector(BookingApplication.textView_Background);

        overlayTrip = (LinearLayout) findViewById(R.id.overlay_trip);
        vehicle_balloon = (LinearLayout) findViewById(R.id.vehicle_balloon);
        vehicle_balloon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicle_balloon.setVisibility(View.GONE);
            }
        });
        menu = (ScrollView) findViewById(R.id.menu);
        history = (RelativeLayout) findViewById(R.id.history);
        //pick_address_spacer = findViewById(R.id.pick_address_spacer);
        cancel_drop_seperator = findViewById(R.id.cancel_drop_seperator);
        //cancel_drop = (ImageButton) findViewById(R.id.cancel_drop);
        tv_skip = (TextView) findViewById(R.id.tv_skip);
        tv_payWithVoucher = (TextView) findViewById(R.id.tv_payWithVoucher);
        btn_drop_notes = (Button) findViewById(R.id.btn_drop_notes);
        ll_selected_time = (LinearLayout) findViewById(R.id.ll_selected_time);
        ll_getting_nearby_progress = (LinearLayout) findViewById(R.id.ll_getting_nearby_progress);
        tv_nearestcab = (TextView) findViewById(R.id.tv_nearestcab);
        tv_sendme = (TextView) findViewById(R.id.tv_sendme);
        tv_app_version = (TextView) findViewById(R.id.tv_app_version);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_address_title = (TextView) findViewById(R.id.tv_address_title);

        ImageSpan is = new ImageSpan(ActivityMain.this, R.drawable.locate_me);
        SpannableString text = new SpannableString(getResources().getString(R.string.map_location));
        text.setSpan(is, getResources().getString(R.string.map_location).indexOf("-"), getResources().getString(R.string.map_location).indexOf("-") + 1, 0);

        tv_address_title.setText(text);

        tv_pick_address = (TextView) findViewById(R.id.tv_pick_address);
        tv_drop_address = (TextView) findViewById(R.id.tv_drop_address);
        tv_selectedcab = (TextView) findViewById(R.id.tv_selected_cab);
        tv_timer = (TextView) findViewById(R.id.tv_timer);
        tv_promo_available = (TextView) findViewById(R.id.tv_promo_available);
        tv_payWith = (TextView) findViewById(R.id.tv_payWith);
        tv_endingWih = (TextView) findViewById(R.id.tv_endingWih);
        tv_airport_note = (TextView) findViewById(R.id.tv_airport_note);
        pointer = (ImageView) findViewById(R.id.imageFrom);
        iv_company_logo = (ImageView) findViewById(R.id.iv_company_logo);
        tripsListView = (ListView) findViewById(R.id.list_booked);


        tv_selected_time = (TextView) findViewById(R.id.tv_selected_time);
        tv_selected_time_title = (TextView) findViewById(R.id.tv_selected_time_title);
        timePicker1 = (TimePicker) findViewById(R.id.timePicker1);
        datePicker1 = (DatePicker) findViewById(R.id.datePicker1);
        et_pickup_person_name = (EditText) findViewById(R.id.et_pickup_person_name);
        et_callback_number = (EditText) findViewById(R.id.et_callback_number);
        et_callback_number.setText(BookingApplication.getUserSimNumber());
        et_driver_notes = (EditText) findViewById(R.id.et_driver_notes);
        tv_fare_estimate = (TextView) findViewById(R.id.tv_fare_estimate);
        tv_mile_estimate = (TextView) findViewById(R.id.tv_mile_estimate);
        cb_fav_drop = (Button) findViewById(R.id.fav_drop);
        cb_fav_pick = (Button) findViewById(R.id.fav_pick);
        layout_later = (LinearLayout) findViewById(R.id.layout_later);
        timer_view = (LinearLayout) findViewById(R.id.timer_view);
        rl_vehicle_company_fare = (RelativeLayout) findViewById(R.id.ll_vehicle_company_fare);

        tv_app_version.setText(getResources().getString(R.string.version, BookingApplication.appVersion));

        String bottomLogo = BookingApplication.userInfoPrefs.getString("BottomLogoImage", "");
        if ((bottomLogo.length() > 0) && (bottomLogo.endsWith("png") || bottomLogo.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(bottomLogo, iv_company_logo);

        placesList = new ArrayList<Place>();
        placesAdaptor = new NearbyPlacesAdapter(this, R.layout.list_item_favorite, placesList);
        lv_nearby_places.setAdapter(placesAdaptor);

        classOfVehicleAdaptor = new ClassOfVehicleAdapter(this, R.layout.list_item_vehicle_class, BookingApplication.classOfVehicles);
        classOfVehicleAdaptor.setNotifyOnChange(true);
        lv_vehicle_classes.setAdapter(classOfVehicleAdaptor);

        trips_adapter = new TripAdapter(this, R.layout.list_item_fromto_trip, BookingApplication.recentTrips);
        tripsListView.setAdapter(trips_adapter);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        google = new GooglePlaces(getResources().getString(R.string.google_places_key));

        connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5000), ActivityMain.this);
                setUpMapIfNeeded();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        };
        connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                BookingApplication.showCustomToast(R.string.common_google_play_services_notification_ticker, connectionResult.toString(), true);
            }
        };

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting())
            googleApiClient.connect();
        geocoder = new Geocoder(ActivityMain.this);
        mapDirections = new GMapV2Direction();

        //Toast.makeText(MainActivity.this, BookingApplication.displayDensityName(), Toast.LENGTH_SHORT).show();

    }

    /*------------------------------------------------------ onStart -------------------------------------------------------------------------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    }

    /*------------------------------------------------------ onDestroy -------------------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.clear();
        trip_requested = null;
        pickAddress = null;
        dropAddress = null;
        path = null;
        pickMarker = null;
        pickMarkerOptions = null;
        dropMarker = null;
        dropMarkerOptions = null;
        routeDoc = null;
        selecting_drop = false;
        selecting_pickup = true;
        BookingApplication.nearByVehicles.clear();
        BookingApplication.nearbyVehiclesMarkers.clear();
        googleApiClient.unregisterConnectionCallbacks(connectionCallbacks);
        googleApiClient.unregisterConnectionFailedListener(connectionFailedListener);
        googleApiClient.disconnect();
        mapFragment = null;
    }

    /*--------------------------------------------------- onUserLeaveHint -------------------------------------------------------------------------------------*/
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        BookingApplication.isMinimized = true;
    }

    /*------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.isMinimized = false;
        BookingApplication.callerContext = ActivityMain.this;
        BookingApplication.currentCallbackListener = ActivityMain.this;
        history.setVisibility(View.GONE);
    }

    /*------------------------------------------------- onBackPressed --------------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if (history.isShown())
            history.setVisibility(View.GONE);
        else if (menu.isShown()) {
            Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_left);
            menu.startAnimation(outAnim);
            menu.setVisibility(View.GONE);
        } else if (rl_vehicle_company_fare.isShown()) {
            Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_top);
            rl_vehicle_company_fare.startAnimation(outAnim);
            rl_vehicle_company_fare.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
            //finish();
        }
    }

    /*---------------------------------------------- AddDriverNotes -------------------------------------------------------------------------------------*/
    public void AddDriverNotes(View v) {
        if (v.getId() == R.id.btn_pick_notes)
            showNotesDialog(R.string.Cancel, R.id.btn_pick_notes);
        else if (v.getId() == R.id.btn_drop_notes)
            showNotesDialog(R.string.Cancel, R.id.btn_drop_notes);
//        else {
//            if (rl_vehicle_company_fare.isShown()) {
//                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_top);
//                rl_vehicle_company_fare.startAnimation(outAnim);
//                rl_vehicle_company_fare.setVisibility(View.GONE);
//            }
//            if (!et_callback_number.isShown()) {
//                Animation inTopAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_top);
//                et_callback_number.startAnimation(inTopAnim);
//                et_callback_number.setVisibility(View.VISIBLE);
//            }
//            if (!et_pickup_person_name.isShown()) {
//                Animation inTopAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_top);
//                et_pickup_person_name.startAnimation(inTopAnim);
//                et_pickup_person_name.setVisibility(View.VISIBLE);
//            }
//        }
    }

    /*---------------------------------------------- showFavorites -------------------------------------------------------------------------------------*/
    public void showFavorites(View v) {
        if (menu.isShown())
            menu.setVisibility(View.GONE);

        BookingApplication.showFavoritesScreen(this, mapFragment.getCameraPosition().target.latitude, mapFragment.getCameraPosition().target.longitude);
    }

    /*---------------------------------------------- showTripsView -------------------------------------------------------------------------------------*/
    public void showTripsView(View v) {
        trips_adapter.notifyDataSetChanged();
        if (menu.isShown())
            menu.setVisibility(View.GONE);
        getCurrentRides(3000);
        history.setVisibility(View.VISIBLE);
    }

    /*--------------------------------------------- closeVehicleMarker ---------------------------------------------------------------------------------*/
    public void closeVehicleMarker(View v) {
        vehicle_balloon.setVisibility(View.GONE);
    }

    /*---------------------------------------------- showHelpView -------------------------------------------------------------------------------------*/
    public void showHelpView(View v) {
        if (menu.isShown() || pickMarker == null) {
            menu.setVisibility(View.GONE);
            overlayTrip.setVisibility(View.VISIBLE);
        } else if (!timer_view.isShown())
            ClearMap();
    }

    /*---------------------------------------------- showHomeScreen -------------------------------------------------------------------------------------*/
    public void showHomeScreen(View v) {
        finish();
    }

    /*---------------------------------------------- showMenuView -------------------------------------------------------------------------------------*/
    public void showMenuView(View v) {
        if (!timer_view.isShown()) {
            Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_left);
            menu.startAnimation(inAnim);
            menu.setVisibility(View.VISIBLE);
        }
    }

    /*---------------------------------------------- tripOverlayClick -------------------------------------------------------------------------------------*/
    public void tripOverlayClick(View v) {
        overlayTrip.setVisibility(View.GONE);
    }

    /*-------------------------------------------------- performLogout -------------------------------------------------------------------------------------*/
    public void performLogout(View v) {
        BookingApplication.userInfoPrefs.edit().putString("UserID", "").commit();
        ClearMap();
        BookingApplication.showLoginScreen(ActivityMain.this);
    }

    /*-------------------------------------------------ShowPaymentOptions--------------------------------------------------------------------------------------*/
    public void ShowPaymentOptions(View view) {
        BookingApplication.showPaymentOptions("", "", "", false, ActivityMain.this, CODES.NONE, false);
    }

    /*-----------------------------------------------------about_Us--------------------------------------------------------------------------------------*/
    public void about_Us(View view) {
        BookingApplication.showWebScreen(ActivityMain.this, BookingApplication.userInfoPrefs.getString("AboutUsLink", getString(R.string.CompanyWeb)));
    }

    /*-----------------------------------------------------share_App--------------------------------------------------------------------------------------*/
    public void share_App(View view) {
        BookingApplication.share_App();
    }

    /*-----------------------------------------------------feedBack--------------------------------------------------------------------------------------*/
    public void feedBack(View view) {
        Intent profile = new Intent(ActivityMain.this, ActivityFeedback.class);
        startActivity(profile);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    /*--------------------------------------------------ShowPriceList--------------------------------------------------------------------------------------*/
    public void ShowPriceList(View v) {
        BookingApplication.showWebScreen(ActivityMain.this, BookingApplication.userInfoPrefs.getString("RatesLink", getString(R.string.CompanyWeb)));
    }

    /*--------------------------------------------------ShowFleet--------------------------------------------------------------------------------------*/
    public void ShowFleet(View v) {
        BookingApplication.showWebScreen(ActivityMain.this, BookingApplication.userInfoPrefs.getString("FleetInfoLink", getString(R.string.CompanyWeb)));
    }

    /*--------------------------------------------------ShowTerms--------------------------------------------------------------------------------------*/
    public void ShowTerms(View v) {
        BookingApplication.showWebScreen(ActivityMain.this, BookingApplication.userInfoPrefs.getString("TCLink", getString(R.string.CompanyWeb)));
    }

    /*--------------------------------------------------ShowPPV--------------------------------------------------------------------------------------*/
    public void ShowPPV(View v) {
        BookingApplication.ShowPPV(ActivityMain.this);
    }

    /*--------------------------------------------------- picklater -------------------------------------------------------------------------------------*/
    public void picklater(View v) {

        pickNow = false;

        if (rl_vehicle_company_fare.getVisibility() == View.VISIBLE) {
            Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_top);
            rl_vehicle_company_fare.startAnimation(outAnim);
            rl_vehicle_company_fare.setVisibility(View.GONE);
        }

        Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_bottom);
        layout_later.startAnimation(inAnim);
        layout_later.setVisibility(View.VISIBLE);

        tv_airport_note.setText(getResources().getString(R.string.airport_drop_note, BookingApplication.airport_hours));
        Calendar clndr = Calendar.getInstance();
        timePicker1.setCurrentMinute(clndr.get(Calendar.MINUTE));
        timePicker1.setCurrentHour(clndr.get(Calendar.HOUR_OF_DAY));
        datePicker1.init(clndr.get(Calendar.YEAR), clndr.get(Calendar.MONTH), clndr.get(Calendar.DAY_OF_MONTH), null);

    }

    /*---------------------------------------------------- picknow -------------------------------------------------------------------------------------*/
    public void picknow(View v) {

        //try {
        pickNow = true;

        //ll_bottom_views.setVisibility(View.GONE);

        if (trip_requested == null)
            trip_requested = new Trip(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, pickMarker.getSnippet(), -1, -1);

        trip_requested.tripType = "CURR";
        trip_requested.companyID = -1;
        trip_requested.vehTypeID = -1;


        BookingApplication.getVehicleClassesFromDB(this, pickNow, classOfVehicleAdaptor);

        tv_selected_time_title.setText(R.string.imready);
        tv_selected_time.setText(R.string.Now);
        tv_nearestcab.setText(R.string.nearestcab);
        tv_nearestcab.setLines(1);
        tv_sendme.setVisibility(View.VISIBLE);

        goToStep(4, false);

        //} catch (Exception e) {
        //Toast.makeText(ActivityMain.this, e.toString(), Toast.LENGTH_LONG).show();
        //e.printStackTrace();
        //}

    }

    /*-----------------------------------------------------ShowSendNearest-----------------------------------------------------------------------------*/
    public void ShowSendNearest(View v) {
        goToStep(4, false);
    }

    /*--------------------------------------------------ShowVehicleSelection-----------------------------------------------------------------------------*/
    public void ShowVehicleSelection(View v) {
//        if (pickNow && v.getId() == R.id.ll_nearest_cab) {
//            if (BookingApplication.classOfVehicles.size() > 0) {
//
//                ArrayList<String> affiliatesIDs = BookingApplication.getAffiliatesIDFromDB(ActivityMain.this);
//
//                if (affiliatesIDs.contains(BookingApplication.CompanyID)) {
//
//                    trip_requested.companyID = Integer.parseInt(BookingApplication.CompanyID);
//                    trip_requested.companyName = BookingApplication.getAffiliateNameFromDB(ActivityMain.this, trip_requested.companyID);
//                    trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, trip_requested.companyName);
//
//                } else if (BookingApplication.nearByVehicles.size() > 0) {
//                    //Find the nearest vehicle on map
//                    double nearestDistance = Double.MAX_VALUE;
//                    for (NearbyVehicle nbv : BookingApplication.nearByVehicles) {
//                        double currentDistance = BookingApplication.getStraightLineDistance(nbv.getLatlong(), pickMarker.getPosition());
//                        if (nearestDistance > currentDistance) {
//                            nearestDistance = currentDistance;
//                            currVehicle = nbv;
//                        }
//                    }
//                    trip_requested.companyID = currVehicle.iAffiliateID;
//                    trip_requested.companyName = BookingApplication.getAffiliateNameFromDB(ActivityMain.this, trip_requested.companyID);
//                    trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, trip_requested.companyName);
//                    //tv_nearestcab.setText(getResources().getString(R.string.cabfrom, BookingApplication.getVehicleNameFromDB(ActivityMain.this, currVehicle.iVehTypeID), BookingApplication.getAffiliateNameFromDB(ActivityMain.this, currVehicle.iAffiliateID) + " (Nearest)"));
//
//                } else {
//                    trip_requested.companyID = -1;
//                    trip_requested.companyName = "Any Company";
//                }
//
//                trip_requested.classofserviceid = -1;
//                trip_requested.vehTypeName = "nearest cab";
//                trip_requested.vehTypeID = -1;
//                trip_requested.vehNo = "-1";
//
//            }
//
//                    tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, trip_requested.companyName));
//
//            if (BookingApplication.isPassengerCountReq(ActivityMain.this, trip_requested.companyID))
//                showPassengerDialog(null);
//            else {
//                trip_requested.passengerCount = 1;
//                trip_requested.childrenCount = 0;
//                trip_requested.bagsCount = 0;
//                goToStep(5, false);
//            }
//        } else {
        goToStep(4, true);
//        }
    }

    /*--------------------------------------------------CancelTime--------------------------------------------------------------------------------------*/
    public void CancelTime(View v) {
        Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_bottom);
        layout_later.startAnimation(inAnim);
        layout_later.setVisibility(View.GONE);

        goToStep(3, false);
    }

    /*--------------------------------------------------DoneTime----------------------------------------------------------------------------------------*/
    public void DoneTime(View v) {
        Calendar chosenDateTime = Calendar.getInstance();
        chosenDateTime.set(datePicker1.getYear(), datePicker1.getMonth(), datePicker1.getDayOfMonth(), timePicker1.getCurrentHour(), timePicker1.getCurrentMinute());
        if (chosenDateTime.after(Calendar.getInstance())) {
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.HOUR_OF_DAY, BookingApplication.MaxLaterHours);
            if (chosenDateTime.compareTo(tomorrow) <= 0) {

                //if (trip_requested == null)
                //trip_requested = new Trip(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, pickMarker.getSnippet(), -1, -1);

                trip_requested.tripType = "FUT";
                trip_requested.companyID = -1;
                trip_requested.vehTypeID = -1;
                trip_requested.classofserviceid = -1;
                trip_requested.PUDateTime = chosenDateTime;

                //BookingApplication.getVehicleClassesFromDB(this, pickNow, classOfVehicleAdaptor);

                tv_selected_time_title.setText(R.string.imreadyat);
                tv_selected_time.setText(BookingApplication.formatDateTime(chosenDateTime, "h:mm a 'on' EEEE"));
                goToStep(4, false);

            } else
                BookingApplication.showCustomToast(0, getResources().getString(R.string.choose_lesser_time, BookingApplication.MaxLaterHours / 24), true);
        } else
            BookingApplication.showCustomToast(R.string.choose_greater_time, "", true);
    }

    /*-------------------------------------------- confirmBooking -------------------------------------------------------------------------------------*/
    public void confirmBooking(View v) {
        try {
            //if (trip_requested == null)
            //trip_requested = new Trip(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, pickMarker.getSnippet(), -1, -1);

            trip_requested.PUlat = pickMarker.getPosition().latitude;
            trip_requested.PUlong = pickMarker.getPosition().longitude;
            trip_requested.PUaddress = pickAddress == null ? pickMarker.getSnippet() : pickAddress.getAddressLine(0);
            //trip_requested.companyID = BookingApplication.getAffiliateIDFromDB(ActivityMain.this, spinner_affiliates.getSelectedItem().toString());
            //trip_requested.vehTypeID = BookingApplication.getVehicleIDFromDB(ActivityMain.this, spinner_vehicles.getSelectedItem().toString());
            trip_requested.callbackName = et_pickup_person_name.getText().toString().length() > 0 ? et_pickup_person_name.getText().toString() : BookingApplication.userName;
            trip_requested.callbackNumber = et_callback_number.getText().toString();
            trip_requested.otherInfo = et_driver_notes.getText().toString();

            if (dropMarker != null) {
                trip_requested.DOaddress = dropAddress == null ? dropMarker.getSnippet() : dropAddress.getAddressLine(0);
                trip_requested.DOlat = dropMarker.getPosition().latitude;
                trip_requested.DOlong = dropMarker.getPosition().longitude;
                if (dropAddress != null) {
                    trip_requested.DOcity = dropAddress.getLocality() == null ? "" : dropAddress.getLocality();
                    trip_requested.DOstate = dropAddress.getAdminArea() == null ? "" : dropAddress.getAdminArea();
                    trip_requested.DOZip = dropAddress.getPostalCode() == null ? "" : dropAddress.getPostalCode();
                    trip_requested.DOcountry = dropAddress.getCountryCode() == null ? "" : dropAddress.getCountryCode();
                }
            }

            BookingApplication.makeReservation(trip_requested, ActivityMain.this, false);

        } catch (Exception e) {
            Toast.makeText(ActivityMain.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*-------------------------------------------- confirm_Payment -------------------------------------------------------------------------------------*/
    public void confirm_Payment(View v) {
        try {
            //if (trip_requested == null)
            //trip_requested = new Trip(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, pickMarker.getSnippet(), -1, -1);

            if (v.getId() == R.id.iv_chooseCard) {
                if (Integer.toString(trip_requested.supportedPaymentType).contains("2")) {
                    if (dropAddress != null) {
                        trip_requested.CreditCardID = "0";
                        BookingApplication.showPaymentOptions("", trip_requested.estimatedCost, rates != null ? rates[3] : "", true, ActivityMain.this, CODES.PAYMENT_OPTION_ACTIVITY, false);
                    } else
                        BookingApplication.showCustomToast(R.string.Choose_Destination, "", false);
                } else
                    Toast.makeText(ActivityMain.this, getResources().getString(R.string.PaymentTypeNotSupported, "Credit Card"), Toast.LENGTH_LONG).show();
            } else if (v.getId() == R.id.ll_payWith) {
                if (Integer.toString(trip_requested.supportedPaymentType).contains("2")) {

                    if (dropAddress != null)
                        if (BookingApplication.ccProfiles.size() > 0) {

                            trip_requested.PaymentType = "2";
                            trip_requested.CreditCardID = BookingApplication.ccProfiles.get(0).id;
                            trip_requested.signatureUrl = "";

                            Intent activityIntent = new Intent(ActivityMain.this, ActivitySignature.class);
                            Bundle paymentOption = new Bundle();
                            paymentOption.putString("fareEstimate", trip_requested.currencySymbol + trip_requested.estimatedCost);
                            activityIntent.putExtras(paymentOption);
                            startActivityForResult(activityIntent, CODES.SIGNATURE_REQUIRED);

                            //BookingApplication.makeReservation(trip_requested, ActivityMain.this, false);
                        } else {
                            trip_requested.CreditCardID = "0";
                            BookingApplication.showPaymentOptions("", trip_requested.estimatedCost, rates != null ? rates[3] : "", true, ActivityMain.this, CODES.PAYMENT_OPTION_ACTIVITY, false);
                        }
                    else
                        BookingApplication.showCustomToast(R.string.Choose_Destination, "", false);

                } else
                    Toast.makeText(ActivityMain.this, getResources().getString(R.string.PaymentTypeNotSupported, "Credit Card"), Toast.LENGTH_LONG).show();
            } else if (v.getId() == R.id.tv_payWithVoucher){
                if (Integer.toString(trip_requested.supportedPaymentType).contains("3")) {
                    trip_requested.PaymentType = "3";
                    trip_requested.CreditCardID = "0";
                    trip_requested.signatureUrl = "";
                    //BookingApplication.makeReservation(trip_requested, ActivityMain.this, false);
                    if(Float.valueOf(ppvBalance) < 1.0
                            ){
                        Intent intent = new Intent(ActivityMain.this, ActivityPPV.class);
                        finish();
                        startActivity(intent);
                    }
                    else
                        goToStep(6, false);
                } else
                    Toast.makeText(ActivityMain.this, getResources().getString(R.string.PaymentTypeNotSupported, getResources().getString(R.string.PayingByVoucher)), Toast.LENGTH_LONG).show();
            } else if (v.getId() == R.id.tv_payInCash)
                if (Integer.toString(trip_requested.supportedPaymentType).contains("1")) {
                    trip_requested.PaymentType = "1";
                    trip_requested.CreditCardID = "0";
                    trip_requested.signatureUrl = "";
                    //BookingApplication.makeReservation(trip_requested, ActivityMain.this, false);
                    goToStep(6, false);
                } else
                    Toast.makeText(ActivityMain.this, getResources().getString(R.string.PaymentTypeNotSupported, getResources().getString(R.string.PayingInCab)), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ActivityMain.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*-------------------------------------------- perform_Hailing -------------------------------------------------------------------------------------*/
    public void perform_Hailing(View v) {
        if (!timer_view.isShown()) {

            trip_requested.isHailed = true;
            trip_requested.PUlat = pickMarker.getPosition().latitude;
            trip_requested.PUlong = pickMarker.getPosition().longitude;
            trip_requested.PUaddress = pickAddress == null ? pickMarker.getSnippet() : pickAddress.getAddressLine(0);

            trip_requested.classofserviceid = currVehicle.iClassID;
            trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, BookingApplication.getAffiliateNameFromDB(ActivityMain.this, currVehicle.iAffiliateID));
            trip_requested.vehNo = currVehicle.VehicleNo;
            trip_requested.tripType = "CURR";

            trip_requested.vehTypeID = currVehicle.iVehTypeID;
            trip_requested.vehTypeName = BookingApplication.getVehicleNameFromDB(ActivityMain.this, currVehicle.iVehTypeID);
            trip_requested.companyID = currVehicle.iAffiliateID;
            trip_requested.companyName = BookingApplication.getAffiliateNameFromDB(ActivityMain.this, trip_requested.companyID);

            //hailRates = BookingApplication.getRatesFromDB(ActivityMain.this, currVehicle.iVehTypeID, currVehicle.iAffiliateID);

            //trip_requested.estimatedCost = Double.toString(Double.valueOf(mapDirections.getDistanceText(routeDoc).split(" ")[0]) * Integer.valueOf(hailRates[1]) + Integer.valueOf(hailRates[0]));

            if (dropMarker != null) {
                trip_requested.DOaddress = dropAddress == null ? dropMarker.getSnippet() : dropAddress.getAddressLine(0);
                trip_requested.DOlat = dropMarker.getPosition().latitude;
                trip_requested.DOlong = dropMarker.getPosition().longitude;
                if (dropAddress != null) {
                    trip_requested.DOcity = dropAddress.getLocality() == null ? "" : dropAddress.getLocality();
                    trip_requested.DOstate = dropAddress.getAdminArea() == null ? "" : dropAddress.getAdminArea();
                    trip_requested.DOZip = dropAddress.getPostalCode() == null ? "" : dropAddress.getPostalCode();
                    trip_requested.DOcountry = dropAddress.getCountryCode() == null ? "" : dropAddress.getCountryCode();
                }
            }

            if (!ll_drop_address.isShown()) {
                tv_drop_address.setText(R.string.will_tell_driver);
                cb_fav_drop.setVisibility(View.GONE);
                btn_drop_notes.setVisibility(View.GONE);
                dropMarker = null;
                dropMarkerOptions = null;

                Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_bottom);
                ll_drop_address.startAnimation(inAnim);
                ll_drop_address.setVisibility(View.VISIBLE);
            }

            tv_selected_time_title.setText(R.string.imready);
            tv_selected_time.setText(R.string.Now);
            tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, BookingApplication.getAffiliateNameFromDB(this, currVehicle.iAffiliateID)));

            trip_requested.CreditCardID = "0";
            trip_requested.passengerCount = 1;
            trip_requested.childrenCount = 0;
            trip_requested.bagsCount = 0;
            goToStep(5, false);
        }
    }

    /*--------------------------------------------- cancelDrop -------------------------------------------------------------------------------------*/
    public void cancelDrop(View v) {

        tv_drop_address.setText(R.string.will_tell_driver);
        btn_drop_notes.setVisibility(View.GONE);
        cb_fav_drop.setVisibility(View.GONE);
        trip_requested.estimatedDistance = 0;
        trip_requested.estimatedDuration = "0";
        if (path != null) {
            path.remove();
            dropMarker.remove();
            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 18));
            path = null;
        }
        dropMarker = null;
        dropMarkerOptions = null;
        selecting_drop = false;
        dropAddress = null;

        if (BookingApplication.getAffiliatesIDFromDB(ActivityMain.this).size() > 0) {
            if (isQuickBooking)
                goToStep(6, false);
            else if (trip_requested.companyID > 0)
                goToStep(5, false);
            else if (BookingApplication.trip_type.equalsIgnoreCase("CURR"))
                picknow(null);
            else if (BookingApplication.trip_type.equalsIgnoreCase("FUT"))
                picklater(null);
        }
    }

    /*--------------------------------------------- Back_Clicked ---------------------------------------------------------------------------------------*/
    public void Back_Clicked(View v) {
        if (!timer_view.isShown()) {
            if (rl_vehicle_company_fare.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_top);
                rl_vehicle_company_fare.startAnimation(outAnim);
                rl_vehicle_company_fare.setVisibility(View.GONE);
            }
            if (ll_cancelConfirm.isShown()) {
                Animation outBottomAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_bottom);
                ll_cancelConfirm.startAnimation(outBottomAnim);
                ll_cancelConfirm.setVisibility(View.GONE);
            }
            if (ll_FairCashCredit.isShown()) {
                Animation outRightAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_FairCashCredit.startAnimation(outRightAnim);
                ll_FairCashCredit.setVisibility(View.GONE);
                tv_promo_available.setVisibility(View.GONE);
            }
            if (ll_selected_cab.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_selected_cab.startAnimation(outAnim);
                ll_selected_cab.setVisibility(View.GONE);
            }
            if (rl_send_nearest.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                rl_send_nearest.startAnimation(outAnim);
                rl_send_nearest.setVisibility(View.GONE);
            }
            if (ll_selected_time.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_selected_time.startAnimation(outAnim);
                ll_selected_time.setVisibility(View.GONE);
            }
            if (ll_pn_pl.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_pn_pl.startAnimation(outAnim);
                ll_pn_pl.setVisibility(View.GONE);
            }
            if (ll_drop_address.isShown()) {
                Animation outRightAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_drop_address.startAnimation(outRightAnim);
                ll_drop_address.setVisibility(View.GONE);
            }

            if (v.getId() == R.id.ll_selected_pick_address) {

                if (ll_pick_address.isShown()) {
                    Animation outRightAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                    ll_pick_address.startAnimation(outRightAnim);
                    ll_pick_address.setVisibility(View.GONE);
                }
                pointer.setVisibility(View.VISIBLE);
                pointer.setImageResource(R.drawable.pupin);
                SpannableString ss1=  new SpannableString(getResources().getString(R.string.pickme));
                ss1.setSpan(new RelativeSizeSpan(1.0f), (getResources().getString(R.string.pickme)).indexOf("\n"), ss1.length(), 0); // set size
                tv_pick_drop.setText(ss1);
                //cancel_drop.setVisibility(View.GONE);
                cancel_drop_seperator.setVisibility(View.GONE);
                tv_skip.setVisibility(View.GONE);
                ll_bottom_views.setVisibility(View.VISIBLE);
                //pick_address_spacer.setVisibility(View.VISIBLE);
                if (pickMarker != null)
                    mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 18));
                selecting_pickup = true;
                selecting_drop = false;
            } else if (v.getId() == R.id.ll_selected_drop_address) {
                pointer.setVisibility(View.VISIBLE);
                pointer.setImageResource(R.drawable.dopin);
                SpannableString ss1=  new SpannableString(getResources().getString(R.string.dropme));
                ss1.setSpan(new RelativeSizeSpan(1.0f), (getResources().getString(R.string.dropme)).indexOf("\n"), ss1.length(), 0); // set size
                tv_pick_drop.setText(ss1);
                //cancel_drop.setVisibility(View.VISIBLE);
                cancel_drop_seperator.setVisibility(View.VISIBLE);
                tv_skip.setVisibility(View.VISIBLE);
                ll_bottom_views.setVisibility(View.VISIBLE);
                //pick_address_spacer.setVisibility(View.VISIBLE);
                if (dropMarker != null)
                    mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(dropMarker.getPosition(), 18));
                //dropMarker.remove();
                //dropMarker = null;
                //dropMarkerOptions = null;
                //mapFragment.clear();
                selecting_pickup = false;
                selecting_drop = true;
            }
        }
    }

    /*--------------------------------------------- Next_Clicked ---------------------------------------------------------------------------------------*/
    public void Next_Clicked(View v) {
        if (selecting_pickup) {
            pointer.setImageResource(R.drawable.dopin);
            SpannableString ss1=  new SpannableString(getResources().getString(R.string.dropme));
            ss1.setSpan(new RelativeSizeSpan(1.0f), (getResources().getString(R.string.dropme)).indexOf("\n"), ss1.length(), 0); // set size
            tv_pick_drop.setText(ss1);
            //instructions.setText(R.string.select_destination);
            if (dropMarker != null)
                mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(dropMarker.getPosition(), 18));
            selecting_pickup = false;
            selecting_drop = true;
            ll_pn_pl.setVisibility(View.VISIBLE);
            ll_FairCashCredit.setVisibility(View.GONE);
        } else if (selecting_drop) {
        }
    }

    /*------------------------------------------- PickDropChosen ---------------------------------------------------------------------------------------*/
    public void PickDropFavChosen(Address currAdd) {
        if (currAdd.getLocality() == null)
            currAdd.setLocality(currAdd.getSubLocality() == null ? "" : currAdd.getSubLocality());
        if (currAdd.getAdminArea() == null)
            currAdd.setAdminArea(currAdd.getSubAdminArea() == null ? currAdd.getLocality() : currAdd.getSubAdminArea());
        if (currAdd.getCountryCode() == null)
            currAdd.setCountryCode("");
        if (currAdd.getPostalCode() == null)
            currAdd.setPostalCode("");

        BookingApplication.syncRequired = true; // this will refresh the Companies List

        if (selecting_pickup)
            try {
//              //tv_address_title.setText("Move Map to select location");
                pickAddress = currAdd;
                tv_pick_address.setText(currAdd.getAddressLine(0) + (currAdd.getAddressLine(0).contains(currAdd.getLocality()) ? "" : ", " + currAdd.getLocality()));

                if (trip_requested == null)
                    trip_requested = new Trip(currAdd.getLatitude(), currAdd.getLongitude(), currAdd.getAddressLine(0), -1, -1);
                else {
                    trip_requested.PUlat = currAdd.getLatitude();
                    trip_requested.PUlong = currAdd.getLongitude();
                    trip_requested.PUaddress = currAdd.getAddressLine(0);
                }

                trip_requested.PUcity = pickAddress.getLocality();
                trip_requested.PUstate = pickAddress.getAdminArea();
                trip_requested.PUZip = pickAddress.getPostalCode();
                trip_requested.PUcountry = pickAddress.getCountryCode();

                if (ll_pick_address.getVisibility() != View.VISIBLE) {
                    Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_from_middle);
                    ll_pick_address.startAnimation(inAnim);
                    ll_pick_address.setVisibility(View.VISIBLE);
                }
                if (BookingApplication.favorites.contains(new Addresses("", "", new LatLng(trip_requested.PUlat, trip_requested.PUlong))))
                {
                    //cb_fav_pick.setChecked(true);
                    cb_fav_pick.setBackgroundResource(R.drawable.button_silver);
                    cb_fav_pick.setText(R.string.RemoveFavourite);
                }
                else {
                    //cb_fav_pick.setChecked(false);
                    cb_fav_pick.setBackgroundResource(R.drawable.button_yellow);
                    cb_fav_pick.setText(R.string.Add_favorite);
                }
                if (pickMarker != null) {
                    pickMarker.remove();
                    pickMarker = null;
                    dragging_pickup = true;
                }
                pickMarkerOptions = new MarkerOptions().position(new LatLng(currAdd.getLatitude(), currAdd.getLongitude())).title(getResources().getString(R.string.PickupLocation)).draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.pupin));
                pickMarkerOptions.title(getResources().getString(R.string.PickupLocation));
                pickMarkerOptions.snippet(currAdd.getAddressLine(0));
                pickMarker = mapFragment.addMarker(pickMarkerOptions);
                mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickMarkerOptions.getPosition().latitude + 0.01, pickMarkerOptions.getPosition().longitude), 13));
                selecting_pickup = false;
                tv_refresh.setText("");
                tv_refresh.setBackgroundResource(R.drawable.refresh);
                selecting_drop = !isQuickBooking;
                if (dropMarkerOptions != null) {
                    pointer.setVisibility(View.GONE);
                    //pick_address_spacer.setVisibility(View.GONE);
                    ll_bottom_views.setVisibility(View.GONE);
                    selecting_drop = false;
                    (new DrawPathTask()).execute(pickMarkerOptions, dropMarkerOptions);
                } else {
                    if (selecting_drop) {
                        pointer.setImageResource(R.drawable.dopin);
                        SpannableString ss1=  new SpannableString(getResources().getString(R.string.dropme));
                        ss1.setSpan(new RelativeSizeSpan(1.0f), (getResources().getString(R.string.dropme)).indexOf("\n"), ss1.length(), 0); // set size
                        tv_pick_drop.setText(ss1);
                        //cancel_drop.setVisibility(View.VISIBLE);
                        cancel_drop_seperator.setVisibility(View.VISIBLE);
                        tv_skip.setVisibility(View.VISIBLE);
                    }
                    BookingApplication.nearByVehicles.clear();
                    drawNearByVehicles();
                    BookingApplication.showCustomProgress(ActivityMain.this, "", false);
                    BookingApplication.getNearbyVehicles(pickAddress, dropAddress, false);
                    if (isQuickBooking) {

                        pickNow = true;
                        trip_requested.vehTypeID = -1;
                        trip_requested.vehTypeName = "Any";
                        trip_requested.companyID = -1;
                        trip_requested.companyName = "Any Company";
                        trip_requested.classofserviceid = -1;
                        trip_requested.tripType = "CURR";
                        trip_requested.PaymentType = "1";
                        trip_requested.CreditCardID = "0";
                        trip_requested.signatureUrl = "";
                        trip_requested.passengerCount = 1;
                        trip_requested.childrenCount = 0;
                        trip_requested.bagsCount = 0;

                        //cancelDrop(null);
                        tv_selected_time.setText(R.string.Now);
                        tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, trip_requested.companyName));

                        goToStep(6, false);
                    }
                }
                //mapFragment.animateCamera(CameraUpdateFactory.zoomBy((float) -7.0));
            } catch (Exception e) {
                if (e != null) {
                    String msgToShow = getResources().getString(R.string.unknown_address) + "\n" + e.getMessage();
                    if (e.getMessage() != null)
                        if (e.getMessage().equalsIgnoreCase("Service not Available"))
                            msgToShow = getResources().getString(R.string.Restart_Phone);
                    Toast.makeText(ActivityMain.this, msgToShow, Toast.LENGTH_LONG).show();
                }
            }
        else
            try {
                dropAddress = currAdd;
                tv_drop_address.setText(currAdd.getAddressLine(0) + (currAdd.getAddressLine(0).contains(currAdd.getLocality()) ? "" : ", " + currAdd.getLocality()));

                trip_requested.DOlat = currAdd.getLatitude();
                trip_requested.DOlong = currAdd.getLongitude();
                trip_requested.DOaddress = currAdd.getAddressLine(0);
                trip_requested.DOlat = currAdd.getLatitude();
                trip_requested.DOlong = currAdd.getLongitude();
                trip_requested.DOcity = dropAddress.getLocality();
                trip_requested.DOstate = dropAddress.getAdminArea();
                trip_requested.DOZip = dropAddress.getPostalCode();
                trip_requested.DOcountry = dropAddress.getCountryCode();

                if (!ll_drop_address.isShown()) {
                    Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_bottom);
                    ll_drop_address.startAnimation(inAnim);
                    ll_drop_address.setVisibility(View.VISIBLE);
                }
                cb_fav_drop.setVisibility(View.VISIBLE);
                btn_drop_notes.setVisibility(View.VISIBLE);
                if (BookingApplication.favorites.contains(new Addresses("", "", new LatLng(trip_requested.DOlat, trip_requested.DOlong))))
                {
                    //cb_fav_pick.setChecked(true);
                    cb_fav_drop.setBackgroundResource(R.drawable.button_red);
                    cb_fav_drop.setText(R.string.RemoveFavourite);
                }
                else {
                    //cb_fav_pick.setChecked(false);
                    cb_fav_drop.setBackgroundResource(R.drawable.button_yellow);
                    cb_fav_drop.setText(R.string.Add_favorite);
                }
                dropMarkerOptions = new MarkerOptions().position(new LatLng(currAdd.getLatitude(), currAdd.getLongitude())).title(getResources().getString(R.string.DropLocation)).draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.dopin));
                //mapFragment.moveCamera(CameraUpdateFactory.newLatLngZoom(dropMarkerOptions.getPosition(), 18));
                dropMarkerOptions.title(getResources().getString(R.string.DropLocation));
                dropMarkerOptions.snippet(currAdd.getAddressLine(0));
                if (dropMarker != null)
                    dropMarker.remove();
                dropMarker = mapFragment.addMarker(dropMarkerOptions);
                //dropMarker.showInfoWindow();
                pointer.setVisibility(View.GONE);
                //pick_address_spacer.setVisibility(View.GONE);
                ll_bottom_views.setVisibility(View.GONE);
                selecting_drop = false;
                selecting_pickup = false;
                dragging_pickup = false;

                try{
                    tt_nbv.cancel();
                    requestStatustimer.purge();
                }catch(Exception e){

                }

                BookingApplication.getNearbyVehicles(pickAddress, dropAddress, !BookingApplication.syncRequired);   //this will call getNearbyVehicles with false bit.
                (new DrawPathTask()).execute(pickMarkerOptions, dropMarkerOptions);

            } catch (Exception e) {
                Toast.makeText(ActivityMain.this, getResources().getString(R.string.unknown_address) + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

        BookingApplication.syncRequired = false;
    }

    /*--------------------------------------------- searchAddress ---------------------------------------------------------------------------------------*/
    public void searchAddress(View v) {
        Intent search = new Intent(ActivityMain.this, ActivitySearch.class);
        Bundle bndle = new Bundle();
        bndle.putInt("Header", selecting_pickup ? R.string.PickupLocation : R.string.DropLocation);
        bndle.putDouble("Latitude", mapFragment.getCameraPosition().target.latitude);
        bndle.putDouble("Longitude", mapFragment.getCameraPosition().target.longitude);
        search.putExtras(bndle);
        startActivityForResult(search, CODES.SEARCHADDRESS);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    /*--------------------------------------------- PickSelected ---------------------------------------------------------------------------------------*/
    public void PickSelected(View v) {
        if (BookingApplication.isNetworkConnected)
            if (addressFound) {

                /*StringBuilder sb = new StringBuilder();
                for (Address currAdd : addressList)
                    if (sb.indexOf((currAdd.getAddressLine(0))) < 0) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append(currAdd.getAddressLine(0));
                    }
                String address = sb.toString();

                Address currAdd = addressList.get(0);
                currAdd.setAddressLine(0, address);*/

                PickDropFavChosen(BookingApplication.currentAddress);

            } else
                Toast.makeText(ActivityMain.this, getResources().getString(R.string.unknown_address), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(ActivityMain.this, getResources().getString(R.string.No_Internet_Connectivity), Toast.LENGTH_LONG).show();

    }

    /*---------------------------------------------------------drawNearByVehicles-----------------------------------------------------------------------------*/
    private void drawNearByVehicles() {

        for (Marker nearbyVehicleMarker : BookingApplication.nearbyVehiclesMarkers)
            nearbyVehicleMarker.remove();

        BookingApplication.nearbyVehiclesMarkers.clear();
        boundsBuilder = new LatLngBounds.Builder();

        if (pickMarker != null)
            boundsBuilder.include(pickMarker.getPosition());

        for (NearbyVehicle nearbyVehicle : BookingApplication.nearByVehicles) {
            BookingApplication.nearbyVehiclesMarkers.add(mapFragment.addMarker(nearbyVehicle.vehMarker));
            boundsBuilder.include(nearbyVehicle.getLatlong());
        }
    }

    /*--------------------------------------------------- getNearbyVehicles ------------------------------------------------------------------------------*/
    private void getNearbyVehicles(final int time_msec) {

        tt_nbv = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        BookingApplication.getNearbyVehicles(pickAddress, dropAddress, !BookingApplication.syncRequired);
                    }
                });
            }
        };

        requestStatustimer.schedule(tt_nbv, time_msec);

    }

    /*-------------------------------------------------toggleGPS----------------------------------------------------------------------------------------*/
    public void toggleGPS(int title, int msgText) {

        showCustomDialog(CODES.GPS_TOGGLE, title, getResources().getString(msgText), R.drawable.gps, true);

    }

    /*------------------------------------------------setUpMapIfNeeded----------------------------------------------------------------------------------*/
    private void setUpMapIfNeeded() {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                toggleGPS(R.string.enable_gps_title, R.string.enable_gps_text);

            if (mapFragment == null)
                mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mapFragment == null)
                return;

            UiSettings map_ui = mapFragment.getUiSettings();
            map_ui.setZoomControlsEnabled(true);
            map_ui.setMapToolbarEnabled(false);
            mapFragment.setMyLocationEnabled(true);

            mapFragment.setPadding(0, BookingApplication.screenHeight / 12, 0, (int)(BookingApplication.screenHeight / 2.5) + 10);
            int cameraY = mapFragment.getProjection().toScreenLocation(mapFragment.getCameraPosition().target).y;
            pointer.setY(cameraY - pointer.getHeight());
            vehicle_balloon.setY(cameraY - vehicle_balloon.getHeight());

            mapFragment.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    ll_bottom_views.setVisibility(View.GONE);
                }
            });

            mapFragment.setOnCameraChangeListener(new OnCameraChangeListener() {
                @Override
                public void onCameraChange(final CameraPosition position) {
                    try {

                        int cameraY = mapFragment.getProjection().toScreenLocation(mapFragment.getCameraPosition().target).y;
                        pointer.setY(cameraY - pointer.getHeight());
                        vehicle_balloon.setY(cameraY - vehicle_balloon.getHeight() + app_action_bar.getHeight());

                        if (selecting_pickup || selecting_drop) {

                            BookingApplication.currentLatLong = position.target;

                            ll_bottom_views.setVisibility(View.GONE);
                            tv_address.setText(R.string.getting_address);
                            addressFound = false;
                            placesAdaptor.notifyDataSetChanged();
                            ll_getting_nearby_progress.setVisibility(View.VISIBLE);

                            if (tt != null)
                                tt.cancel();
                            tt = new TimerTask() {

                                @Override
                                public void run() {
                                    //if (rl_bottom_views.getVisibility() == View.VISIBLE)
                                    getFromLocation(position.target.latitude, position.target.longitude, 2);
                                }
                            };

                            requestStatustimer.schedule(tt, 2000);
                        }
                    } catch (Exception e) {
                        BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
                    }
                }
            });

            mapFragment.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {

                @Override
                public boolean onMyLocationButtonClick() {
                    if (lastLocation == null)
                        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                    if (lastLocation == null || lastLocation.getAccuracy() > 70)
                        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                            toggleGPS(R.string.enable_wifigps_title, R.string.enable_wifigps_text);
                        else if (!BookingApplication.wifiManager.isWifiEnabled())
                            showCustomDialog(CODES.WIFI_TOGGLE, R.string.enable_wifigps_title, getResources().getString(R.string.enable_wifi_text), 0, false);
                        else
                            showCustomDialog(CODES.START_MAPS, R.string.enable_wifigps_title, getResources().getString(R.string.start_maps), 0, true);

                    if (auto_zoom) {
                        if (lastLocation != null)
                            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18));
                        return true;
                    } else
                        return false;
                }
            });


            mapFragment.setOnMarkerClickListener(new OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker marker) {
                    try {
                        if (marker.getTitle().equalsIgnoreCase("Vehicle")) {

                            currVehicle = BookingApplication.nearByVehicles.get(Integer.parseInt(marker.getSnippet()));

                            Button btn_hail = (Button) vehicle_balloon.findViewById(R.id.btn_hail);
                            btn_hail.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (currVehicle.bHailingAllowed)
                                        perform_Hailing(null);
                                    vehicle_balloon.setVisibility(View.GONE);
                                    marker.hideInfoWindow();
                                }
                            });

                            ImageView driverImage = (ImageView) vehicle_balloon.findViewById(R.id.img_driver);
                            ImageView selected_veh_logo = (ImageView) vehicle_balloon.findViewById(R.id.selected_veh_logo);
                            TextView driverName = (TextView) vehicle_balloon.findViewById(R.id.txt_driver_name);
                            TextView company_name = (TextView) vehicle_balloon.findViewById(R.id.txt_company_name);
                            TextView vehicleNumber = (TextView) vehicle_balloon.findViewById(R.id.txt_veh_plate);
                            TextView vehicleMake = (TextView) vehicle_balloon.findViewById(R.id.txt_veh_make);
                            TextView totalRatings = (TextView) vehicle_balloon.findViewById(R.id.txt_total_ratings);
                            TextView vehicle_rate = (TextView) vehicle_balloon.findViewById(R.id.vehicle_rate);
                            RatingBar rating = (RatingBar) vehicle_balloon.findViewById(R.id.rating);

                            TextView ourRates = (TextView) vehicle_balloon.findViewById(R.id.ratesLink);
                            ourRates.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (currVehicle.ourRates.length() > 0)
                                        BookingApplication.showWebScreen(ActivityMain.this, currVehicle.ourRates);
                                }
                            });

                            TextView ourFleet = (TextView) vehicle_balloon.findViewById(R.id.fleetLink);
                            ourFleet.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (currVehicle.ourFleet.length() > 0)
                                        BookingApplication.showWebScreen(ActivityMain.this, currVehicle.ourFleet);
                                }
                            });

                            TextView ourTerms = (TextView) vehicle_balloon.findViewById(R.id.termsLink);
                            ourTerms.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (currVehicle.ourTerms.length() > 0)
                                        BookingApplication.showWebScreen(ActivityMain.this, currVehicle.ourTerms);
                                }
                            });

                            TextView aboutUs = (TextView) vehicle_balloon.findViewById(R.id.aboutUs);
                            aboutUs.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (currVehicle.aboutUs.length() > 0)
                                        BookingApplication.showWebScreen(ActivityMain.this, currVehicle.aboutUs);
                                }
                            });

                            if (currVehicle.bHailingAllowed)
                                btn_hail.setVisibility(View.VISIBLE);
                            else
                                btn_hail.setVisibility(View.GONE);
                            driverName.setText(currVehicle.driverName);
                            company_name.setText(BookingApplication.getAffiliateNameFromDB(ActivityMain.this, currVehicle.iAffiliateID));
                            rating.setRating((float) currVehicle.avg_Rating);
                            totalRatings.setText(Integer.toString(currVehicle.iTotalRatings));
                            vehicleNumber.setText(currVehicle.VehicleNo);
                            vehicleMake.setText(currVehicle.vehicleMake);
                            hailRates = BookingApplication.getRatesFromDB(ActivityMain.this, currVehicle.iVehTypeID, currVehicle.iAffiliateID, currVehicle.iClassID);
                            if (hailRates != null && !hailRates[0].equalsIgnoreCase("0") && !hailRates[0].equalsIgnoreCase("")) {
                                String rate = getResources().getString(R.string.Initial_Charges, hailRates[3] + " " + hailRates[0]) + "\n" + getResources().getString(R.string.per_unit_Charges) + " "
                                        + hailRates[3] + " " + hailRates[1] + "\\" + hailRates[2];
                                vehicle_rate.setText(rate);
                            } else
                                vehicle_rate.setText("See Our Rates.");

                            if (currVehicle.driverPicture.length() > 0 && (currVehicle.driverPicture.endsWith("png") || currVehicle.driverPicture.endsWith("jpg")))
                                BookingApplication.imagedownloader.DisplayImage(currVehicle.driverPicture, driverImage);
                            else
                                driverImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_driver));

                            if (currVehicle.companyLogo.length() > 0)
                                BookingApplication.imagedownloader.DisplayImage(currVehicle.companyLogo, selected_veh_logo);

                            vehicle_balloon.setVisibility(View.VISIBLE);
                            return false;

                        }

                    } catch (Exception e) {
                        Toast.makeText(ActivityMain.this, e.toString(), Toast.LENGTH_LONG).show();

                    }

                    return true;
                }
            });

            mapFragment.setOnMapClickListener(new OnMapClickListener() {

                @Override
                public void onMapClick(LatLng arg0) {
                    vehicle_balloon.setVisibility(View.GONE);
                }
            });

            zoomToLastLocation();
        }//else
    }

    /*--------------------------------------------------addRemoveFavorite-----------------------------------------------------------------------------------*/
    public void addRemoveFavorite(String caption, Address address, Addresses addrs, int callerViewId) {
        Addresses myFav;
        if (address != null)
            myFav = new Addresses(caption, address.getAddressLine(0), new LatLng(address.getLatitude(), address.getLongitude()));
        else
            myFav = addrs;

        myFav.zip = address.getPostalCode();
        myFav.city = address.getLocality();
        myFav.state = address.getAdminArea();
        myFav.country = address.getCountryCode();

        if (BookingApplication.favorites.contains(myFav)) {
            BookingApplication.removeFavorite(BookingApplication.favorites.get(BookingApplication.favorites.indexOf(myFav)), ActivityMain.this);
            BookingApplication.favorites.remove(myFav);
            if (callerViewId == R.id.fav_pick)
            {
                cb_fav_pick.setBackgroundResource(R.drawable.button_yellow);
                cb_fav_pick.setText(R.string.Add_favorite);
            }
            else if (callerViewId == R.id.fav_drop)
            {
                cb_fav_drop.setBackgroundResource(R.drawable.button_yellow);
                cb_fav_drop.setText(R.string.Add_favorite);
            }
        } else {
            if (callerViewId == R.id.fav_pick)
            {
                cb_fav_pick.setBackgroundResource(R.drawable.button_silver);
                cb_fav_pick.setText(R.string.RemoveFavourite);
            }
            else if (callerViewId == R.id.fav_drop)
            {
                cb_fav_drop.setBackgroundResource(R.drawable.button_silver);
                cb_fav_drop.setText(R.string.RemoveFavourite);
            }
            showFavoriteDialog(myFav, R.string.Cancel, callerViewId);
        }
    }

    /*------------------------------------------------- addRemoveFavorite ---------------------------------------------------------------------------------*/
    public void addRemoveFavrite(View v) {
        if (v.getId() == R.id.fav_pick)
            addRemoveFavorite(tv_pick_address.getText().toString(), pickAddress, null, v.getId());
        else if (v.getId() == R.id.fav_drop && dropMarker != null)
            addRemoveFavorite(tv_drop_address.getText().toString(), dropAddress, null, v.getId());
    }

    /*------------------------------------------------ showFavoriteDialog ---------------------------------------------------------------------------------*/
    public void showFavoriteDialog(final Addresses fav_address, final int cancelBtnCaption, final int callerViewId) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_favorites, null);

        final BookingApplication.CustomDialog bb = new BookingApplication.CustomDialog(this, layout);

        //TextView title_text = (TextView) layout.findViewById(R.id.fav_dialog_title);
        //title_text.setText(dialogTitle);

        final EditText address_name = (EditText) layout.findViewById(R.id.fav_name);
        TextView streetAddress = (TextView) layout.findViewById(R.id.fav_address);

        address_name.setText("");
        streetAddress.setText(fav_address.address);

        TextView btn_OK = (TextView) layout.findViewById(R.id.btnSAVE);
        TextView btn_CANCEL = (TextView) layout.findViewById(R.id.btnCANCEL);
        btn_CANCEL.setText(cancelBtnCaption);

        btn_OK.setOnClickListener(new OnClickListener() {
            Addresses adrs = fav_address;

            @Override
            public void onClick(View v) {
                if(address_name.length() > 0) {
                    bb.dismiss();

                    adrs.caption = address_name.getText().toString();
                    BookingApplication.addUpdateFavorite(adrs);
                }else{
                    BookingApplication.showCustomToast(0,getString(R.string.ProvideAddressName), false);
                }
            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bb.dismiss();

                if (cancelBtnCaption == R.string.Remove) {

                    Boolean favFound = false;
                    int i;
                    Addresses currFav = null;
                    for (i = 0; i < BookingApplication.favorites.size(); i++) {
                        currFav = BookingApplication.favorites.get(i);
                        if (fav_address.equals(currFav)) {
                            favFound = true;
                            break;
                        }
                    }
                    if (favFound)
                        BookingApplication.favorites.remove(i);

                    BookingApplication.removeFavorite(fav_address, ActivityMain.this);
                } else if (callerViewId == R.id.fav_pick)
                {
                    cb_fav_pick.setBackgroundResource(R.drawable.button_yellow);
                    cb_fav_pick.setText(R.string.Add_favorite);
                }
                else if (callerViewId == R.id.fav_drop)
                {
                    cb_fav_drop.setBackgroundResource(R.drawable.button_yellow);
                    cb_fav_drop.setText(R.string.Add_favorite);
                }
            }
        });

        bb.show();

        //thisDialog.setView(layout);
        //thisDialog.show();
    }

    /*------------------------------------------------ showNotesDialog ---------------------------------------------------------------------------------*/
    public void showNotesDialog(final int cancelBtnCaption, final int callerViewId) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_driver_notes, null);

        final BookingApplication.CustomDialog bb = new BookingApplication.CustomDialog(this, layout);

        TextView title_text = (TextView) layout.findViewById(R.id.driver_notes_dialog_title);
        final EditText driver_notes = (EditText) layout.findViewById(R.id.et_driver_notes);

        if (callerViewId == R.id.btn_pick_notes) {
            title_text.setText(getResources().getString(R.string.AddNotes, getResources().getString(R.string.pickup)));
            driver_notes.setText(trip_requested.pickNotes);
        } else {
            title_text.setText(getResources().getString(R.string.AddNotes, getResources().getString(R.string.drop)));
            driver_notes.setText(trip_requested.dropNotes);
        }

        TextView btn_OK = (TextView) layout.findViewById(R.id.btnSAVE);
        TextView btn_CANCEL = (TextView) layout.findViewById(R.id.btnCANCEL);
        btn_CANCEL.setText(cancelBtnCaption);

        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bb.dismiss();

                if (callerViewId == R.id.btn_pick_notes)
                    trip_requested.pickNotes = driver_notes.getText().toString();
                else
                    trip_requested.dropNotes = driver_notes.getText().toString();
            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bb.dismiss();

            }
        });

        bb.show();

        //thisDialog.setView(layout);
        //thisDialog.show();
    }

    /*------------------------------------------------ showPromotions ---------------------------------------------------------------------------------*/
    public void showPromotions(View v) {
        BookingApplication.showPromotions(this);
    }

    /*------------------------------------------------ showPromoDialog ---------------------------------------------------------------------------------*/
    public void showPromoDialog(View v) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_promo, null);
        layout.setBackgroundResource(BookingApplication.textView_Background);

        final BookingApplication.CustomDialog bb = new BookingApplication.CustomDialog(this, layout);

        final EditText promoCode = (EditText) layout.findViewById(R.id.promo_code);
        final EditText tip = (EditText) layout.findViewById(R.id.flat_tip);
        final RadioGroup rg = (RadioGroup) layout.findViewById(R.id.radio_tip);

        final ListView lv_promos = (ListView) layout.findViewById(R.id.lv_promos);
        final PromotionsAdapter promos_adapter = new PromotionsAdapter(this, R.layout.list_item_promo_detail, BookingApplication.activePromotions);
        lv_promos.setAdapter(promos_adapter);
        lv_promos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookingApplication.Campaign camp = BookingApplication.activePromotions.get(position);
                promoCode.setText(camp.PromoCode);
                lv_promos.setVisibility(View.GONE);
            }
        });

        final Button btn_choose = (Button) layout.findViewById(R.id.btn_choose);
        btn_choose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lv_promos.isShown())
                    lv_promos.setVisibility(View.GONE);
                else
                    lv_promos.setVisibility(View.VISIBLE);

                promos_adapter.notifyDataSetChanged();
            }
        });

        TextView btn_OK = (TextView) layout.findViewById(R.id.btnSAVE);
        TextView btn_CANCEL = (TextView) layout.findViewById(R.id.btnCANCEL);

        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                trip_requested.promoCode = promoCode.getText().toString();
                if (tip.getText().length() > 0)
                    trip_requested.tip = tip.getText().toString();
                else if (rg.getCheckedRadioButtonId() == R.id.tip_15)
                    trip_requested.tip = "15%";
                else if (rg.getCheckedRadioButtonId() == R.id.tip_20)
                    trip_requested.tip = "20%";
                else if (rg.getCheckedRadioButtonId() == R.id.tip_25)
                    trip_requested.tip = "25%";
                bb.dismiss();

                BookingApplication.getFareInfo(trip_requested, ActivityMain.this);

            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                trip_requested.promoCode = "0";
                trip_requested.tip = "0";
                bb.dismiss();

                BookingApplication.getFareInfo(trip_requested, ActivityMain.this);

            }
        });

        bb.show();

        BookingApplication.getUserPromoDetail(BookingApplication.CompanyID);
    }

    /*------------------------------------------------ showPromoDialog ---------------------------------------------------------------------------------*/
    public void showPassengerDialog(View v) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_shuttle, null);
        layout.setBackgroundResource(BookingApplication.textView_Background);

        final BookingApplication.CustomDialog bb = new BookingApplication.CustomDialog(this, layout);
        TextView btn_OK = (TextView) layout.findViewById(R.id.btnDONE);

        final TextView tv_adults = (TextView) layout.findViewById(R.id.tv_adults);
        TextView btn_add_adults = (TextView) layout.findViewById(R.id.add_adults);
        btn_add_adults.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_adults.setText(Integer.toString(Integer.parseInt(tv_adults.getText().toString()) + 1));
            }
        });
        TextView btn_remove_adults = (TextView) layout.findViewById(R.id.remove_adults);
        btn_remove_adults.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.parseInt(tv_adults.getText().toString()) - 1) > 0)
                    tv_adults.setText(Integer.toString((Integer.parseInt(tv_adults.getText().toString()) - 1)));
            }
        });

        final TextView tv_children = (TextView) layout.findViewById(R.id.tv_children);
        TextView btn_add_children = (TextView) layout.findViewById(R.id.add_children);
        btn_add_children.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_children.setText(Integer.toString(Integer.parseInt(tv_children.getText().toString()) + 1));
            }
        });
        TextView btn_remove_children = (TextView) layout.findViewById(R.id.remove_children);
        btn_remove_children.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.parseInt(tv_children.getText().toString()) - 1) > -1)
                    tv_children.setText(Integer.toString(Integer.parseInt(tv_children.getText().toString()) - 1));
            }
        });

        final TextView tv_bags = (TextView) layout.findViewById(R.id.tv_bags);
        TextView btn_add_bags = (TextView) layout.findViewById(R.id.add_bags);
        btn_add_bags.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_bags.setText(Integer.toString(Integer.parseInt(tv_bags.getText().toString()) + 1));
            }
        });
        TextView btn_remove_bags = (TextView) layout.findViewById(R.id.remove_bags);
        btn_remove_bags.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((Integer.parseInt(tv_bags.getText().toString()) - 1) > -1)
                    tv_bags.setText(Integer.toString(Integer.parseInt(tv_bags.getText().toString()) - 1));
            }
        });

        final CheckBox cb_ex_ride = (CheckBox) layout.findViewById(R.id.cb_ex_ride);

        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                trip_requested.exclusiveRide = cb_ex_ride.isChecked();
                if (Integer.parseInt(tv_adults.getText().toString()) > 0) {
                    trip_requested.childrenCount = Integer.parseInt(tv_children.getText().toString());
                    trip_requested.passengerCount = Integer.parseInt(tv_adults.getText().toString()) + trip_requested.childrenCount;
                    trip_requested.bagsCount = Integer.parseInt(tv_bags.getText().toString());
                } else {
                    BookingApplication.showCustomToast(0, getResources().getString(R.string.InvalidAdultsCount), false);
                    return;
                }

                bb.dismiss();

                goToStep(5, false);
                //BookingApplication.getFareInfo(trip_requested, ActivityMain.this);

            }
        });

        bb.show();

        //thisDialog.setView(layout);
        //thisDialog.show();
    }

    /*--------------------------------------------------onActivityResult-----------------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Address addr;
        if (resultCode == APIs.UPDATEPROFILE) {
            if (data.getBooleanExtra("RebootApp", false)) {
                this.finish();
                BookingApplication.restartAPP(ActivityMain.this);
            }
        } else if (resultCode == CODES.SEARCHADDRESS) {
            addr = (Address) data.getParcelableExtra("Address");
            PickDropFavChosen(addr);
        } else if (resultCode == CODES.PAYMENT_OPTION_ACTIVITY || resultCode == CODES.SIGNATURE_REQUIRED)
            if (trip_requested != null) {
                if (data.hasExtra("signatureUrl"))
                    trip_requested.signatureUrl = data.getStringExtra("signatureUrl");
                if (data.hasExtra("PaymentType"))
                    trip_requested.PaymentType = data.getStringExtra("PaymentType");
                if (data.hasExtra("CardID"))
                    trip_requested.CreditCardID = data.getStringExtra("CardID");

                BookingApplication.callerContext = ActivityMain.this;
                if (trip_requested.signatureUrl.length() > 4)
                    BookingApplication.makeReservation(trip_requested, ActivityMain.this, false);
            }
    }

    /*--------------------------------------------------showProfileScreen----------------------------------------------------------------------------------*/
    public void showProfileScreen(View v) {
        BookingApplication.showProfileScreen(this);
    }//showProfileScreen

    /*--------------------------------------------------showNowLater----------------------------------------------------------------------------------*/
    public void showNowLater(View v) {
        goToStep(3, false);
    }

    /*--------------------------------------------------goToStep----------------------------------------------------------------------------------*/
    public void goToStep(int step, boolean bShowCompanyList) {
        if (step < 7) {
            pointer.setVisibility(View.GONE);
            ll_bottom_views.setVisibility(View.GONE);

            if (bShowCompanyList) {
                //classOfVehicleAdaptor.clear();
                if (BookingApplication.getVehicleClassesFromDB(ActivityMain.this, pickNow, classOfVehicleAdaptor) > 0)
                    classOfVehicleAdaptor.notifyDataSetChanged();
                if (!rl_vehicle_company_fare.isShown()) {
                    Animation inAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_top);
                    rl_vehicle_company_fare.startAnimation(inAnim);
                    rl_vehicle_company_fare.setVisibility(View.VISIBLE);
                }
            } else if (rl_vehicle_company_fare.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_top);
                rl_vehicle_company_fare.startAnimation(outAnim);
                rl_vehicle_company_fare.setVisibility(View.GONE);
            }

            if (ll_pn_pl.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_pn_pl.startAnimation(outAnim);
                ll_pn_pl.setVisibility(View.GONE);
            }
            if (rl_send_nearest.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                rl_send_nearest.startAnimation(outAnim);
                rl_send_nearest.setVisibility(View.GONE);
            }
            if (layout_later.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_bottom);
                layout_later.startAnimation(outAnim);
                layout_later.setVisibility(View.GONE);
            }

            if (step == 6) {
                if(!isQuickBooking) {
                    if (!ll_drop_address.isShown()) {
                        Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                        ll_drop_address.startAnimation(rightInAnim);
                        ll_drop_address.setVisibility(View.VISIBLE);
                    }
                    if (!ll_selected_time.isShown()) {
                        Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                        ll_selected_time.startAnimation(rightInAnim);
                        ll_selected_time.setVisibility(View.VISIBLE);
                    }
                    if (!ll_selected_cab.isShown()) {
                        Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                        ll_selected_cab.startAnimation(rightInAnim);
                        ll_selected_cab.setVisibility(View.VISIBLE);
                    }
                    if (!ll_FairCashCredit.isShown()) {
                        Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                        ll_FairCashCredit.startAnimation(rightInAnim);
                        ll_FairCashCredit.setVisibility(View.VISIBLE);

                        if (BookingApplication.promotions.size() > 0)
                            tv_promo_available.setVisibility(View.VISIBLE);
                    }
                }
                if (!ll_cancelConfirm.isShown()) {
                    Animation slideInBottom = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_bottom);
                    ll_cancelConfirm.startAnimation(slideInBottom);
                    ll_cancelConfirm.setVisibility(View.VISIBLE);
                }
            }
        }
        if (step < 6) {

            //pick_address_spacer.setVisibility(View.GONE);
            ll_cancelConfirm.setVisibility(View.GONE);

            if (step == 5) {
                if (!ll_drop_address.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_drop_address.startAnimation(rightInAnim);
                    ll_drop_address.setVisibility(View.VISIBLE);
                }
                if (!ll_selected_time.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_selected_time.startAnimation(rightInAnim);
                    ll_selected_time.setVisibility(View.VISIBLE);
                }
                if (!ll_selected_cab.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_selected_cab.startAnimation(rightInAnim);
                    ll_selected_cab.setVisibility(View.VISIBLE);
                }
                if (!ll_FairCashCredit.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_FairCashCredit.startAnimation(rightInAnim);
                    ll_FairCashCredit.setVisibility(View.VISIBLE);

                    if (BookingApplication.promotions.size() > 0)
                        tv_promo_available.setVisibility(View.VISIBLE);
                }

                BookingApplication.getFareInfo(trip_requested, ActivityMain.this);

                if (BookingApplication.ccProfiles.size() > 0) {
                    tv_payWith.setText(getResources().getString(R.string.Paywith, BookingApplication.ccProfiles.get(0).type));
                    tv_endingWih.setText(getResources().getString(R.string.EndingIn, BookingApplication.ccProfiles.get(0).last4));
                } else {
                    tv_payWith.setText(getResources().getString(R.string.Paywith, getResources().getString(R.string.creditCards)));
                    tv_endingWih.setText(getResources().getString(R.string.NoCreditCard));
                }

                BookingApplication.showCustomToast(R.string.ChoosePaymentOption, "", false);
            }
        }
        if (step < 5) {
            if (ll_FairCashCredit.isShown()) {
                Animation outRightAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_FairCashCredit.startAnimation(outRightAnim);
                ll_FairCashCredit.setVisibility(View.GONE);
                tv_promo_available.setVisibility(View.GONE);
            }
            if (ll_selected_cab.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_selected_cab.startAnimation(outAnim);
                ll_selected_cab.setVisibility(View.GONE);
            }
            if (step == 4) {

                if (BookingApplication.getVehicleClassesFromDB(ActivityMain.this, pickNow, classOfVehicleAdaptor) > 0)
                    classOfVehicleAdaptor.notifyDataSetChanged();

                if (!ll_drop_address.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_drop_address.startAnimation(rightInAnim);
                    ll_drop_address.setVisibility(View.VISIBLE);
                }
                if (!ll_selected_time.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_selected_time.startAnimation(rightInAnim);
                    ll_selected_time.setVisibility(View.VISIBLE);
                }

                if (BookingApplication.classOfVehicles.size() > 0) {


                    tv_nearestcab.setText(R.string.chooseVehicleType);
                    tv_nearestcab.setLines(2);
                    tv_sendme.setVisibility(View.GONE);

                    if (!rl_send_nearest.isShown()) {
                        Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                        rl_send_nearest.startAnimation(rightInAnim);
                        rl_send_nearest.setVisibility(View.VISIBLE);
                    }
                } else
                    showCustomDialog(CODES.BOOKING_FAILED, R.string.Booking_Status, getResources().getString(R.string.NoServiceProvider, trip_requested.tripType.equalsIgnoreCase("FUT") ? getResources().getString(R.string.Later) : getResources().getString(R.string.Now)), 0, false);
            }
        }
        if (step < 4) {
            if (ll_selected_time.isShown()) {
                Animation outAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_selected_time.startAnimation(outAnim);
                ll_selected_time.setVisibility(View.GONE);
            }
            if (step == 3) {
                if (!ll_drop_address.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_drop_address.startAnimation(rightInAnim);
                    ll_drop_address.setVisibility(View.VISIBLE);
                }
                if (!ll_pn_pl.isShown()) {
                    Animation rightInAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_in_right);
                    ll_pn_pl.startAnimation(rightInAnim);
                    ll_pn_pl.setVisibility(View.VISIBLE);
                }
            }
        }
    }//gotoStepNumber

    /*-------------------------------------------------zoomToLastLocation----------------------------------------------------------------------------------*/
    private void zoomToLastLocation() {
        if (mapFragment != null) {
            if (lastLocation == null)
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                LatLng lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mapFragment.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, 18));
                if (lastLocationMarker != null)
                    lastLocationMarker.remove();
                lastLocationMarker = mapFragment.addMarker(new MarkerOptions().position(lastLatLng).title("").snippet("").icon(BitmapDescriptorFactory.fromResource(R.drawable.graydot)));
            }
        }
    }

    /*-----------------------------------------------------DrawPath----------------------------------------------------------------------------------------*/
    protected ArrayList<LatLng> DrawPath(MarkerOptions pickup, MarkerOptions dropoff) {
        if (pickup != null && dropoff != null) {
            routeDoc = mapDirections.getDocument(pickup.getPosition(), dropoff.getPosition(), GMapV2Direction.MODE_DRIVING);
            ArrayList<LatLng> directionPoint = mapDirections.getDirection(routeDoc);
            return directionPoint;
        }
        return new ArrayList<LatLng>();
    }

    /*------------------------------------------------ Custom  Dialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(final int reasonCode, int dialogTitle, final String dialogText, int imageResID, final Boolean showCancelBtn) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(false);

        final AlertDialog thisDialog = adb.create();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_layout, null);

        TextView title_text = (TextView) layout.findViewById(R.id.dialog_title);
        TextView text = (TextView) layout.findViewById(R.id.dialog_msg);
        if (imageResID > 0)
            title_text.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageResID, 0);
        title_text.setText(dialogTitle);
        text.setText(dialogText);
        Button btn_OK = (Button) layout.findViewById(R.id.btnYES);
        Button btn_CANCEL = (Button) layout.findViewById(R.id.btnNo);
        if (!showCancelBtn)
            btn_CANCEL.setVisibility(View.GONE);
        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();
                switch (reasonCode) {
                    case CODES.GPS_TOGGLE: {
                        Intent settingActivity = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        settingActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settingActivity);
                        break;
                    }
                    case CODES.WIFI_TOGGLE: {
                        Intent settingActivity = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        settingActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settingActivity);
                        break;
                    }
                    case CODES.START_MAPS: {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if (lastLocation != null)
                            intent.setData(android.net.Uri.parse("geo:" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "?z=18"));
                        else
                            intent.setData(android.net.Uri.parse("geo:0.0,0.0?z=18"));
                        if (intent.resolveActivity(getPackageManager()) != null)
                            startActivity(intent);
                        break;
                    }
                    case CODES.RESERVATION_NOVEHICLE: {
                        BookingApplication.makeReservation(trip_requested, ActivityMain.this, true);
                        trip_requested.isWallRequested = true;
                        break;
                    }

                    case CODES.BOOKING_FAILED: {
                        if (showCancelBtn)
                            BookingApplication.putOnWall(trip_requested.iServiceID, ActivityMain.this, true);
                        break;
                    }

                    case CODES.BOOKING_SUCCESS: {
                        if (trip_requested != null) {
                            Trip curr_trip = trip_requested;
                            BookingApplication.showTrackingScreen(ActivityMain.this, curr_trip);
                            ClearMap();
                        }
                        break;
                    }
                    case CODES.RESERVATION_SUCCESS: {
                        ClearMap();
                        break;
                    }
                    case CODES.PPV_RESPONSE: {
                        break;
                    }
                    case CODES.SIGNATURE_REQUIRED: {
                        BookingApplication.cancelTrip(trip_requested.ConfirmNumber, dialogText, trip_requested.iServiceID, trip_requested.tripType, ActivityMain.this);
                        break;
                    }
                    case CODES.PAYMENT_OPTION_ACTIVITY: {
                        BookingApplication.cancelTrip(trip_requested.ConfirmNumber, dialogText, trip_requested.iServiceID, trip_requested.tripType, ActivityMain.this);
                        break;
                    }
                    case CODES.EXIT: {
                        BookingApplication.exitAPP();
                    }
                }//switch
            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();
                switch (reasonCode) {
                    case CODES.BOOKING_FAILED: {
                        BookingApplication.cancelTrip(trip_requested.ConfirmNumber, dialogText, trip_requested.iServiceID, trip_requested.tripType, ActivityMain.this);
                        break;
                    }
                }//switch
            }
        });

        thisDialog.setView(layout);
        thisDialog.show();
    }

    /*-------------------------------------------------- callbackResponseReceived -------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressListReturned, boolean success) {

        try {
            switch (apiCalled) {
                case APIs.GETFAREINFO:
                    if (success) {

                        tv_fare_estimate.setText(jsonResponse.getString("responseMessage"));
                        if (dropMarker == null)
                            tv_mile_estimate.setText(getResources().getString(R.string.distance) + ": 1 " + BookingApplication.getAffiliateDistanceUnit(ActivityMain.this, jsonResponse.getInt("companyID")));

                        trip_requested.estimatedCost = jsonResponse.getString("estimatedFare");
                        trip_requested.currencySymbol = jsonResponse.getString("currencySymbol");

                        if (trip_requested.companyID != jsonResponse.getInt("companyID") && BookingApplication.isPassengerCountReq(ActivityMain.this, jsonResponse.getInt("companyID")))
                            showPassengerDialog(null);

                        trip_requested.companyID = jsonResponse.getInt("companyID");
                        trip_requested.companyName = BookingApplication.getAffiliateNameFromDB(ActivityMain.this, trip_requested.companyID);
                        trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, trip_requested.companyName);
                        tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, BookingApplication.getAffiliateNameFromDB(ActivityMain.this, trip_requested.companyID)));
                        ppvBalance = jsonResponse.getString("CustomerPPVBalance");
                        tv_payWithVoucher.setText(getResources().getString(R.string.PayWithVoucher, "SAR"+ ppvBalance));
                    } else {
                        showCustomDialog(CODES.BOOKING_FAILED, R.string.Booking_Status, jsonResponse.getString("ReasonPhrase"), 0, false);
                        goToStep(3, false);
                    }

                    break;
                case APIs.GETCUSTOMERRIDES:
                    trips_adapter.notifyDataSetChanged();
                    if (success)
                        BookingApplication.getFavorites();
                    break;
                case APIs.GETNEARBYVEHICLES: {
                    if (success) {
                        if (pickAddress != null) {
                            if (!selecting_pickup && !selecting_drop && !vehicle_balloon.isShown())
                                mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 13));
                            //mapFragment.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 130));

                            drawNearByVehicles();
                        }
                    } else {
                        BookingApplication.db.delete("Affiliates", null, null);
                        if (ll_drop_address.isShown() && !BookingApplication.syncRequired) {
                            ClearMap();
                            showCustomDialog(CODES.BOOKING_FAILED, R.string.Booking_Status, jsonResponse.getString("ReasonPhrase"), 0, false);
                        }
                    }

                    if (!ll_selected_time.isShown() || BookingApplication.syncRequired)
                        getNearbyVehicles(7 * 1000);
                }
                break;
                case APIs.PUTTRIPONWALL:
                    if (success)
                        showCustomDialog(CODES.RESERVATION_SUCCESS, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, false);
                    break;
                case APIs.MAKERESERVATION:
                    if (success) {
                        trip_requested.iServiceID = jsonResponse.getString("iServiceID");
                        trip_requested.state = "ACCEPTED";
                        BookingApplication.recentTrips.add(trip_requested);
                        BookingApplication.unPerformedTripsCount++;
                        Collections.sort(BookingApplication.recentTrips);
                        trips_adapter.notifyDataSetChanged();
                        if (jsonResponse.getInt("waitCountDown") == 0)
                            showCustomDialog(CODES.RESERVATION_SUCCESS, R.string.Booking_Status, getResources().getString(R.string.Booking_Success, jsonResponse.getString("confirmationNo")), 0, false);
                        else {
                            if (ll_FairCashCredit.isShown()) {
                                Animation outRightAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                                ll_FairCashCredit.startAnimation(outRightAnim);
                                ll_FairCashCredit.setVisibility(View.GONE);
                                tv_promo_available.setVisibility(View.GONE);
                            }
                            if(trip_requested.tripType.equalsIgnoreCase("FUT"))
                                startRequestStatusPolling(5);
                            else
                                startRequestStatusPolling(15);
                            BookingApplication.userInfoPrefs.edit().putInt("waitTimer", 15).commit();
                        }
                    } else if (jsonResponse.getInt("FaultCode") == CODES.BLACKLIST_LIMIT_REACHED)
                        showCustomDialog(CODES.BLACKLIST_LIMIT_REACHED, R.string.Booking_Status, jsonResponse.getString("ReasonPhrase"), 0, false);
                    else if (jsonResponse.getInt("FaultCode") == CODES.RESERVATION_MULTIPLE_REQUESTS)
                        showCustomDialog(CODES.RESERVATION_MULTIPLE_REQUESTS, R.string.Booking_Status, jsonResponse.getString("ReasonPhrase"), 0, false);
                    else if (jsonResponse.getInt("FaultCode") == CODES.RESERVATION_NOVEHICLE)
                        showCustomDialog(CODES.RESERVATION_NOVEHICLE, R.string.Booking_Status, jsonResponse.getString("ReasonPhrase"), 0, true);

                    break;
                case APIs.GETREQUESTSTATUS:
                    switch (jsonResponse.getInt("requestStatusCode")) {

                        case CODES.BOOKING_SUCCESS:
                            mCountDownTimer.cancel();
                            mCountDownTimer.onFinish();
                            trip_requested.iServiceID = jsonResponse.getString("iServiceID");
                            trip_requested.ConfirmNumber = jsonResponse.getString("confirmationNo");
                            if (trip_requested.tripType.equals("CURR"))
                                showCustomDialog(CODES.BOOKING_SUCCESS, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, false);
                            else if (trip_requested.tripType.equals("FUT"))
                                showCustomDialog(CODES.RESERVATION_SUCCESS, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, false);
                            break;

                        case CODES.SIGNATURE_REQUIRED:
                            mCountDownTimer.cancel();
                            mCountDownTimer.onFinish();
                            BookingApplication.showSignatureScreen(jsonResponse.getString("fareEstimate"), ActivityMain.this);
                            showCustomDialog(CODES.PAYMENT_OPTION_ACTIVITY, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, false);
                            break;

                        case CODES.PAYMENT_OPTION_ACTIVITY:
                            trip_requested.isPaymentDeclined = true;
                            mCountDownTimer.cancel();
                            mCountDownTimer.onFinish();
                            showCustomDialog(CODES.PAYMENT_OPTION_ACTIVITY, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, false);
                            break;

                        case CODES.BOOKING_FAILED:
                            showCustomDialog(CODES.BOOKING_FAILED, R.string.Booking_Status, jsonResponse.getString("responseMessage"), 0, jsonResponse.getBoolean("bShowWallOption"));
                            mCountDownTimer.cancel();
                            mCountDownTimer.onFinish();
                            break;

                    }
                    break;
                case APIs.SIGNATUREUPLOAD:
                    startRequestStatusPolling(BookingApplication.userInfoPrefs.getInt("waitTimer", 15));
                    break;
                case APIs.UPDATEPAYMENTINFO:
                    startRequestStatusPolling(BookingApplication.userInfoPrefs.getInt("waitTimer", 15));
                    break;
                case APIs.GeoCoderResponse:
                    // ll_bottom_views.setVisibility(View.VISIBLE);
                    String address = getResources().getString(R.string.unknown_address);
                    if (addressListReturned != null)
                        try {
                            if (this.addressList != null)
                                this.addressList.clear();

                            this.addressList = addressListReturned;
                            if (this.addressList.size() > 0) {
                                addressFound = true;
                                BookingApplication.currentAddress = this.addressList.get(0);
                                StringBuilder sb = new StringBuilder();
                                for (Address currAdd : this.addressList)
                                    if (sb.indexOf((currAdd.getAddressLine(0))) < 0) {
                                        if (sb.length() > 0)
                                            sb.append(", ");
                                        sb.append(currAdd.getAddressLine(0));
                                    }
                                address = sb.toString();
                            } else {
                                BookingApplication.currentAddress.setAddressLine(0, "");
                                BookingApplication.currentAddress.setLocality("");
                                BookingApplication.currentAddress.setAdminArea("");
                                BookingApplication.currentAddress.setPostalCode("");
                                BookingApplication.currentAddress.setCountryCode("");
                                BookingApplication.currentAddress.setLatitude(0.0);
                                BookingApplication.currentAddress.setLongitude(0.0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    tv_address.setText(address);
                    break;
            }
        } catch (Exception e) {
            BookingApplication.showCustomToast(0, e.getMessage(), true);
        }
    }

    /*------------------------------------------------------ ClearMap ------------------------------------------------------------------------------------*/
    private void ClearMap() {
        mapFragment.moveCamera(CameraUpdateFactory.newLatLng(pickMarkerOptions.getPosition()));
        mapFragment.clear();
        trip_requested = null;
        pickAddress = null;
        dropAddress = null;
        path = null;
        pickMarker = null;
        pickMarkerOptions = null;
        dropMarker = null;
        dropMarkerOptions = null;
        routeDoc = null;
        BookingApplication.nearByVehicles.clear();
        BookingApplication.nearbyVehiclesMarkers.clear();
        rl_vehicle_company_fare.setVisibility(View.GONE);
        layout_later.setVisibility(View.GONE);
        rl_send_nearest.setVisibility(View.GONE);
        et_pickup_person_name.setVisibility(View.GONE);
        et_callback_number.setVisibility(View.GONE);
        ll_cancelConfirm.setVisibility(View.GONE);
        ll_FairCashCredit.setVisibility(View.GONE);
        tv_promo_available.setVisibility(View.GONE);
        ll_selected_cab.setVisibility(View.GONE);
        ll_selected_time.setVisibility(View.GONE);
        ll_drop_address.setVisibility(View.GONE);
        ll_pick_address.setVisibility(View.GONE);
        ll_pn_pl.setVisibility(View.GONE);
        //cancel_drop.setVisibility(View.GONE);
        cancel_drop_seperator.setVisibility(View.GONE);
        tv_skip.setVisibility(View.GONE);
        vehicle_balloon.setVisibility(View.GONE);
        pointer.setImageResource(R.drawable.pupin);
        pointer.setVisibility(View.VISIBLE);
        et_driver_notes.setText("");
        et_pickup_person_name.setText("");
        et_callback_number.setText(BookingApplication.getUserSimNumber());
        tv_refresh.setText(R.string.Help_Mark);
        tv_refresh.setBackgroundResource(BookingApplication.textView_Background);
        SpannableString ss1=  new SpannableString(getResources().getString(R.string.pickme));
        ss1.setSpan(new RelativeSizeSpan(1.0f), (getResources().getString(R.string.pickme)).indexOf("\n"), ss1.length(), 0); // set size
        tv_pick_drop.setText(ss1);
        ll_bottom_views.setVisibility(View.VISIBLE);
        //rl_address_pointer.setVisibility(View.VISIBLE);
        selecting_pickup = true;
        selecting_drop = false;
    }

    /*------------------------------------------------------ getCurrentRides -------------------------------------------------------------------------------------*/
    private void getCurrentRides(final int time_msec) {
        if (!BookingApplication.ridesFetched)
            BookingApplication.fetchCustomerRides(ActivityMain.this, "all");
        try {
            requestStatustimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BookingApplication.ridesFetched = false;
                        }
                    });
                }
            }, time_msec);
        } catch (Exception e) {
            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
        }
    }

    /*------------------------------------------------------ startRequestStatusPolling -------------------------------------------------------------------*/
    private void startRequestStatusPolling(final int time_sec) {

        mCountDownTimer = new CountDownTimer(countDownLimit, 1000) {

            private boolean firstCall = true;

            @Override
            public void onTick(long millisUntilFinished) {
                tv_timer.setText(Long.toString(millisUntilFinished / 1000));

                if (trip_requested.isWallRequested || trip_requested.tripType.equalsIgnoreCase("FUT")) {
                    if (!BookingApplication.customProgressDialog.isShowing())
                        BookingApplication.showCustomProgress(ActivityMain.this, "", true);
                } else
                    timer_view.setVisibility(View.VISIBLE);

                if (millisUntilFinished / 1000 % time_sec == 0 && firstCall) {
                    firstCall = false;
                    BookingApplication.getRequestStatus(trip_requested.iServiceID, false, ActivityMain.this);
                } else if (!firstCall && millisUntilFinished / 1000 % 5 == 0)
                    BookingApplication.getRequestStatus(trip_requested.iServiceID, false, ActivityMain.this);
                else if (!firstCall && millisUntilFinished <= 2000)
                    BookingApplication.getRequestStatus(trip_requested.iServiceID, true, ActivityMain.this);
            }

            @Override
            public void onFinish() {

                if (trip_requested.isWallRequested)
                    BookingApplication.customProgressDialog.dismiss();
                else
                    timer_view.setVisibility(View.GONE);

                if (tv_selected_time.isShown()) {
                    ll_FairCashCredit.setVisibility(View.VISIBLE);
                    if (BookingApplication.promotions.size() > 0)
                        tv_promo_available.setVisibility(View.VISIBLE);
                }
            }
        };
        mCountDownTimer.start();
    }

    /*------------------------------------------------------ onLocationChanged -------------------------------------------------------------------*/
    @Override
    public void onLocationChanged(Location loc) {
        if (auto_zoom || lastLocation == null || lastLocation.distanceTo(loc) > 1000) {
            //getFromLocation(loc.getLatitude(), loc.getLongitude(), 2);
            lastLocation = loc;
        }

        if (lastLocationMarker != null)
            lastLocationMarker.remove();
        if (auto_zoom) {
            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 18));
            tv_address.setText((BookingApplication.currentAddress.getAddressLine(0) + BookingApplication.currentAddress.getLocality()).trim().startsWith("null") ? "" : ", " + BookingApplication.currentAddress.getLocality());
            auto_zoom = false;
        }
    }

    /*------------------------------------------------------ getFromLocation -------------------------------------------------------------------*/
    private void getFromLocation(double latitude, double longitude, int numOfResults) {

        final double lat = latitude;
        final double lon = longitude;
        final int numResults = numOfResults;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ll_bottom_views.setVisibility(View.VISIBLE);
                        }
                    });
                    final List<Address> addressList = geocoder.getFromLocation(lat, lon, numResults);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callbackResponseReceived(APIs.GeoCoderResponse, null, addressList, true);
                        }
                    });

                    final List<Place> places = google.getNearbyPlaces(lat, lon, 300, new Param[]{new Param("types").value("establishment"), new Param("rankBy").value("distance")});

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            placesList.clear();
                            placesList.addAll(places);
                            placesAdaptor.notifyDataSetChanged();
                            ll_getting_nearby_progress.setVisibility(View.GONE);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
                        }
                    });
                }
            }
        }).start();
    }

    /*---------------------------------------------------------------------------------------------------------------------
     *------------------------------------- DrawPathTask AsyncTask --------------------------------------------------------
     *---------------------------------------------------------------------------------------------------------------------
     */
    private class DrawPathTask extends AsyncTask<MarkerOptions, Void, ArrayList<LatLng>> {

        @Override
        protected ArrayList<LatLng> doInBackground(MarkerOptions... params) {
            return DrawPath(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> directionPoint) {
            super.onPostExecute(directionPoint);
            if (trip_requested != null)
                try {

                    if (path != null) {
                        path.remove();
                        path = null;
                    }
                    if (directionPoint.size() > 0) {
                        PolylineOptions rectLine = new PolylineOptions().width(14).color(Color.argb(255, 114, 208, 251));

                        for (int i = 0; i < directionPoint.size(); i++) {
                            rectLine.add(directionPoint.get(i));
                            //boundsBuilder.include(directionPoint.get(i));
                        }

                        path = mapFragment.addPolyline(rectLine);

                        //mapFragment.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 130));
                        mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 13));
                    }

                    if (selecting_pickup || dragging_pickup) {
                        selecting_pickup = false;
                        dragging_pickup = false;
                    }

                    //if (trip_requested == null)
                    //trip_requested = new Trip(pickMarker.getPosition().latitude, pickMarker.getPosition().longitude, tv_pick_address.getText().toString(), -1, -1);

                    if (routeDoc != null) {

                        trip_requested.estimatedDistance = mapDirections.getDistanceValue(routeDoc);
                        trip_requested.estimatedDuration = Integer.toString(mapDirections.getDurationValue(routeDoc));
                        trip_requested.distanceUnit = mapDirections.getDistanceText(routeDoc).contains("km") ? "km" : "mi";
                        tv_mile_estimate.setText(getResources().getString(R.string.distance) + ": " + mapDirections.getDistanceText(routeDoc));
                    }

                } catch (Exception e) {
                    Toast.makeText(ActivityMain.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();

                    if (BookingApplication.customProgressDialog != null)
                        while (BookingApplication.customProgressDialog.isShowing())
                            BookingApplication.customProgressDialog.dismiss();
                } finally {

                    if (selecting_pickup || dragging_pickup) {
                        selecting_pickup = false;
                        dragging_pickup = false;
                    }

                    if (BookingApplication.customProgressDialog != null)
                        while (BookingApplication.customProgressDialog.isShowing())
                            BookingApplication.customProgressDialog.dismiss();

                    if (trip_requested.companyID > 0)
                        goToStep(5, false);
                    else if (BookingApplication.trip_type.equalsIgnoreCase("CURR"))
                        picknow(null);
                    else if (BookingApplication.trip_type.equalsIgnoreCase("FUT"))
                        picklater(null);
                }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BookingApplication.showCustomProgress(ActivityMain.this, "", true);
            if (ll_pn_pl.isShown()) {
                Animation rightOutAnim = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.slide_out_right);
                ll_pn_pl.startAnimation(rightOutAnim);
                ll_pn_pl.setVisibility(View.GONE);
            }
        }
    }

    /*---------------------------------------------------------------------------------------------------------------------
     *-------------------------------------------- TripAdapter ------------------------------------------------------------
     *---------------------------------------------------------------------------------------------------------------------
     */
    public class TripAdapter extends ArrayAdapter<Trip> {

        private final ArrayList<Trip> tripList;
        private final int tripViewResource;
        protected boolean favorite_clicked = false;
        private Context myContext;

        public TripAdapter(Context context, int tripViewResourceId, ArrayList<Trip> mTrips) {

            super(context, tripViewResourceId, mTrips);
            myContext = context;
            tripViewResource = tripViewResourceId;
            tripList = mTrips;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns the position of the specified item in the array.
         *
         * @param item The item to retrieve the position of.
         * @return The position of the specified item, matching the Lat Long.
         * @author Muhammad Zahid
         */
        @Override
        public int getPosition(Trip item) {
            for (int i = 0; i < tripList.size(); i++)
                if (tripList.get(i).ConfirmNumber.equals(item.ConfirmNumber))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final Trip currentTrip = tripList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(tripViewResource, null);
            }
            if (currentTrip != null) {

                final TextView time = (TextView) currentView.findViewById(R.id.tv_time);
                final TextView date = (TextView) currentView.findViewById(R.id.tv_date);
                final TextView pickup = (TextView) currentView.findViewById(R.id.tv_from);
                final TextView drop = (TextView) currentView.findViewById(R.id.tv_to);
                final TextView bookingID = (TextView) currentView.findViewById(R.id.tv_bookingID);
                final ImageView tripstatus = (ImageView) currentView.findViewById(R.id.iv_status_icon);
                //final ImageView chooseFrom = (ImageView) currentView.findViewById(R.id.iv_choose_from);
                //final ImageView chooseTo = (ImageView) currentView.findViewById(R.id.iv_choose_to);
                //chooseFrom.setOnClickListener(new OnClickListener() {
                //
                //    @Override
                //    public void onClick(View arg0) {
                //        history.setVisibility(View.GONE);
                //        Address addr = new Address(Locale.getDefault());
                //        addr.setAddressLine(0, currentTrip.PUaddress);
                //        addr.setLocality(currentTrip.PUcity);
                //        addr.setAdminArea(currentTrip.PUstate);
                //        addr.setPostalCode(currentTrip.PUZip);
                //        addr.setCountryCode(currentTrip.PUcountry);
                //        addr.setLatitude(currentTrip.PUlat);
                //        addr.setLongitude(currentTrip.PUlong);
                //        PickDropFavChosen(addr);
                //    }
                //});
                //
                //chooseTo.setOnClickListener(new OnClickListener() {
                //
                //    @Override
                //    public void onClick(View arg0) {
                //        history.setVisibility(View.GONE);
                //        Address addr = new Address(Locale.getDefault());
                //        addr.setAddressLine(0, currentTrip.DOaddress);
                //        addr.setLocality(currentTrip.DOcity);
                //        addr.setAdminArea(currentTrip.DOstate);
                //        addr.setPostalCode(currentTrip.DOZip);
                //        addr.setCountryCode(currentTrip.DOcountry);
                //        addr.setLatitude(currentTrip.DOlat);
                //        addr.setLongitude(currentTrip.DOlong);
                //        PickDropFavChosen(addr);
                //    }
                //});

                if (currentTrip.state.equalsIgnoreCase("DROPPED"))
                    tripstatus.setImageResource(R.drawable.icondone);
                else if (currentTrip.state.equalsIgnoreCase("CANCELLED"))
                    tripstatus.setImageResource(R.drawable.iconcancel);
                else if (currentTrip.state.equalsIgnoreCase("IRTPU"))
                    tripstatus.setImageResource(R.drawable.irtpu);
                else if (currentTrip.state.equalsIgnoreCase("IRTDO") || currentTrip.state.equalsIgnoreCase("PICKEDUP"))
                    tripstatus.setImageResource(R.drawable.icon_picked);
                else if (currentTrip.state.equalsIgnoreCase("HOLDSIGREQD") || currentTrip.state.equalsIgnoreCase("SALESIGREQD"))
                    tripstatus.setImageResource(R.drawable.signature);
                else if (currentTrip.state.equalsIgnoreCase("TRIPOFFERED") || currentTrip.state.equalsIgnoreCase("ACCEPTED") || currentTrip.state.equalsIgnoreCase("TSPACCEPTED")
                        || currentTrip.state.equalsIgnoreCase("ASSIGNED") || currentTrip.state.equalsIgnoreCase("DISPATCHED"))
                    tripstatus.setImageResource(R.drawable.waiting);
                else
                    tripstatus.setImageResource(R.drawable.icon_unknown);

                time.setText(BookingApplication.timeFormat.format(currentTrip.PUDateTime.getTime()));
                date.setText(BookingApplication.dateFormat.format(currentTrip.PUDateTime.getTime()));
                pickup.setText(currentTrip.PUaddress);
                if (currentTrip.DOlat > 0)
                    drop.setText(currentTrip.DOaddress);
                bookingID.setText(currentTrip.ConfirmNumber);

                currentView.setTag(currentTrip.ConfirmNumber);

                currentView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        if (currentTrip.state.equalsIgnoreCase("HOLDSIGREQD") || currentTrip.state.equalsIgnoreCase("SALESIGREQD")) {
                            ClearMap();
                            trip_requested = currentTrip;
                            BookingApplication.showSignatureScreen(currentTrip.estimatedCost, ActivityMain.this);
                        } else if (!currentTrip.state.equalsIgnoreCase("CANCELLED"))
                            BookingApplication.showTrackingScreen(ActivityMain.this, currentTrip);
                    }
                });
            }
            return currentView;
        }

    } // TripAdapter Class

    /*---------------------------------------------------------------------------------------------------------------------
     *----------------------------------------- VehicleListAdapter --------------------------------------------------------
     *---------------------------------------------------------------------------------------------------------------------
     */
    public class VehicleListAdapter extends ArrayAdapter<String> {

        private ArrayList<String> vehicles;

        public VehicleListAdapter(Context context, int textViewResourceId, ArrayList<String> mVehicles) {

            super(context, textViewResourceId, mVehicles);
            vehicles = mVehicles;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_vehicles, parent, false);

            //int vehIconRes = BookingApplication.getVehicleDrawable(BookingApplication.classOfVehicles.get().vehicles_images.get(position));

            TextView vehName = (TextView) row.findViewById(R.id.vehTypeText);
            vehName.setPadding(0, 10, 0, 0);
            vehName.setText(vehicles.get(position));
            //vehName.setCompoundDrawablesWithIntrinsicBounds(0, vehIconRes, 0, 0);

            return row;
        }

    } // VehicleListAdapter Class

    /*---------------------------------------------------------------------------------------------------------------------
     *---------------------------------------- AffiliateListAdapter -------------------------------------------------------
     *---------------------------------------------------------------------------------------------------------------------
     */
    public class AffiliateListAdapter extends ArrayAdapter<String> {

        private ArrayList<String> affiliates;

        public AffiliateListAdapter(Context context, int textViewResourceId, ArrayList<String> mAffiliates) {

            super(context, textViewResourceId, mAffiliates);
            affiliates = mAffiliates;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_vehicles, parent, false);

            TextView vehName = (TextView) row.findViewById(R.id.vehTypeText);
            vehName.setPadding(0, 20, 0, 20);
            vehName.setText(affiliates.get(position));

            return row;
        }
    } // AffiliateListAdapter Class

    /*---------------------------------------------------------------------------------------------------------------------
     *--------------------------------------- ClassOfVehicleAdapter -------------------------------------------------------
     *---------------------------------------------------------------------------------------------------------------------
     */
    public class ClassOfVehicleAdapter extends ArrayAdapter<ClassOfVehicle> {

        private ArrayList<ClassOfVehicle> vehicle_classes;

        public ClassOfVehicleAdapter(Context context, int viewResourceId, ArrayList<ClassOfVehicle> vehicleClasses) {

            super(context, viewResourceId, vehicleClasses);
            vehicle_classes = vehicleClasses;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            final ClassOfVehicle currClassOfVehicle = BookingApplication.classOfVehicles.get(position);

            LayoutInflater inflater = getLayoutInflater();
            row = inflater.inflate(R.layout.list_item_vehicle_class, parent, false);

            ImageView classImage = (ImageView) row.findViewById(R.id.iv_class_image);
            int vehIconRes = BookingApplication.getVehicleDrawable(currClassOfVehicle.classLogoLink);
            classImage.setBackgroundResource(vehIconRes);

            TextView vehClass = (TextView) row.findViewById(R.id.tv_vehicle_class);
            vehClass.setText(currClassOfVehicle.className);

            RatingBar availability = (RatingBar) row.findViewById(R.id.availability_bar);
            availability.setRating(currClassOfVehicle.availability);

            TextView tv_dropRequired = (TextView) row.findViewById(R.id.tv_dropoff_required);

            final TextView classRate = (TextView) row.findViewById(R.id.tv_class_rate);
            if (trip_requested.estimatedDistance > 0) {
                tv_dropRequired.setVisibility(View.GONE);
            } else {
                tv_dropRequired.setVisibility(View.VISIBLE);
            }
            final Spinner spinner_vehicles = (Spinner) row.findViewById(R.id.spinner_vehicles);
            final Spinner spinner_affiliates = (Spinner) row.findViewById(R.id.spinner_affiliates);

            vehAdaptor = new VehicleListAdapter(ActivityMain.this, R.layout.list_item_vehicles, currClassOfVehicle.vehicles);
            spinner_vehicles.setAdapter(vehAdaptor);

            affAdaptor = new AffiliateListAdapter(ActivityMain.this, R.layout.list_item_vehicles, currClassOfVehicle.affiliates);
            spinner_affiliates.setAdapter(affAdaptor);

            spinner_vehicles.setOnItemSelectedListener(new OnItemSelectedListener() {
                protected boolean isInitialized = false;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currClassOfVehicle.getAffiliatesFromDB(ActivityMain.this, "", pickNow, affAdaptor);
                    if (currClassOfVehicle.affiliates.size() > 0) {
                        try {
                            rates = BookingApplication.getRatesFromDB(ActivityMain.this, currClassOfVehicle.vehicles.get(position), spinner_affiliates.getSelectedItem().toString(), currClassOfVehicle.className);
                            if (rates != null) {
                                //InitialFare, CostPerUnit, VehRateUnit, CurrencyUnit, ADU, AddPassengerCost
                                if (trip_requested.estimatedDistance > 0) {
                                    if (trip_requested.distanceUnit.equals("km"))
                                        classRate.setText(rates[3] + " " + BookingApplication.df.format(Double.parseDouble(rates[0]) + Double.parseDouble(rates[1]) * trip_requested.estimatedDistance / 1000 / Double.parseDouble(rates[4])));
                                    else
                                        classRate.setText(rates[3] + " " + BookingApplication.df.format(Double.parseDouble(rates[0]) + Double.parseDouble(rates[1]) * trip_requested.estimatedDistance / 1609.34 / Double.parseDouble(rates[4])));
                                }
                            }
                        } catch (Exception e) {
                            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
                            classRate.setText("N/A");
                        }
                        trip_requested.companyName = spinner_affiliates.getSelectedItem().toString();
                        trip_requested.companyID = BookingApplication.getAffiliateIDFromDB(ActivityMain.this, trip_requested.companyName);
                        trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, trip_requested.companyName);
                        trip_requested.vehTypeName = currClassOfVehicle.vehicles.get(position);
                        trip_requested.vehTypeID = BookingApplication.getVehicleIDFromDB(ActivityMain.this, trip_requested.vehTypeName);
                        trip_requested.vehNo = "-1";

                        tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, trip_requested.companyName));

                        if (isInitialized)
                            if (BookingApplication.isPassengerCountReq(ActivityMain.this, trip_requested.companyID))
                                showPassengerDialog(null);
                            else {
                                trip_requested.passengerCount = 1;
                                trip_requested.childrenCount = 0;
                                trip_requested.bagsCount = 0;
                                //BookingApplication.getFareInfo(trip_requested, ActivityMain.this);
                            }
                        else
                            isInitialized = true;
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            spinner_affiliates.setOnItemSelectedListener(new OnItemSelectedListener() {
                protected boolean isInitialized = false;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (currClassOfVehicle.affiliates.size() > 0) {
                        currClassOfVehicle.getVehiclesFromDB(ActivityMain.this, currClassOfVehicle.affiliates.get(position), pickNow, vehAdaptor);

                        try {
                            rates = BookingApplication.getRatesFromDB(ActivityMain.this, currClassOfVehicle.vehicles.get(spinner_vehicles.getSelectedItemPosition()), currClassOfVehicle.affiliates.get(position), currClassOfVehicle.className);
                            if (rates != null) {
                                //InitialFare, CostPerUnit, VehRateUnit, CurrencyUnit, ADU, AddPassengerCost
                                if (trip_requested.estimatedDistance > 0) {
                                    if (trip_requested.distanceUnit.equals("km"))
                                        classRate.setText(rates[3] + " " + BookingApplication.df.format(Double.parseDouble(rates[0]) + Double.parseDouble(rates[1]) * trip_requested.estimatedDistance / 1000 / Double.parseDouble(rates[4])));
                                    else
                                        classRate.setText(rates[3] + " " + BookingApplication.df.format(Double.parseDouble(rates[0]) + Double.parseDouble(rates[1]) * trip_requested.estimatedDistance / 1609.34 / Double.parseDouble(rates[4])));
                                }
                            }
                        } catch (Exception e) {
                            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
                            classRate.setText("N/A");
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

            Button btn_proceed = (Button) row.findViewById(R.id.btn_proceed_booking);
            btn_proceed.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currClassOfVehicle.affiliates.size() > 0) {
                        trip_requested.classofserviceid = currClassOfVehicle.classID;
                        trip_requested.companyName = spinner_affiliates.getSelectedItem().toString();
                        trip_requested.companyID = BookingApplication.getAffiliateIDFromDB(ActivityMain.this, trip_requested.companyName);
                        trip_requested.supportedPaymentType = BookingApplication.getAffiliatePaymentType(ActivityMain.this, trip_requested.companyName);
                        trip_requested.vehTypeName = spinner_vehicles.getSelectedItem().toString();
                        trip_requested.vehTypeID = BookingApplication.getVehicleIDFromDB(ActivityMain.this, trip_requested.vehTypeName);
                        trip_requested.vehNo = "-1";

                        tv_selectedcab.setText(getResources().getString(R.string.cabfrom, trip_requested.vehTypeName, trip_requested.companyName));

                        if (BookingApplication.isPassengerCountReq(ActivityMain.this, trip_requested.companyID))
                            showPassengerDialog(null);
                        else {
                            trip_requested.passengerCount = 1;
                            trip_requested.childrenCount = 0;
                            trip_requested.bagsCount = 0;
                            //BookingApplication.getFareInfo(trip_requested, ActivityMain.this);
                        }

                        goToStep(5, false);
                    }
                }
            });
            return row;
        }
    } // ClassOfVehicleAdapter Class

    public class NearbyPlacesAdapter extends ArrayAdapter<Place> {

        private final ArrayList<Place> placeList;
        private final int textViewResource;
        Address addrs = new Address(Locale.US);
        private Context myContext;

        public NearbyPlacesAdapter(Context context, int textViewResourceId, ArrayList<Place> mPlaces) {

            super(context, textViewResourceId, mPlaces);
            myContext = context;
            textViewResource = textViewResourceId;
            placeList = mPlaces;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public int getPosition(Place item) {
            for (int i = 0; i < placeList.size(); i++)
                if (placeList.get(i).equals(item))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final Place currentPlace = placeList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(textViewResource, null);
            }
            if (currentPlace != null) {

                TextView caption = (TextView) currentView.findViewById(R.id.tv_fav_header);
                TextView tv_fav_address = (TextView) currentView.findViewById(R.id.tv_fav_address);
                CheckBox cb_fav = (CheckBox) currentView.findViewById(R.id.cb_fav);

                cb_fav.setVisibility(View.GONE);
                caption.setText(currentPlace.getName());
                tv_fav_address.setText(currentPlace.getVicinity());

                currentView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Address addr = addressList.get(0);
                        addr.setAddressLine(0, currentPlace.getName() + ", " + currentPlace.getVicinity());
                        addr.setLatitude(currentPlace.getLatitude());
                        addr.setLongitude(currentPlace.getLongitude());
                        //addr.setLocality(currentPlace.);
                        //addr.setAdminArea(currentPlace.state);
                        //addr.setPostalCode(currentPlace.zip);
                        //addr.setCountryCode(currentPlace.country);

                        PickDropFavChosen(addr);
                    }
                });

                currentView.setTag(currentPlace.getPlaceId());
            }
            return currentView;
        }
    } // NearbyPlacesAdapter Class

    public class PromotionsAdapter extends ArrayAdapter<BookingApplication.Campaign> {

        private final ArrayList<BookingApplication.Campaign> promosDetailList;
        private final int viewResourceId;
        private Context myContext;

        public PromotionsAdapter(Context context, int _viewResourceId, ArrayList<BookingApplication.Campaign> promoList) {

            super(context, _viewResourceId, promoList);
            myContext = context;
            viewResourceId = _viewResourceId;
            promosDetailList = promoList;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        /**
         * Returns the position of the specified item in the array.
         *
         * @param item The item to retrieve the position of.
         * @return The position of the specified item, matching the Lat Long.
         * @author Muhammad Zahid
         */
        @Override
        public int getPosition(BookingApplication.Campaign item) {
            for (int i = 0; i < promosDetailList.size(); i++)
                if (promosDetailList.get(i).PromoCode.equalsIgnoreCase(item.PromoCode))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final BookingApplication.Campaign currentPromotion = promosDetailList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(viewResourceId, null);
            }
            if (currentPromotion != null) {

                ImageView companyLogo = (ImageView) currentView.findViewById(R.id.iv_promo_type);
                TextView tv_promo_type = (TextView) currentView.findViewById(R.id.tv_promo_type);
                TextView tv_PromotionCode = (TextView) currentView.findViewById(R.id.tv_PromotionCode);
                TextView tv_promo_balance = (TextView) currentView.findViewById(R.id.tv_promo_balance);
                TextView tv_promo_expiry = (TextView) currentView.findViewById(R.id.tv_promo_expiry);
                tv_promo_expiry.setVisibility(View.GONE);

                tv_promo_type.setText(currentPromotion.CampaignName);
                tv_PromotionCode.setText(currentPromotion.PromoCode);
                tv_promo_balance.setText(currentPromotion.Balance);

                if (currentPromotion.PromoURL.length() > 0)
                    BookingApplication.imagedownloader.DisplayImage(currentPromotion.PromoURL, companyLogo);

            }
            return currentView;
        }
    } // PromotionDetailAdapter Class

}
