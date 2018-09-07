package com.bhm.sdk.rxlibrary.rxjava.callback;

import io.reactivex.disposables.Disposable;

/** 事件执行的回调
 * Created by bhm on 2018/5/14.
 */

public class CallBack<T> extends CallBackImp<T>{

    @Override
    public void onStart(Disposable disposable) {

    }

    @Override
    public void onSuccess(T response) {

    }

    @Override
    public void onFail(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
