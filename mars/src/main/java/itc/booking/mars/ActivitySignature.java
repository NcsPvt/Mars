package itc.booking.mars;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import itc.booking.mars.BookingApplication.CODES;
import itcurves.mars.R;

public class ActivitySignature extends Activity {

    RelativeLayout rl_canvas;
    LinearLayout ll_book_trip, ll_Button_Bar;
    signature mSignature;
    public static String tempDir;
    public int count = 1;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File filePath;
    Bundle extras;

    private String uniqueId;
    private TextView fairEstimate, timeStamp;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingApplication.setMyTheme(ActivitySignature.this);

        extras = getIntent().getExtras();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signature);

        tempDir = Environment.getExternalStorageDirectory() + "/SignatureCapture/";
        // ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // File directory = cw.getDir("SignatureCapture", Context.MODE_PRIVATE);

        prepareDirectory();

        rl_canvas = (RelativeLayout) findViewById(R.id.rl_canvas);
        if (BookingApplication.theme_color == R.color.mars_red)
            rl_canvas.setBackgroundResource(R.drawable.signature_border_red);
        else if (BookingApplication.theme_color == R.color.mars_yellow)
            rl_canvas.setBackgroundResource(R.drawable.signature_border_yellow);
        else if (BookingApplication.theme_color == R.color.mars_blue)
            rl_canvas.setBackgroundResource(R.drawable.signature_border_blue);

        ll_Button_Bar = (LinearLayout) findViewById(R.id.ll_Button_Bar);
        ll_Button_Bar.setBackgroundResource(BookingApplication.textView_Background);
        ll_book_trip = (LinearLayout) findViewById(R.id.ll_book_trip);
        ll_book_trip.setEnabled(false);
        fairEstimate = (TextView) findViewById(R.id.fairEstimate);
        timeStamp = (TextView) findViewById(R.id.timeStamp);
        timeStamp.setText(BookingApplication.getDateTime("MMM dd h:mm a"));

        mSignature = new signature(this, null);
        rl_canvas.addView(mSignature, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mView = rl_canvas;

        if (extras != null)
            fairEstimate.setText(getResources().getString(R.string.fairEstimate, extras.getString("fareEstimate")));
    }

    public void book_trip(View v) {
        mView.setDrawingCacheEnabled(true);
        mSignature.save(mView);
        Bundle b = new Bundle();
        b.putString("signatureUrl", filePath.getAbsolutePath());
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(CODES.SIGNATURE_REQUIRED, intent);
        finish();
    }

    public void clear_canvas(View v) {
        mSignature.clear();
        ll_book_trip.setEnabled(false);
    }

    public void cancel_signature(View v) {
        Bundle b = new Bundle();
        b.putString("signatureUrl", "");
        Intent intent = new Intent();
        intent.putExtras(b);
        setResult(CODES.SIGNATURE_REQUIRED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        Log.w("GetSignature", "onDestory");
        super.onDestroy();
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate = (c.get(Calendar.YEAR) * 10000) + ((c.get(Calendar.MONTH) + 1) * 100) + (c.get(Calendar.DAY_OF_MONTH));
        return (String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime = (c.get(Calendar.HOUR_OF_DAY) * 10000) + (c.get(Calendar.MINUTE) * 100) + (c.get(Calendar.SECOND));
        return (String.valueOf(currentTime));

    }

    private boolean prepareDirectory() {
        try {
            if (makedirs())
                return true;
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs() {
        File tempdir = new File(tempDir);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory()) {
            File[] files = tempdir.listFiles();
            for (File file : files)
                if (!file.delete())
                    System.out.println("Failed to delete " + file);
        }
        return (tempdir.isDirectory());
    }

    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private final Paint paint = new Paint();
        private final Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());

            uniqueId = BookingApplication.phoneNumber + "_" + getTodaysDate() + "_" + getCurrentTime();
            current = uniqueId + ".png";
            filePath = new File(tempDir, current);

            if (mBitmap == null)
                mBitmap = Bitmap.createBitmap(rl_canvas.getWidth(), rl_canvas.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(mBitmap);
            try {
                FileOutputStream mFileOutStream = new FileOutputStream(filePath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 80, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();

            } catch (Exception e) {
                Log.e("log_tag", e.toString());
            }
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            ll_book_trip.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH), (int) (dirtyRect.top - HALF_STROKE_WIDTH), (int) (dirtyRect.right + HALF_STROKE_WIDTH), (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left)
                dirtyRect.left = historicalX;
            else if (historicalX > dirtyRect.right)
                dirtyRect.right = historicalX;

            if (historicalY < dirtyRect.top)
                dirtyRect.top = historicalY;
            else if (historicalY > dirtyRect.bottom)
                dirtyRect.bottom = historicalY;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}