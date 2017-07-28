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

    @Override
    protected Result doInBackground(URL... urls) {

        URL url = urls[0];
        URLData urlData = new URLData(url);
        try {
            urlData.connect();

            // Can you think of a way to improve the performance of loading data
            // using HTTP headers???
            //{@link URLData} has the answers

            // Also, Do you trust any utils thrown your way????

            // Interesting philosophical point. Depends.
            // You need a degree of trust, I would trust Android libs for instance.
            // After that, there's reputation and due diligence, like looking for a solid test suite or other proofs.
            // Popularity of a library speaks for something.
            // It's good to be humble and accept that other people write code as good as you occasionally,
            // and you can't do everything yourself.

            //Now I'll go off and read that code...

            byte[] bytes = urlData.readData();

            // Read in charset of HTTP content.
            // Convert byte array to appropriate encoded string.
            String jsonText = new String(bytes, urlData.getCharset());

            // Read string as JSON.
            return new Result(new JSONArray(jsonText));
        } catch (IOException ioe) {
            return new Result(ioe);
        } catch (JSONException je) {
            return new Result(je);
        } finally {
            urlData.disconnect();
        }
    }
}
