package com.bhm.sdk.rxlibrary.rxjava;

import android.app.Activity;
import android.os.Build;
import android.webkit.WebSettings;

import java.io.File;

import okhttp3.Cache;

/**
 * Created by bhm on 2018/5/11.
 * <p>
 */
public class HttpCache {

    private static final int HTTP_RESPONSE_DISK_CACHE_MAX_SIZE = 50 * 1024 * 1024;

    public static Cache getCache(Activity activity) {
        return new Cache(new File(activity.getApplicationContext()
                .getCacheDir().getAbsolutePath() + File
                .separator + "data/NetCache"),
                HTTP_RESPONSE_DISK_CACHE_MAX_SIZE);
    }

    /**
     * 获取UserAgent
     *
     * @return UserAgent
     */
    public static String getUserAgent(Activity activity) {
        String userAgent = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                userAgent = WebSettings.getDefaultUserAgent(activity);
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
        } else {
            userAgent = System.getProperty("http.agent");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = userAgent.length(); i < length; i++) {
            char c = userAgent.charAt(i);
            if (c <= '\u001f' || c >= '\u007f') {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
