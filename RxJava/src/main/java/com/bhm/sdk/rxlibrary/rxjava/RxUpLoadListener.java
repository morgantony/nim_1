package com.bhm.sdk.rxlibrary.rxjava;

/**
 * Created by bhm on 2018/5/28.
 */

public interface RxUpLoadListener extends RxListener{

    void onStartUpload();

    void onProgress(long bytesWritten, long contentLength);

    void onFinishUpload();

    void onFail(String errorInfo);
}
