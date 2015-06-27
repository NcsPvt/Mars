package itc.booking.mars;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import itc.emailsender.gmail.GMailSender;
import itcurves.mars.R;

public class ActivityFeedback extends Activity {
    /*-----------------------------------------------------------------------------------------------------------------------------------------------
     *---------------------------------------------------------- FeedbackTask AsyncTask ------------------------------------------------------------
     *-----------------------------------------------------------------------------------------------------------------------------------------------
     */
    private class FeedbackTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                GMailSender sender = new GMailSender(params[2], "Regency8201");
                sender.sendMail(params[0] + " from " + BookingApplication.phoneNumber, params[1], params[2], params[3]);
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
                Toast.makeText(ActivityFeedback.this, response, Toast.LENGTH_LONG).show();
            else
                finish();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            BookingApplication.showCustomProgress(ActivityFeedback.this, "", true);
        }
    }

    EditText feedbackMessage;
    RadioButton feedback;
    RadioButton bug;
    RadioButton feature;

    /*--------------------------------------------------- cancel_feedback ----------------------------------------------------------------------------------*/
    public void cancel_feedback(View v) {
        finish();
    }

    /*------------------------------------------------------ onCreate -------------------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BookingApplication.setMyTheme(ActivityFeedback.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_screen);

        feedback = (RadioButton) findViewById(R.id.feedback_feedback);
        bug = (RadioButton) findViewById(R.id.feedback_bug);
        feature = (RadioButton) findViewById(R.id.feedback_feature);
        feedbackMessage = (EditText) findViewById(R.id.feedback_message);
        feedback.setChecked(true);

    }

    /*------------------------------------------------------ submit_feedback -------------------------------------------------------------------------------*/
    public void submit_feedback(View v) {
        try {
            String feedbackType = "";

            if (feedback.isChecked())
                feedbackType = "Booking App - Comments";
            else if (bug.isChecked())
                feedbackType = "Booking App - Bug";
            else if (feature.isChecked())
                feedbackType = "Booking App - Suggestion";

            if (!feedbackMessage.getText().toString().trim().equals(""))
                //new FeedbackTask().execute(feedbackType, feedbackMessage.getText().toString(), getResources().getString(R.string.FeedbackSenderEmail), BookingApplication.receiverEmail);
                ShowEmailClients(BookingApplication.receiverEmail, feedbackType, feedbackMessage.getText().toString());
            else
                Toast.makeText(getApplicationContext(), R.string.ProvideFeedback, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(ActivityFeedback.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------------ ShowEmailClients --------------------------------------------------------------------------------*/
    void ShowEmailClients(String mailto, String subject, String body) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mailto, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.SendEmailUsing)));
    }

}
