package com.bhm.sdk.demo.tools;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxjava.RxManager;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;

/**
 * Created by bhm on 2018/5/14.
 */

public class MyLoadingDialog extends RxLoadingDialog{

    @Override
    public Dialog initDialog(RxManager rxManager, @NonNull Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View v = inflater.inflate(com.bhm.sdk.rxlibrary.R.layout.layout_dialog_app_loading, null);// 得到加载view
        ProgressBar pb_loading = v.findViewById(R.id.pb_loading);
        pb_loading.setVisibility(View.GONE);
        TextView textView = v.findViewById(R.id.dialog_text_loading);
        textView.setText("测试我的Loading");
        Dialog dialog = new Dialog(activity, com.bhm.sdk.rxlibrary.R.style.loading_dialog);// 创建自定义样式dialog
        dialog.setCancelable(false);// false不可以用“返回键”取消
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(v, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));// 设置布局
        return dialog;
    }
}
