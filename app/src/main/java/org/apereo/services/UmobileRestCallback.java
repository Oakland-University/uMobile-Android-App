package org.apereo.services;

public abstract class UmobileRestCallback<T> implements RestCallback.OnBegin<T>, RestCallback.OnSuccess<T>, RestCallback.OnError<T>, RestCallback.OnFinish<T> {

    public static final int UNKNOWN_ERROR_CODE = 418;

    @Override
    public abstract void onError(Exception e, T response);

    @Override
    public abstract void onSuccess(T response);

    @Override
    public void onBegin() {
    }

    @Override
    public void onFinish() {
    }

}