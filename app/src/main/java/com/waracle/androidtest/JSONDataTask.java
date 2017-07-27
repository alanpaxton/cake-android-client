package com.waracle.androidtest;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alan on 27/07/2017.
 */

class JSONDataTask extends AsyncTask<URL, Void, JSONDataTask.Result> {

    /**
     * Encapsulate the result of the JSON request
     */
    final static class Result {
        final Exception e;
        final JSONArray json;

        Result(@NonNull  Exception e) {
            this.e = e;
            this.json = new JSONArray();
        }

        Result(@NonNull JSONArray json) {
            this.e = null;
            this.json = json;
        }

        boolean ok() {
            return e == null;
        }

        @NonNull String getMessage() {
            if (ok()) return "";
            return e.getMessage();
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(String contentType) {
        if (contentType != null) {
            String[] params = contentType.split(",");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return "UTF-8";
    }

    @Override
    protected Result doInBackground(URL... urls) {

        URL url = urls[0];
        HttpURLConnection urlConnection = null;
        InputStream in;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???

            // Also, Do you trust any utils thrown your way????

            byte[] bytes = StreamUtils.readUnknownFully(in);

            // Read in charset of HTTP content.
            String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, charset);

            // Read string as JSON.
            return new Result(new JSONArray(jsonText));
        } catch (IOException ioe) {
            return new Result(ioe);
        } catch (JSONException je) {
            return new Result(je);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
