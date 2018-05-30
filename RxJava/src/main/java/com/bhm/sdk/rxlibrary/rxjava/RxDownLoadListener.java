package com.bhm.sdk.rxlibrary.rxjava;

/**
 * Created by bhm on 2018/5/11.
 */

public interface RxDownLoadListener extends RxListener{

    void onStartDownload();

    void onProgress(int progress);

    void onFinishDownload();

    void onFail(String errorInfo);
}
