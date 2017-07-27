package com.waracle.androidtest;

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Riad on 20/05/2015.
 */
public class StreamUtils {
    private static final String TAG = StreamUtils.class.getSimpleName();

    // Can you see what's wrong with this???

    //Possible to get a read() IOException after close, and then not return the stream to that point ?
    //Potentially ++ memory use when there's a big stream to read (Byte objects and byte[])
    //Will fix by using the Content-Length header to read a pre-allocated byte[]
    
    public static byte[] readUnknownFully(InputStream stream) throws IOException {
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

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
