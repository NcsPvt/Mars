package itc.booking.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import itc.booking.mars.BookingApplication.APIs;
import itcurves.mars.R;

public class ActivityProfile extends Activity implements CallbackResponseListener {

    private LinearLayout newPasswordViews;
    ArrayAdapter<String> adapter;
    private AutoCompleteTextView profile_email;
    private Spinner preferedLanguage, profile_secret_question, spinner_prefered_company;
    private CheckBox cb_showNumberToDriver, isExclusive;
    private TextView language_lbl, profile_phone, profile_marsID;
    private EditText profile_fullname, profile_user_password, profile_new_password, profile_repeatNew_password, profile_secret_answer;
    private ImageView iv_changePassword;
    private Button saveBtn;
    private JSONArray jsonArray;
    private JSONObject tempObject;
    protected String tspPreferrence = "None";
    protected String tspID = "0";
    private LinearLayout preferredCompanyRow;
    int spinnerLastPosition = 0;
    int questionsIterator = -1;
    public static ArrayList<TSPs> tsplist = new ArrayList<TSPs>();
    public static ArrayList<String> tspListNames = new ArrayList<String>();

    public static class TSPs {
        public String tspID;
        public String tspName;
        public String preferrence;

        public TSPs(String _tspID, String _tspName, String _preferrence) {
            tspID = _tspID;
            tspName = _tspName;
            preferrence = _preferrence;
        }
    }

    public class AffiliateListAdapter<T> extends ArrayAdapter<String> {

        public AffiliateListAdapter(Context context, int textViewResourceId, ArrayList<String> tsplist) {

            super(context, textViewResourceId, tsplist);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            TextView tspName = (TextView) findViewById(android.R.id.text1);
            tspName.setText(tsplist.get(position).tspName);

            return row;
        }
    } // AffiliateListAdapter Class

    @Override
    /*------------------------------------------------- onCreate ---------------------------------------------------------------------*/
    protected void onCreate(Bundle arg0) {
        BookingApplication.setMyTheme(ActivityProfile.this);

        super.onCreate(arg0);
        setContentView(R.layout.activity_profile);

        preferredCompanyRow = (LinearLayout) findViewById(R.id.preferredCompanyRow);
        isExclusive = (CheckBox) findViewById(R.id.isExclusive);
        isExclusive.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    tspPreferrence = "Exclusive";
                else
                    tspPreferrence = "Preferred";

            }
        });
        profile_secret_question = (Spinner) findViewById(R.id.profile_secret_question);

        spinner_prefered_company = (Spinner) findViewById(R.id.prefered_company);
        spinner_prefered_company.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                tspID = tsplist.get(position).tspID;
                if (position > 0)
                    //isExclusive.setVisibility(View.VISIBLE);
                    //if (isExclusive.isChecked())
                    //tspPreferrence = "Exclusive";
                    //else
                    tspPreferrence = "Preferred";
                else {
                    isExclusive.setVisibility(View.GONE);
                    tspPreferrence = "None";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

            }
        });

        language_lbl = (TextView) findViewById(R.id.language_lbl);
        profile_phone = (TextView) findViewById(R.id.profile_phone_number);
        profile_phone.setText(BookingApplication.phoneNumber);
        profile_marsID = (TextView) findViewById(R.id.profile_marsid);
        profile_marsID.setText(BookingApplication.marsID);
        profile_fullname = (EditText) findViewById(R.id.profile_fullname);
        profile_user_password = (EditText) findViewById(R.id.profile_user_password);
        newPasswordViews = (LinearLayout) findViewById(R.id.newPasswordViews);
        profile_new_password = (EditText) findViewById(R.id.profile_new_password);
        profile_repeatNew_password = (EditText) findViewById(R.id.profile_confirm_new_password);
        profile_email = (AutoCompleteTextView) findViewById(R.id.profile_email);
        profile_secret_answer = (EditText) findViewById(R.id.profile_secret_answer);
        cb_showNumberToDriver = (CheckBox) findViewById(R.id.profile_showNumberToDriver);
        iv_changePassword = (ImageView) findViewById(R.id.iv_changePassword);
        iv_changePassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (newPasswordViews.getVisibility() == View.GONE)
                    newPasswordViews.setVisibility(View.VISIBLE);
                else
                    newPasswordViews.setVisibility(View.GONE);
            }
        });

        saveBtn = (Button) findViewById(R.id.btn_Register);
        ArrayAdapter<String> emailAdapter = new ArrayAdapter<String>(ActivityProfile.this, android.R.layout.simple_spinner_dropdown_item, BookingApplication.possibleEmail);
        profile_email.setAdapter(emailAdapter);

    }

    /*--------------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
        BookingApplication.fetchProfile(BookingApplication.password, this);
    }

    /*-----------------------------------------------------ITC_SaveProfile--------------------------------------------------------------------------------------*/
    public void ITC_SaveProfile(View v) {
        if (profile_user_password.getText().toString().length() > 3)
            if (profile_new_password.getText().toString().equals(profile_repeatNew_password.getText().toString())) {
                BookingApplication.performUpdateProfile(Boolean.toString(cb_showNumberToDriver.isChecked()), profile_user_password.getText().toString(), profile_new_password.getText().toString(), profile_fullname.getText().toString(), profile_email.getText().toString(), profile_secret_question.getSelectedItem().toString(), profile_secret_answer.getText().toString(), tspPreferrence, tspID, ActivityProfile.this);
            } else {
                profile_repeatNew_password.setError(getResources().getString(R.string.invalid_password_match));
                Toast.makeText(ActivityProfile.this, R.string.invalid_password_match, Toast.LENGTH_SHORT).show();
            }
        else {
            profile_user_password.setError(getResources().getString(R.string.invalid_password_length));
            Toast.makeText(ActivityProfile.this, R.string.invalid_password_length, Toast.LENGTH_SHORT).show();
        }
    }

    /*------------------------------------------------------ callbackResponseReceived -------------------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean paramBoolean) {

        switch (apiCalled) {

            case APIs.UPDATEPROFILE:
                Intent resultIntent = new Intent();
                Bundle rebootOption = new Bundle();

                if (isExclusive.isChecked())
                    rebootOption.putBoolean("RebootApp", true);
                else
                    rebootOption.putBoolean("RebootApp", false);

                resultIntent.putExtras(rebootOption);
                setResult(APIs.UPDATEPROFILE, resultIntent);
                finish();
                break;
            case APIs.FETCHPROFILE: {
                //{"ResponseCode":0,"responseMessage":"Success","Language":"en","UserID":21,"ResponseType":"GetUserInfoResponse","username":"Test Account",
                //"phoneno":"3219424794","email":"mzahid@ncspvt.com","vLanguage":"en","bShowPhone":"True","vSecretAnswer":"oooo","vSecretQuestion":"Name of your favorite car ?",
                //"Affiliates":[{"TSPID":0,"TSPName":"Any Company","Preference":"None"},{"TSPID":2,"TSPName":"Regency","Preference":"None"},{"TSPID":3,"TSPName":"SupremeShuttle","Preference":"None"},{"TSPID":4,"TSPName":"Challenger","Preference":"None"}]}
                try {
                    if (jsonResponse.getString("vLanguage").equalsIgnoreCase("en")) {
                        preferedLanguage.setSelection(0);
                        BookingApplication.selected_lang = "en";
                        //BookingApplication.setMyLanguage("en");
                    } else if (jsonResponse.getString("vLanguage").equalsIgnoreCase("es")) {
                        preferedLanguage.setSelection(1);
                        BookingApplication.selected_lang = "es";
                        //BookingApplication.setMyLanguage("es");
                    } else if (jsonResponse.getString("vLanguage").equalsIgnoreCase("ar")) {
                        preferedLanguage.setSelection(2);
                        BookingApplication.selected_lang = "ar";
                        //BookingApplication.setMyLanguage("ar");
                    }
                    cb_showNumberToDriver.setChecked(jsonResponse.getBoolean("bShowPhone"));
                    profile_fullname.setText(jsonResponse.getString("username"));
                    profile_email.setText(jsonResponse.getString("email"));
                    profile_secret_answer.setText(jsonResponse.getString("vSecretAnswer"));
                    String[] questionsArray = getResources().getStringArray(R.array.questions);
                    int i;
                    for (questionsIterator = 0; questionsIterator < questionsArray.length; questionsIterator++)
                        if (questionsArray[questionsIterator].equalsIgnoreCase(jsonResponse.getString("vSecretQuestion")))
                            break;
                    if (questionsIterator < questionsArray.length)
                        profile_secret_question.setSelection(questionsIterator);

                    if (jsonResponse.has("Affiliates")) {
                        jsonArray = jsonResponse.getJSONArray("Affiliates");
                        if (jsonArray.length() > 0) {
                            tsplist.clear();
                            tspListNames.clear();
                            for (i = 0; i < jsonArray.length(); i++) {
                                tempObject = jsonArray.getJSONObject(i);
                                tsplist.add(new TSPs(tempObject.getString("TSPID"), tempObject.getString("TSPName"), tempObject.getString("Preference")));
                                tspListNames.add(tempObject.getString("TSPName"));
                                if (tempObject.getString("Preference").equalsIgnoreCase("Preferred"))
                                    spinnerLastPosition = i;
                                else if (tempObject.getString("Preference").equalsIgnoreCase("Exclusive")) {
                                    spinnerLastPosition = i;
                                    isExclusive.setChecked(true);
                                }
                            }
                            preferredCompanyRow.setVisibility(View.VISIBLE);
                            isExclusive.setVisibility(View.VISIBLE);
                            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tspListNames);
                            spinner_prefered_company.setAdapter(adapter);

                            spinner_prefered_company.setSelection(spinnerLastPosition);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            }//FETCHPROFILE
        }
    }

    private void updateTexts() {
        cb_showNumberToDriver.setText(R.string.showNumber);
        language_lbl.setText(R.string.Language);
        profile_fullname.setHint(R.string.Full_name);
        profile_user_password.setHint(R.string.password);
        profile_secret_answer.setHint(R.string.secret_answer);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.questions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profile_secret_question.setAdapter(adapter);


        if (questionsIterator > -1)
            profile_secret_question.setSelection(questionsIterator);

        saveBtn.setText(R.string.Save);

    }
}
