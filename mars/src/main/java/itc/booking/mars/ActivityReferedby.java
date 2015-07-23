package itc.booking.mars;

import android.app.Activity;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

import itc.booking.mars.BookingApplication.CODES;
import itc.downloader.image.FileCache;
import itc.emailsender.gmail.GMailSender;
import itcurves.mars.R;

public class ActivityReferedby extends Activity implements CallbackResponseListener {
    /*-----------------------------------------------------------------------------------------------------------------------------------------------
         *---------------------------------------------------------- FeedbackTask AsyncTask ------------------------------------------------------------
         *-----------------------------------------------------------------------------------------------------------------------------------------------
         */
    private class FeedbackTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                GMailSender sender = new GMailSender(params[2], BookingApplication.senderEmailPassword.split("@")[1]);
                sender.sendMail(params[0], params[1], params[2], params[3]);
                return null;
            } catch (javax.mail.AuthenticationFailedException afe) {
                return getResources().getString(R.string.invalid_password_match);
            } catch (Exception e) {
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null)
                Toast.makeText(ActivityReferedby.this, response, Toast.LENGTH_LONG).show();
            else {
                fileCache.openFile("userID");
                fileCache.writeOpenedFile(BookingApplication.phoneNumber);
                BookingApplication.showSplashScreen(ActivityReferedby.this);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BookingApplication.showCustomProgress(ActivityReferedby.this, "", true);
        }
    }

    EditText feedbackMessage, et_marsid, et_mobilenumber, et_companywebsite;
    RadioButton friend, company, driver, other;
    FileCache fileCache;

    /*--------------------------------------------------- cancel_feedback ----------------------------------------------------------------------------------*/
    public void cancel_feedback(View v) {
        if (BookingApplication.ccProfiles.isEmpty())
            BookingApplication.showPaymentOptions(getResources().getString(R.string.Skip), "", "", false, ActivityReferedby.this, CODES.NONE, true);
        else
            BookingApplication.showSplashScreen(ActivityReferedby.this);
    }

    /*------------------------------------------------------ onCreate -------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        BookingApplication.setMyTheme(ActivityReferedby.this);
        setContentView(R.layout.activity_feedback_referedby);

        friend = (RadioButton) findViewById(R.id.referredBy_friend);
        company = (RadioButton) findViewById(R.id.referredBy_company);
        driver = (RadioButton) findViewById(R.id.referredBy_driver);
        other = (RadioButton) findViewById(R.id.referredBy_other);
        et_marsid = (EditText) findViewById(R.id.et_marsid);
        feedbackMessage = (EditText) findViewById(R.id.feedback_message);
        et_mobilenumber = (EditText) findViewById(R.id.et_mobilenumber);
        et_companywebsite = (EditText) findViewById(R.id.et_companywebsite);

        driver.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_marsid.setVisibility(View.VISIBLE);
                    et_mobilenumber.setVisibility(View.VISIBLE);
                    et_companywebsite.setVisibility(View.GONE);

                }
            }
        });

        friend.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_marsid.setVisibility(View.GONE);
                    et_mobilenumber.setVisibility(View.VISIBLE);
                    et_companywebsite.setVisibility(View.GONE);
                }
            }
        });

        company.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_marsid.setVisibility(View.GONE);
                    et_mobilenumber.setVisibility(View.GONE);
                    et_companywebsite.setVisibility(View.VISIBLE);
                }
            }
        });

        other.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_marsid.setVisibility(View.GONE);
                    et_mobilenumber.setVisibility(View.VISIBLE);
                    et_companywebsite.setVisibility(View.GONE);
                }
            }
        });

        friend.setChecked(true);
        fileCache = new FileCache(this);
    }

    /*------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = ActivityReferedby.this;
        BookingApplication.currentCallbackListener = ActivityReferedby.this;
    }

    /*------------------------------------------------------ submit_feedback -------------------------------------------------------------------------------*/
    public void submit_ReferredBy(View v) {
        try {
            String referredby = "";

            if (friend.isChecked())
                if (et_mobilenumber.getText().toString().trim().equals("")) {
                    BookingApplication.showCustomToast(R.string.provideMobileNumber, "", false);
                    return;
                } else
                    referredby = "friend"; // et_mobilenumber.getText().toString();
            else if (company.isChecked())
                if (et_companywebsite.getText().toString().trim().equals("")) {
                    BookingApplication.showCustomToast(R.string.provideCompanyWebsite, "", false);
                    return;
                } else
                    referredby = "company"; // et_companywebsite.getText().toString();
            else if (driver.isChecked())
                if (et_mobilenumber.getText().toString().trim().equals("")) {
                    BookingApplication.showCustomToast(R.string.provideMobileNumber, "", false);
                    return;
                } else
                    referredby = "driver"; // et_marsid.getText().toString();
            else if (other.isChecked())
                if (et_marsid.getText().toString().trim().equals("")) {
                    BookingApplication.showCustomToast(R.string.provideMobileNumber, "", false);
                    return;
                } else
                    referredby = "other"; // et_marsid.getText().toString();

            //new FeedbackTask().execute(referredby, feedbackMessage.getText().toString(), BookingApplication.senderEmailPassword.split("@")[0] + "@gmail.com", BookingApplication.receiverEmail);
            BookingApplication.submitReferredBy(et_mobilenumber.getText().toString(), et_marsid.getText().toString(), et_companywebsite.getText().toString(), feedbackMessage.getText().toString(), referredby, ActivityReferedby.this);

        } catch (Exception e) {
            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
        }
    }

    /*-------------------------------------------------- callbackResponseReceived ------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {
        try {
            switch (apiCalled) {
                case BookingApplication.APIs.SubmitReferredBy:
                    fileCache.openFile("userID");
                    fileCache.writeOpenedFile(BookingApplication.phoneNumber);
                    BookingApplication.showSplashScreen(ActivityReferedby.this);
                    break;
            }
        } catch (Exception e) {
            BookingApplication.showCustomToast(0, e.getLocalizedMessage(), true);
        }
    }

}
