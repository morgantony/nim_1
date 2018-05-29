package com.bhm.sdk.rxlibrary.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bhm.sdk.rxlibrary.R;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;

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
     * rxManager 用户按返回关闭，请求取消
     * isCancelable true,单击返回键，dialog关闭；false,1s内双击返回键，dialog关闭，否则dialog不关闭
     */
    public void showLoading(final RxBuilder builder){
        if (dialog == null || !dialog.isShowing()) {
            if (builder.getActivity() != null && !builder.getActivity().isFinishing()) {
                dialog = initDialog(builder);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_BACK && dialog.isShowing()
                                && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            if(builder.isCancelable()){
                                if(null != builder.getRxManager()) {
                                    builder.getRxManager().removeObserver();
                                }
                                dismissLoading();
                                return false;
                            }
                            if ((System.currentTimeMillis() - onBackPressed) > 1000) {
                                onBackPressed = System.currentTimeMillis();
                            }else{
                                if(null != builder.getRxManager()) {
                                    builder.getRxManager().removeObserver();
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

    public Dialog initDialog(RxBuilder builder){
        LayoutInflater inflater = LayoutInflater.from(builder.getActivity());
        View v = inflater.inflate(R.layout.layout_dialog_app_loading, null);// 得到加载view
        dialog = new Dialog(builder.getActivity(), R.style.loading_dialog);// 创建自定义样式dialog
        dialog.setCancelable(builder.isCancelable());// false不可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(builder.isCanceledOnTouchOutside());
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
