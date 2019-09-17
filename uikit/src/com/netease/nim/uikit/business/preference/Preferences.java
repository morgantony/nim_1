package com.netease.nim.uikit.business.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String quantijinyan = "quantijinyan";

    static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }


    /*Sp  Boolean  群聊-全体禁言开关*/
    public static Boolean getquantijinyan(Context context) {
        return getquantijinyanBlooen(quantijinyan,context);
    }
    public static void savequantijinyan(Boolean a,Context context) {
        savequantijinyanBlooen(quantijinyan, a,context);
    }
    private static void savequantijinyanBlooen(String key, Boolean value,Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    private static Boolean getquantijinyanBlooen(String key,Context context) {
        return getSharedPreferences(context).getBoolean(key, false);
    }
    /*Sp  Boolean*/
}
