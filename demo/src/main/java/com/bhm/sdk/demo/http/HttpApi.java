package com.bhm.sdk.demo.http;

import com.bhm.sdk.demo.entity.DoGetEntity;
import com.bhm.sdk.demo.entity.UpLoadEntity;

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

    @GET("/api/4/news/latest")
    Observable<DoGetEntity> getData(@Header("token") String token,
                                                    @Query("type")  String type);

    @FormUrlEncoded
    @POST("v2/movie/in_theaters")
    Observable<DoGetEntity> getDataPost(@Field("once") boolean once_no);

    /*上传文件*/
    @Multipart
    @POST("common/update-avatar")
    Observable<UpLoadEntity> upload(
            @Header("Authorization") String token,
            @Part("role_id") RequestBody role_id,
            @Part MultipartBody.Part file);

    /*下载*/
    @Streaming
    @GET
    Observable<ResponseBody> downLoad(@Header("RANGE") String range, @Url String url);
}
