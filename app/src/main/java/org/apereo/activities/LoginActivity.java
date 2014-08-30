package org.apereo.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.Rest;
import org.apache.commons.lang.StringUtils;
import org.apereo.App;
import org.apereo.R;
import org.apereo.constants.AppConstants;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Layout;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.LayoutManager;
import org.apereo.utils.Logger;

/**
 * Created by schneis on 8/28/14.
 */
@EActivity(R.layout.portlet_webview)
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();

    @ViewById(R.id.webview)
    WebView webView;

    @Extra
    String url;

    @Bean
    RestApi restApi;

    @Bean
    LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void initiailize() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.equalsIgnoreCase(getString(R.string.home_page))) {
                    App.setIsAuth(true);
                    getLoggedInFeed();
                }
                Logger.d(TAG, "starting " + url);
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (StringUtils.equalsIgnoreCase(url, getString(R.string.logout_url))) {
                    App.setIsAuth(false);
                    restApi.setCookie("");
                    getFeed();
                }
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(url);
    }

    private void getLoggedInFeed() {
        String cookie = CookieManager.getInstance().getCookie(getString(R.string.base_url));

        if (cookie != null) {
            String[] temp = cookie.split(" ");
            for (String key : temp) {
                if (key.contains(AppConstants.JSESSIONID)) {
                    restApi.setCookie(key);
                    break;
                }
            }
        }
        getFeed();
    }

    private void getFeed() {
        restApi.getMainFeed(new UmobileRestCallback<String>() {

            @Override
            public void onBegin() {
                super.onBegin();
                showSpinner();
            }

            @Override
            public void onError(Exception e, String responseBody) {
                Logger.e(TAG, responseBody, e);

            }

            @Override
            public void onSuccess(String response) {
                Gson g = new GsonBuilder()
                        .registerTypeAdapter(Layout.class, new LayoutDeserializer())
                        .create();

                Layout layout = g.fromJson(response, Layout.class);
                Logger.d(TAG, response);
                layoutManager.setLayout(layout);
                HomePage_
                        .intent(LoginActivity.this)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .start();
                finish();

            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismissSpinner();
            }
        });
    }

}
