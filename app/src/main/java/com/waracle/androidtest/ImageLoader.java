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

/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    public ImageLoader() { /**/ }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(final String url, final ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

        LoadImageDataTask loadImageDataTask = new LoadImageDataTask() {

            @Override public void onPostExecute(byte[] result) {
                if (!isCancelled()) {
                    setImageView(imageView, convertToBitmap(result));
                } else {
                    Log.e(TAG, "Failed to load image " + url);
                }
            }
        };
        try {
            loadImageDataTask.execute(new URL[]{new URL(url)});
        } catch (MalformedURLException mue) {
            Log.e(TAG, "Bad (malformed) image URL, cannot load", mue);
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
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = null;
            try {
                try {
                    // Read data from workstation
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    // Read the error from the workstation
                    inputStream = connection.getErrorStream();
                }

                // Can you think of a way to make the entire
                // HTTP more efficient using HTTP headers??

                return StreamUtils.readUnknownFully(inputStream);
            } finally {
                // Close the input stream if it exists.
                StreamUtils.close(inputStream);

                // Disconnect the connection
                connection.disconnect();
            }
        }
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
