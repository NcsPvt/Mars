package itc.booking.mars;

import com.google.android.gms.iid.InstanceIDListenerService;

import itcurves.mars.R;

/**
 * Created by chzahid on 6/15/2015.
 */
public class MyInstanceIDService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        BookingApplication.registerGCMInBackground(getResources().getString(R.string.project_id));
        super.onTokenRefresh();
    }
}
