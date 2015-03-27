package org.apereo.utils;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Resources;
import android.webkit.CookieSyncManager;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.App;
import org.apereo.R;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajclisso on 12/4/14.
 */
@EBean
public class CasClient {

    @Bean
    RestApi restApi;

    private static final String TAG = CasClient.class.getName();

    private String tgt;
    private HttpURLConnection serviceTicketLocationConnection, serviceTicketConnection; // to be closed outside their methods

    private final Resources resources = App.getInstance().getResources();
    private final String ACCOUNT_TYPE = resources.getString(R.string.account_type);

    AccountManager accountManager;

    @Background
    public void authenticate(String username, String password, Context context,
                             UmobileRestCallback<String> callback) {
        try {
            // Perform the CAS authentication dance.
            List<NameValuePair> postData = generatePostData(username, password);
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
        if (responseCode == 200) {
            clearCookies();
            removeAccount();
            App.setIsAuth(false);
            callback.onSuccess(responseCode);
        } else {
            callback.onError(null, responseCode);
        }
        callback.onFinish();
    }

    private void clearCookies() {
        try {
            App.getCookieManager().getCookieStore().removeAll();
        } catch (NullPointerException e) {  }
        android.webkit.CookieManager.getInstance().removeAllCookie();
        CookieSyncManager.getInstance().sync();
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
            return getConnection.getResponseCode();
        } catch (IOException e) {
            Logger.e(TAG, "error sending logout request", e);
            return UmobileRestCallback.UNKNOWN_ERROR_CODE;
        }
    }

    private List<NameValuePair> generatePostData(String username, String password)
            throws IOException {

        List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));

        return postData;
    }

    private String postForServiceTicketLocation(List<NameValuePair> postData)
            throws IOException {

        String postPath = resources.getString(R.string.ticket_url);
        URL postUrl = new URL(postPath);
        serviceTicketLocationConnection = (HttpURLConnection) postUrl.openConnection();
        serviceTicketLocationConnection.setDoOutput(true);
        serviceTicketLocationConnection.addRequestProperty("Content-Type", "text/html");

        OutputStream os = new BufferedOutputStream(serviceTicketLocationConnection.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

        writer.write(getQuery(postData));
        writer.flush();
        writer.close();
        os.close();
        serviceTicketLocationConnection.connect();

        // Service ticket created
        String serviceTicketLocation = serviceTicketLocationConnection.getHeaderField("Location");
        if (serviceTicketLocation != null) {
            serviceTicketLocation = serviceTicketLocation.replace("http", "https");
            tgt = serviceTicketLocation.split("tickets/")[1];
        }

        return serviceTicketLocation;
    }

    private String postForServiceTicket() throws IOException {
        String postPath = resources.getString(R.string.ticket_url) + "/" + tgt;
        URL postST = new URL(postPath);
        serviceTicketConnection = (HttpURLConnection) postST.openConnection();
        serviceTicketConnection.setDoOutput(true);
        serviceTicketConnection.addRequestProperty("Content-Type", "text/html");

        List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
        postData.add(new BasicNameValuePair("service",
                resources.getString(R.string.login_service)));
        OutputStream os2 = new BufferedOutputStream(serviceTicketConnection.getOutputStream());
        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2, "UTF-8"));
        writer2.write(getQuery(postData));
        writer2.flush();
        writer2.close();
        os2.close();
        serviceTicketConnection.connect();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(serviceTicketConnection.getInputStream()));

        // Return service ticket
        return in.readLine();
    }

    private void validateServiceTicket(String serviceTicket) throws IOException {
        URL url = new URL(resources.getString(R.string.login_service) + "?ticket=" + serviceTicket);
        HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
        getConnection.connect();
        // Necessary
        getConnection.getHeaderField("Set-Cookie");
        getConnection.disconnect();

        setCasCookie();
        setJSession();
        CookieSyncManager.getInstance().sync();
    }

    private void setJSession() {
        String uPortalDomain = resources.getString(R.string.uportal_domain);
        HttpCookie cookie = App.getCookieManager().getCookieStore().getCookies().get(0);
        android.webkit.CookieManager.getInstance().setCookie(uPortalDomain, "JSESSIONID=" + cookie.getValue() + "; Path=/; HttpOnly");
    }

    private void setCasCookie() {
        String casDomain = resources.getString(R.string.cas_domain);
        android.webkit.CookieManager.getInstance().setCookie(casDomain, "CASTGC=" + tgt);
    }

    // URL encoding helper method. (http://stackoverflow.com/a/13486223/2546659)
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (NameValuePair pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
