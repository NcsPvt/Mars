package itc.booking.mars;

import org.json.JSONObject;

import android.location.Address;

import java.util.List;

public abstract interface CallbackResponseListener {
    public abstract void callbackResponseReceived(int apiCalled, JSONObject jsonResponse, List<Address> addressList, boolean paramBoolean);
}
