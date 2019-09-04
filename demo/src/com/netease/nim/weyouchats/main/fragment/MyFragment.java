package com.netease.nim.weyouchats.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bhm.sdk.onresult.ActivityResult;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.User;
import com.netease.nim.weyouchats.main.activity.AccountSettingActivity;
import com.netease.nim.weyouchats.main.activity.EditUserInfoActivity;
import com.netease.nim.weyouchats.main.activity.QRActivity;
import com.netease.nim.weyouchats.main.model.MainTab;

public class MyFragment extends MainTabFragment {

    private HeadImageView headImageView;
    private TextView tv_name;
    private TextView tv_des;
    private ImageView iv_qr;
    private ImageView iv_edit;
    private LinearLayout ll_info;
    private LinearLayout ll_account_setting;

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
        initData();
        initEvent();
    }

    private void findViews(){
        headImageView = findView(R.id.hv_robot);
        tv_name = findView(R.id.tv_name);
        iv_qr = findView(R.id.iv_qr);
        iv_edit = findView(R.id.iv_edit);
        ll_info = findView(R.id.ll_info);
        tv_des = findView(R.id.tv_des);
        ll_account_setting = findView(R.id.ll_account_setting);
    }

    private void initData(){
        User user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        if(null != user){
            headImageView.loadAvatar(user.getIcon());
            tv_name.setText(user.getName());
            if(!TextUtils.isEmpty(user.getSign())){
                tv_des.setText(user.getSign());
            }
        }
    }

    private void initEvent(){
        iv_qr.setOnClickListener(v -> startActivity(new Intent(getActivity(), QRActivity.class)));
        ll_info.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditUserInfoActivity.class)));
        iv_edit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditUserInfoActivity.class);
            intent.putExtra("edit", true);
            new ActivityResult(getActivity()).startForResult(intent, (resultCode, data) -> {
                if(data != null){
                    //更新头像和网名等
                    String head = data.getStringExtra("head");
                    String name = data.getStringExtra("name");
                    String sign = data.getStringExtra("sign");
                    if(!TextUtils.isEmpty(head)){
                        Glide.with(this).load(head).into(headImageView);
                    }
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
    }
}
