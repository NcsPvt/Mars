package itc.booking.mars;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SystemBroadcastReceiver extends BroadcastReceiver {
    @SuppressWarnings("unused")
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.i(getClass().toString(), "android.net.conn.CONNECTIVITY_CHANGE");
            NetworkInfo activeNetwork = BookingApplication.cm.getActiveNetworkInfo();
            BookingApplication.isNetworkConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else if (action.equals("com.android.vending.INSTALL_REFERRER")) {
            Log.i("INSTALL_REFERRER", intent.getStringExtra("referrer"));
            BookingApplication.appID = intent.getStringExtra("referrer");
            BookingApplication.userInfoPrefs.edit().putString("appID", BookingApplication.appID).commit();
        } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {

            String strMsgSrc = "";
            String strMsgBody = "";
            String strTimeStamp = "";

            if (extras != null) {
                Object[] smsextras = (Object[]) extras.get("pdus");

                for (Object smsextra : smsextras) {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextra);

                    strMsgSrc = smsmsg.getOriginatingAddress();
                    strTimeStamp = Long.toString(smsmsg.getTimestampMillis());
                    strMsgBody += smsmsg.getMessageBody();
                }

                if (strMsgBody.matches("^Your.+code\\sis\\s([0-9]{6})$")) {
                    abortBroadcast();
                    try {
                        strMsgBody = strMsgBody.substring(strMsgBody.lastIndexOf(" ") + 1, strMsgBody.length());
                        if (BookingApplication.callerContext != null)
                            if (BookingApplication.callerContext.getClass().getName().equalsIgnoreCase(ActivityVerifyNumber.class.getName()))
                                BookingApplication.performPostActivation(strMsgBody, "", "");
                    } catch (Exception e) {

                    }
                }
            }
        }
    }// onReceive

}
