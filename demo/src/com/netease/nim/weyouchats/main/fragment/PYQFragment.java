package com.netease.nim.weyouchats.main.fragment;

import android.os.Bundle;

import com.netease.nim.weyouchats.main.model.MainTab;

/**
 * 圈子
 */
public class PYQFragment extends MainTabFragment {

    public PYQFragment() {
        this.setContainerId(MainTab.PYQ.fragmentId);
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
