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

    private static boolean isAuth;

    private static String cookie;

    private static String tgt;

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

    public static boolean getIsAuth() {
        return isAuth;
    }

    public static void setIsAuth(boolean isAuth) {
        App.isAuth = isAuth;
    }

    public static String getRootUrl() {
        return getInstance().getResources().getString(R.string.root_url);
    }

    public static void setCookie(String cookie) {
        App.cookie = cookie;
    }

    public static void setTgt(String tgt) {
        App.tgt = tgt;
    }

    public static String getCookie() {
        return App.cookie;
    }

    public static String getTgt() {
        return App.tgt;
    }

    public static boolean isDebugMode() {
        return instance.debuggable();
    }

    private boolean debuggable() {
        return ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }
}
