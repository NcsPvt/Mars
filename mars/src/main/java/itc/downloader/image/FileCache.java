package itc.downloader.image;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import itc.booking.mars.BookingApplication;

public class FileCache {

    private File cacheDir;
    private File mfile;

    public FileCache(Context context) {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), BookingApplication.appID);
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File openFile(String url) {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String fileName = String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        mfile = new File(cacheDir, fileName);
        return mfile;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

    public String readOpenedFile() {
        try {
            FileInputStream fIn = new FileInputStream(mfile);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);
            StringBuilder fileContent = new StringBuilder();
            String tempString;
            while (true) {
                tempString = buffreader.readLine();
                if (tempString == null)
                    break;
                fileContent.append(tempString);
            }
            buffreader.close();
            isr.close();
            fIn.close();

            return fileContent.toString();

        } catch (Exception e) {
            return "";
        }
    }

    public void writeOpenedFile(String data) {
        try {
            FileOutputStream fOut = new FileOutputStream(mfile);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(data);
            osw.flush();
            osw.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}