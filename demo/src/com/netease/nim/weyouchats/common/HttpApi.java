package com.netease.nim.weyouchats.common;

import com.netease.nim.weyouchats.common.entity.ChangePassWordEntity;
import com.netease.nim.weyouchats.common.entity.CommenEntity;
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity;
import com.netease.nim.weyouchats.common.entity.UpdatePositionEntity;
import com.netease.nim.weyouchats.config.DemoServers;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface HttpApi {

    String HOST = DemoServers.API_COUSMER;

    /*上传文件*/
    @Multipart
    @POST("user/updateUserInfo")
    Observable<UpLoadUserInfoEntity> upload(
            @Part("accid") String accid,
            @Part MultipartBody.Part file);

    /*上传建议*/
    @FormUrlEncoded
    @POST("user/kefu")
    Observable<UpLoadUserInfoEntity> kefu(
            @Field("token") String token,
            @Field("message") String message);//建议内容

    /*上传文件*/
    @FormUrlEncoded
    @POST("user/updateUserInfo")
    Observable<UpLoadUserInfoEntity> upload(
            @Field("accid") String accid,
            @Field("name") String name,
            @Field("sign") String sign,
            @Field("email") String email,
            @Field("birth") String birth,
            @Field("mobile") String mobile,
            @Field("gender") String gender);

    /*修改密码*/
    @FormUrlEncoded
    @POST("user/modifyPassword")
    Observable<ChangePassWordEntity> changePassWord(
            @Field("token") String token,
            @Field("password") String password,
            @Field("newPassword") String newPassword);

    /*更新位置*/

    @GET("near/updatePosition")
    Observable<UpdatePositionEntity> updatePosition(
            @Query("token") String token,
            @Query("latitude") String password,
            @Query("longitude") String newPassword);

    /*附近的人*/

    @GET("near/nearPersonList")
    Observable<CommenEntity> nearPersonList(
            @Query("token") String token);

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
