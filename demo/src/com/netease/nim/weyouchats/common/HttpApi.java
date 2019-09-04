package com.netease.nim.weyouchats.common;

import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity;
import com.netease.nim.weyouchats.config.DemoServers;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface HttpApi {

    String HOST = DemoServers.API_COUSMER;

    /*上传文件*/
    @Multipart
    @POST("user/updateUserInfo")
    Observable<UpLoadUserInfoEntity> upload(
            @PartMap Map<String,String> params,
            @Part MultipartBody.Part file);
    /*上传文件*/
    @Multipart
    @POST("user/updateUserInfo")
    Observable<UpLoadUserInfoEntity> upload(
            @PartMap Map<String,String> params);

}
