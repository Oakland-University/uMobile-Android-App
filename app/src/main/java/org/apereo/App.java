package org.apereo;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import org.androidannotations.annotations.EApplication;

/**
 * Created by schneis on 8/26/14.
 */
@EApplication
public class App extends Application {
    private static App instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        if (instance == null) {
            throw new RuntimeException("Application instance is null");
        }
        return instance;
    }

    public static String getRootUrl() {
        return getInstance().getResources().getString(R.string.rootUrl);
    }

    public static boolean isDebugMode() {
        return instance.debuggable();
    }

    private boolean debuggable() {
        return ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }
}
