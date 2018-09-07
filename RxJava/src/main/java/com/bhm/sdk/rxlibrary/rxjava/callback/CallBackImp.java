package com.bhm.sdk.rxlibrary.rxjava.callback;

import io.reactivex.disposables.Disposable;

/** 事件执行的回调
 * Created by bhm on 2018/5/14.
 */

public abstract class CallBackImp<T> {

    public abstract void onStart(Disposable disposable);

    public abstract void onSuccess(T response);

    public abstract void onFail(Throwable e);

    public abstract void onComplete();
}
