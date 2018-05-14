package com.bhm.sdk.rxlibrary.rxjava;

import android.app.Activity;
import android.widget.Toast;

import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * Created by bhm on 2018/5/14.
 */

public abstract class RxObserver<T> implements Observer<T> {

    private boolean isShowDialog;
    private Activity activity;
    private RxLoadingDialog dialog;
    private boolean isDefaultToast;
    private RxManager rxManager;

    public RxObserver(RxBuilder builder){
        this.isShowDialog = builder.isShowDialog();
        this.dialog = builder.getDialog();
        this.isDefaultToast = builder.isDefaultToast();
        this.rxManager = builder.getRxManager();
        this.activity = builder.getActivity();
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if(rxManager != null){
            rxManager.subscribe(d);
        }
        onStart(d);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        onFail(e);
        if(isShowDialog && null != dialog){
            dialog.dismissLoading();
        }
        if(isDefaultToast) {
            if (e instanceof HttpException) {
                if (((HttpException) e).code() == 404) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (((HttpException) e).code() == 504) {
                    Toast.makeText(activity, "请检查网络连接！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "请检查网络连接！", Toast.LENGTH_SHORT).show();
                }
            } else if (e instanceof IndexOutOfBoundsException
                    || e instanceof NullPointerException
                    || e instanceof JsonSyntaxException
                    || e instanceof IllegalStateException) {
                Toast.makeText(activity, "数据异常，解析失败！", Toast.LENGTH_SHORT).show();
            } else if (e instanceof TimeoutException) {
                Toast.makeText(activity, "连接超时，请重试！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "请求失败，请稍后再试！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNext(@NonNull T t) {
        onSuccess(t);
        if(isShowDialog && null != dialog){
            dialog.dismissLoading();
        }
    }

    @Override
    public void onComplete() {
        onDone();
        if(isShowDialog && null != dialog){
            dialog.dismissLoading();
        }
    }

    abstract public void onStart(Disposable disposable);

    abstract public void onSuccess(T response);

    abstract public <K> void onFail(Throwable t);

    abstract public void onDone();
}
