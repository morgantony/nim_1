package com.netease.nim.weyouchats.main.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.weyouchats.BuildConfig;
import com.netease.nim.weyouchats.R;

public class AboutActivity extends UI {

    private TextView version;
    private TextView versionDate;
    private TitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        version = findViewById(R.id.version_detail);
        versionDate = findViewById(R.id.version_detail_date);
        titleBar = findViewById(R.id.titleBar);
    }

    private void initViewData() {
        version.setText("Version: " + BuildConfig.VERSION_NAME);
        versionDate.setText("Build Date:" + BuildConfig.BUILD_DATE);
        titleBar.setLeftOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
