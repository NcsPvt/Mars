package itc.booking.mars;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import itcurves.mars.R;

public class ActivityTrack extends FragmentActivity implements CallbackResponseListener {

    /*-----------------------------------------------------------------------------------------------------------------------------------------------
     *---------------------------------------------------------- DrawPathTask AsyncTask ------------------------------------------------------------
     *-----------------------------------------------------------------------------------------------------------------------------------------------
     */
    private class DrawPathTask extends AsyncTask<LatLng, Void, ArrayList<LatLng>> {

        private Polyline pathLine;

        @Override
        protected ArrayList<LatLng> doInBackground(LatLng... params) {
            return DrawPath(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> directionPoint) {
            super.onPostExecute(directionPoint);
            try {

                boundsBuilder = new LatLngBounds.Builder();
                PolylineOptions rectLine = new PolylineOptions().width(14).color(Color.argb(255, 114, 208, 251));
                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                    boundsBuilder.include(directionPoint.get(i));
                }

                if (pathLine != null)
                    pathLine.remove();

                pathLine = mapFragment.addPolyline(rectLine);

                if (header.getText().toString().equalsIgnoreCase(getString(R.string.pickedup)))
                    eta.setText(getResources().getString(R.string.ETD, mapDirections.getDurationText(routeDoc)));
                else
                    eta.setText(getResources().getString(R.string.ETA, mapDirections.getDurationText(routeDoc)));

                if (autozoom) {
                    autozoom = false;
                    mapFragment.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 130));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Boolean rateLater = false;
    Boolean autozoom = true;
    ProgressDialog progress;
    GoogleMap mapFragment;
    private GMapV2Direction mapDirections;
    private Document routeDoc;
    NearbyVehicle currVehicle;
    Bundle extras;
    String iServiceID = "-1", confirmationNo = "0", driverPhone = "";
    Marker pickMarker, dropMarker, vehicleMarker;
    Button btn_call, bt_cancel;
    Timer requestStatustimer = new Timer();
    MediaPlayer callout_Sound;
    LinearLayout ll_rating, vehicle_balloon;
    RelativeLayout rl_top;
    TextView header, eta, comments;
    RatingBar rating_service, rating_clean, rating_etiquette, rating_TaxiOnTime;
    private LatLngBounds.Builder boundsBuilder;

    protected MultipartEntity outEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "~~~", Charset.forName("UTF-8"));

    @Override
    protected void onCreate(Bundle arg0) {
        BookingApplication.setMyTheme(ActivityTrack.this);

        extras = getIntent().getExtras();
        iServiceID = extras.getString("ServiceID");
        confirmationNo = extras.getString("ConfirmationNumber");

        callout_Sound = MediaPlayer.create(this, R.raw.dingdong);

        super.onCreate(arg0);
        setContentView(R.layout.activity_track_screen);

        btn_call = (Button) findViewById(R.id.bt_call);
        eta = (TextView) findViewById(R.id.tv_eta);
        eta.setText(getResources().getString(R.string.ETA, getResources().getString(R.string.coming_soon)));
        header = (TextView) findViewById(R.id.header_tracking);
        ll_rating = (LinearLayout) findViewById(R.id.ll_rating);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        rl_top = (RelativeLayout) findViewById(R.id.rl_top);
        vehicle_balloon = (LinearLayout) findViewById(R.id.vehicle_balloon_track_screen);

        rating_service = (RatingBar) findViewById(R.id.rating_service);
        rating_clean = (RatingBar) findViewById(R.id.rating_clean);
        rating_etiquette = (RatingBar) findViewById(R.id.rating_etiquette);
        rating_TaxiOnTime = (RatingBar) findViewById(R.id.rating_TaxiOnTime);
        comments = (TextView) findViewById(R.id.comments);

        if (extras.getInt("iEtiquetteQuality") > 0 || extras.getInt("iTaxiLate") > 0 || extras.getInt("iCleanQuality") > 0 || extras.getInt("iServiceQuality") > 0) {
            rateLater = true;
            rating_etiquette.setRating(extras.getInt("iEtiquetteQuality"));
            rating_TaxiOnTime.setRating(extras.getInt("iTaxiLate"));
            rating_clean.setRating(extras.getInt("iCleanQuality"));
            rating_service.setRating(extras.getInt("iServiceQuality"));
            comments.setText(extras.getString("vComments"));
        } else {
            rating_etiquette.setRating(5);
            rating_TaxiOnTime.setRating(5);
            rating_clean.setRating(5);
            rating_service.setRating(5);
        }
        mapDirections = new GMapV2Direction();
        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview_track)).getMap();
        mapFragment.getUiSettings().setZoomControlsEnabled(false);

        mapFragment.setOnMarkerClickListener(new OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker marker) {
                try {
                    if (marker.getTitle().equalsIgnoreCase("Vehicle")) {

                        showVehicleBaloon();
                        marker.hideInfoWindow();
                        return false;
                    }

                } catch (Exception e) {
                    Toast.makeText(ActivityTrack.this, e.toString(), Toast.LENGTH_LONG).show();

                }

                return true;
            }
        });

        mapFragment.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                vehicle_balloon.setVisibility(View.GONE);
            }
        });

        mapFragment.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(final CameraPosition position) {

                int cameraY = mapFragment.getProjection().toScreenLocation(mapFragment.getCameraPosition().target).y;
                vehicle_balloon.setY(cameraY - vehicle_balloon.getHeight() + rl_top.getHeight());

            }
        });

        if (extras.containsKey("PULat"))
            pickMarker = mapFragment.addMarker(new MarkerOptions().position(new LatLng(extras.getDouble("PULat"), extras.getDouble("PULng"))).title(getResources().getString(R.string.PickupLocation)).snippet(extras.getString("PUAddress")).icon(BitmapDescriptorFactory.fromResource(R.drawable.pupin)));
        if (extras.containsKey("DOLat"))
            dropMarker = mapFragment.addMarker(new MarkerOptions().position(new LatLng(extras.getDouble("DOLat"), extras.getDouble("DOLng"))).title(getResources().getString(R.string.DropLocation)).snippet(extras.getString("DOAddress")).icon(BitmapDescriptorFactory.fromResource(R.drawable.dopin)));
    }

    /*--------------------------------------- showVehicleBaloon -------------------------------------------------------------------------------------*/
    private void showVehicleBaloon() {

        Button btn_hail = (Button) vehicle_balloon.findViewById(R.id.btn_hail);

        if (currVehicle.driverPhone.length() > 7) {
            btn_hail.setVisibility(View.VISIBLE);
            btn_hail.setText(R.string.Call);
        } else
            btn_hail.setVisibility(View.GONE);

        btn_hail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currVehicle.driverPhone.length() > 7)
                    if (BookingApplication.isDialerAvailable(ActivityTrack.this))
                        new AlertDialog.Builder(ActivityTrack.this).setMessage(getResources().getString(R.string.Call) + " " + currVehicle.driverPhone + " ?").setPositiveButton(R.string.Call, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                BookingApplication.AddC2DHistoryInOutLoad(ActivityTrack.this, iServiceID);

                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + currVehicle.driverPhone));
                                startActivity(callIntent);
                            }
                        }).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        ImageView driverImage = (ImageView) vehicle_balloon.findViewById(R.id.img_driver);
        ImageView selected_veh_logo = (ImageView) vehicle_balloon.findViewById(R.id.selected_veh_logo);
        TextView driverName = (TextView) vehicle_balloon.findViewById(R.id.txt_driver_name);
        TextView company_name = (TextView) vehicle_balloon.findViewById(R.id.txt_company_name);
        TextView vehicleNumber = (TextView) vehicle_balloon.findViewById(R.id.txt_veh_plate);
        TextView vehicleMake = (TextView) vehicle_balloon.findViewById(R.id.txt_veh_make);
        TextView totalRatings = (TextView) vehicle_balloon.findViewById(R.id.txt_total_ratings);
        TextView vehicle_rate = (TextView) vehicle_balloon.findViewById(R.id.vehicle_rate);
        RatingBar rating = (RatingBar) vehicle_balloon.findViewById(R.id.rating);

        TextView ourRates = (TextView) vehicle_balloon.findViewById(R.id.ratesLink);
        ourRates.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currVehicle.ourRates.length() > 0)
                    BookingApplication.showWebScreen(ActivityTrack.this, currVehicle.ourRates);
            }
        });

        TextView ourFleet = (TextView) vehicle_balloon.findViewById(R.id.fleetLink);
        ourFleet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currVehicle.ourFleet.length() > 0)
                    BookingApplication.showWebScreen(ActivityTrack.this, currVehicle.ourFleet);
            }
        });

        TextView ourTerms = (TextView) vehicle_balloon.findViewById(R.id.termsLink);
        ourTerms.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currVehicle.ourTerms.length() > 0)
                    BookingApplication.showWebScreen(ActivityTrack.this, currVehicle.ourTerms);
            }
        });

        TextView aboutUs = (TextView) vehicle_balloon.findViewById(R.id.aboutUs);
        aboutUs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currVehicle.aboutUs.length() > 0)
                    BookingApplication.showWebScreen(ActivityTrack.this, currVehicle.aboutUs);
            }
        });

        driverName.setText(currVehicle.driverName);
        company_name.setText(BookingApplication.getAffiliateNameFromDB(ActivityTrack.this, currVehicle.iAffiliateID));
        rating.setRating((float) currVehicle.avg_Rating);
        totalRatings.setText(Integer.toString(currVehicle.iTotalRatings));
        vehicleNumber.setText(currVehicle.VehicleNo);
        vehicleMake.setText(currVehicle.vehicleMake);
        String[] hailRates = BookingApplication.getRatesFromDB(ActivityTrack.this, currVehicle.iVehTypeID, currVehicle.iAffiliateID, currVehicle.iClassID);
        if (hailRates != null && !hailRates[0].equalsIgnoreCase("0") && !hailRates[0].equalsIgnoreCase("")) {
            String rate = getResources().getString(R.string.Initial_Charges, hailRates[3] + " " + hailRates[0]) + "\n" + getResources().getString(R.string.per_unit_Charges) + " "
                    + hailRates[3] + " " + hailRates[1] + "\\" + hailRates[2];
            vehicle_rate.setText(rate);
        } else
            vehicle_rate.setText("See Our Rates.");

        if (currVehicle.driverPicture.length() > 0 && (currVehicle.driverPicture.endsWith("png") || currVehicle.driverPicture.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(currVehicle.driverPicture, driverImage);

        if (currVehicle.companyLogo.length() > 0 && (currVehicle.companyLogo.endsWith("png") || currVehicle.companyLogo.endsWith("jpg")))
            BookingApplication.imagedownloader.DisplayImage(currVehicle.companyLogo, selected_veh_logo);

        vehicle_balloon.setVisibility(View.VISIBLE);
    }

    /*------------------------------------------- onDestroy -------------------------------------------------------------------------------------*/
    @Override
    protected void onDestroy() {
        mapFragment.clear();
        mapFragment = null;
        requestStatustimer.cancel();
        super.onDestroy();
    }

    /*------------------------------------------ onResume -------------------------------------------------------------------------------------*/
    @Override
    protected void onResume() {
        super.onResume();
        BookingApplication.callerContext = this;
        BookingApplication.getRequestStatus(iServiceID, false, ActivityTrack.this);
    }

    /*------------------------------------------DrawPath--------------------------------------------------------------------------------------*/
    protected ArrayList<LatLng> DrawPath(LatLng pickup, LatLng dropoff) {
        routeDoc = mapDirections.getDocument(pickup, dropoff, GMapV2Direction.MODE_DRIVING);
        ArrayList<LatLng> directionPoint = mapDirections.getDirection(routeDoc);
        return directionPoint;
    }

    /*------------------------------------------ startRequestStatusPolling ------------------------------------------------------------------------------*/
    private void startRequestStatusPolling(final int time_msec) {

        requestStatustimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        BookingApplication.getRequestStatus(iServiceID, false, ActivityTrack.this);
                    }
                });
            }
        }, time_msec);

    }

    /*------------------------------------------ callbackResponseReceived ---------------------------------------------------------------------------------*/
    @Override
    public void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean paramBoolean) {
        if (apiCalled == BookingApplication.APIs.GETREQUESTSTATUS)
            try {

                if (jsonResponse.has("DriverPhone") && !jsonResponse.isNull("DriverPhone"))
                    driverPhone = jsonResponse.getString("DriverPhone");

                if (pickMarker == null)
                    if (jsonResponse.has("PickLat") && !jsonResponse.isNull("PickLat"))
                        pickMarker = mapFragment.addMarker(new MarkerOptions().position(new LatLng(jsonResponse.getDouble("PickLat"), jsonResponse.getDouble("PickLong"))).title(getResources().getString(R.string.PickupLocation)).icon(BitmapDescriptorFactory.fromResource(R.drawable.pupin)));

                if (jsonResponse.has("VehDriverInfo") && !jsonResponse.isNull("VehDriverInfo")) {

                    JSONObject driver = jsonResponse.getJSONObject("VehDriverInfo");
                    if (currVehicle == null)
                        currVehicle = new NearbyVehicle(driver.getString("vehicleType"), new LatLng(driver.getDouble("Latitude"), driver.getDouble("Longitude")));
                    else {
                        currVehicle.vehColor = driver.getString("vehicleType");
                        currVehicle.setLatlong(new LatLng(driver.getDouble("Latitude"), driver.getDouble("Longitude")));
                    }

                    currVehicle.companyLogo = jsonResponse.getString("BottomLogoLink");

                    currVehicle.driverPicture = driver.getString("DriverPicture");
                    currVehicle.driverName = driver.getString("DriverName");
                    currVehicle.iAffiliateID = driver.getInt("iAffiliateID");
                    currVehicle.driverPhone = driver.getString("DriverPhone");
                    currVehicle.vinNumber = driver.getString("VinNum");
                    currVehicle.VehicleNo = driver.getString("VehicleNum");
                    currVehicle.iTotalRatings = driver.getInt("Ratings");
                    currVehicle.avg_Rating = driver.getDouble("AverageRating");
                    currVehicle.vehicleMake = driver.getString("VehicleMake");
                    currVehicle.iWorkYears = driver.getInt("WorkYears");
                    currVehicle.ourRates = jsonResponse.has("RatesLink") ? jsonResponse.getString("RatesLink") : "";
                    currVehicle.ourFleet = jsonResponse.has("FleetInfoLink") ? jsonResponse.getString("FleetInfoLink") : "";
                    currVehicle.ourTerms = jsonResponse.has("TCLink") ? jsonResponse.getString("TCLink") : "";
                    currVehicle.aboutUs = jsonResponse.has("AboutUsLink") ? jsonResponse.getString("AboutUsLink") : "";

                    btn_call.setText(R.string.Call_driver);
                    driverPhone = driver.getString("DriverPhone");

                    if (vehicleMarker != null)
                        vehicleMarker.remove();

                    vehicleMarker = mapFragment.addMarker(currVehicle.vehMarker);
                    if (autozoom) {
                        autozoom = false;
                        mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(currVehicle.getLatlong(), 16));
                        showVehicleBaloon();
                    }
                }

                switch (jsonResponse.getInt("requestStatusCode")) {
                    case BookingApplication.CODES.BOOKING_SUCCESS:

                        if (jsonResponse.getString("requestStatus").equalsIgnoreCase("TSPACCEPTED") || jsonResponse.getString("requestStatus").equalsIgnoreCase("ACCEPTED")
                                || jsonResponse.getString("requestStatus").equalsIgnoreCase("IRTPU")) {
                            startRequestStatusPolling(15 * 1000);
                            if (jsonResponse.getString("requestStatus").equalsIgnoreCase("IRTPU"))
                                (new DrawPathTask()).execute(pickMarker.getPosition(), vehicleMarker.getPosition());
                            else if (autozoom) {
                                autozoom = false;
                                mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 16));
                            }
                        } else if (jsonResponse.getString("requestStatus").equalsIgnoreCase("CALLOUT")) {
                            callout_Sound.start();
                            Toast.makeText(ActivityTrack.this, R.string.driverwaiting, Toast.LENGTH_LONG).show();
                            startRequestStatusPolling(15 * 1000);

                        } else if (jsonResponse.getString("requestStatus").equalsIgnoreCase("IRTDO") || jsonResponse.getString("requestStatus").equalsIgnoreCase("PICKEDUP")) {

                            header.setText(R.string.pickedup);
                            bt_cancel.setVisibility(View.GONE);
                            startRequestStatusPolling(15 * 1000);
                            if (dropMarker != null)
                                (new DrawPathTask()).execute(vehicleMarker.getPosition(), dropMarker.getPosition());
                            else
                                eta.setVisibility(View.GONE);

                        } else if (jsonResponse.getString("requestStatus").equalsIgnoreCase("DROPPED") || jsonResponse.getString("requestStatus").equalsIgnoreCase("COMPLETED")) {
                            eta.setVisibility(View.GONE);
                            bt_cancel.setVisibility(View.GONE);

                            if (!rateLater)
                                if (!ll_rating.isShown()) {
                                    Animation inAnim = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.slide_in_bottom);
                                    ll_rating.startAnimation(inAnim);
                                    ll_rating.setVisibility(View.VISIBLE);
                                }

                            if (dropMarker != null) {
                                (new DrawPathTask()).execute(pickMarker.getPosition(), dropMarker.getPosition());
                            }
                        }

                        break;

                    case BookingApplication.CODES.NOSHOWREQUESTED:

                        new AlertDialog.Builder(ActivityTrack.this).setMessage(jsonResponse.getString("responseMessage")).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BookingApplication.sendNoShowResponse(iServiceID, true, ActivityTrack.this);
                                startRequestStatusPolling(7 * 1000);
                            }
                        }).setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BookingApplication.sendNoShowResponse(iServiceID, false, ActivityTrack.this);
                                startRequestStatusPolling(7 * 1000);
                            }
                        }).show();

                        break;

                    case BookingApplication.CODES.ACTIVATION_REQUIRED:

                        new AlertDialog.Builder(ActivityTrack.this).setMessage(jsonResponse.getString("responseMessage")).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                        break;

                    case BookingApplication.CODES.TRIP_SERVED:
                        if (jsonResponse.getString("requestStatus").equalsIgnoreCase("CANCELLED") || jsonResponse.getString("requestStatus").equalsIgnoreCase("NOSHOW")) {
                            new AlertDialog.Builder(ActivityTrack.this).setMessage(jsonResponse.getString("responseMessage")).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).show();
                            break;
                        }
                        header.setText(jsonResponse.has("requestStatus") ? jsonResponse.getString("requestStatus") : getString(R.string.Completed));
                        eta.setVisibility(View.GONE);
                        bt_cancel.setVisibility(View.GONE);


                        if (!rateLater)
                            if (!ll_rating.isShown()) {
                                Animation inAnim = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.slide_in_bottom);
                                ll_rating.startAnimation(inAnim);
                                ll_rating.setVisibility(View.VISIBLE);
                            }

                        if (dropMarker != null) {
                            (new DrawPathTask()).execute(pickMarker.getPosition(), dropMarker.getPosition());
                        } else
                            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(pickMarker.getPosition(), 16));

                        showCustomDialog(BookingApplication.CODES.TRIP_SERVED, getApplicationContext().getApplicationInfo().name, jsonResponse.getString("responseMessage"), 0, false);

                        break;
                }//switch

            } catch (Exception e) {
                Toast.makeText(ActivityTrack.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        else if (apiCalled == BookingApplication.APIs.SUBMITRATING) {
            if (header.getText().toString().equalsIgnoreCase(getResources().getString(R.string.Completed)) || header.getText().toString().equalsIgnoreCase("DROPPED") || header.getText().toString().equalsIgnoreCase("CANCELLED"))
                finish();
        } else if (apiCalled == BookingApplication.APIs.CANCELTRIP) {
            Trip tempTrip = new Trip(0.0, 0.0, "", 0, 0);
            tempTrip.iServiceID = iServiceID;
            BookingApplication.recentTrips.remove(tempTrip);
            finish();
        }
    }

    /*---------------------------------------------- closeVehicleMarker ---------------------------------------------------------------------------------*/
    public void closeVehicleMarker(View v) {
        vehicle_balloon.setVisibility(View.GONE);
    }

    /*---------------------------------------------- Cancel_Booking -------------------------------------------------------------------------------------*/
    public void Cancel_Booking(View v) {
        showCustomDialog(BookingApplication.APIs.CANCELTRIP, getString(R.string.Cancel_ride), getResources().getString(R.string.Confirm_Cancel), 0, true);
    }

    /*---------------------------------------------- Call_Driver -------------------------------------------------------------------------------------*/
    public void Call_Driver(View v) {
        if (driverPhone.length() > 7)
            if (BookingApplication.isDialerAvailable(ActivityTrack.this))
                new AlertDialog.Builder(ActivityTrack.this).setMessage(getResources().getString(R.string.Call) + " " + driverPhone + " ?").setPositiveButton(R.string.Call, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        BookingApplication.AddC2DHistoryInOutLoad(ActivityTrack.this, iServiceID);

                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + driverPhone));
                        startActivity(callIntent);
                    }
                }).setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /*----------------------------------------------- Cancel_Rating -------------------------------------------------------------------------------------*/
    public void Cancel_Rating(View v) {
        rateLater = true;
        Animation inAnim = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.slide_out_bottom);
        ll_rating.startAnimation(inAnim);
        ll_rating.setVisibility(View.GONE);
    }

    /*----------------------------------------------- Submit_Rating -------------------------------------------------------------------------------------*/
    public void Submit_Rating(View v) {
        rateLater = true;
        BookingApplication.submitRating(iServiceID, Integer.toString((int) rating_service.getRating()), Integer.toString((int) rating_clean.getRating()), Integer.toString((int) rating_etiquette.getRating()), Integer.toString((int) rating_TaxiOnTime.getRating()), comments.getText().toString(), this);
        Animation inAnim = AnimationUtils.loadAnimation(ActivityTrack.this, R.anim.slide_out_bottom);
        ll_rating.startAnimation(inAnim);
        ll_rating.setVisibility(View.GONE);
    }

    /*------------------------------------------------ Custom  Dialog -------------------------------------------------------------------------------------*/
    public void showCustomDialog(final int reasonCode, String dialogTitle, String dialogText, int imageResID, Boolean showCancelBtn) {
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
        if (reasonCode == BookingApplication.APIs.CANCELTRIP || reasonCode == BookingApplication.CODES.EXIT)
            btn_OK.setText(R.string.Yes);
        else if (reasonCode == BookingApplication.CODES.TRIP_SERVED)
            btn_OK.setText(R.string.OK);

        Button btn_CANCEL = (Button) layout.findViewById(R.id.btnNo);
        btn_CANCEL.setText(R.string.No);
        if (!showCancelBtn)
            btn_CANCEL.setVisibility(View.GONE);
        btn_OK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.dismiss();
                switch (reasonCode) {

                    case BookingApplication.APIs.CANCELTRIP:
                        BookingApplication.cancelTrip(confirmationNo, "", iServiceID, "CURR", ActivityTrack.this);
                        break;
                    case BookingApplication.CODES.EXIT:
                        finish();
                        break;
                }
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
