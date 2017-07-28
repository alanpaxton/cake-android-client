package com.waracle.androidtest;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 28/07/2017.
 */

public class URLData {

    private static final String TAG = URLData.class.getSimpleName();

    private final URL url;
    private HttpURLConnection urlConnection;

    private int contentLength = -1;

    public URLData(URL url) {
        this.url = url;
    }

    public void connect() throws IOException {
        urlConnection = (HttpURLConnection) url.openConnection();
        Map<String, List<String>> headers = urlConnection.getHeaderFields();

        List<String> contentLengthS = headers.get("Content-Length");
        if (contentLengthS != null && contentLengthS.size() > 0) {
            try {
                contentLength = Integer.parseInt(contentLengthS.get(0));
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Could not parse Content-Length: " + contentLengthS);
            }
        }
    }

    public void disconnect() {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    public String getCharset() {
        return parseCharset(urlConnection.getRequestProperty("Content-Type"));
    }

    /**
     * Improve this by using the contentLength if we have it, to avoid copies, pessimistic buffer allocation.
     *
     * @return
     * @throws IOException
     */
    public byte[] readData() throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(urlConnection.getInputStream());
            return StreamUtils.readUnknownFully(is);
        } finally {
            StreamUtils.close(is);
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
}
