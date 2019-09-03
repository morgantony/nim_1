package com.netease.nim.weyouchats.main.fragment;

import android.os.Bundle;

import com.netease.nim.weyouchats.main.model.MainTab;

public class MyFragment extends MainTabFragment {

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

    }

    private void findViews(){

    }
}
