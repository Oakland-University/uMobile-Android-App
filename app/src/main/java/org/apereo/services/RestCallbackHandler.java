package org.apereo.services;

import org.androidannotations.annotations.EBean;

@EBean
public class RestCallbackHandler {

    public <T> void onBegin(UmobileRestCallback<T> callback) {
        if (callback != null) {
            callback.onBegin();
        }
    }

    public <T> void onSuccess(UmobileRestCallback<T> callback, T response) {
        if (callback != null) {
            callback.onSuccess(response);
        }
    }

    public <T> void onError(UmobileRestCallback<T> callback, Exception e, String msg) {
        if (callback != null) {
            String responseBody = null;
            callback.onError(e, responseBody);
        }
    }

    public <T> void onFinish(UmobileRestCallback<T> callback) {
        if (callback != null) {
            callback.onFinish();
        }
    }
}
