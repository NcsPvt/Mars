package itc.booking.mars;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import itcurves.mars.R;

/**
 * Created by chzahid on 12/26/2014.
 */
public class GcmListenService extends GcmListenerService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmListenService() {
        super();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d("PushMsg", "From: " + from);
        Log.d("PushMsg", "Message: " + message);

        sendNotification(message);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {

        String contentTitle = "Update Received";
        String tickerText = "";
        String contentText = "";
        String PromotionLink = "";
        Boolean bShowTrackScreen = false;
        String TripID = "0";

        try {
            //JSONArray JSONaray = new JSONArray(msg);
            //JSONObject JSONResp = JSONaray.getJSONObject(0);
            JSONObject JSONResp = new JSONObject(msg);

            if (JSONResp.has("MessageTitle"))
                contentTitle = JSONResp.getString("MessageTitle");
            if (JSONResp.has("MessageTitle"))
                tickerText = JSONResp.getString("Message");
            if (JSONResp.has("Message"))
                contentText = JSONResp.getString("Message");
            if (JSONResp.has("bShowTrackScreen"))
                bShowTrackScreen = JSONResp.getBoolean("bShowTrackScreen");
            if (JSONResp.has("TripID"))
                TripID = JSONResp.getString("TripID");
            if (JSONResp.has("CustomData"))
                JSONResp = JSONResp.getJSONObject("CustomData");

            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            final PendingIntent contentIntent;
            final Intent notifyIntent;
            if (bShowTrackScreen)
                notifyIntent = (new Intent(this, ActivityTrack.class)).putExtra("ServiceID", TripID);
            else
                notifyIntent = new Intent(this, ActivitySplash.class);

            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);

            if (JSONResp.has("PromotionLink")) {
                ImageView promoImage = new ImageView(this);
                final String finalTickerText = tickerText;
                final JSONObject finalJSONResp = JSONResp;
                final String finalContentTitle = contentTitle;
                promoImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(GcmListenService.this)
                                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                                    .setContentTitle(finalContentTitle)
                                    .setContentText(finalTickerText)
                                    .setTicker(finalTickerText)
                                    .setSmallIcon(R.drawable.launcher_icon)
                                    .setStyle(new NotificationCompat.BigPictureStyle().setBigContentTitle(finalContentTitle).bigPicture(BookingApplication.imagedownloader.getBitmap(finalJSONResp.getString("PromotionLink"), true)))
                                    .setAutoCancel(true)
                                    .setContentIntent(contentIntent);

                            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                BookingApplication.imagedownloader.DisplayImage(JSONResp.getString("PromotionLink"), promoImage);
            } else {

                try {
                    Notification.Builder mBuilder = new Notification.Builder(GcmListenService.this)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setTicker(tickerText)
                            .setSmallIcon(R.drawable.launcher_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.launcher_icon))
                                    //.setStyle(new NotificationCompat.)
                            .setAutoCancel(true)
                            .setContentIntent(contentIntent);

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                    else
                        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.getNotification());

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}