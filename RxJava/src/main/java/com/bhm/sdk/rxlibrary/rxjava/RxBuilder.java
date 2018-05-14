package com.bhm.sdk.rxlibrary.rxjava;

import android.app.Activity;

import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;

import okhttp3.OkHttpClient;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxBuilder {

    private Activity activity;
    private boolean isShowDialog;
    private boolean cancelable;
    private boolean isCanceledOnTouchOutside;
    private RxLoadingDialog dialog;
    private boolean isDefaultToast;
    private RxManager rxManager;
    private int readTimeOut;
    private int connectTimeOut;
    private OkHttpClient okHttpClient;
    private boolean isLogOutPut = true;

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
            dialog.showLoading(rxManager, activity, cancelable);
        }
        return new RetrofitCreateHelper(activity)
                .setHttpTimeOut(readTimeOut, connectTimeOut)
                .setOkHttpClient(okHttpClient)
                .setIsLogOutPut(isLogOutPut)
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
            dialog.showLoading(rxManager, activity, cancelable);
        }
        return new RetrofitCreateHelper(activity)
                .setHttpTimeOut(readTimeOut, connectTimeOut)
                .setOkHttpClient(okHttpClient)
                .setIsLogOutPut(isLogOutPut)
                .setDownLoadListener(listener)
                .createApi(cla, host);
    }

    public static Builder newBuilder(RxBaseActivity activity) {
        return new Builder(activity);
    }

    public static final class Builder {

        private Activity activity;
        private boolean isShowDialog;
        private boolean cancelable;
        private boolean isCanceledOnTouchOutside;
        private RxLoadingDialog dialog;
        private boolean isDefaultToast;
        private RxManager rxManager;
        private int readTimeOut;
        private int connectTimeOut;
        private OkHttpClient okHttpClient;
        private boolean isLogOutPut = true;

        public Builder(Activity activity) {
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
