package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    ImageLoader() { /**/ }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param urlS       image url string
     * @param imageView view to set image too.
     */
    void load(final String urlS, final ImageView imageView) {
        if (TextUtils.isEmpty(urlS)) {
            throw new InvalidParameterException("URL is empty!");
        }
        final URL url;
        try {
            url = new URL(urlS);
        } catch (MalformedURLException mue) {
            Log.e(TAG, "Bad (malformed) image URL, cannot load", mue);
            return;
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

        //The cache we use here is just a map, which we extend indefinitely.
        //This works because we have a small, finite number of different images.
        //We would be better off using Picasso or Glide libraries.
        //If we must roll our own cache, we need to add an entry limit and LRU:
        //LinkedHashMap can help there if we're careful.

        //For this cache, we are doing multiple fetches of the same image on the first load,
        //because we have (e.g.) 3 rows using the same image, and this gets requested
        if (imageCache.containsKey(url)) {
            //Use the cached image
            setImageView(imageView, imageCache.get(url));
        } else if (requests.containsKey(url)) {
            //Add to the list of requests for the pending image
            requests.get(url).add(imageView);
        } else {
            //build the list of views waiting for the image at this url
            ArrayList<ImageView> views = new ArrayList<>();
            views.add(imageView);
            requests.put(url, views);
            LoadImageDataTask loadImageDataTask = new LoadImageDataTask() {

                @Override public void onPostExecute(byte[] result) {
                    if (!isCancelled()) {
                        Bitmap bitmap = convertToBitmap(result);
                        imageCache.put(url, bitmap);
                        for (ImageView view : requests.get(url)) {
                            setImageView(view, bitmap);
                        }
                        requests.remove(url);
                    } else {
                        Log.e(TAG, "Failed to load image " + url);
                    }
                }
            };
            loadImageDataTask.execute(url);
        }
    }

    private static class LoadImageDataTask extends AsyncTask<URL, Void, byte[]> {

        @Override
        protected byte[] doInBackground(URL... urls) {
            URL url = urls[0];
            try {
                return loadImageData(url);
            } catch (IOException e) {
                Log.e(TAG, "Could not load image data for " + url, e);
                return new byte[0];
            }
        }

        private static byte[] loadImageData(URL url) throws IOException {
            URLData urlData = new URLData(url);
            try {
                urlData.connect();
                return urlData.readData();
            } finally {
                urlData.disconnect();
            }
        }
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    private static Map<URL, Bitmap> imageCache = new HashMap<>();
    private static Map<URL, List<ImageView>> requests = new HashMap<>();
}
