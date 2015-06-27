package itc.downloader.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    //Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
                photoToLoad.imageView.performClick();
            }
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                Bitmap bmp = getBitmap(photoToLoad.url, true);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    //private static double			ratio1024x768	= 1024.0 / 768.0;

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;

    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    ExecutorService executorService;
    Handler handler = new Handler();                                                        //handler to display images in UI thread

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    public void DisplayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.performClick();
        } else
            queuePhoto(url, imageView);
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            //bitmapOptions.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream1, null, bitmapOptions);
            stream1.close();

			/*
             * double sourceRatio = (double) bitmapOptions.outWidth / (double) bitmapOptions.outHeight;
			 * 
			 * if (sourceRatio > ratio1024x768) bitmap = Bitmap.createScaledBitmap(bitmap, 1024, (int) (1024 / sourceRatio), true); else bitmap = Bitmap.createScaledBitmap(bitmap,
			 * (int) (768 * sourceRatio), 768, true);
			 */
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmap(String url, Boolean tryCacheSearch) {

        File f = fileCache.openFile(url);

        if (tryCacheSearch) {
            //from SD cache
            Bitmap b = decodeFile(f);
            if (b != null)
                return b;
        }
        //from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if ((tag == null) || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

}
