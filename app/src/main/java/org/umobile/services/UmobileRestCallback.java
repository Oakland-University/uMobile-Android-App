package org.umobile.services;

public abstract class UmobileRestCallback<T> implements RestCallback.OnBegin<T>, RestCallback.OnSuccess<T>, RestCallback.OnError<T>, RestCallback.OnFinish<T> {

    @Override
    public abstract void onError(Exception e, String responseBody);

    @Override
    public abstract void onSuccess(T response);

    @Override
    public void onBegin() {
    }

    @Override
    public void onFinish() {
    }

}