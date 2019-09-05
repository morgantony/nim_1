package com.netease.nim.weyouchats.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhm.sdk.onresult.ActivityResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.User;
import com.netease.nim.weyouchats.main.activity.AboutActivity;
import com.netease.nim.weyouchats.main.activity.AccountSettingActivity;
import com.netease.nim.weyouchats.main.activity.EditUserInfoActivity;
import com.netease.nim.weyouchats.main.activity.HelpingCenterActivity;
import com.netease.nim.weyouchats.main.activity.QRActivity;
import com.netease.nim.weyouchats.main.activity.SystemSettingActivity;
import com.netease.nim.weyouchats.main.model.MainTab;

public class MyFragment extends MainTabFragment {

    private HeadImageView headImageView;
    private TextView tv_name;
    private TextView tv_des;
    private ImageView iv_qr;
    private ImageView iv_edit;   //已隐藏
    private LinearLayout ll_info;
    private LinearLayout ll_account_setting;
    private LinearLayout ll_about;
    private LinearLayout ll_help;
    private LinearLayout ll_system_setting;

    public MyFragment() {
        this.setContainerId(MainTab.MY.fragmentId);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCurrent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onInit() {
        findViews();
        initEvent();
    }

    private void findViews(){
        headImageView = findView(R.id.hv_robot);
        tv_name = findView(R.id.tv_name);
        iv_qr = findView(R.id.iv_qr);
        iv_edit = findView(R.id.iv_edit);   //已隐藏
        ll_info = findView(R.id.ll_info);
        tv_des = findView(R.id.tv_des);
        ll_account_setting = findView(R.id.ll_account_setting);
        ll_about = findView(R.id.ll_about);
        ll_help = findView(R.id.ll_help);
        ll_system_setting = findView(R.id.ll_system_setting);
    }

    private void initData(){
        User user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        if(null != user){
            RequestOptions requestOptions = new RequestOptions().centerCrop().error(R.drawable.nim_avatar_default);
            Glide.with(this).load(user.getIcon()).apply(requestOptions).into(headImageView);
            tv_name.setText(user.getName());
            if(!TextUtils.isEmpty(user.getSign())){
                tv_des.setText(user.getSign());
            }
        }
    }

    private void initEvent(){
        iv_qr.setOnClickListener(v -> startActivity(new Intent(getActivity(), QRActivity.class)));
        ll_info.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditUserInfoActivity.class)));
        //已隐藏
        iv_edit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditUserInfoActivity.class);
            intent.putExtra("edit", true);
            new ActivityResult(getActivity()).startForResult(intent, (resultCode, data) -> {
                if(data != null){
                    //更新头像和网名等
                    String head = data.getStringExtra("head");
                    String name = data.getStringExtra("name");
                    String sign = data.getStringExtra("sign");
                    RequestOptions requestOptions = new RequestOptions().centerCrop().error(R.drawable.nim_avatar_default);
                    Glide.with(this).load(head).apply(requestOptions).into(headImageView);
                    if(!TextUtils.isEmpty(name)){
                        tv_name.setText(name);
                    }
                    if(!TextUtils.isEmpty(sign)){
                        tv_des.setText(sign);
                    }
                }
            });
        });
        ll_account_setting.setOnClickListener(v -> startActivity(new Intent(getActivity(), AccountSettingActivity.class)));
        ll_about.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutActivity.class)));
        ll_help.setOnClickListener(v -> startActivity(new Intent(getActivity(), HelpingCenterActivity.class)));
        ll_system_setting.setOnClickListener(v -> startActivity(new Intent(getActivity(), SystemSettingActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
