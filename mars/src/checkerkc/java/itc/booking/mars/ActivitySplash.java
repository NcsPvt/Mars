package itc.booking.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import itc.booking.mars.BookingApplication.APIs;
import itc.booking.mars.BookingApplication.CODES;
import itcurves.mars.R;

public class ActivitySplash extends Activity implements CallbackResponseListener {

    private TextView promotion_message, tv_app_version, menu_header, tv_help, tv_quick_booking, tv_detail_booking, tv_now_booking, tv_later_booking;
    private ImageView iv_main_logo, iv_company_logo, iv_menu, iv_splash_parent;
    private ListView tripsListView;
    private TripAdapter trips_adapter;
    private Timer requestTimer;
    private ScrollView layout_menu;
    private RelativeLayout layout_history, rl_Selected_Promo;
    private TimerTask tt;
    private LinearLayout ll_quick_detail, ll_now_later, ll_now_booking, ll_later_booking, ll_quick_booking, ll_detail_booking;

    public class TripAdapter extends ArrayAdapter<Trip> {

        private final ArrayList<Trip> tripList;
        private final int tripViewResource;
        private Context myContext;
        protected boolean favorite_clicked = false;

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
                            BookingApplication.showSignatureScreen(currentTrip.estimatedCost, ActivitySplash.this);
                        } else if (!currentTrip.state.equalsIgnoreCase("CANCELLED"))
                            BookingApplication.showTrackingScreen(ActivitySplash.this, currentTrip);
                    }
                });
            }
            return currentView;
        }

    } // TripAdapter Class

    /*--------------------------------------------------onCreate----------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BookingApplication.callerContext = this;
        BookingApplication.setMyLanguage(BookingApplication.userInfoPrefs.getString("lang", "en"));
        BookingApplication.setMyTheme(ActivitySplash.this);
        try {
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // Removes

            setContentView(R.layout.activity_splash);

            iv_splash_parent = (ImageView) findViewById(R.id.iv_splash_parent);
            iv_company_logo = (ImageView) findViewById(R.id.iv_company_logo);
            iv_main_logo = (ImageView) findViewById(R.id.iv_main_logo);
            iv_menu = (ImageView) findViewById(R.id.iv_menu_splash);
            iv_menu.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
            ll_quick_booking = (LinearLayout) findViewById(R.id.ll_quick_booking);
            tv_quick_booking = (TextView) findViewById(R.id.tv_quick_booking);
            tv_quick_booking.setBackgroundResource(BookingApplication.button_Background);
            ll_detail_booking = (LinearLayout) findViewById(R.id.ll_detail_booking);
            tv_detail_booking = (TextView) findViewById(R.id.tv_detail_booking);
            tv_detail_booking.setBackgroundResource(BookingApplication.button_Background);
            ll_now_later = (LinearLayout) findViewById(R.id.ll_now_later);
            ll_quick_detail = (LinearLayout) findViewById(R.id.ll_quick_detail);
            ll_now_booking = (LinearLayout) findViewById(R.id.ll_now_booking);
            tv_now_booking = (TextView) findViewById(R.id.tv_now_booking);
            tv_now_booking.setBackgroundResource(BookingApplication.button_Background);
            ll_later_booking = (LinearLayout) findViewById(R.id.ll_later_booking);
            tv_later_booking = (TextView) findViewById(R.id.tv_later_booking);
            tv_later_booking.setBackgroundResource(BookingApplication.button_Background);
            layout_menu = (ScrollView) findViewById(R.id.layout_menu);
            layout_history = (RelativeLayout) findViewById(R.id.layout_history);
            rl_Selected_Promo = (RelativeLayout) findViewById(R.id.rl_Selected_Promo);
            promotion_message = (TextView) findViewById(R.id.promotion_message);
            promotion_message.setText(BookingApplication.userInfoPrefs.getString("PromotionMsg", ""));
            menu_header = (TextView) findViewById(R.id.menu_header);
            menu_header.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
            tv_help = (TextView) findViewById(R.id.tv_help);
            tv_help.setBackgroundResource(BookingApplication.textView_Background);
            tv_help.setTextColor(getResources().getColor(BookingApplication.theme_color));
            tv_app_version = (TextView) findViewById(R.id.tv_app_version);
            tv_app_version.setText(getResources().getString(R.string.version, BookingApplication.appVersion));
            tv_app_version.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showServerSettings(null);
                    return false;
                }
            });
            tripsListView = (ListView) findViewById(R.id.list_booked);

            trips_adapter = new TripAdapter(this, R.layout.list_item_fromto_trip, BookingApplication.recentTrips);
            tripsListView.setAdapter(trips_adapter);

            iv_splash_parent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (BookingApplication.checkPlayServices()) {

                        String regid = BookingApplication.getRegistrationId(ActivitySplash.this);

                        if (regid.isEmpty())
                            BookingApplication.registerGCMInBackground(getResources().getString(R.string.project_id));

                        Log.d("PushMsgsToken", regid);

                        if (BookingApplication.bHandshakeSuccess)
                            try {
                                if (!BookingApplication.userInfoPrefs.getString("UserID", "").equalsIgnoreCase("") && !BookingApplication.phoneNumber.equalsIgnoreCase("")) {
                                    BookingApplication.isAutoLogin = true;
                                    BookingApplication.performLogin(BookingApplication.phoneNumber, BookingApplication.password, BookingApplication.code, ActivitySplash.this);
                                } else {
                                    BookingApplication.showLoginScreen(ActivitySplash.this);
                                }
                            } catch (Exception e) {
                                Toast.makeText(ActivitySplash.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                    }
                }
            });

            String bottomLogo = BookingApplication.userInfoPrefs.getString("BottomLogoImage", "");
            if ((bottomLogo.length() > 0) && (bottomLogo.endsWith("png") || bottomLogo.endsWith("jpg")))
                BookingApplication.imagedownloader.DisplayImage(bottomLogo, iv_company_logo);

            String mainLogo = BookingApplication.userInfoPrefs.getString("MainLogoImage", "");
            if ((mainLogo.length() > 0) && (mainLogo.endsWith("png") || mainLogo.endsWith("jpg")))
                BookingApplication.imagedownloader.DisplayImage(mainLogo, iv_main_logo);

            String backgroundImage = BookingApplication.userInfoPrefs.getString("BackgroundImage", "");
            if ((backgroundImage.length() > 0) && (backgroundImage.endsWith("png") || backgroundImage.endsWith("jpg")))
                BookingApplication.imagedownloader.DisplayImage(backgroundImage, iv_splash_parent);

        } catch (Exception e) {
            Toast.makeText(ActivitySplash.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /*------------------------------------------------- onResume ---------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.isMinimized = false;
        BookingApplication.callerContext = this;
        layout_history.setVisibility(View.GONE);
        if (ll_now_later.isShown()) {
            ll_now_later.setVisibility(View.GONE);
            ll_quick_detail.setVisibility(View.VISIBLE);
        }

        requestTimer = new Timer();
        if (!BookingApplication.bHandshakeSuccess) {
            BookingApplication.showCustomProgress(ActivitySplash.this, "", true);
            tt = new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String uri = "";

                            try {
                                FileInputStream input = getApplicationContext().openFileInput("gaInstallData");
                                byte[] bytes = new byte['?'];
                                int readLen = input.read(bytes);
                                if (input.available() > 0)
                                    input.close();
                                else if (readLen <= 0) {
                                    Log.e("Analytics", "clientId file seems empty.");
                                    input.close();
                                } else {
                                    uri = new String(bytes, 0, readLen);
                                    BookingApplication.appID = uri;
                                    BookingApplication.userInfoPrefs.edit().putString("appID", BookingApplication.appID).commit();
                                    input.close();
                                }
                            } catch (FileNotFoundException e) {
                                Log.e("Analytics", "file not found");
                            } catch (IOException e) {
                                Log.e("Analytics", "Error reading gaClientId file.");
                            }

                            Toast.makeText(ActivitySplash.this, BookingApplication.appID, Toast.LENGTH_SHORT).show();
                            JSONObject response = new JSONObject();
                            callbackResponseReceived(APIs.RegisterForPushMessages, response, null, true);
                        }

                    });
                }
            };

            requestTimer.schedule(tt, 2000);
        }
    }

    /*------------------------------------------------- onStart ---------------------------------------------------------------*/
    @Override
    public void onStart() {
        super.onStart();
    }

    /*--------------------------------------------- onStop -----------------------------------------------------------------*/
    @Override
    public void onStop() {
        super.onStop();
    }

    /*------------------------------------------------ showServerSettings ---------------------------------------------------------------------------------*/
    public void showServerSettings(View v) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_favorites, null);

        final BookingApplication.CustomDialog bb = new BookingApplication.CustomDialog(this, layout);

        TextView title_text = (TextView) layout.findViewById(R.id.fav_dialog_title);
        title_text.setText("SERVER IP String");

        final EditText address_name = (EditText) layout.findViewById(R.id.fav_name);
        TextView streetAddress = (TextView) layout.findViewById(R.id.fav_address);

        address_name.setText(BookingApplication.SERVER_IP);
        streetAddress.setVisibility(View.GONE);

        TextView btn_OK = (TextView) layout.findViewById(R.id.btnSAVE);
        TextView btn_CANCEL = (TextView) layout.findViewById(R.id.btnCANCEL);

        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BookingApplication.userInfoPrefs.edit().putString("ServerIP", address_name.getText().toString()).commit();
                finish();
                BookingApplication.restartAPP(ActivitySplash.this);
                bb.dismiss();
            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bb.dismiss();

            }
        });

        bb.show();

    }

    /*---------------------------------------------- showHomeScreen -------------------------------------------------------------------------------------*/
    public void showHomeScreen(View v) {
        if (layout_history.isShown())
            layout_history.setVisibility(View.GONE);
        if (ll_now_later.isShown()) {
            ll_now_later.setVisibility(View.GONE);
            ll_quick_detail.setVisibility(View.VISIBLE);
        }
        if (layout_menu.isShown()) {
            Animation outAnim = AnimationUtils.loadAnimation(ActivitySplash.this, R.anim.slide_out_left);
            layout_menu.startAnimation(outAnim);
            layout_menu.setVisibility(View.GONE);
        }
    }

    /*------------------------------------------------- onBackPressed --------------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if (layout_history.isShown())
            layout_history.setVisibility(View.GONE);
        else if (layout_menu.isShown()) {
            Animation outAnim = AnimationUtils.loadAnimation(ActivitySplash.this, R.anim.slide_out_left);
            layout_menu.startAnimation(outAnim);
            layout_menu.setVisibility(View.GONE);
        } else if (ll_now_later.isShown()) {
            ll_now_later.setVisibility(View.GONE);
            ll_quick_detail.setVisibility(View.VISIBLE);
        } else
            showCustomDialog(CODES.EXIT, BookingApplication.appID.substring(BookingApplication.appID.lastIndexOf('_') + 1), getResources().getString(R.string.Exit_Application), R.drawable.exit, true, "");
    }

    /*------------------------------------------------ showCustomDialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(final int reasonCode, String dialogTitle, final String dialogText, int imageResID, final Boolean showCancelBtn, final String url) {
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
                    case CODES.SOFTWARE_UPDATE: {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        System.exit(0);
                        break;
                    }
                    case CODES.PPV_RESPONSE: {
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
                    case CODES.SOFTWARE_UPDATE: {
                        System.exit(0);
                        break;
                    }
                }
            }
        });

        thisDialog.setView(layout);
        thisDialog.show();
    }

    /*---------------------------------------------- showHelpView -------------------------------------------------------------------------------------*/
    public void showHelpView(View v) {
        if (layout_menu.isShown())
            layout_menu.setVisibility(View.GONE);
        BookingApplication.showCustomToast(R.string.No_Help_Available, "", false);
    }

    /*---------------------------------------------- showMenuView -------------------------------------------------------------------------------------*/
    public void showMenuView(View v) {
        Animation inAnim = AnimationUtils.loadAnimation(ActivitySplash.this, R.anim.slide_in_left);
        layout_menu.startAnimation(inAnim);
        layout_menu.setVisibility(View.VISIBLE);
    }

    /*---------------------------------------------- showFavorites -------------------------------------------------------------------------------------*/
    public void showFavorites(View v) {

        BookingApplication.showFavoritesScreen(this, 0.0, 0.0);

        if (layout_menu.isShown())
            layout_menu.setVisibility(View.GONE);
    }

    /*--------------------------------------------------showMainScreen----------------------------------------------------------------------------------*/
    public void showMainScreen(View v) {
        if (v.getId() == R.id.ll_quick_booking)
            BookingApplication.showMainScreen(this, true);
        else {
            BookingApplication.showMainScreen(this, false);
            /*------------ For MyTaxi ---------------*/
            //ll_now_later.setVisibility(View.VISIBLE);
            //ll_quick_detail.setVisibility(View.GONE);
        }
    }//showMainScreen

    /*--------------------------------------------------startNowLaterTrip----------------------------------------------------------------------------------*/
    public void startNowLaterTrip(View v) {
        if (v.getId() == R.id.ll_now_booking)
            BookingApplication.trip_type = "CURR";
        else
            BookingApplication.trip_type = "FUT";

        BookingApplication.showMainScreen(this, false);
    }//startNowLaterTrip

    /*--------------------------------------------------showProfileScreen----------------------------------------------------------------------------------*/
    public void showProfileScreen(View v) {
        BookingApplication.showProfileScreen(this);
    }//showProfileScreen

    /*------------------------------------------------ showPromotions ---------------------------------------------------------------------------------*/
    public void showPromotions(View v) {
        BookingApplication.showPromotions(this);
    }

    /*-------------------------------------------------- performLogout -------------------------------------------------------------------------------------*/
    public void performLogout(View v) {
        BookingApplication.userInfoPrefs.edit().putString("UserID", "").commit();
        BookingApplication.showLoginScreen(ActivitySplash.this);
    }

    /*-------------------------------------------------ShowPaymentOptions--------------------------------------------------------------------------------------*/
    public void ShowPaymentOptions(View view) {
        BookingApplication.showPaymentOptions("", "", "", false, ActivitySplash.this, CODES.NONE, false);
    }

    /*--------------------------------------------------ShowPPV--------------------------------------------------------------------------------------*/
    public void ShowPPV(View v) {
        BookingApplication.ShowPPV(BookingApplication.CompanyID, BookingApplication.SupportedPaymentMethod, ActivitySplash.this);
    }

    /*-----------------------------------------------------about_Us--------------------------------------------------------------------------------------*/
    public void about_Us(View view) {
        BookingApplication.showWebScreen(ActivitySplash.this, BookingApplication.userInfoPrefs.getString("AboutUsLink", getString(R.string.CompanyWeb)));
    }

    /*-----------------------------------------------------share_App--------------------------------------------------------------------------------------*/
    public void share_App(View view) {
        BookingApplication.share_App();
    }

    /*-----------------------------------------------------feedBack--------------------------------------------------------------------------------------*/
    public void feedBack(View view) {
        Intent profile = new Intent(ActivitySplash.this, ActivityFeedback.class);
        startActivity(profile);
        overridePendingTransition(R.anim.slide_in_right, 0);
    }

    /*--------------------------------------------------ShowPriceList--------------------------------------------------------------------------------------*/
    public void ShowPriceList(View v) {
        BookingApplication.showWebScreen(ActivitySplash.this, BookingApplication.userInfoPrefs.getString("RatesLink", getString(R.string.CompanyWeb)));
    }

    /*--------------------------------------------------ShowFleet--------------------------------------------------------------------------------------*/
    public void ShowFleet(View v) {
        BookingApplication.showWebScreen(ActivitySplash.this, BookingApplication.userInfoPrefs.getString("FleetInfoLink", getString(R.string.CompanyWeb)));
    }

    /*--------------------------------------------------ShowTerms--------------------------------------------------------------------------------------*/
    public void ShowTerms(View v) {
        BookingApplication.showWebScreen(ActivitySplash.this, BookingApplication.userInfoPrefs.getString("TCLink", getString(R.string.CompanyWeb)));
    }

    /*---------------------------------------------- showTripsView -------------------------------------------------------------------------------------*/
    public void showTripsView(View v) {
        trips_adapter.notifyDataSetChanged();
        if (layout_menu.isShown())
            layout_menu.setVisibility(View.GONE);
        getCurrentRides(3000);
        layout_history.setVisibility(View.VISIBLE);
    }


    /*------------------------------------------------------ getCurrentRides -------------------------------------------------------------------------------------*/
    private void getCurrentRides(final int time_msec) {
        if (!BookingApplication.ridesFetched)
            BookingApplication.fetchCustomerRides(ActivitySplash.this, "all");
        try {
            requestTimer.schedule(new TimerTask() {
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

        }
    }

    /*----------------------------------------- callbackResponseReceived -------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {
        try {
            switch (apiCalled) {
                case APIs.RegisterForPushMessages:
                    BookingApplication.performHandshake(ActivitySplash.this);
                    break;
                case APIs.HANDSHAKE:

                    tv_help.setMinHeight(iv_menu.getHeight());

                    if (jsonResponse.has("SoftwareUpdateLink") && jsonResponse.getString("SoftwareUpdateLink").length() > 4)
                        showCustomDialog(CODES.SOFTWARE_UPDATE, getString(R.string.SoftwareUpdate), jsonResponse.getString("responseMessage"), 0, true, jsonResponse.getString("SoftwareUpdateLink"));
                    else if (jsonResponse.has("PromotionMsg") && jsonResponse.has("WebLink") && jsonResponse.has("ColorScheme") && jsonResponse.has("AboutUsLink") && jsonResponse.has("TCLink")
                            && jsonResponse.has("RatesLink") && jsonResponse.has("FleetInfoLink") && jsonResponse.has("SenderEmailPass") && jsonResponse.has("ReceiverEmail")
                            && jsonResponse.has("MainLogoImage") && jsonResponse.has("BottomLogoImage") && jsonResponse.has("BackgroundImage")) {

                        promotion_message.setText(jsonResponse.getString("PromotionMsg"));
                        BookingApplication.colorScheme = jsonResponse.getString("ColorScheme");
                        BookingApplication.receiverEmail = jsonResponse.getString("ReceiverEmail");
                        BookingApplication.senderEmailPassword = jsonResponse.getString("SenderEmailPass");
                        BookingApplication.userInfoPrefs.edit().putString("PromotionMsg", jsonResponse.getString("PromotionMsg")).putString("MainLogoImage", jsonResponse.getString("MainLogoImage")).putString("BackgroundImage", jsonResponse.getString("BackgroundImage")).putString("BottomLogoImage", jsonResponse.getString("BottomLogoImage")).putString("WebLink", jsonResponse.getString("WebLink")).putString("ColorScheme", jsonResponse.getString("ColorScheme")).putString("AboutUsLink", jsonResponse.getString("AboutUsLink")).putString("TCLink", jsonResponse.getString("TCLink")).putString("RatesLink", jsonResponse.getString("RatesLink")).putString("FleetInfoLink", jsonResponse.getString("FleetInfoLink")).putString("SenderEmailPass", jsonResponse.getString("SenderEmailPass")).putString("ReceiverEmail", jsonResponse.getString("ReceiverEmail")).commit();

                        BookingApplication.bHandshakeSuccess = success;

                        String mainLogo = jsonResponse.getString("MainLogoImage");
                        if ((mainLogo.length() > 0) && (mainLogo.endsWith("png") || mainLogo.endsWith("jpg")))
                            BookingApplication.imagedownloader.DisplayImage(mainLogo, iv_main_logo);

                        String bottomLogo = jsonResponse.getString("BottomLogoImage");
                        if ((bottomLogo.length() > 0) && (bottomLogo.endsWith("png") || bottomLogo.endsWith("jpg")))
                            BookingApplication.imagedownloader.DisplayImage(bottomLogo, iv_company_logo);

                        String BackgroundImage = jsonResponse.getString("BackgroundImage");
                        if ((BackgroundImage.length() > 0) && (BackgroundImage.endsWith("png") || BackgroundImage.endsWith("jpg")))
                            BookingApplication.imagedownloader.DisplayImage(BackgroundImage, iv_splash_parent);
                        else
                            iv_splash_parent.performClick();

                        //Cursor result = BookingApplication.db.rawQuery("SELECT Value FROM Settings WHERE VariableName='TimeStamp'", null);
                        //startManagingCursor(result);
                        //int rowCount = result.getCount();
                        //if (rowCount > 0) {
                        //	result.moveToFirst();
                        //	BookingApplication.currentDBTimeStamp = result.getLong(0);
                        //	result.moveToNext();
                        //	result.close();
                        //}
                        //
                        //try {
                        //	Long serverDBTimeStamp = BookingApplication.inFormat1.parse(jsonResponse.getString("DBUTCTimeStamp")).getTime();
                        //	BookingApplication.syncRequired = serverDBTimeStamp > BookingApplication.currentDBTimeStamp;
                        //	if (BookingApplication.syncRequired) {
                        //		ContentValues values = new ContentValues(1);
                        //		values.put("Value", serverDBTimeStamp);
                        //		BookingApplication.db.update("Settings", values, "VariableName=?", new String[] { "TimeStamp" });
                        //	}
                        //} catch (Exception e) {
                        //	BookingApplication.syncRequired = true;
                        //	e.printStackTrace();
                        //}

                    } else {
                        Toast.makeText(ActivitySplash.this, getString(R.string.invalid_response, APIs.getApiName(apiCalled)), Toast.LENGTH_LONG).show();
                        System.exit(0);
                    }
                    break;
                case APIs.LOGIN:
                    if (success) {
                        iv_splash_parent.setVisibility(View.GONE);
                        iv_main_logo.setVisibility(View.GONE);
                        ll_quick_detail.setVisibility(View.VISIBLE);
                        BookingApplication.fetchCustomerRides(ActivitySplash.this, "all");
                    } else
                        try {
                            if (jsonResponse.has("FaultCode"))
                                if (jsonResponse.getInt("FaultCode") == CODES.ACTIVATION_REQUIRED) {
                                    BookingApplication.userInfoPrefs.edit().putString("UserID", jsonResponse.getString("UserID")).commit();
                                    if (BookingApplication.bsendsms) {
                                        Toast.makeText(ActivitySplash.this, jsonResponse.getString("ReasonPhrase"), Toast.LENGTH_LONG).show();
                                        BookingApplication.showVerificationScreen(ActivitySplash.this);
                                    } else {
                                        if (BookingApplication.isAutoLogin) {
                                            BookingApplication.showLoginScreen(ActivitySplash.this);
                                        } else {
                                            BookingApplication.code = jsonResponse.getString("ReasonPhrase");
                                            BookingApplication.performPostActivation(BookingApplication.code, "", BookingApplication.phoneNumber);
                                        }
                                    }
                                } else
                                    BookingApplication.showLoginScreen(ActivitySplash.this);
                            else
                                BookingApplication.showLoginScreen(ActivitySplash.this);
                        } catch (JSONException e) {
                            BookingApplication.showLoginScreen(ActivitySplash.this);
                        }
                    break;
                case APIs.POSTACTIVATE:
                    if (success)
                        BookingApplication.fetchCustomerRides(ActivitySplash.this, "all");
                    break;
                case APIs.GETCUSTOMERRIDES:
                    trips_adapter.notifyDataSetChanged();
                    BookingApplication.getFavorites();
                    break;
                case APIs.GETFAVORITES:
                    BookingApplication.getCCProfiles(ActivitySplash.this);
                    break;
                case APIs.GETCCPROFILES:
                    BookingApplication.sendRegistrationIdToBackend();
                    break;
            }

        } catch (Exception e) {
            Toast.makeText(ActivitySplash.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
    }
}
