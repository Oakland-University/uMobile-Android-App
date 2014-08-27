package org.apereo.utils;

import android.util.Log;

import org.apereo.App;

/**
 * Created by schneis on 8/26/14.
 */
public class Logger {
    public static void i(String TAG, String log) {
        if(App.isDebugMode()) Log.i(TAG, log);
    }
    public static void d(String TAG, String log) {
        if(App.isDebugMode())Log.d(TAG,log);
    }
    public static void v(String TAG, String log) {
        if(App.isDebugMode())Log.v(TAG,log);
    }
    public static void w(String TAG, String log) {
        if(App.isDebugMode())Log.w(TAG,log);
    }
    public static void wtf(String TAG, String log) {
        if(App.isDebugMode())Log.wtf(TAG,log);
    }
    public static void e(String TAG, String log) {
        if(App.isDebugMode())Log.e(TAG,log);
    }
    public static void e(String TAG, String log,Exception e) {
        if(App.isDebugMode())Log.e(TAG,log,e);
    }
}
