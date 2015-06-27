package itc.booking.mars;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import itc.booking.mars.BookingApplication.APIs;

public class JSONhandler {

    private static InputStream is = null;
    private static JSONObject JSONResp = null;
    private static double ratio640x480 = 640.0 / 480.0;

    private static String result = "";

    public static File getImage(String imagePath, Boolean performCompression) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        if (bitmap == null)
            throw new FileNotFoundException(imagePath.substring(imagePath.lastIndexOf('/') + 1) + "\nNot Found");
        else {
            double sourceRatio = (double) bitmap.getWidth() / (double) bitmap.getHeight();

            if (sourceRatio > ratio640x480)
                bitmap = Bitmap.createScaledBitmap(bitmap, 640, (int) (640 / sourceRatio), true);
            else
                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (480 * sourceRatio), 480, true);
        }

        FileOutputStream outStream = new FileOutputStream(imagePath);
        if (performCompression)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);

        outStream.flush();
        outStream.close();

        File bin = new File(imagePath);
        return bin;
    }

    public static JSONObject getJSONfromURL(MultipartEntity... params) throws ClientProtocolException, IOException, JSONException {
        MultipartEntity outEntity = params[0];
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParameters, 30000);
        HttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpPost httpPost = new HttpPost(APIs.GetURIFor(BookingApplication.apiCalled));

        outEntity.addPart("AppID", new StringBody(BookingApplication.appID));
        outEntity.addPart("UserID", new StringBody(BookingApplication.userInfoPrefs.getString("UserID", "")));
        outEntity.addPart("language", new StringBody(BookingApplication.userInfoPrefs.getString("lang", "en")));

        httpPost.setEntity(outEntity);

        HttpResponse response = httpClient.execute(httpPost);

        HttpEntity entity = response.getEntity();
        if (entity != null) {

//            is = entity.getContent();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null)
//                sb.append(line + "\n");
//            is.close();
//            result = sb.toString();

            result = EntityUtils.toString(response.getEntity(), "UTF-8");

            Log.d("JSONResponse", result);

            try {
                JSONResp = new JSONObject(result);
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                    JSONResp.put("Fault", true);
                    if (!JSONResp.has("ReasonPhrase"))
                        JSONResp.put("ReasonPhrase", response.getStatusLine().getReasonPhrase());
                }
            } catch (JSONException e) {
                JSONResp = new JSONObject();
                JSONResp.put("Fault", true);
                JSONResp.put("ReasonPhrase", e.getMessage());
            }
        }
        return JSONResp;
    }
}
