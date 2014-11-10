package org.apereo.services;


import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apereo.utils.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


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
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("https://mysail.oakland.edu/uPortal/layout.json");
        httpGet.setHeader("Cookie", cookie);
        try {
            powerClient.getMainFeed();
            String response = getResponse(client, httpGet);
            callbackHandler.onSuccess(callback, response);
        } catch (Exception e) {
            callbackHandler.onError(callback, e, "getMainFeed");
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

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
