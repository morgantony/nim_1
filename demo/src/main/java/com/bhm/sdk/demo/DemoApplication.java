package com.bhm.sdk.demo;

import android.app.Application;

import com.bhm.sdk.demo.tools.MyLoadingDialog;
import com.bhm.sdk.rxlibrary.rxjava.RxConfig;

/**
 * Created by bhm on 2018/11/13.
 */

public class DemoApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        /*配置默认的Rx配置项*/
        RxConfig.newBuilder()
                .setRxLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
                .isDefaultToast(true)
                .isLogOutPut(true)
                .setReadTimeOut(30000)
                .setConnectTimeOut(30000)
                .setOkHttpClient(null)
                .build();
    }
}
