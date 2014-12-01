package org.apereo.services;


import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.apereo.App;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author macklinu
 * @author berberk
 */
@EBean
public class RestApi {
    private static final String TAG = "RestApi";

    @RestService
    RestInterface powerClient;
    @Bean
    UmobileHeaderInterceptor headerInterceptor;
    @Bean
    RestCallbackHandler callbackHandler;

    private String cookie = "";

    @AfterInject
    void initialize() {
        String rootUrl = "https://mysail.oakland.edu";
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
    public void getMainFeed(UmobileRestCallback<String> callback) {
        callbackHandler.onBegin(callback);
        HttpURLConnection getConnection = null;
        try {
            powerClient.getMainFeed();
            getConnection = (HttpURLConnection) new URL("https://mysail.oakland.edu/uPortal/layout.json").openConnection();
            getConnection.setRequestProperty("Cookie", App.getCookie());
            String response = getResponse(getConnection);
            callbackHandler.onSuccess(callback, response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    private String getResponse(HttpURLConnection connection) throws Exception {
        StringBuilder builder = new StringBuilder();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
