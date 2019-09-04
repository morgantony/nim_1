package com.netease.nim.weyouchats.common;

import com.netease.nim.weyouchats.common.entity.ChangePassWordEntity;
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity;
import com.netease.nim.weyouchats.config.DemoServers;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

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

    /*修改密码*/

    @GET("user/modifyPassword")
    Observable<ChangePassWordEntity> changePassWord(
            @Query("token") String token,
            @Query("password") String password,
            @Query("newPassword") String newPassword);

    /*找回密码中的获取验证码*/
    @FormUrlEncoded
    @POST("user/sendcode")
    Observable<ChangePassWordEntity> getCode(
            @Field("mobile") String mobile,
            @Field("type") int type);//0 --注册 1--修改密码

    /*找回密码*/
    @FormUrlEncoded
    @POST("user/updatePassword")
    Observable<ChangePassWordEntity> findPassWord(
            @Field("mobile") String mobile,
            @Field("password") String password,
            @Field("code") String code);
}
