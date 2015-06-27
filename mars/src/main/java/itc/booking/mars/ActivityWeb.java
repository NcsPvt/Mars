package itc.booking.mars;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import itcurves.mars.R;

public class ActivityWeb extends FragmentActivity {

    Bundle extras;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        extras = getIntent().getExtras();
        setContentView(R.layout.activity_web_screen);

        WebView webView = (WebView) findViewById(R.id.webView1);
        WebSettings settings = webView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(extras.getString("url"));

        setResult(BookingApplication.CODES.WEB_ACTIVITY);
    }

}
