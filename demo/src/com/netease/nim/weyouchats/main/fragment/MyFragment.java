package com.netease.nim.weyouchats.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bhm.sdk.onresult.ActivityResult;
import com.google.gson.Gson;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.User;
import com.netease.nim.weyouchats.main.activity.EditUserInfoActivity;
import com.netease.nim.weyouchats.main.activity.QRActivity;
import com.netease.nim.weyouchats.main.model.MainTab;

public class MyFragment extends MainTabFragment {

    private HeadImageView headImageView;
    private TextView tv_name;
    private ImageView iv_qr;
    private ImageView iv_edit;

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
    }

    private void initData(){
        User user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        if(null != user){
            headImageView.loadAvatar(user.getIcon());
            tv_name.setText(user.getName());
        }
    }

    private void initEvent(){
        iv_qr.setOnClickListener(v -> startActivity(new Intent(getActivity(), QRActivity.class)));
        iv_edit.setOnClickListener(v -> {
            new ActivityResult(getActivity()).startForResult(EditUserInfoActivity.class, (resultCode, data) -> {
                if(data != null){
                    //更新头像和网名等
                }
            });
        });
    }
}
