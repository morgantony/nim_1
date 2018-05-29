package com.bhm.sdk.rxlibrary.rxjava;

import io.reactivex.disposables.Disposable;

/** 事件执行的回调
 * Created by bhm on 2018/5/14.
 */

public interface CallBack<T> {

    void onStart(Disposable disposable);

    void onSuccess(T response);

    void onFail(Throwable e);

    void onComplete();
}
