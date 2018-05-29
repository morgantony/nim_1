package com.bhm.sdk.demo.entity;

import com.bhm.sdk.rxlibrary.rxjava.BaseResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Created by bhm on 2018/5/29.
 */

public class UpLoadEntity extends BaseResponse{

    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    private DataEntity data;

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

    public DataEntity getData() {
        return data;
    }

    public void setData(DataEntity data) {
        this.data = data;
    }

    public class DataEntity extends BaseResponse{
        @SerializedName("url")
        private String url;
        @SerializedName("path")
        private String path;
        @SerializedName("name")
        private String name;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
