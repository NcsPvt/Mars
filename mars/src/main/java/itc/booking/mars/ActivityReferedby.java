package itc.booking.mars;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import itc.booking.mars.BookingApplication.CODES;
import itc.downloader.image.FileCache;
import itc.emailsender.gmail.GMailSender;
import itcurves.mars.R;

public class ActivityReferedby extends Activity {
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

    EditText feedbackMessage, et_drivernumber, et_mobilenumber, et_companywebsite;
    RadioButton friend;
    RadioButton company;
    RadioButton driver;
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

        friend = (RadioButton) findViewById(R.id.feedback_friend);
        company = (RadioButton) findViewById(R.id.feedback_company);
        driver = (RadioButton) findViewById(R.id.feedback_driver);
        et_drivernumber = (EditText) findViewById(R.id.et_drivernumber);
        feedbackMessage = (EditText) findViewById(R.id.feedback_message);
        et_mobilenumber = (EditText) findViewById(R.id.et_mobilenumber);
        et_companywebsite = (EditText) findViewById(R.id.et_companywebsite);

        driver.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_drivernumber.setVisibility(View.VISIBLE);
                    et_mobilenumber.setVisibility(View.GONE);
                    et_companywebsite.setVisibility(View.GONE);

                } else {
                    et_drivernumber.setVisibility(View.GONE);
                    feedbackMessage.setText("");
                }
            }
        });

        friend.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_drivernumber.setVisibility(View.GONE);
                    et_mobilenumber.setVisibility(View.VISIBLE);
                    et_companywebsite.setVisibility(View.GONE);
                } else {
                    et_mobilenumber.setVisibility(View.GONE);
                }
            }
        });

        company.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_drivernumber.setVisibility(View.GONE);
                    et_mobilenumber.setVisibility(View.GONE);
                    et_companywebsite.setVisibility(View.VISIBLE);
                } else {
                    et_companywebsite.setVisibility(View.GONE);
                }
            }
        });
        friend.setChecked(true);

        fileCache = new FileCache(this);
    }

    /*------------------------------------------------------ submit_feedback -------------------------------------------------------------------------------*/
    public void submit_feedback(View v) {
        try {
            String feedbackType = "";

            if (friend.isChecked())
                if (et_mobilenumber.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.provideMobileNumber, Toast.LENGTH_LONG).show();
                    return;
                } else
                    feedbackType = "Booking App - Recommended by Friend " + et_mobilenumber.getText().toString();
            else if (company.isChecked())
                if (et_companywebsite.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.provideCompanyWebsite, Toast.LENGTH_LONG).show();
                    return;
                } else
                    feedbackType = "Booking App - Recommended by Company's Website " + et_companywebsite.getText().toString();
            else if (driver.isChecked())
                if (et_drivernumber.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.ProvideDrivernumber, Toast.LENGTH_LONG).show();
                    return;
                } else
                    feedbackType = "Booking App - Recommended by Driver " + et_drivernumber.getText().toString();

            new FeedbackTask().execute(feedbackType, feedbackMessage.getText().toString(), BookingApplication.senderEmailPassword.split("@")[0] + "@gmail.com", BookingApplication.receiverEmail);

        } catch (Exception e) {
            Toast.makeText(ActivityReferedby.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
