package com.netease.nim.weyouchats.contact;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.weyouchats.DemoCache;
import com.netease.nim.weyouchats.config.DemoServers;
import com.netease.nim.weyouchats.login.User;
import com.netease.nim.uikit.common.http.NimHttpClient;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 通讯录数据获取协议的实现
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public class ContactHttpClient {
    private static final String TAG = "ContactHttpClient";

    // code
    private static final int RESULT_CODE_SUCCESS = 200;

    // 找回密码
    private static final String API_NAME_UPDATEPASSWORD = "user/updatePassword";

    // 注册
    private static final String API_NAME_REGISTER = "user/createUser";

    //登录
    private static final String API_NAME_LOGIN = "user/login";
    //获取验证码
    private static final String API_NAME_SENDCODE = "user/sendcode";

    // header
    private static final String HEADER_KEY_APP_KEY = "appkey";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_USER_AGENT = "User-Agent";

    // request
    private static final String REQUEST_USER_NAME = "mobile";
    private static final String REQUEST_NICK_NAME = "code";
    private static final String REQUEST_PASSWORD = "password";

    // result
    private static final String RESULT_KEY_RES = "res";
    private static final String RESULT_KEY_ERROR_MSG = "msg";


    public interface ContactHttpCallback<T> {
        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }

    private static ContactHttpClient instance;

    public static synchronized ContactHttpClient getInstance() {
        if (instance == null) {
            instance = new ContactHttpClient();
        }

        return instance;
    }

    private ContactHttpClient() {
        NimHttpClient.getInstance().init(DemoCache.getContext());
    }


    /**
     * 向应用服务器创建账号（注册账号）
     * 由应用服务器调用WEB SDK接口将新注册的用户数据同步到云信服务器
     */
    public void register(String account, String nickName, String password, final ContactHttpCallback<Void> callback) {
        String url = DemoServers.API_COUSMER + API_NAME_REGISTER;
        try {
            nickName = URLEncoder.encode(nickName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        headers.put(HEADER_USER_AGENT, "nim_demo_android");
        headers.put(HEADER_KEY_APP_KEY, appKey);

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_NICK_NAME).append("=").append(nickName).append("&")
                .append(REQUEST_PASSWORD).append("=").append(password);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errMsg);
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue("code");
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    public void clientLogin(String account, String password,final ContactHttpCallback<User> callback) {
        String url = DemoServers.API_COUSMER + API_NAME_LOGIN;
        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        headers.put(HEADER_USER_AGENT, "nim_demo_android");
        headers.put(HEADER_KEY_APP_KEY, appKey);

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_PASSWORD).append("=").append(password);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errMsg);
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue("code");
                    if (resCode == RESULT_CODE_SUCCESS) {
                        User data = resObj.getObject("data", User.class);
                        callback.onSuccess(data);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 找回密码
     */
    public void findPW(String account, String nickName, String password, final ContactHttpCallback<Void> callback) {
        String url = DemoServers.API_COUSMER + API_NAME_UPDATEPASSWORD;
        try {
            nickName = URLEncoder.encode(nickName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        headers.put(HEADER_USER_AGENT, "nim_demo_android");
        headers.put(HEADER_KEY_APP_KEY, appKey);

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(account.toLowerCase()).append("&")
                .append(REQUEST_NICK_NAME).append("=").append(nickName).append("&")
                .append(REQUEST_PASSWORD).append("=").append(password);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errMsg);
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue("code");
                    if (resCode == RESULT_CODE_SUCCESS) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    public void sendCode(String mobile, int type,final ContactHttpCallback<User> callback) {
        String url = DemoServers.API_COUSMER + API_NAME_SENDCODE;
        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        headers.put(HEADER_USER_AGENT, "nim_demo_android");
        headers.put(HEADER_KEY_APP_KEY, appKey);

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_NAME).append("=").append(mobile.toLowerCase()).append("&")
                .append(REQUEST_PASSWORD).append("=").append(type);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, Throwable exception) {
                if (code != 200 || exception != null) {
                    String errMsg = exception != null ? exception.getMessage() : "null";
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errMsg);
                    if (callback != null) {
                        callback.onFailed(code, errMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue("code");
                    if (resCode == RESULT_CODE_SUCCESS) {
                        User data = resObj.getObject("data", User.class);
                        callback.onSuccess(data);
                    } else {
                        String error = resObj.getString(RESULT_KEY_ERROR_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    private String readAppKey() {
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
