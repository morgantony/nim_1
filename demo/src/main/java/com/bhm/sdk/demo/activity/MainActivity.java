package com.bhm.sdk.demo.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bhm.sdk.demo.adapter.MainUIAdapter;
import com.bhm.sdk.demo.entity.DoGetEntity;
import com.bhm.sdk.demo.entity.UpLoadEntity;
import com.bhm.sdk.demo.http.HttpApi;
import com.bhm.sdk.demo.tools.Entity;
import com.bhm.sdk.demo.tools.MyLoadingDialog;
import com.bhm.sdk.demo.tools.Utils;
import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxbus.RxBus;
import com.bhm.sdk.rxlibrary.rxbus.Subscribe;
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack;
import com.bhm.sdk.rxlibrary.rxjava.RxBaseActivity;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.rxjava.callback.RxDownLoadCallBack;
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack;
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainActivity extends RxBaseActivity {

    protected RecyclerView main_recycle_view;
    private MainUIAdapter adapter;
    private TitleBar titleBar;
    private ProgressBar progressBarHorizontal;
    private RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBus.get().register(this);
        rxPermissions = new RxPermissions(this);//权限申请
        initView();
        initListener();
    }

    private void initView() {
        main_recycle_view = (RecyclerView) findViewById(R.id.main_recycle_view);
        progressBarHorizontal = (ProgressBar) findViewById(R.id.progressBarHorizontal);
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        LinearLayoutManager ms = new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.VERTICAL);
        main_recycle_view.setLayoutManager(ms);
        main_recycle_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        main_recycle_view.setHasFixedSize(false);
        adapter = new MainUIAdapter(getItems());
        main_recycle_view.setAdapter(adapter);
    }

    private List<String> getItems() {
        List<String> list = new ArrayList<>();
        list.add("RxJava2+Retrofit2,Get请求");
        list.add("RxJava2+Retrofit2,post请求");
        list.add("RxJava2+Retrofit2,文件上传（带进度）");
        list.add("RxJava2+Retrofit2,文件下载（带进度）");
        list.add("");
        list.add("RxBus");
        return list;
    }

    private void initListener() {
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                openUI(position);
            }
        });
    }

    private void openUI(int position) {
        switch (position) {
            case 0:
                doGet();
                break;
            case 1:
                doPost();
                break;
            case 2:
                rxPermissions
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if(!aBoolean){
                                    Toast.makeText(MainActivity.this, "无法获取权限，请在设置中授权",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    upLoadFile();//上传文件
                                }
                            }
                        });
                break;
            case 3:
                rxPermissions
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if(!aBoolean){
                                    Toast.makeText(MainActivity.this, "无法获取权限，请在设置中授权",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    downLoadFile();//下载文件
                                }
                            }
                        });
                break;
            case 5:
                startActivity(new Intent(this, RxBusActivity.class));
            default:
                return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.get().unRegister(this);
    }

    @Subscribe(code = 1111)
    public void rxBusEvent(Entity entity){
        if(null != entity){
            Toast.makeText(this, "RxBus改变了MainActivity的标题", Toast.LENGTH_SHORT).show();
            titleBar.setTitleText(entity.getMsg());
        }
    }

    private void doGet() {
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
//                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
                //.setHttpTimeOut()
//                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<DoGetEntity> observable = builder
                .createApi(HttpApi.class, "http://news-at.zhihu.com")
                .getData("Bearer aedfc1246d0b4c3f046be2d50b34d6ff", "1");
        builder.setCallBack(observable, new CallBack<DoGetEntity>() {
            @Override
            public void onSuccess(DoGetEntity response) {
                Log.i("MainActivity--> ", response.getDate());
                Toast.makeText(MainActivity.this, response.getDate(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doPost() {
        RxBuilder builder = RxBuilder.newBuilder(this)
//                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
                //.setHttpTimeOut()
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<DoGetEntity> observable = builder
                .createApi(HttpApi.class, "Https://api.douban.com/")
                .getDataPost(true);
        builder.setCallBack(observable, new CallBack<DoGetEntity>() {
            @Override
            public void onSuccess(DoGetEntity response) {
                Log.i("MainActivity--> ", response.toString());
                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void upLoadFile() {
        File file = Utils.getFile();
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"),file);
        MultipartBody.Part part= MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
//                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(false, false, false)
                //.setHttpTimeOut()
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Observable<UpLoadEntity> observable = builder
                .createApi(HttpApi.class, "http://cloudapi.dev-chexiu.cn/", rxUpLoadListener)
                .upload("Bearer a7f998422ae0a9008a3bc5be1273946a",
                        RequestBody.create(MediaType.parse("text/plain"), "9"),
                        part);
        builder.setCallBack(observable, new CallBack<UpLoadEntity>() {
            @Override
            public void onStart(Disposable disposable) {
                rxUpLoadListener.onStart();
            }

            @Override
            public void onSuccess(UpLoadEntity response) {
                Log.i("MainActivity--> ", response.getMsg());
                Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(Throwable e) {
                rxUpLoadListener.onFail(e.getMessage());
            }

            @Override
            public void onComplete() {
                rxUpLoadListener.onFinish();
            }
        });
    }

    private void downLoadFile(){
        RxBuilder builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
//                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(false, false, false)
//                .setHttpTimeOut()
//                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Disposable disposable = builder
                //域名随便填写,但必须以“/”为结尾
                .createApi(HttpApi.class, "http://dldir1.qq.com/weixin/", rxDownLoadListener)
                .downLoad("http://dldir1.qq.com/weixin/android/weixin666android1300.apk")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(@NonNull ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
//                .observeOn(Schedulers.computation()) // 用于计算任务
                // 由于writeFile注释掉了，所以onFinishDownload中的提示语必须在ui线程中，正常情况下使用Schedulers.computation()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream){
                        //得到整个文件流
                        try {
                            rxDownLoadListener.onProgress(100, 100, 100);
//                          writeFile(inputStream, filePath);//注释掉
                            if(null != inputStream){
                                inputStream.close();
                                System.gc();
                            }
                            rxDownLoadListener.onFinish();
                        }catch (Exception e){
                            rxDownLoadListener.onFail(e.getMessage());
                            rxManager.removeObserver();
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        rxDownLoadListener.onFail(throwable.getMessage());
                        rxManager.removeObserver();
                    }
                });
        rxManager.subscribe(disposable);
    }

    private RxUpLoadCallBack rxUpLoadListener = new RxUpLoadCallBack() {
        @Override
        public void onStart() {
            progressBarHorizontal.setProgress(0);
        }

        @Override
        public void onProgress(int progress, long bytesWritten, long contentLength) {
            progressBarHorizontal.setProgress(progress);
        }

        @Override
        public void onFinish() {
            Toast.makeText(MainActivity.this, "onFinishUpload", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(String errorInfo) {
            Toast.makeText(MainActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
        }
    };

    private RxDownLoadCallBack rxDownLoadListener = new RxDownLoadCallBack() {
        @Override
        public void onStart() {
            progressBarHorizontal.setProgress(0);
        }

        @Override
        public void onProgress(int progress, long bytesWritten, long contentLength) {
            progressBarHorizontal.setProgress(progress);
        }

        @Override
        public void onFinish() {
            Toast.makeText(MainActivity.this, "onFinishDownload", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFail(String errorInfo) {
            Toast.makeText(MainActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
        }
    };
}