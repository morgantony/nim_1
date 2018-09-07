package com.bhm.sdk.rxlibrary.rxjava.callback;

/**
 * Created by bhm on 2018/5/30.
 */

public abstract class RxStreamCallBackImp<T> {

    public abstract void onStart();

    public abstract void onProgress(int progress, long bytesWritten, long contentLength);

    public abstract void onFinish();

    public abstract  void onFail(String errorInfo);
}
