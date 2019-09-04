package com.netease.nim.weyouchats.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.netease.nim.weyouchats.DemoCache;

public class HttpUtils {

    public static String readAppKey() {
        try {
            ApplicationInfo appInfo = DemoCache.getContext().
                    getPackageManager().
                    getApplicationInfo(DemoCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
