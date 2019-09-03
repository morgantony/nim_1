package com.netease.nim.weyouchats.main.activity;

import android.os.Bundle;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.google.gson.Gson;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.User;

public class EditUserInfoActivity extends UI {

    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        initView();
        initEvent();
    }

    private void initView(){
        titleBar = findView(R.id.titleBar);
        User user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        if(null != user){

        }
    }

    private void initEvent(){
        titleBar.setLeftOnClickListener(v -> finish());

    }
}
