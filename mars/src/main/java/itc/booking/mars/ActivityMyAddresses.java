package itc.booking.mars;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import itc.booking.mars.BookingApplication.APIs;
import itcurves.mars.R;

public class ActivityMyAddresses extends Activity implements CallbackResponseListener {

    public class SearchListAdapter extends ArrayAdapter<String> {

        public SearchListAdapter(Context context, int textViewResourceId, ArrayList<String> suggestions) {

            super(context, textViewResourceId, suggestions);
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
            row = inflater.inflate(R.layout.autocompletetextview, parent, false);

            TextView searchResult = (TextView) row.findViewById(R.id.searctext);
            searchResult.setText(predictions.get(position));

            return row;
        }
    } // SearchListAdapter Class

    public class FavoritesAdapter extends ArrayAdapter<Addresses> {

        private final ArrayList<Addresses> addressesList;
        private final int textViewResource;
        private Context myContext;
        Address addrs = new Address(Locale.US);

        public FavoritesAdapter(Context context, int textViewResourceId, ArrayList<Addresses> mPlaces) {

            super(context, textViewResourceId, mPlaces);
            myContext = context;
            textViewResource = textViewResourceId;
            addressesList = mPlaces;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public int getPosition(Addresses item) {
            for (int i = 0; i < addressesList.size(); i++)
                if (addressesList.get(i).latlong.equals(item.latlong))
                    return i;
            return -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final Addresses currentPlace = addressesList.get(position);
            View currentView = convertView;

            if (currentView == null) {
                LayoutInflater vi = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                currentView = vi.inflate(textViewResource, null);
            }
            if (currentPlace != null) {

                TextView caption = (TextView) currentView.findViewById(R.id.tv_fav_header);
                TextView tv_fav_address = (TextView) currentView.findViewById(R.id.tv_fav_address);
                final CheckBox cb_fav = (CheckBox) currentView.findViewById(R.id.cb_fav);
                if (currentPlace.favId > 0)
                    cb_fav.setChecked(true);
                else
                    cb_fav.setChecked(false);
                cb_fav.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton paramCompoundButton, boolean isChecked) {
                        if (!isChecked) {
                            if (currentPlace.favId > 0)
                                showFavoriteDialog(currentPlace, R.string.Remove, 0);
                            cb_fav.setChecked(true);
                        }
                    }
                });

                caption.setText(currentPlace.caption);
                tv_fav_address.setText(currentPlace.address);

                currentView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Address addr = new Address(Locale.getDefault());
                        addr.setAddressLine(0, currentPlace.address);
                        addr.setLocality(currentPlace.city);
                        addr.setAdminArea(currentPlace.state);
                        addr.setPostalCode(currentPlace.zip);
                        addr.setCountryCode(currentPlace.country);
                        addr.setLatitude(currentPlace.latlong.latitude);
                        addr.setLongitude(currentPlace.latlong.longitude);

                        Intent searchResult = new Intent();
                        Bundle bndle = new Bundle();
                        bndle.putParcelable("Address", addr);
                        searchResult.putExtras(bndle);

                        setResult(BookingApplication.CODES.SEARCHADDRESS, searchResult);
                        ActivityMyAddresses.this.finish();
                    }
                });

                currentView.setTag(currentPlace.latlong);
            }
            return currentView;
        }
    } // FavoritesAdapter Class

    Bundle extras;
    private Geocoder geocoder;
    Timer requestStatustimer = new Timer();
    TimerTask timerTask;
    TextView header, tv_recent;
    EditText searchBox;
    CheckBox cb_SearchAroundGlobe;
    ListView lv_suggestions, lv_favorites, lv_recents;
    LinearLayout rl_recent_favorite;
    ArrayList<String> predictions = new ArrayList<String>();
    private ArrayAdapter<String> searchListAdaptor;
    private ArrayAdapter<Addresses> favListAdaptor;
    private ProgressBar search_progressBar;
    private List<Address> addressList;
    private String countryName = "";

    @Override
    protected void onCreate(Bundle arg0) {
        BookingApplication.setMyTheme(ActivityMyAddresses.this);

        extras = getIntent().getExtras();

        super.onCreate(arg0);
        setContentView(R.layout.activity_search_address);

        header = (TextView) findViewById(R.id.tv_searchHeader);
        header.setBackgroundColor(getResources().getColor(BookingApplication.theme_color));
        header.setText(R.string.MyAddresses);
        search_progressBar = (ProgressBar) findViewById(R.id.search_progressBar);
        searchBox = (EditText) findViewById(R.id.et_address_search);
        cb_SearchAroundGlobe = (CheckBox) findViewById(R.id.cb_SearchAroundGlobe);
        rl_recent_favorite = (LinearLayout) findViewById(R.id.rl_recent_favorite);
        lv_suggestions = (ListView) findViewById(R.id.lv_suggestions);
        lv_favorites = (ListView) findViewById(R.id.lv_favorites);
        tv_recent = (TextView) findViewById(R.id.tv_recent);
        lv_recents = (ListView) findViewById(R.id.lv_recents);
        tv_recent.setVisibility(View.GONE);
        lv_recents.setVisibility(View.GONE);

        favListAdaptor = new FavoritesAdapter(this, R.layout.list_item_favorite, BookingApplication.favorites);
        lv_favorites.setAdapter(favListAdaptor);

        searchListAdaptor = new SearchListAdapter(this, R.layout.autocompletetextview, predictions);
        lv_suggestions.setAdapter(searchListAdaptor);
        lv_suggestions.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Address address = addressList.get(position);
                Addresses myFav = new Addresses(address.getAddressLine(0), address.getAddressLine(0), new LatLng(address.getLatitude(), address.getLongitude()));
                myFav.zip = address.getPostalCode();
                myFav.city = address.getLocality();
                myFav.state = address.getAdminArea();
                myFav.country = address.getCountryCode();
                showFavoriteDialog(myFav, R.string.Cancel, 0);

                searchBox.setText("");
            }
        });
        //searchBox.setHint(getResources().getString(R.string.Enter_Your, getResources().getString(extras.getInt("Header"))));
        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                startPredictions(s.toString());
            }
        });

        geocoder = new Geocoder(ActivityMyAddresses.this);
    }

    /*-------------------------------------------------------------- onDestroy -------------------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        requestStatustimer.cancel();
        super.onDestroy();
    }

    /*--------------------------------------------------------------- onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
    }

    /*----------------------------------------------------------- onWindowFocusChanged -------------------------------------------------------------------------------------*/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
            getCurrentLocation();
        super.onWindowFocusChanged(hasFocus);
    }

    /*---------------------------------------------------------- getCurrentLocation -------------------------------------------------------------------------------------*/
    private void getCurrentLocation() {
        try {
            List<Address> addressList = geocoder.getFromLocation(extras.getDouble("Latitude"), extras.getDouble("Longitude"), 3);
            if (addressList.size() > 0)
                //StringBuilder sb = new StringBuilder();
                //for (Address currAdd : addressList)
                //if (sb.indexOf((currAdd.getAddressLine(0))) < 0) {
                //if (sb.length() > 0)
                //sb.append(", ");
                //sb.append(currAdd.getAddressLine(0));
                //}
                //searchBox.setText(sb.toString());
                countryName = ", " + addressList.get(0).getCountryName();
        } catch (Exception e) {
            if (e != null) {
                String msgToShow = getResources().getString(R.string.unknown_address) + "\n" + e.getMessage();
                if (e.getMessage().equalsIgnoreCase("Service not Available"))
                    msgToShow = getResources().getString(R.string.Restart_Phone);
                BookingApplication.showCustomToast(0, msgToShow, true);
            }
        }
    }

    /*------------------------------------------------------ expandSearch -------------------------------------------------------------------------------------*/
    public void expandSearch(View v) {
        if (searchBox.getText().length() > 0)
            startPredictions(searchBox.getText().toString());
    }

    /*------------------------------------------------------ startRequestStatusPolling -------------------------------------------------------------------------------------*/
    private void startPredictions(final String locationName) {

        if (timerTask != null)
            timerTask.cancel();
        requestStatustimer.purge();

        timerTask = (new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            search_progressBar.post(new Runnable() {

                                @Override
                                public void run() {
                                    search_progressBar.setVisibility(View.VISIBLE);
                                }
                            });

                            if (!cb_SearchAroundGlobe.isChecked() && locationName.length() > 0)
                                addressList = geocoder.getFromLocationName(locationName + countryName, 5);
                            else
                                addressList = geocoder.getFromLocationName(locationName, 5);

                            predictions.clear();
                            StringBuilder sb = new StringBuilder();
                            for (Address currAdd : addressList)
                                if (!currAdd.getFeatureName().equalsIgnoreCase(currAdd.getPostalCode())) {
                                    sb.delete(0, sb.length());
                                    sb.append(currAdd.getAddressLine(0));
                                    if (currAdd.getMaxAddressLineIndex() > 0) {
                                        sb.append(", ");
                                        sb.append(currAdd.getAddressLine(1));
                                    }
                                    if (currAdd.getMaxAddressLineIndex() > 1) {
                                        sb.append(", ");
                                        sb.append(currAdd.getAddressLine(2));
                                    }
                                    predictions.add(sb.toString());
                                }

                            if (predictions.size() > 0) {
                                lv_suggestions.setVisibility(View.VISIBLE);
                                rl_recent_favorite.setVisibility(View.GONE);
                            } else {
                                lv_suggestions.setVisibility(View.GONE);
                                rl_recent_favorite.setVisibility(View.VISIBLE);
                            }
                            searchListAdaptor.notifyDataSetChanged();
                            search_progressBar.post(new Runnable() {

                                @Override
                                public void run() {
                                    search_progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        } catch (Exception e) {
                            search_progressBar.post(new Runnable() {

                                @Override
                                public void run() {
                                    search_progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        requestStatustimer.schedule(timerTask, 1000);

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

        address_name.setText(fav_address.caption);
        streetAddress.setText(fav_address.address);

        TextView btn_OK = (TextView) layout.findViewById(R.id.btnSAVE);
        TextView btn_CANCEL = (TextView) layout.findViewById(R.id.btnCANCEL);
        btn_CANCEL.setText(cancelBtnCaption);

        btn_OK.setOnClickListener(new OnClickListener() {
            Addresses adrs = fav_address;

            @Override
            public void onClick(View v) {
                bb.dismiss();

                adrs.caption = address_name.getText().toString();
                BookingApplication.addUpdateFavorite(adrs);

            }
        });

        btn_CANCEL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bb.dismiss();

                if (cancelBtnCaption == R.string.Remove) {

                    if (BookingApplication.favorites.contains(fav_address)) {
                        //BookingApplication.db.delete("Favorites", "favId=?", new String[] { Integer.toString(currFav.favId) });
                        BookingApplication.removeFavorite(fav_address, ActivityMyAddresses.this);
                        BookingApplication.favorites.remove(fav_address);
                        favListAdaptor.notifyDataSetChanged();
                    }
                }
            }
        });

        bb.show();
    }

    /*-------------------------------------------------- callbackResponseReceived -------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean success) {

        try {
            switch (apiCalled) {
                case APIs.REMOVEFAVORITE:
                    favListAdaptor.notifyDataSetChanged();
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(ActivityMyAddresses.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
