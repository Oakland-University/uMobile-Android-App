package org.umobile.services;

public interface RestCallback<T> {

    public interface OnBegin<T> extends RestCallback<T> {
        public void onBegin();
    }

    public interface OnError<T> extends RestCallback<T> {
        public void onError(Exception e, String responseBody);
    }

    public interface OnFinish<T> extends RestCallback<T> {
        public void onFinish();
    }

    public interface OnSuccess<T> extends RestCallback<T> {
        public void onSuccess(T response);
    }

}
