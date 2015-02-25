package org.apereo.services;


import android.content.Context;
import android.content.pm.PackageManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.rest.RestService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apereo.App;
import org.apereo.R;
import org.apereo.activities.LaunchActivity_;
import org.apereo.utils.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;


/**
 * @author macklinu
 * @author berberk
 */
@EBean(scope = Scope.Singleton)
public class RestApi {
    private static final String TAG = "RestApi";

    @RestService
    RestInterface powerClient;
    @Bean
    UmobileHeaderInterceptor headerInterceptor;
    @Bean
    RestCallbackHandler callbackHandler;

    @AfterInject
    void initialize() {
        String rootUrl = "https://mysaildev.oakland.edu";
        powerClient.setRootUrl(rootUrl);

        RestTemplate template = powerClient.getRestTemplate();

        SimpleClientHttpRequestFactory requestFactory;
        requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(30 * 1000); // 30 seconds
        requestFactory.setConnectTimeout(5 * 1000); // 5 seconds
        template.setRequestFactory(requestFactory);
    }

    public void setRootUrl(String rootUrl) {
        powerClient.setRootUrl(rootUrl);
    }

    public RestInterface getRestInterface() {
        return powerClient;
    }

    @Background
    public void getMainFeed(Context context, UmobileRestCallback<String> callback) {
        callbackHandler.onBegin(callback);

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(context.getResources().getString(R.string.layout_json_url));

        if (!App.getCookieManager().getCookieStore().getCookies().isEmpty()) {
            App.setIsAuth(true);
            HttpCookie cookie = App.getCookieManager().getCookieStore().getCookies().get(0);
            httpGet.setHeader("Cookie", "JSESSIONID=" + cookie.getValue() + "; Path=/; HttpOnly");
        }

        try {
            powerClient.getMainFeed();
            String response = getResponse(client, httpGet);
            callbackHandler.onSuccess(callback, response);
        } catch (ResourceAccessException rae) {
            LaunchActivity_.intent(App.getInstance());
        } catch (Exception e) {
            callbackHandler.onError(callback, e, "getMainFeed");
        } finally {
            callbackHandler.onFinish(callback);
        }
    }

    @Background
    public void getGlobalConfig(Context context, UmobileRestCallback<String> callback) {
        callbackHandler.onBegin(callback);

        HttpClient client = new DefaultHttpClient();
        String configUrl = context.getResources().getString(R.string.global_config_url);

        // build global config url
        String appVersion = "0";
        try {
            appVersion = App.getInstance()
                    .getPackageManager()
                    .getPackageInfo(App.getInstance().getPackageName(), 0).versionName;
            configUrl += appVersion + "/config";
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d(TAG, e.getMessage());
        }

        HttpGet httpGet = new HttpGet(configUrl);

        try {
            powerClient.getGlobalConfig(appVersion);
            String response = getResponse(client, httpGet);
            callbackHandler.onSuccess(callback, response);
        } catch (Exception e) {
            callbackHandler.onError(callback, e, "getGlobalConfig");
        } finally {
            callbackHandler.onFinish(callback);
        }
    }

    private String getResponse(HttpClient client, HttpGet httpGet) throws Exception {
        StringBuilder builder = new StringBuilder();
        HttpResponse response = client.execute(httpGet);
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } else {
            throw new Exception("Not a 200 status code");
        }

        return builder.toString();
    }
}
