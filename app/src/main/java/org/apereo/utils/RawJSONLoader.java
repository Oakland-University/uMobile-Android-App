package org.apereo.utils;

import android.util.Log;

import org.apereo.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by schneis on 8/27/14.
 */
public class RawJSONLoader {

     public static String loadFeed(int rawResourceId) throws IOException {

        InputStream is = App.getInstance().getResources().openRawResource(rawResourceId);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }
}
