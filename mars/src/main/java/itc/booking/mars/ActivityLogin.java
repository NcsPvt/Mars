package itc.booking.mars;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.List;

import itc.booking.mars.BookingApplication.APIs;
import itc.booking.mars.BookingApplication.CODES;
import itc.downloader.image.FileCache;
import itcurves.mars.R;

public class ActivityLogin extends Activity implements CallbackResponseListener {

    LinearLayout ll_register_signin_buttons, forgotPasswordViews, registerViews;
    Button btn_login, btn_register;
    CheckBox showNumberToDrivers, cb_Terms;
    TextView forgot_password, tv_secret_question, lbl_OR, termOfUse;
    AutoCompleteTextView userPhone, cc, email;
    EditText loginPass, repeatPass, confirm_new_password, secret_answer_recovery, fullName, secretAnswer;
    String phoneNumber, password;
    Spinner preferedLanguage, secretQuestion;
    protected ProgressDialog progress;
    protected MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));
    private ImageView logo_login;
    private ScrollView scrollview_login;

    /*------------------------------------------performForgotPassword ----------------------------------------------------------------*/
    public void ForgotPassword(View v) {
        phoneNumber = "+" + cc.getText().toString().trim() + (userPhone.getText().toString().length() < 10 ? "0" + userPhone.getText().toString() : userPhone.getText().toString());
        if (BookingApplication.isValidPhone(cc.getText().toString().trim(), userPhone.getText().toString().trim()))
            BookingApplication.performForgotPassword(phoneNumber, this);
        else {
            userPhone.setError(getResources().getString(R.string.invalid_phone_number));
            Toast.makeText(ActivityLogin.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------- ResetPassword ----------------------------------------------------------------*/
    public void ResetPassword(View v) {
        if (BookingApplication.isValidPhone(cc.getText().toString().trim(), userPhone.getText().toString().trim()))
            if (loginPass.getText().toString().length() > 1)
                if (confirm_new_password.getText().toString().equals(loginPass.getText().toString())) {
                    phoneNumber = "+" + cc.getText().toString().trim() + (userPhone.getText().toString().length() < 10 ? "0" + userPhone.getText().toString() : userPhone.getText().toString());
                    password = loginPass.getText().toString();
                    BookingApplication.performResetPassword(phoneNumber, secret_answer_recovery.getText().toString(), password, this);
                } else {
                    confirm_new_password.setError(getResources().getString(R.string.invalid_password_match));
                    Toast.makeText(ActivityLogin.this, R.string.invalid_password_match, Toast.LENGTH_SHORT).show();
                }
            else {
                loginPass.setError(getResources().getString(R.string.invalid_password_length));
                Toast.makeText(ActivityLogin.this, R.string.invalid_password_length, Toast.LENGTH_SHORT).show();
            }
        else {
            userPhone.setError(getResources().getString(R.string.invalid_phone_number));
            Toast.makeText(ActivityLogin.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
        }

    }

    /*------------------------------------------------- ITC_Register -----------------------------------------------------------------*/
    public void ITC_Register(View v) {
        try {
            if (registerViews.getVisibility() == View.VISIBLE) {
                if (BookingApplication.isValidPhone(cc.getText().toString().trim(), userPhone.getText().toString().trim()))
                    if (loginPass.getText().toString().length() > 3)
                        if (repeatPass.getText().toString().equals(loginPass.getText().toString()))
                            if (cb_Terms.isChecked()) {
                                phoneNumber = "+" + cc.getText().toString().trim() + userPhone.getText().toString().trim();
                                password = loginPass.getText().toString();
                                repeatPass.setError(null);
                                BookingApplication.performRegister(phoneNumber, Boolean.toString(showNumberToDrivers.isChecked()), password, fullName.getText().toString(), email.getText().toString(), secretQuestion.getSelectedItemPosition() > 0 ? secretQuestion.getSelectedItem().toString()
                                        : "", secretAnswer.getText().toString(), this);
                            } else
                                Toast.makeText(ActivityLogin.this, R.string.AcceptTerms, Toast.LENGTH_SHORT).show();
                        else {
                            repeatPass.setError(getResources().getString(R.string.invalid_password_match));
                            Toast.makeText(ActivityLogin.this, R.string.invalid_password_match, Toast.LENGTH_SHORT).show();
                        }
                    else {
                        loginPass.setError(getResources().getString(R.string.invalid_password_length));
                        Toast.makeText(ActivityLogin.this, R.string.invalid_password_length, Toast.LENGTH_SHORT).show();
                    }
                else {
                    userPhone.setError(getResources().getString(R.string.invalid_phone_number));
                    Toast.makeText(ActivityLogin.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
                }
            } else {
                loginPass.setHint(R.string.New_password);
                registerViews.setVisibility(View.VISIBLE);
                showNumberToDrivers.setVisibility(View.VISIBLE);
                forgot_password.setVisibility(View.GONE);
                btn_login.setVisibility(View.GONE);
                lbl_OR.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------- ITC_Login --------------------------------------------------------------------*/
    public void ITC_Login(View v) {
        try {
            if (BookingApplication.isValidPhone(cc.getText().toString().trim(), userPhone.getText().toString().trim()))
                if (loginPass.getText().toString().length() > 1) {
                    phoneNumber = "+" + cc.getText().toString().trim() + userPhone.getText().toString().trim();
                    password = loginPass.getText().toString();
                    loginPass.setError(null);
                    BookingApplication.isAutoLogin = false;
                    BookingApplication.performLogin(phoneNumber, password, BookingApplication.code, this);
                } else {
                    loginPass.setError(getResources().getString(R.string.invalid_password_length));
                    Toast.makeText(ActivityLogin.this, R.string.invalid_password_length, Toast.LENGTH_SHORT).show();
                }
            else {
                userPhone.setError(getResources().getString(R.string.invalid_phone_number));
                Toast.makeText(ActivityLogin.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------- onBackPressed ----------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if (registerViews.getVisibility() == View.VISIBLE) {
            loginPass.setHint(R.string.password);
            registerViews.setVisibility(View.GONE);
            forgot_password.setVisibility(View.VISIBLE);
            showNumberToDrivers.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
            lbl_OR.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }

    /*------------------------------------------------- onCreate ---------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        BookingApplication.setMyLanguage(BookingApplication.selected_lang);
        BookingApplication.setMyTheme(ActivityLogin.this);

        setContentView(R.layout.activity_login);

        cc = (AutoCompleteTextView) findViewById(R.id.country_code);
        cc.setText(Integer.toString(BookingApplication.getUserCountryCode()));
        scrollview_login = (ScrollView) findViewById(R.id.scrollview_login);
        logo_login = (ImageView) findViewById(R.id.logo_login);
        userPhone = (AutoCompleteTextView) findViewById(R.id.user_phone);
        userPhone.setText(BookingApplication.getUserSimNumber());
        userPhone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400);
            }
        });
        loginPass = (EditText) findViewById(R.id.user_password);
        loginPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 2);
            }
        });
        ll_register_signin_buttons = (LinearLayout) findViewById(R.id.ll_register_signin_buttons);
        registerViews = (LinearLayout) findViewById(R.id.registerViews);
        forgotPasswordViews = (LinearLayout) findViewById(R.id.forgotPasswordViews);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_register = (Button) findViewById(R.id.btn_Register);
        lbl_OR = (TextView) findViewById(R.id.lbl_or);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        tv_secret_question = (TextView) findViewById(R.id.tv_secret_question);
        showNumberToDrivers = (CheckBox) findViewById(R.id.cb_showNumberToDriver);
        cb_Terms = (CheckBox) findViewById(R.id.cb_AgreeTerms);
        termOfUse = (TextView) findViewById(R.id.terms_of_use);
        String[] country_codes = getResources().getStringArray(R.array.country_codes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.autocompletetextview, country_codes);
        cc.setAdapter(adapter);

        repeatPass = (EditText) findViewById(R.id.confirm_password);
        repeatPass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 3);
            }
        });
        confirm_new_password = (EditText) findViewById(R.id.confirm_new_password);
        confirm_new_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 4);
            }
        });
        secret_answer_recovery = (EditText) findViewById(R.id.secret_answer_recovery);
        secret_answer_recovery.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 5);
            }
        });
        fullName = (EditText) findViewById(R.id.fullname);
        fullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 6);
            }
        });
        email = (AutoCompleteTextView) findViewById(R.id.email);
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 7);
            }
        });
        secretAnswer = (EditText) findViewById(R.id.secret_answer);
        secretAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    scrollview_login.scrollTo(0, 400 + v.getHeight() * 8);
            }
        });
        secretQuestion = (Spinner) findViewById(R.id.secret_question);
        preferedLanguage = (Spinner) findViewById(R.id.prefered_language);
        preferedLanguage.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        BookingApplication.selected_lang = "en";
                        break;
                    case 1:
                        BookingApplication.selected_lang = "es";
                        break;
                    case 2:
                        BookingApplication.selected_lang = "ar";
                        break;
                }
                BookingApplication.setMyLanguage(BookingApplication.selected_lang);
                updateTexts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        String mainLogo = BookingApplication.userInfoPrefs.getString("MainLogoImage", "");
        if ((mainLogo.length() > 0) && (mainLogo.endsWith("png") || mainLogo.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(BookingApplication.userInfoPrefs.getString("MainLogoImage", ""), logo_login);

        if (BookingApplication.selected_lang.equalsIgnoreCase("en"))
            preferedLanguage.setSelection(0);
        else if (BookingApplication.selected_lang.equalsIgnoreCase("es"))
            preferedLanguage.setSelection(1);
        else if (BookingApplication.selected_lang.equalsIgnoreCase("ar"))
            preferedLanguage.setSelection(2);

        ArrayAdapter<String> emailAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, BookingApplication.possibleEmail);
        if (BookingApplication.possibleEmail.size() > 0)
            email.setText(BookingApplication.possibleEmail.get(0));
        email.setAdapter(emailAdapter);

    }

    /*--------------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
    }

    /*--------------------------------------------------ShowTerms--------------------------------------------------------------------------------------*/
    public void ShowTerms(View v) {
        BookingApplication.showWebScreen(ActivityLogin.this, BookingApplication.userInfoPrefs.getString("TCLink", getString(R.string.CompanyWeb)));
    }

    /*------------------------------------------------------ callbackResponseReceived -------------------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {

        switch (apiCalled) {
            case APIs.LOGIN:
                try {
                    if (success) {
                        BookingApplication.showCustomToast(0, jsonResponse.getString("responseMessage"), false);
                        BookingApplication.showSplashScreen(ActivityLogin.this);
                    } else if (jsonResponse.has("FaultCode"))
                        if (jsonResponse.getInt("FaultCode") == BookingApplication.CODES.ACTIVATION_REQUIRED) {

                            BookingApplication.userInfoPrefs.edit().putString("UserID", jsonResponse.getString("UserID")).commit();
                            if (BookingApplication.bsendsms) {
                                Toast.makeText(ActivityLogin.this, jsonResponse.getString("ReasonPhrase"), Toast.LENGTH_LONG).show();
                                BookingApplication.showVerificationScreen(ActivityLogin.this);
                            } else {
                                BookingApplication.code = jsonResponse.getString("ReasonPhrase");
                                BookingApplication.performPostActivation(BookingApplication.code, "", phoneNumber);
                            }
                        } else
                            Toast.makeText(ActivityLogin.this, jsonResponse.getString("ReasonPhrase"), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    BookingApplication.showLoginScreen(ActivityLogin.this);
                }
                break;

            case APIs.REGISTER:
                if (BookingApplication.bsendsms)
                    BookingApplication.showVerificationScreen(ActivityLogin.this);
                else
                    BookingApplication.performPostActivation(BookingApplication.code, "", phoneNumber);
                break;

            case APIs.POSTACTIVATE:
                if (success)
                    BookingApplication.getCCProfiles(ActivityLogin.this);
                break;
            case APIs.GETCCPROFILES:
                FileCache fileCache = new FileCache(this);
                fileCache.openFile("userID");

                if (!fileCache.readOpenedFile().equalsIgnoreCase(BookingApplication.phoneNumber))
                    BookingApplication.showReferedbyScreen(ActivityLogin.this);
                else if (BookingApplication.ccProfiles.isEmpty())
                    BookingApplication.showPaymentOptions(getResources().getString(R.string.Skip), "", "", ActivityLogin.this, CODES.NONE, true);
                else
                    BookingApplication.showSplashScreen(ActivityLogin.this);
                break;

            case APIs.FORGOTPASSWORD:
                if (success)
                    try {
                        if (jsonResponse.has("vSecretQuestion")) {
                            forgotPasswordViews.setVisibility(View.VISIBLE);
                            forgot_password.setVisibility(View.GONE);
                            ll_register_signin_buttons.setVisibility(View.GONE);
                            registerViews.setVisibility(View.GONE);
                            tv_secret_question.setText(jsonResponse.getString("vSecretQuestion"));
                            loginPass.setHint(R.string.New_password);
                        } else
                            Toast.makeText(ActivityLogin.this, R.string.contact_backoffice, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                break;

            case APIs.RESETPASSWORD:
                if (success)
                    try {
                        confirm_new_password.setText("");
                        secret_answer_recovery.setText("");
                        forgotPasswordViews.setVisibility(View.GONE);
                        forgot_password.setVisibility(View.VISIBLE);
                        ll_register_signin_buttons.setVisibility(View.VISIBLE);
                        loginPass.setText("");
                        if (jsonResponse.has("responseMessage"))
                            Toast.makeText(ActivityLogin.this, jsonResponse.getString("responseMessage"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(ActivityLogin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                break;

        }
    }


    private void updateTexts() {
        userPhone.setHint(R.string.Phone_number);
        loginPass.setHint(R.string.New_password);
        forgot_password.setText(R.string.Forgot_Password);
        btn_login.setText(R.string.login_signin);
        lbl_OR.setText(R.string.OR);
        btn_register.setText(R.string.Register);
        showNumberToDrivers.setText(R.string.AllowNumberVisible);
        cb_Terms.setText(R.string.AgreeTerms);
        termOfUse.setText(R.string.termsofuse);
        repeatPass.setHint(R.string.Repeat_password);
        fullName.setHint(R.string.Full_name);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secretQuestion.setAdapter(adapter);
        secretAnswer.setHint(R.string.secret_answer);

    }
}
