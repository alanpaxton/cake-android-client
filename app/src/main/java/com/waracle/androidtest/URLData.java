package com.waracle.androidtest;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
            if (contentLength >= 0) {
                return readKnown(is, contentLength);
            } else {
                return readUnknownFully(is);
            }
        } finally {
            close(is);
        }
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    static String parseCharset(String contentType) {
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

    static byte[] readKnown(InputStream stream, int size) throws IOException {

        byte[] bytes = new byte[size];
        int count = 0;
        while (count < size) {
            int extra = stream.read(bytes, count, size-count);
            if (extra < 0) break;
            count += extra;
        }
        return bytes;
    }

    // Can you see what's wrong with this???

    //Possible to get a read() IOException after close, and then not return the stream to that point ?
    //Potentially ++ memory use when there's a big stream to read (Byte objects and byte[])
    //Will fix by using the Content-Length header to read a pre-allocated byte[]

    static byte[] readUnknownFully(InputStream stream) throws IOException {
        // Read in stream of bytes
        ArrayList<Byte> data = new ArrayList<>();
        while (true) {
            int result = stream.read();
            if (result == -1) {
                break;
            }
            data.add((byte) result);
        }

        // Convert ArrayList<Byte> to byte[]
        byte[] bytes = new byte[data.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get(i);
        }

        // Return the raw byte array.
        return bytes;
    }

    /**
     * Close helper
     *
     * @param closeable
     */
    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
