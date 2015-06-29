package itc.booking.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import itc.booking.mars.BookingApplication.APIs;
import itc.booking.mars.BookingApplication.CODES;
import itcurves.mars.R;

public class ActivityPaymentOptions extends Activity implements CallbackResponseListener {

    private LinearLayout overlayLayout;
    private boolean fromTripScreen;

    public class CreditCardAdapter extends ArrayAdapter<CreditCardProfile> {

        private final ArrayList<CreditCardProfile> ccProfilesList;
        private final int viewResourceId;
        private Context myContext;
        protected boolean favorite_clicked = false;

        public CreditCardAdapter(Context context, int _viewResourceId, ArrayList<CreditCardProfile> profilesList) {

            super(context, _viewResourceId, profilesList);
            myContext = context;
            viewResourceId = _viewResourceId;
            ccProfilesList = profilesList;
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
        public int getPosition(CreditCardProfile item) {
            for (int i = 0; i < ccProfilesList.size(); i++)
                if (ccProfilesList.get(i).id.equalsIgnoreCase(item.id))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final CreditCardProfile currentProfile = ccProfilesList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(viewResourceId, null);
            }
            if (currentProfile != null) {

                ImageButton cardType = (ImageButton) currentView.findViewById(R.id.cardType);
                TextView ccType = (TextView) currentView.findViewById(R.id.tv_cardType);
                TextView ccNumber = (TextView) currentView.findViewById(R.id.tv_ccnumber);
                TextView expiry = (TextView) currentView.findViewById(R.id.tv_expiry);
                ccNumber.setText("xxxx-xxxx-xxxx-" + currentProfile.last4);
                expiry.setText("Expiry: " + currentProfile.expiry);
                ccType.setText(currentProfile.type);

                if (currentProfile.type.equalsIgnoreCase("Visa"))
                    cardType.setBackgroundResource(R.drawable.visa);
                else if (currentProfile.type.equalsIgnoreCase("MasterCard"))
                    cardType.setBackgroundResource(R.drawable.master);
                else if (currentProfile.type.equalsIgnoreCase("Discover"))
                    cardType.setBackgroundResource(R.drawable.discover);
                else if (currentProfile.type.equalsIgnoreCase("American Express") || currentProfile.type.equalsIgnoreCase("amex"))
                    cardType.setBackgroundResource(R.drawable.amex);
                else {
                    cardType.setVisibility(View.GONE);
                    ccType.setVisibility(View.VISIBLE);
                }

                final LinearLayout confirmation_layout = (LinearLayout) currentView.findViewById(R.id.ll_ConfirmCCDeletion);
                Button yes = (Button) currentView.findViewById(R.id.btn_Yes);
                Button no = (Button) currentView.findViewById(R.id.btn_No);

                yes.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Animation inAnim = AnimationUtils.loadAnimation(ActivityPaymentOptions.this, R.anim.slide_out_bottom);
                        confirmation_layout.startAnimation(inAnim);
                        confirmation_layout.setVisibility(View.GONE);
                        //remove(currentProfile);

                        BookingApplication.RemoveUserCCProfile(currentProfile.id, ActivityPaymentOptions.this);
                    }
                });

                no.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Animation inAnim = AnimationUtils.loadAnimation(ActivityPaymentOptions.this, R.anim.slide_out_bottom);
                        inAnim.setDuration(500);
                        confirmation_layout.startAnimation(inAnim);
                        confirmation_layout.setVisibility(View.GONE);
                    }
                });

                ImageButton delete = (ImageButton) currentView.findViewById(R.id.btn_delete);
                delete.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Animation inAnim = AnimationUtils.loadAnimation(ActivityPaymentOptions.this, R.anim.slide_in_bottom);
                        inAnim.setDuration(500);
                        confirmation_layout.startAnimation(inAnim);
                        confirmation_layout.setVisibility(View.VISIBLE);
                    }
                });

                currentView.setTag(currentProfile.id);

                currentView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            selectedCardID = currentProfile.id;
                            if (Double.parseDouble(fareEstimate) > 0) {

                                selectedCardID = currentProfile.id;

                                Intent activityIntent = new Intent(ActivityPaymentOptions.this, ActivitySignature.class);
                                Bundle paymentOption = new Bundle();
                                paymentOption.putString("fareEstimate", currencySymbol + fareEstimate);
                                activityIntent.putExtras(paymentOption);
                                startActivityForResult(activityIntent, CODES.SIGNATURE_REQUIRED);

                                //Intent resultIntent = new Intent();
                                //Bundle paymentOption = new Bundle();
                                //paymentOption.putString("PaymentType", "credit card");
                                //paymentOption.putString("CardID", currentProfile.id);
                                //resultIntent.putExtras(paymentOption);
                                //setResult(CODES.PAYMENT_OPTION_ACTIVITY, resultIntent);
                                //ActivityPaymentOptions.this.finish();
                            } else if (Integer.parseInt(fareEstimate) == -1) {
                                Intent resultIntent = new Intent();
                                Bundle paymentOption = new Bundle();
                                paymentOption.putString("CardID", selectedCardID);
                                resultIntent.putExtras(paymentOption);
                                setResult(CODES.CC_ID_REQUIRED, resultIntent);
                                ActivityPaymentOptions.this.finish();
                            } else if (fromTripScreen)
                                Toast.makeText(ActivityPaymentOptions.this, R.string.Choose_Destination, Toast.LENGTH_LONG).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(ActivityPaymentOptions.this, R.string.Choose_Destination, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            return currentView;
        }
    } // CreditCardAdapter Class

    Timer requestStatustimer = new Timer();
    public Bundle extras;
    private CreditCardAdapter creditCard_adapter;
    private ListView creditCardsListView;
    String sessionid = "0";
    String sessionurl = "";
    String selectedCardID = "";
    String fareEstimate = "";
    String currencySymbol = "";
    int retryCount = 0;
    Boolean timerCancelled = false;
    TextView paymentHeader, tv_overlay_payment;

    @Override
    /*------------------------------------------------- onCreate -----------------------------------------------------------------------*/
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        BookingApplication.setMyTheme(ActivityPaymentOptions.this);
        setContentView(R.layout.activity_payment);
        tv_overlay_payment = (TextView) findViewById(R.id.tv_overlay_payment);
        paymentHeader = (TextView) findViewById(R.id.paymentHeader);
        paymentHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));

        overlayLayout = (LinearLayout) findViewById(R.id.ll_overlay);
        extras = getIntent().getExtras();
        if (extras.getString("fareEstimate").equalsIgnoreCase(""))
            fareEstimate = "0";
        else
            fareEstimate = extras.getString("fareEstimate");
        currencySymbol = extras.getString("currencySymbol");
        fromTripScreen = extras.getBoolean("fromTripScreen");

        Button btnSkip = (Button) findViewById(R.id.btnSkip);
        if (extras.getString("SkipBtnLabel").length() > 0)
            btnSkip.setText(extras.getString("SkipBtnLabel"));
        else
            btnSkip.setVisibility(View.GONE);


        if (extras.getString("SkipBtnLabel").length() > 0) {
            tv_overlay_payment.setText(getString(R.string.overlayText, BookingApplication.getApplicationName(ActivityPaymentOptions.this)));
            overlayLayout.setVisibility(View.VISIBLE);
        }

        creditCardsListView = (ListView) findViewById(R.id.list_cards);
        creditCard_adapter = new CreditCardAdapter(this, R.layout.list_item_creditcard, BookingApplication.ccProfiles);
        creditCardsListView.setAdapter(creditCard_adapter);

    }

    /*------------------------------------------------- onResume -------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
    }

    /*------------------------------------------------- onBackPressed --------------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        requestStatustimer.purge();
        requestStatustimer.cancel();
        setResult(-1, null);
        ActivityPaymentOptions.this.finish();
    }

    /*--------------------------------------------------onActivityResult-----------------------------------------------------------------------------------*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODES.SIGNATURE_REQUIRED) {
            String signatureUrl = data.getStringExtra("signatureUrl");

            if (signatureUrl.length() > 4) {

                Intent resultIntent = new Intent();
                Bundle paymentOption = new Bundle();
                paymentOption.putString("PaymentType", "2");
                paymentOption.putString("signatureUrl", signatureUrl);
                paymentOption.putString("CardID", selectedCardID);

                resultIntent.putExtras(paymentOption);
                setResult(CODES.PAYMENT_OPTION_ACTIVITY, resultIntent);

                ActivityPaymentOptions.this.finish();
            }
        }
    }

    /*-------------------------------------------------SkipPaymentOptions-----------------------------------------------------------------*/
    public void SkipPaymentOptions(View view) {
        if (extras.getString("SkipBtnLabel").contains(getResources().getString(R.string.Skip)))
            BookingApplication.showSplashScreen(ActivityPaymentOptions.this);
        else if (extras.getString("SkipBtnLabel").length() > 0) {
            Intent resultIntent = new Intent();
            Bundle paymentOption = new Bundle();
            paymentOption.putString("PaymentType", "1");
            paymentOption.putString("signatureUrl", "");
            paymentOption.putString("CardID", "0");
            resultIntent.putExtras(paymentOption);

            setResult(extras.getInt("requestCode"), resultIntent);

            ActivityPaymentOptions.this.finish();
        }
    }

    /*-------------------------------------------------SkipPaymentOptions-----------------------------------------------------------------*/
    public void overlayPressed(View view) {
        if (extras.getString("SkipBtnLabel").equalsIgnoreCase(getResources().getString(R.string.Skip))) {
            overlayLayout.setVisibility(View.GONE);
        }

    }

    /*--------------------------------------------------addCard---------------------------------------------------------------------------*/
    public void addCard(View view) {
        if (timerCancelled) {
            requestStatustimer = new Timer();
            timerCancelled = false;
        }
        BookingApplication.createCCSession(this);
    }

    /*---------------------------------------------- startRequestStatusPolling -----------------------------------------------------------*/
    private void startRequestStatusPolling(final int time_msec) {
        try {
            requestStatustimer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BookingApplication.checkCCSession(sessionid, ActivityPaymentOptions.this);
                        }
                    });
                }
            }, time_msec);
        } catch (Exception e) {
            finish();
        }

    }

    /*---------------------------------------------- callbackResponseReceived-------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean paramBoolean) {
        try {
            switch (apiCalled) {
                case APIs.CREATECCSESSION:
                    if (jsonResponse.has("Sessionid")) {
                        sessionid = jsonResponse.getString("Sessionid");
                        sessionurl = jsonResponse.getString("SessionUrl");
                        BookingApplication.showWebScreen(ActivityPaymentOptions.this, sessionurl);
                        startRequestStatusPolling(15 * 1000);
                    } else if (retryCount < 4) {
                        BookingApplication.createCCSession(this);
                        retryCount++;
                    } else {
                        retryCount = 0;
                        Toast.makeText(this, R.string.Unknown_Error, Toast.LENGTH_LONG).show();
                    }//if (extras.getString("SkipBtnLabel").equalsIgnoreCase(getResources().getString(R.string.Skip)))
                    //BookingApplication.showMainScreen(ActivityPaymentOptions.this);
                    break;
                case APIs.CHECKCCSESSION:
                    if (!paramBoolean && (retryCount < 20)) {
                        startRequestStatusPolling(15 * 1000);
                        retryCount++;
                    } else {
                        retryCount = 0;
                        requestStatustimer.purge();
                        requestStatustimer.cancel();
                        timerCancelled = true;
                        finishActivity(BookingApplication.CODES.WEB_ACTIVITY);
                        creditCard_adapter.notifyDataSetChanged();
                    }
                    break;
                case APIs.GETCCPROFILES:
                    creditCard_adapter.notifyDataSetChanged();
                    break;
                case APIs.REMOVECCPROFILE:
                    int position = creditCard_adapter.getPosition(new CreditCardProfile(jsonResponse.getString("UniqueID"), "", "", ""));
                    if (position >= 0) {
                        BookingApplication.ccProfiles.remove(position);
                        creditCard_adapter.notifyDataSetChanged();
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }//callbackResponseReceived
}
