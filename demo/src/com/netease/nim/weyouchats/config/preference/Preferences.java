package com.netease.nim.weyouchats.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.nim.weyouchats.DemoCache;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
    private static final String KEY_USER_INFO = "user";

    private static final String toggle = "toggleNotification";

    private static final String quantijinyan = "quantijinyan";

    public static void saveUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public static void saveUserInfo(String info) {
        saveString(KEY_USER_INFO, info);
    }

    public static String getUserInfo() {
        return getString(KEY_USER_INFO);
    }

    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static void saveUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }


    private static String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }

    /*Sp  Boolean  系统设置 推送开关*/
    public static Boolean gettoggle() {
        return getBlooen(toggle);
    }
    public static void savetoggle(Boolean a) {
        saveBlooen(toggle, a);
    }
    private static void saveBlooen(String key, Boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    private static Boolean getBlooen(String key) {
        return getSharedPreferences().getBoolean(key, true);
    }
    /*Sp  Boolean*/

    /*Sp  Boolean  群聊-全体禁言开关*/
    public static Boolean getquantijinyan() {
        return getquantijinyanBlooen(quantijinyan);
    }
    public static void savequantijinyan(Boolean a) {
        savequantijinyanBlooen(quantijinyan, a);
    }
    private static void savequantijinyanBlooen(String key, Boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    private static Boolean getquantijinyanBlooen(String key) {
        return getSharedPreferences().getBoolean(key, true);
    }
    /*Sp  Boolean*/
}
