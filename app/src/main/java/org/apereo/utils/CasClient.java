package org.apereo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

    private String cookie, tgt;
    private HttpURLConnection postConnection, postConnection2; // to be closed outside their methods

    private Resources resources;

    @Background
    public void authenticate(String username, String password, Context context,
                             UmobileRestCallback<String> callback) {

        resources = context.getResources();

        try {
            // Perform the CAS authentication dance.
            List<NameValuePair> postData = getAndParsePostData(username, password);
            String stLocation = sendPostForServiceTicketLocation(postData);
            if (stLocation != null) {
                String st = sendPostForServiceTicket(stLocation);
                validateServiceTicket(st);
                syncCookies();

                callback.onSuccess(null);
            } else {
                callback.onError(null, "Username or password is incorrect.");
            }
        } catch (MalformedURLException e) {
            callback.onError(e, null);
        } catch (IOException e) {
            callback.onError(e, null);
        } finally {
            if (postConnection != null) { postConnection.disconnect(); }
            if (postConnection2 != null) { postConnection2.disconnect(); }
        }
    }

    private List<NameValuePair> getAndParsePostData(String username, String password)
            throws IOException {
        String lt = null;
        String execution = null;

        URL url = new URL(resources.getString(R.string.login_url));
        HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(getConnection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("<input type=\"hidden\" name=\"lt\" value=")) {
                lt = line.substring(41, line.lastIndexOf("\""));
            }
            if (line.contains("<input type=\"hidden\" name=\"execution\" value=\"")) {
                execution = line.substring(48, line.lastIndexOf("\""));
            }
        }
        List<NameValuePair> postData = new ArrayList<NameValuePair>(6);
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("lt", lt));
        postData.add(new BasicNameValuePair("execution", execution));
        postData.add(new BasicNameValuePair("_eventId", "submit"));
        postData.add(new BasicNameValuePair("submit", "Sign In"));

        cookie = getConnection.getHeaderField("Set-Cookie");
        restApi.setCookie(cookie);

        return postData;
    }

    private String sendPostForServiceTicketLocation(List<NameValuePair> postData)
            throws IOException {

        String postPath = resources.getString(R.string.ticket_url);
        URL postUrl = new URL(postPath);
        postConnection = (HttpURLConnection) postUrl.openConnection();
        postConnection.setInstanceFollowRedirects(true);
        postConnection.setRequestProperty("Cookie", cookie);
        HttpURLConnection.setFollowRedirects(true);
        postConnection.setDoOutput(true);
        postConnection.setChunkedStreamingMode(0);
        OutputStream os = new BufferedOutputStream(postConnection.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(postData));
        writer.flush();
        writer.close();
        os.close();
        postConnection.connect();

        // Service ticket created.
        String serviceTicketLocation = postConnection.getHeaderField("Location");

        if (serviceTicketLocation != null) {
            serviceTicketLocation = serviceTicketLocation.replace("http", "https");
            tgt = serviceTicketLocation.split("tickets/")[1];
        }

        return serviceTicketLocation;
    }

    private String sendPostForServiceTicket(String location) throws IOException {
        URL postST = new URL(location);
        postConnection2 = (HttpURLConnection) postST.openConnection();
        postConnection2.setInstanceFollowRedirects(true);
        postConnection2.setRequestProperty("Cookie", cookie);
        List<NameValuePair> postData = new ArrayList<NameValuePair>(6);
        postData.add(new BasicNameValuePair("service",
                resources.getString(R.string.login_service)));
        postConnection2.setDoOutput(true);
        postConnection2.setChunkedStreamingMode(0);
        OutputStream os2 = new BufferedOutputStream(postConnection2.getOutputStream());
        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2, "UTF-8"));
        writer2.write(getQuery(postData));
        writer2.flush();
        writer2.close();
        os2.close();
        postConnection2.connect();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(postConnection2.getInputStream()));
        String serviceTicket;
        serviceTicket = in.readLine();

        return serviceTicket;
    }

    private void validateServiceTicket(String serviceTicket) throws IOException {
        URL url = new URL(resources.getString(R.string.login_service) + "?ticket=" + serviceTicket);
        HttpURLConnection getConnection = (HttpURLConnection) url.openConnection();
        getConnection.setRequestProperty("Cookie", cookie);
        getConnection.connect();
        getConnection.getContent();
    }

    private void syncCookies() {
        CookieManager.getInstance().removeSessionCookie();

        String uportalDomain = resources.getString(R.string.uportal_domain);
        String casDomain = resources.getString(R.string.cas_domain);
        CookieManager.getInstance().setCookie(uportalDomain, cookie);
        CookieManager.getInstance().setCookie(casDomain, "CASTGC=" + tgt);

        CookieSyncManager.getInstance().sync();
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
