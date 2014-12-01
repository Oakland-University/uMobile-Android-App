package org.apereo.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.App;
import org.apereo.R;
import org.apereo.deserializers.LayoutDeserializer;
import org.apereo.models.Layout;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;
import org.apereo.utils.LayoutManager;
import org.apereo.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by schneis on 8/28/14.
 */
@EActivity(R.layout.login_page)
public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getName();

    private final String ACCOUNT_TYPE = App.getInstance().getResources().getString(R.string.account_type);

    @ViewById(R.id.login_container)
    RelativeLayout container;

    @ViewById(R.id.web_view)
    WebView webView;

    @ViewById(R.id.login_username)
    EditText userNameView;
    @ViewById(R.id.login_password)
    EditText passwordView;
    @ViewById(R.id.rememberMe)
    CheckBox rememberMe;
    @ViewById(R.id.forgot_password)
    TextView forgotPassword;

    @Extra
    String username;
    @Extra
    String password;
    @Extra
    String url;

    @Bean
    RestApi restApi;

    @Bean
    LayoutManager layoutManager;

    AccountManager accountManager =
            (AccountManager) App.getInstance().getSystemService(ACCOUNT_SERVICE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @AfterViews
    void initialize() {
        if (url.equalsIgnoreCase(getString(R.string.logout_url))) {
            container.setVisibility(View.GONE);
            openBackgroundLogoutWebView();
            return;
        }

        passwordView.setTypeface(Typeface.DEFAULT);
        passwordView.setTransformationMethod(new PasswordTransformationMethod());

        passwordView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    loginClick();
                    return true;
                }
                return false;
            }
        });

        checkAccount(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAccount(boolean initialCheck) {
        if (username != null) {
            Account newAccount = new Account(username, ACCOUNT_TYPE);

            accountManager.addAccountExplicitly(newAccount, password, null);

            if (initialCheck) {
                container.setVisibility(View.GONE);
                openBackgroundLoginWebView();
            }
        }
    }


    @Click(R.id.login_button)
    protected void loginClick() {
        if (!userNameView.getText().toString().isEmpty() &&
                !passwordView.getText().toString().isEmpty()) {
            username = userNameView.getText().toString();
            password = passwordView.getText().toString();

            openBackgroundLoginWebView();
        } else {
            showShortToast(getResources().getString(R.string.form_error));
        }
    }


    @Click(R.id.forgot_password)
    protected void forgotPasswordClick() {
        PortletWebViewActivity_
                .intent(LoginActivity.this)
                .url(App.getInstance().getResources().getString(R.string.forgot_password_url))
                .start();
    }

    protected void openBackgroundLoginWebView() {
        getActionBar().setDisplayHomeAsUpEnabled(false);
        //showSpinner();
        doget();
    }

    @Background
    public void doget() {
        showSpinner();

        URL url;
        BufferedReader reader;
        String lt = null;
        String execution = null;
        String cookie = null;
        String portletHeader = null;

        HttpURLConnection getConnection = null;
        HttpURLConnection postConnection = null;
        // region POST
        String postPath = "https://cas.oakland.edu/cas/v1/tickets";
        URL postUrl;

        try {

            // Auth success and TGT Created
            url = new URL("https://cas.oakland.edu/cas/login?service=https://mysail.oakland.edu/uPortal/Login");
            getConnection = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(getConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<input type=\"hidden\" name=\"lt\" value=")) {
                    lt = line.substring(41, line.lastIndexOf("\""));
                }
                if (line.contains("<input type=\"hidden\" name=\"execution\" value=\"")) {
                    execution = line.substring(48, line.lastIndexOf("\""));
                }
            }
            cookie = getConnection.getHeaderField("Set-Cookie");

            Logger.d(TAG, "POSTING TO: " + postPath);
            postUrl = new URL(postPath);
            postConnection = (HttpURLConnection) postUrl.openConnection();
            postConnection.setInstanceFollowRedirects(true);
            postConnection.setRequestProperty("Cookie", cookie);
            HttpURLConnection.setFollowRedirects(true);
            List<NameValuePair> postData = new ArrayList<NameValuePair>(6);
            postData.add(new BasicNameValuePair("username", username));
            postData.add(new BasicNameValuePair("password", password));
            postData.add(new BasicNameValuePair("lt", lt));
            postData.add(new BasicNameValuePair("execution", execution));
            postData.add(new BasicNameValuePair("_eventId", "submit"));
            postData.add(new BasicNameValuePair("submit", "Sign In"));
            postConnection.setDoOutput(true);
            postConnection.setChunkedStreamingMode(0);
            OutputStream os = new BufferedOutputStream(postConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(postData));
            writer.flush();
            writer.close();
            os.close();
            postConnection.connect();
            Logger.d(TAG, "" + postConnection.getHeaderFields());
            Logger.d(TAG, "End sending POST");

            // Service Ticket Created
            String requestST = postConnection.getHeaderField("Location");
            String tgt = requestST.split("tickets/")[1];
            requestST = requestST.replace("http", "https");
            Logger.d(TAG, "POSTING TO: " + requestST);
            Logger.d(TAG, requestST);
            URL postST = new URL(requestST);
            HttpURLConnection postConnection2 = (HttpURLConnection) postST.openConnection();
            postConnection2.setInstanceFollowRedirects(true);
            postConnection2.setRequestProperty("Cookie", cookie);
            List<NameValuePair> postData2 = new ArrayList<NameValuePair>(6);
            postData2.add(new BasicNameValuePair("service", "https://mysail.oakland.edu/uPortal/Login"));
            postConnection2.setDoOutput(true);
            postConnection2.setChunkedStreamingMode(0);
            OutputStream os2 = new BufferedOutputStream(postConnection2.getOutputStream());
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2, "UTF-8"));
            writer2.write(getQuery(postData2));
            writer2.flush();
            writer2.close();
            os2.close();
            postConnection2.connect();
            Logger.d(TAG, "" + postConnection2.getHeaderFields());
            BufferedReader in = new BufferedReader(new InputStreamReader(postConnection2.getInputStream()));
            String serviceTicket;
            serviceTicket = in.readLine();
            Logger.d(TAG, "ST = " + serviceTicket);
            Logger.d(TAG, "End sending POST");

            // Proxy Granting Ticket and Service ticket validated
            url = new URL("https://mysail.oakland.edu/uPortal/Login?ticket="+serviceTicket);
            Logger.d(TAG, "GET TO: " + url.toString());
            getConnection = (HttpURLConnection) url.openConnection();
            getConnection.setRequestProperty("Cookie", cookie);
            getConnection.connect();
            Logger.d(TAG, "" + getConnection.getHeaderFields());
            Logger.d(TAG, "End sending GET");

            App.setCookie(cookie);
            App.setTgt(tgt);

            getFeed();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (postConnection != null)
                postConnection.disconnect();
        }
        // endregion
    }

    // http://stackoverflow.com/a/13486223/2546659
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    protected void openBackgroundLogoutWebView() {
        showSpinner();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            private boolean receivedError = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (receivedError) {
                    showLongToast(getString(R.string.error_network_connection));
                    super.onPageFinished(view, url);
                    finish();

                    dismissSpinner();
                    return;
                }

                // logged out successfully
                restApi.setCookie("");
                removeAccount();
                App.setIsAuth(false);
                getFeed();
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                receivedError = true;
            }
        });
        webView.loadUrl(url);
    }

    private void removeAccount() {
        if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
            accountManager.removeAccount(
                    accountManager.getAccountsByType(ACCOUNT_TYPE)[0], null, null);
        }
    }

    private void getFeed() {
        restApi.getMainFeed(new UmobileRestCallback<String>() {

            @Override
            public void onBegin() {
                super.onBegin();
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
                layoutManager.setLayout(layout);

                if (rememberMe.isChecked()) {
                    checkAccount(false);
                }

                App.setIsAuth(true);
                dismissSpinner();

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
