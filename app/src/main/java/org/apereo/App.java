package org.apereo;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import org.androidannotations.annotations.EApplication;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by schneis on 8/26/14.
 */
@EApplication
public class App extends Application {
    private static App instance;
    private static CookieManager cookieManager;

    private static boolean isAuth;

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


    public static void setCookieManager(CookieManager cookieManager) {
        App.cookieManager = cookieManager;
    }

    public static String getRootUrl() {
        return getInstance().getResources().getString(R.string.root_url);
    }

    public static CookieManager getCookieManager() {
        return cookieManager;
    }

    public void resetCookies() {
        android.webkit.CookieManager.getInstance().removeAllCookie();
        App.setCookieManager(new CookieManager());
        CookieHandler.setDefault(App.getCookieManager());
    }

    public static boolean isDebugMode() {
        return instance.debuggable();
    }

    private boolean debuggable() {
        return ( 0 != ( getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE ) );
    }
}
