package itc.booking.mars;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import itc.booking.mars.BookingApplication.APIs;
import itc.booking.mars.BookingApplication.CODES;
import itc.downloader.image.FileCache;
import itcurves.mars.R;

public class ActivityVerifyNumber extends Activity implements CallbackResponseListener {

    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;
    TextView verificationnumber, verificationnumber2, description_2_top;
    EditText sms_code_input;
    LinearLayout verificationInProcess, verificationFailed;
    int i = 0;
    FileCache fileCache;
    ImageView imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BookingApplication.setMyTheme(ActivityVerifyNumber.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_sms);

        verificationInProcess = (LinearLayout) findViewById(R.id.verify_sms_pane_trying_layout);
        verificationFailed = (LinearLayout) findViewById(R.id.sms_pane_failed_layout);
        verificationnumber = (TextView) findViewById(R.id.verify_sms_phone_number);
        verificationnumber2 = (TextView) findViewById(R.id.verify_sms_phone_number_2);
        description_2_top = (TextView) findViewById(R.id.description_2_top);
        description_2_top.setText(getResources().getString(R.string.verify_sms_manual_header_2, BookingApplication.appID));
        sms_code_input = (EditText) findViewById(R.id.verify_sms_code_input);
        imageView1 = (ImageView) findViewById(R.id.imageView1);

        String mainLogo = BookingApplication.userInfoPrefs.getString("MainLogoImage", "");
        if ((mainLogo.length() > 0) && (mainLogo.endsWith("png") || mainLogo.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(BookingApplication.userInfoPrefs.getString("MainLogoImage", ""), imageView1);

        sms_code_input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 6)
                    BookingApplication.performPostActivation(s.toString(), "", "");
            }
        });

        verificationnumber.setText(BookingApplication.phoneNumber);
        verificationnumber2.setText(BookingApplication.phoneNumber);

        BookingApplication.performPreActivation("", ActivityVerifyNumber.this);

        mProgressBar = (ProgressBar) findViewById(R.id.verify_progress_bar);
        mProgressBar.setProgress(i);
        mCountDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                i++;
                mProgressBar.setProgress(i);

            }

            @Override
            public void onFinish() {
                i++;
                mProgressBar.setProgress(i);

                verificationInProcess.setVisibility(View.GONE);
                verificationFailed.setVisibility(View.VISIBLE);
                sms_code_input.requestFocus();
            }
        };
        mCountDownTimer.start();

        fileCache = new FileCache(this);
    }

    /*--------------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
    }

    /*------------------------------------------------------------- onBackPressed --------------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        showCustomDialog(BookingApplication.CODES.EXIT, R.string.app_name, getResources().getString(R.string.Exit_Application), R.drawable.exit, true);
    }

    /*------------------------------------------------------------- RegisterAgain --------------------------------------------------------------------------------------*/
    public void RegisterAgain(View v) {
        BookingApplication.showLoginScreen(ActivityVerifyNumber.this);
    }

    /*--------------------------------------------------------- Custom  Dialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(final int reasonCode, int dialogTitle, String dialogText, int imageResID, Boolean showCancelBtn) {
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
                System.exit(0);
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

    /*---------------------------------------------- callbackResponseReceived---------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean paramBoolean) {
        switch (apiCalled) {
            case APIs.PREACTIVATE:
                BookingApplication.getCCProfiles(ActivityVerifyNumber.this);
                break;
            case APIs.POSTACTIVATE:
                if (paramBoolean) {
                    mCountDownTimer.cancel();
                    fileCache.openFile("userID");

                    if (!fileCache.readOpenedFile().equalsIgnoreCase(BookingApplication.phoneNumber))
                        BookingApplication.showReferedbyScreen(ActivityVerifyNumber.this);
                    else if (BookingApplication.ccProfiles.isEmpty())
                        BookingApplication.showPaymentOptions(getResources().getString(R.string.Skip), "", "", ActivityVerifyNumber.this, CODES.NONE, true);
                    else
                        BookingApplication.showSplashScreen(ActivityVerifyNumber.this);
                }
        }
    }

}
