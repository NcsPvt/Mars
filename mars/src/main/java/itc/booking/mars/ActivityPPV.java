package itc.booking.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

import itc.booking.mars.BookingApplication.APIs;
import itcurves.mars.R;

public class ActivityPPV extends Activity implements CallbackResponseListener {


    private LinearLayout ll_pageHeader;
    private TextView voucherHeader, creditHeader, availableBalance;
    private EditText voucherNumber, topUpAmount;
    private static String topUPAmt = "";

    @Override
    /*------------------------------------------------- onCreate ---------------------------------------------------------------------*/
    protected void onCreate(Bundle arg0) {
        BookingApplication.setMyTheme(ActivityPPV.this);

        super.onCreate(arg0);
        setContentView(R.layout.activity_ppv);

        ll_pageHeader = (LinearLayout) findViewById(R.id.ll_pageHeader);
        ll_pageHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));

        voucherHeader = (TextView) findViewById(R.id.voucherChargingHeader);
        voucherHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));

        creditHeader = (TextView) findViewById(R.id.creditChargingHeader);
        creditHeader.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));

        voucherNumber = (EditText) findViewById(R.id.voucher_number);
        availableBalance = (TextView) findViewById(R.id.no_promo_available);

        topUpAmount = (EditText) findViewById(R.id.topup_Amount);
//        availableBalance.setText("Available Balance: " + BookingApplication.availableBalance);
    }

    /*--------------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();

    }

    /*---------------------------------------------- chargeByVoucher -------------------------------------------------------------------------------------*/
    public void chargeByVoucher(View v) {
        if (voucherNumber.getText().toString().length() == 14)
            BookingApplication.topUpBalance(voucherNumber.getText().toString(), BookingApplication.phoneNumber, BookingApplication.CompanyID, this);
        else {
            voucherNumber.setError("Invalid Card Number");
            Toast.makeText(ActivityPPV.this, "Invalid Card Number", Toast.LENGTH_SHORT).show();
        }
    }

    /*---------------------------------------------- chargeByCC -------------------------------------------------------------------------------------*/
    public void chargeByCC(View v) {
        if (topUpAmount.getText().toString().length() > 0) {
            topUPAmt = topUpAmount.getText().toString();
            BookingApplication.showPaymentOptions("", "-1", "", false, ActivityPPV.this, BookingApplication.CODES.CC_ID_REQUIRED, false);

        } else {
            topUpAmount.setError("Enter Valid Amount");
            Toast.makeText(ActivityPPV.this, "Enter Valid Amount", Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------------ callbackResponseReceived -------------------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {
        try {
            switch (apiCalled) {
                case APIs.TOPUPBALANCE:
                    if (success) {
                        if (jsonResponse.getInt("TopupAmount") != -1) {
                            availableBalance.setText("Available Balance: " + jsonResponse.getString("UpdatedAmount"));
                            BookingApplication.availableBalance = jsonResponse.getString("UpdatedAmount");
                            voucherNumber.setText("");
                            showCustomDialog(BookingApplication.CODES.PPV_RESPONSE, "PPV", "Topup Successfull by Voucher", 0, false);
                        } else {
                            voucherNumber.setError("Invalid Card Number");
                            availableBalance.setText("Available Balance: " + jsonResponse.getString("UpdatedAmount"));
                            BookingApplication.availableBalance = jsonResponse.getString("UpdatedAmount");
                            showCustomDialog(BookingApplication.CODES.PPV_RESPONSE, "PPV", "Invalid Card Number", 0, false);

                        }

                    }
                    break;
                case APIs.TOPUPBALANCEBYCC:
                    if (success) {
                        if (jsonResponse.getInt("ResponseCode") == 0) {
                            availableBalance.setText("Available Balance: " + jsonResponse.getString("Newbalance"));
                            BookingApplication.availableBalance = jsonResponse.getString("Newbalance");
                            topUpAmount.setText("");
                            showCustomDialog(BookingApplication.CODES.PPV_RESPONSE, "PPV", "Topup Successful By CC", 0, false);
                        } else {
                            availableBalance.setText("Available Balance: " + jsonResponse.getString("Newbalance"));
                            BookingApplication.availableBalance = jsonResponse.getString("Newbalance");
                            showCustomDialog(BookingApplication.CODES.PPV_RESPONSE, "PPV", "Invalid Card Number", 0, false);
                        }
                        topUPAmt = "";
                    }
                    break;
            }

        } catch (Exception e) {
            Toast.makeText(ActivityPPV.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == BookingApplication.CODES.CC_ID_REQUIRED) {

            BookingApplication.topUpBalanceByCC(data.getExtras().getString("CardID"), BookingApplication.CompanyID, topUPAmt, this);
        }
    }


    /*------------------------------------------------ showCustomDialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(final int reasonCode, String dialogTitle, final String dialogText, int imageResID, final Boolean showCancelBtn) {
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
                    case BookingApplication.CODES.GPS_TOGGLE: {
                        Intent settingActivity = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        settingActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(settingActivity);
                        break;
                    }
                    case BookingApplication.CODES.PPV_RESPONSE: {
                        break;
                    }

                }//switch
            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();

            }
        });

        thisDialog.setView(layout);
        thisDialog.show();
    }

}
