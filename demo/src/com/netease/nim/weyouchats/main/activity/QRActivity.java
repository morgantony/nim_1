package com.netease.nim.weyouchats.main.activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.gson.Gson;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.User;

public class QRActivity extends UI {

    private TitleBar titleBar;
    private ImageView iv_qr;
    private Dialog mDialog;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        initView();
        initEvent();
    }

    private void initView(){
        titleBar = findView(R.id.titleBar);
        iv_qr = findView(R.id.iv_qr);
        ImageView iv_head = findView(R.id.iv_head);
        TextView tv_name = findView(R.id.tv_name);
        User user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        if(null != user){
            FutureTarget<Bitmap> bitmaps = Glide.with(this)
                    .asBitmap()
                    .load(user.getIcon())
                    .submit();
            try{
                bitmap = bitmaps.get();
                iv_head.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
//            Glide.with(this).load(user.getIcon()).into(iv_head);
            tv_name.setText(user.getName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap != null){
            bitmap.recycle();
        }
    }

    private void initEvent(){
        titleBar.setLeftOnClickListener(v -> finish());
        iv_qr.setOnClickListener(v -> {
            ToastHelper.showToast(QRActivity.this, "没有二维码地址");
            showDialog();
        });
    }

    private void showDialog() {
        if (mDialog == null) {
            initShareDialog();
        }
        mDialog.show();
    }

    private void initShareDialog() {
        mDialog = new Dialog(this, R.style.dialog_bottom_full);
        mDialog.setCanceledOnTouchOutside(true); //手指触碰到外界取消
        mDialog.setCancelable(true);             //可取消 为true
        Window window = mDialog.getWindow();      // 得到dialog的窗体
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.share_animation);

        View view = View.inflate(this, R.layout.dialog_qr, null); //获取布局视图
        view.findViewById(R.id.tv_save).setOnClickListener(view1 -> {
            //保存二维码
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            try{
                if(bitmap == null){
                    ToastHelper.showToast(QRActivity.this, "获取不到二维码");
                }else {
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,
                            "demo_qr", "demo_qr.png");
                    ToastHelper.showToast(QRActivity.this, "保存成功");
                }
            }catch (Exception e){

            }
        });
        view.findViewById(R.id.tv_sao).setOnClickListener(view1 -> {
            //扫描二维码
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_reset).setOnClickListener(view1 -> {
            //重置二维码
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        });
        window.setContentView(view);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);//设置横向全屏
    }
}
