package org.apereo.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
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
@EActivity(R.layout.login_page)
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();

    @ViewById(R.id.web_view)
    WebView webView;

    @ViewById(R.id.login_username)
    EditText userNameView;
    @ViewById(R.id.login_password)
    EditText passwordView;
    @ViewById(R.id.login_button)
    Button submitButton;

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
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userNameView.getText().toString().equals("") ||
                        !passwordView.getText().toString().equals("")) {
                    String username = userNameView.getText().toString();
                    String password = passwordView.getText().toString();
                    openBackgroundWebView(username, password);
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Please enter a username and a password",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void openBackgroundWebView(final String username, final String password) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            private boolean initialLoginRequest = true;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.equalsIgnoreCase(getString(R.string.home_page))) {
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
                } else if (initialLoginRequest) {
                    initialLoginRequest = false;
                    view.loadUrl("javascript:$('#username').val('" + username + "');");
                    view.loadUrl("javascript:$('#password').val('" + password + "');");
                    view.loadUrl("javascript:$('.btn-submit').click();");
                } else {
                    if (url.equalsIgnoreCase(getString(R.string.login_url))) {
                        Logger.d(TAG, "login failure");
                    }
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

    protected class MyWebClient extends WebChromeClient {
    }

}
