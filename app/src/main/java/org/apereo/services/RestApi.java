package org.apereo.services;


import android.content.Context;
import android.content.pm.PackageManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.rest.RestService;
import org.apereo.App;
import org.apereo.R;
import org.apereo.utils.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


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
        powerClient.setRootUrl(App.getInstance().getString(R.string.root_url));

        RestTemplate template = powerClient.getRestTemplate();

        SimpleClientHttpRequestFactory requestFactory;
        requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(30 * 1000); // 30 seconds
        requestFactory.setConnectTimeout(5 * 1000); // 5 seconds
        template.setRequestFactory(requestFactory);
    }

    @Background
    public void getMainFeed(Context context, UmobileRestCallback<String> callback) {
        callbackHandler.onBegin(callback);

        String layoutUrl = context.getResources().getString(R.string.layout_json_url);
        HttpURLConnection httpGet = null;
        try {
            httpGet = (HttpURLConnection) new URL(layoutUrl).openConnection();
            while (httpGet.getHeaderField("Location") != null) {
                layoutUrl = httpGet.getHeaderField("Location");
                httpGet = (HttpURLConnection) new URL(layoutUrl).openConnection();
            }
        } catch (IOException e) {
            callbackHandler.onError(callback, e, "Main feed URL construction");
        }

        try {
            powerClient.getMainFeed();
            String response = getResponse(httpGet);
            callbackHandler.onSuccess(callback, response);
        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
            callbackHandler.onError(callback, e, "getMainFeed");
        } finally {
            callbackHandler.onFinish(callback);
        }
    }

    @Background
    public void getGlobalConfig(Context context, UmobileRestCallback<String> callback) {
        callbackHandler.onBegin(callback);

        String configUrl = context.getResources().getString(R.string.global_config_url);
        String appVersion = "0";

        try {
            appVersion = App.getInstance()
                    .getPackageManager()
                    .getPackageInfo(App.getInstance().getPackageName(), 0).versionName;
            configUrl += appVersion + "/config";
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d(TAG, e.getMessage());
            callbackHandler.onError(callback, e, "getGlobalConfig");
        }

        HttpURLConnection httpGet = null;
        try {
            httpGet = (HttpURLConnection) new URL(configUrl).openConnection();
        } catch (IOException e) {
            callbackHandler.onError(callback, e, "Global config URL construction");
        }

        try {
            powerClient.getGlobalConfig(appVersion);
            String response = getResponse(httpGet);
            callbackHandler.onSuccess(callback, response);
        } catch (Exception e) {
            Logger.d(TAG, e.getMessage());
            callbackHandler.onError(callback, e, "getGlobalConfig");
        } finally {
            callbackHandler.onFinish(callback);
        }
    }

    private String getResponse(HttpURLConnection httpGet) throws Exception {
        StringBuilder builder = new StringBuilder();
        httpGet.connect();
        int statusCode = httpGet.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            InputStream content = httpGet.getInputStream();
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
