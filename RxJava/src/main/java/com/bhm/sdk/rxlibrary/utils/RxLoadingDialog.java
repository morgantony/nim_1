package com.bhm.sdk.rxlibrary.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bhm.sdk.rxlibrary.R;
import com.bhm.sdk.rxlibrary.rxjava.RxManager;

/**
 * Created by bhm on 2018/5/11.
 */

public class RxLoadingDialog {

    private static Dialog dialog;
    private static long onBackPressed = 0l;
    private static RxLoadingDialog RxDialog;

    public static RxLoadingDialog getDefaultDialog(){
        if(null == RxDialog){
            RxDialog = new RxLoadingDialog();
        }
        return RxDialog;
    }

    /**
     * @param rxManager 用户按返回关闭，请求取消
     * @param activity
     * @param isCancelable true,单击返回键，dialog关闭；false,1s内双击返回键，dialog关闭，否则dialog不关闭
     */
    public void showLoading(final RxManager rxManager, @NonNull Activity activity, final boolean isCancelable){
        if (dialog == null || !dialog.isShowing()) {
            if (activity != null && !activity.isFinishing()) {
                dialog = initDialog(rxManager, activity);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_BACK && dialog.isShowing()
                                && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            if(isCancelable){
                                if(null != rxManager) {
                                    rxManager.removeObserver();
                                }
                                dismissLoading();
                                return false;
                            }
                            if ((System.currentTimeMillis() - onBackPressed) > 1000) {
                                onBackPressed = System.currentTimeMillis();
                            }else{
                                if(null != rxManager) {
                                    rxManager.removeObserver();
                                }
                                dismissLoading();
                            }
                        }
                        return false;
                    }
                });
                dialog.show();
            }
        }
    }

    public Dialog initDialog(final RxManager rxManager, @NonNull Activity activity){
        LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(R.layout.layout_dialog_app_loading, null);// 得到加载view
        dialog = new Dialog(activity, R.style.loading_dialog);// 创建自定义样式dialog
        dialog.setCancelable(false);// false不可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));// 设置布局
        return dialog;
    }

    public void dismissLoading(){
        if(null != dialog && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
            System.gc();
        }
    }
}
