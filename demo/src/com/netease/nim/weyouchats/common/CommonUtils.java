package com.netease.nim.weyouchats.common;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.weyouchats.DemoCache;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.UserPreferences;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.misc.DirCacheFileType;
import com.netease.nimlib.sdk.misc.MiscService;
import com.netease.nimlib.sdk.mixpush.MixPushService;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

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

    public static void setMessageNotify(Activity activity, SwitchButton switchButton, final boolean checkState) {
        // 如果接入第三方推送（小米），则同样应该设置开、关推送提醒
        // 如果关闭消息提醒，则第三方推送消息提醒也应该关闭。
        // 如果打开消息提醒，则同时打开第三方推送消息提醒。
        NIMClient.getService(MixPushService.class).enable(checkState).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                ToastHelper.showToast(activity, R.string.user_info_update_success);
                switchButton.setCheck(checkState);
                setToggleNotification(checkState);
            }

            @Override
            public void onFailed(int code) {
                switchButton.setCheck(!checkState);
                // 这种情况是客户端不支持第三方推送
                if (code == ResponseCode.RES_UNSUPPORT) {
                    switchButton.setCheck(checkState);
                    setToggleNotification(checkState);
                } else if (code == ResponseCode.RES_EFREQUENTLY) {
                    ToastHelper.showToast(activity, R.string.operation_too_frequent);
                } else {
                    ToastHelper.showToast(activity, R.string.user_info_update_failed);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    private static void setToggleNotification(boolean checkState) {
        try {
            UserPreferences.setNotificationToggle(checkState);
            NIMClient.toggleNotification(checkState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearCache(Activity activity){
        //            NIMClient.getService(LuceneService::class.java).clearCache()//这个清理全文检索缓存
        List<DirCacheFileType> types = new ArrayList<>();
        types.add(DirCacheFileType.AUDIO);
        types.add(DirCacheFileType.THUMB);
        types.add(DirCacheFileType.IMAGE);
        types.add(DirCacheFileType.VIDEO);
        types.add(DirCacheFileType.OTHER);

        NIMClient.getService(MiscService.class).clearDirCache(types, 0, 0).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                ToastHelper.showToast(activity, "清除成功");
            }
        });
    }
}
