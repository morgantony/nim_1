package com.bhm.sdk.rxlibrary.rxjava;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import retrofit2.HttpException;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxBuilder {

    private RxBaseActivity activity;
    private boolean isShowDialog;
    private boolean cancelable;
    private boolean isCanceledOnTouchOutside;
    private RxLoadingDialog dialog;
    private boolean isDefaultToast;
    private RxManager rxManager;
    private int readTimeOut;
    private int connectTimeOut;
    private OkHttpClient okHttpClient;
    private boolean isLogOutPut = false;
    private CallBack callBack;

    public RxBuilder(Builder builder){
        this.activity = builder.activity;
        this.isShowDialog = builder.isShowDialog;
        this.cancelable = builder.cancelable;
        this.isCanceledOnTouchOutside = builder.isCanceledOnTouchOutside;
        this.dialog = builder.dialog;
        this.isDefaultToast = builder.isDefaultToast;
        this.rxManager = builder.rxManager;
        this.readTimeOut = builder.readTimeOut;
        this.connectTimeOut = builder.connectTimeOut;
        this.okHttpClient = builder.okHttpClient;
        this.isLogOutPut = builder.isLogOutPut;
    }

    public Activity getActivity() {
        return activity;
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    public boolean isLogOutPut() {
        return isLogOutPut;
    }

    public boolean isCanceledOnTouchOutside() {
        return isCanceledOnTouchOutside;
    }

    public RxLoadingDialog getDialog() {
        return dialog;
    }

    public boolean isDefaultToast() {
        return isDefaultToast;
    }

    public RxManager getRxManager() {
        return rxManager;
    }

    /** 一般请求
     * @param cla
     * @param host 请求地址
     * @return
     */
    public <T> T createApi(Class<T> cla, String host){
        if(isShowDialog && null != dialog){
            dialog.showLoading(this);
        }
        return new RetrofitCreateHelper(activity)
                .setHttpTimeOut(readTimeOut, connectTimeOut)
                .setOkHttpClient(okHttpClient)
                .setIsLogOutPut(isLogOutPut)
                .createApi(cla, host);
    }

    /** 上传请求
     * @param cla
     * @param host 请求地址
     * @param listener
     * @return
     */
    public <T> T createApi(Class<T> cla, String host, RxUpLoadListener listener){
        if(isShowDialog && null != dialog){
            dialog.showLoading(this);
        }
        return new RetrofitCreateHelper(activity)
                .setHttpTimeOut(readTimeOut, connectTimeOut)
                .setOkHttpClient(okHttpClient)
                .setIsLogOutPut(isLogOutPut)
                .setUpLoadListener(listener)
                .createApi(cla, host);
    }

    /** 下载请求
     * @param cla
     * @param host 请求地址
     * @param listener
     * @return
     */
    public <T> T createApi(Class<T> cla, String host, RxDownLoadListener listener){
        if(isShowDialog && null != dialog){
            dialog.showLoading(this);
        }
        return new RetrofitCreateHelper(activity)
                .setHttpTimeOut(readTimeOut, connectTimeOut)
                .setOkHttpClient(okHttpClient)
                .setIsLogOutPut(isLogOutPut)
                .setDownLoadListener(listener)
                .createApi(cla, host);
    }

    public <T> Disposable setCallBack(Observable<T> observable, final CallBack<T> callBack){
        this.callBack = callBack;
        return observable.compose(activity.bindToLifecycle())////管理生命周期
                .compose(RxManager.rxSchedulerHelper())//发布事件io线程
                .subscribe(getBaseConsumer(),
                        getThrowableConsumer(),
                        getDefaultAction(),
                        getConsumer());
    }

    private <T> Consumer<T> getBaseConsumer(){
        return new Consumer<T>(){
            @Override
            public void accept(T t) throws Exception {
                if(null != getCallBack()){
                    getCallBack().onSuccess(t);
                }
                if(isShowDialog() && null !=getDialog()){
                    getDialog().dismissLoading();
                }
            }
        };
    }

    private Consumer<Throwable> getThrowableConsumer(){
        return new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                if(null == e){
                    return;
                }
                if(isLogOutPut()) {
                    Log.e("ThrowableConsumer-> ", e.getMessage());//抛异常
                }
                if(null != getCallBack()){
                    getCallBack().onFail(e);
                }
                if(isShowDialog() && null != getDialog()){
                    getDialog().dismissLoading();
                }
                if(isDefaultToast()) {
                    if (e instanceof HttpException) {
                        if (((HttpException) e).code() == 404) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (((HttpException) e).code() == 504) {
                            Toast.makeText(getActivity(), "请检查网络连接！", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "请检查网络连接！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (e instanceof IndexOutOfBoundsException
                            || e instanceof NullPointerException
                            || e instanceof JsonSyntaxException
                            || e instanceof IllegalStateException) {
                        Toast.makeText(getActivity(), "数据异常，解析失败！", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof TimeoutException) {
                        Toast.makeText(getActivity(), "连接超时，请重试！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "请求失败，请稍后再试！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    /** 最终结果的处理
     * @return 和getThrowableConsumer互斥
     */
    private Action getDefaultAction(){
        return new Action() {
            @Override
            public void run() throws Exception {
                if(null != callBack){
                    callBack.onComplete();
                }
                if(isShowDialog() && null != getDialog()){
                    getDialog().dismissLoading();
                }
            }
        };
    }

    /** 做准备工作
     * @return
     */
    private Consumer<Disposable> getConsumer(){
        return new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                //做准备工作
                if(null != callBack){
                    callBack.onStart(disposable);
                }
                if(rxManager != null){
                    rxManager.subscribe(disposable);
                }
            }
        };
    }

    public static Builder newBuilder(RxBaseActivity activity) {
        return new Builder(activity);
    }

    public static final class Builder {

        private RxBaseActivity activity;
        private boolean isShowDialog;
        private boolean cancelable;
        private boolean isCanceledOnTouchOutside;
        private RxLoadingDialog dialog;
        private boolean isDefaultToast;
        private RxManager rxManager;
        private int readTimeOut;
        private int connectTimeOut;
        private OkHttpClient okHttpClient;
        private boolean isLogOutPut = false;

        public Builder(RxBaseActivity activity) {
            this.activity = activity;
        }

        public Builder setLoadingDialog(RxLoadingDialog dialog){
            this.dialog = dialog;
            return this;
        }

        public Builder setDialogAttribute(boolean isShowDialog, boolean cancelable, boolean isCanceledOnTouchOutside){
            this.isShowDialog = isShowDialog;
            this.cancelable = cancelable;
            this.isCanceledOnTouchOutside = isCanceledOnTouchOutside;
            return this;
        }

        public Builder setIsDefaultToast(boolean isDefaultToast, RxManager rxManager){
            this.isDefaultToast = isDefaultToast;
            this.rxManager = rxManager;
            return this;
        }

        public Builder setHttpTimeOut(int readTimeOut, int connectTimeOut){
            this.readTimeOut = readTimeOut;
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public Builder setOkHttpClient(OkHttpClient okHttpClient){
            this.okHttpClient = okHttpClient;
            return this;
        }

        public Builder setIsLogOutPut(boolean isLogOutPut){
            this.isLogOutPut = isLogOutPut;
            return this;
        }

        public RxBuilder bindRx(){
            return new RxBuilder(this);
        }
    }
}
