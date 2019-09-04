package com.netease.nim.weyouchats.common.entity;

import com.bhm.sdk.rxlibrary.rxjava.BaseResponse;
import com.google.gson.annotations.SerializedName;

public class ChangePassWordEntity extends BaseResponse {
    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
