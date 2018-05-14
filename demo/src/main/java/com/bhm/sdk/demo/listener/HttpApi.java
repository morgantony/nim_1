package com.bhm.sdk.demo.listener;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by bhm on 2018/5/11.
 */

public interface HttpApi {

    @GET("福利/10/1")
    Observable<Object> getData(@Header("token") String token,
                               @Query("type")  String type);

    @FormUrlEncoded
    @POST("AppFiftyToneGraph/videoLink")
    Observable<Object> getDataPost(@Field("once") boolean once_no);

    /*上传文件*/
    @Multipart
    @POST("upload/uploadFile.do")
    Observable<Object> upload(
            @Header("token") String token,
            @Part("filename") RequestBody description,
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file);

    /*下载*/
    @Streaming
    @GET
    Observable<ResponseBody> downLoad(@Url String url);
}
