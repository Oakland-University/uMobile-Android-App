package org.apereo.utils;

import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apereo.App;
import org.apereo.R;
import org.apereo.constants.AppConstants;
import org.apereo.services.RestApi;
import org.apereo.services.UmobileRestCallback;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by ajclisso on 12/4/14.
 */
@EBean
public class CasClient {

    private static final String TAG = CasClient.class.getName();
    private final Resources resources = App.getInstance().getResources();
    private final String ACCOUNT_TYPE = resources.getString(R.string.account_type);

    private String tgt;
    private HttpURLConnection serviceTicketLocationConnection, serviceTicketConnection; // to be closed outside their methods

    @Bean
    RestApi restApi;

    AccountManager accountManager;

    @Background
    public void authenticate(String username, String password, Context context,
                             UmobileRestCallback<String> callback) {
        try {
            // Perform the CAS authentication dance.
            ContentValues postData = generatePostData(username, password);
            String stLocation = postForServiceTicketLocation(postData);
            if (stLocation != null) {
                String st = postForServiceTicket();
                validateServiceTicket(st);
                callback.onSuccess(null);
            } else {
                callback.onError(null, resources.getString(R.string.error));
            }
        } catch (Exception e) {
            callback.onError(e, resources.getString(R.string.error));
        } finally {
            if (serviceTicketLocationConnection != null) { serviceTicketLocationConnection.disconnect(); }
            if (serviceTicketConnection != null) { serviceTicketConnection.disconnect(); }
            callback.onFinish();
        }
    }

    @Background
    public void logOut(UmobileRestCallback<Integer> callback) {
        Integer responseCode = sendLogOutRequest();
        if (responseCode == 200 || responseCode == 302) {
            App.getInstance().resetCookies();
            removeAccount();
            App.setIsAuth(false);
            callback.onSuccess(responseCode);
        } else {
            callback.onError(null, responseCode);
        }
        callback.onFinish();
    }

    private void removeAccount() {
        // Lazily instantiate the account manager if necessary.
        if (accountManager == null) {
            accountManager =
                    (AccountManager) App.getInstance().getSystemService(Context.ACCOUNT_SERVICE);
        }
        // Remove the account.
        if (accountManager.getAccountsByType(ACCOUNT_TYPE).length != 0) {
            accountManager.removeAccount(
                    accountManager.getAccountsByType(ACCOUNT_TYPE)[0], null, null);
        }
    }

    // Attempts to log out and returns the HTTP response code encountered; 200 if OK.
    private int sendLogOutRequest() {
        try {
            URL url = new URL(App.getInstance().getResources().getString(R.string.logout_url));
            HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
            getConnection.connect();

            App.getInstance().resetCookies();

            return getConnection.getResponseCode();
        } catch (IOException e) {
            Logger.e(TAG, "error sending logout request", e);
            return UmobileRestCallback.UNKNOWN_ERROR_CODE;
        }
    }

    private ContentValues generatePostData(String username, String password)
            throws IOException {
        ContentValues postData = new ContentValues();
        postData.put("username", username);
        postData.put("password", password);
        return postData;
    }

    private String postForServiceTicketLocation(ContentValues postData) throws IOException {

        String postPath = resources.getString(R.string.ticket_url);
        URL postUrl = new URL(postPath);

        serviceTicketLocationConnection = (HttpURLConnection) postUrl.openConnection();
        serviceTicketLocationConnection = configureHttpURLConnection(serviceTicketLocationConnection);
        serviceTicketLocationConnection = configurePost(serviceTicketLocationConnection, postData);

        // Service ticket created
        String serviceTicketLocation = serviceTicketLocationConnection.getHeaderField("Location");
        if (serviceTicketLocation != null) {
            tgt = serviceTicketLocation.split("tickets/")[1];
        }

        return serviceTicketLocation;
    }

    private HttpURLConnection configurePost(HttpURLConnection connection, ContentValues postData)
            throws IOException {
        OutputStream os = new BufferedOutputStream(connection.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        writer.write(getQuery(postData));
        writer.flush();
        writer.close();
        os.close();
        connection.connect();

        return connection;
    }

    private HttpURLConnection configureHttpURLConnection(HttpURLConnection connection) {
        connection.setDoOutput(true);
        connection.addRequestProperty("Content-Type", "text/html");
        return connection;
    }

    private String postForServiceTicket() throws IOException {
        String postPath = resources.getString(R.string.ticket_url) + "/" + tgt;
        URL postST = new URL(postPath);
        serviceTicketConnection = (HttpURLConnection) postST.openConnection();
        serviceTicketConnection = configureHttpURLConnection(serviceTicketConnection);

        ContentValues postData = new ContentValues(1);
        postData.put("service", resources.getString(R.string.login_service));
        serviceTicketConnection = configurePost(serviceTicketConnection, postData);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(serviceTicketConnection.getInputStream()));

        // Return service ticket
        return in.readLine();
    }

    private void validateServiceTicket(String serviceTicket) throws IOException {
        URL url = new URL(resources.getString(R.string.login_service) + "?ticket=" + serviceTicket);
        HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();

        // Necessary
        getConnection.getHeaderField("Set-Cookie");

        getConnection.connect();
        getConnection.disconnect();

        setJSession();
        setCasCookie();
    }

    private void setJSession() {
        String uPortalDomain = resources.getString(R.string.uportal_domain);
        HttpCookie cookie = App.getCookieManager().getCookieStore().getCookies().get(0);
        android.webkit.CookieManager.getInstance().setCookie(uPortalDomain,
                AppConstants.JSESSIONID + "=" + cookie.getValue() + "; Path=/; HttpOnly");
    }

    private void setCasCookie() {
        String casDomain = resources.getString(R.string.cas_domain);
        android.webkit.CookieManager.getInstance().setCookie(casDomain, "CASTGC=" + tgt);
    }

    // URL encoding helper method. (http://stackoverflow.com/a/13486223/2546659)
    private String getQuery(ContentValues params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String, Object> pair : params.valueSet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode((String) pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
