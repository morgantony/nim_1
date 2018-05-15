package com.bhm.sdk.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bhm.sdk.demo.adapter.MainUIAdapter;
import com.bhm.sdk.demo.listener.Entity;
import com.bhm.sdk.demo.listener.HttpApi;
import com.bhm.sdk.demo.tools.MyLoadingDialog;
import com.bhm.sdk.rxlibrary.demo.R;
import com.bhm.sdk.rxlibrary.rxbus.RxBus;
import com.bhm.sdk.rxlibrary.rxbus.Subscribe;
import com.bhm.sdk.rxlibrary.rxjava.RxBaseActivity;
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder;
import com.bhm.sdk.rxlibrary.rxjava.RxDownLoadListener;
import com.bhm.sdk.rxlibrary.rxjava.RxManager;
import com.bhm.sdk.rxlibrary.rxjava.RxObserver;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBus.get().register(this);
        initView();
        initListener();
    }

    private void initView() {
        main_recycle_view = (RecyclerView) findViewById(R.id.main_recycle_view);
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
        list.add("RxJava2+Retrofit2,文件下载");
        list.add("RxJava2+Retrofit2,文件上传");
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
                upLoadFile();//上传文件
                break;
            case 3:
                downLoadFile();//下载文件
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
//                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
//                .setHttpTimeOut()
//                .setIsLogOutPut(false)//默认是true
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        builder.createApi(HttpApi.class, "http://gank.io/api/data/")
                .getData("Bearer aedfc1246d0b4c3f046be2d50b34d6ff", "1")
                .compose(bindToLifecycle())//管理生命周期
                .compose(RxManager.rxSchedulerHelper())//发布事件io线程
                .subscribe(new RxObserver<Object>(builder) {//Object可以替换成实体类，无需再解析
                    //根据业务需要，可继承RxObserver重写类，对onFail和onSuccess进行解析，根据resultCode进行处理
                    @Override
                    public void onStart(Disposable disposable) {

                    }

                    @Override
                    public void onSuccess(Object response) {
                        Log.i("onSuccess-------> ", response.toString());
                    }

                    @Override
                    public void onDone() {

                    }

                    @Override
                    public void onFail(Throwable t) {

                    }
                });
    }

    private void doPost() {
        RxBuilder builder = RxBuilder.newBuilder(this)
//                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
//                .setHttpTimeOut()
//                .setIsLogOutPut(false)//默认是true
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        builder.createApi(HttpApi.class, "https://www.izaodao.com/Api/")
                .getDataPost(true)
                .compose(bindToLifecycle())//管理生命周期
                .compose(RxManager.rxSchedulerHelper())//发布事件io线程
                .subscribe(new RxObserver<Object>(builder) {//Object可以替换成实体类，无需再解析
                    //根据业务需要，可继承RxObserver重写类，对onFail和onSuccess进行解析，根据resultCode进行处理
                    @Override
                    public void onStart(Disposable disposable) {

                    }

                    @Override
                    public void onSuccess(Object response) {
                        Log.i("onSuccess-------> ", response.toString());
                    }

                    @Override
                    public void onDone() {

                    }

                    @Override
                    public void onFail(Throwable t) {

                    }
                });
    }

    private void upLoadFile() {
        File file = new File("filePath");
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"),file);
        MultipartBody.Part part= MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        RxBuilder builder = RxBuilder.newBuilder(this)
//                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(true, false, false)
//                .setHttpTimeOut()
//                .setIsLogOutPut(false)//默认是true
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        builder.createApi(HttpApi.class, "http://gank.io/api/data/")
                .upload("Bearer aedfc1246d0b4c3f046be2d50b34d6ff",
                        RequestBody.create(MediaType.parse("text/plain"), "filename"),
                        RequestBody.create(MediaType.parse("text/plain"),"id"),
                        part)
                .compose(bindToLifecycle())//管理生命周期
                .compose(RxManager.rxSchedulerHelper())//发布事件io线程
                .subscribe(new RxObserver<Object>(builder) {//Object可以替换成实体类，无需再解析
                    //根据业务需要，可继承RxObserver重写类，对onFail和onSuccess进行解析，根据resultCode进行处理
                    @Override
                    public void onStart(Disposable disposable) {

                    }

                    @Override
                    public void onSuccess(Object response) {
                        Log.d("onSuccess-------> ", response.toString());
                    }

                    @Override
                    public void onDone() {

                    }

                    @Override
                    public void onFail(Throwable t) {

                    }
                });
    }

    private void downLoadFile(){
        RxBuilder builder = RxBuilder.newBuilder(this)
//                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setLoadingDialog(new MyLoadingDialog())
                .setDialogAttribute(false, false, false)
//                .setHttpTimeOut()
//                .setIsLogOutPut(false)//默认是true
                .setIsDefaultToast(true, rxManager)
                .bindRx();
        Disposable disposable = builder.createApi(HttpApi.class, "http://gank.io/api/data/", rxDownLoadListener)
                .downLoad("下载文件的url")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(@NonNull ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
                .observeOn(Schedulers.computation()) // 用于计算任务
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream){
                        //得到整个文件流
                        try {
                            rxDownLoadListener.onProgress(100);
//                          writeFile(inputStream, filePath);
                            rxDownLoadListener.onFinishDownload();
                        }catch (Exception e){
                            rxDownLoadListener.onFail(e.getMessage());
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
                        Toast.makeText(MainActivity.this, "下载失败，请重试", Toast.LENGTH_SHORT).show();
                        rxManager.removeObserver();
                    }
                });
        rxManager.subscribe(disposable);
    }

    private RxDownLoadListener rxDownLoadListener = new RxDownLoadListener() {
        @Override
        public void onStartDownload() {

        }

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onFinishDownload() {

        }

        @Override
        public void onFail(String errorInfo) {

        }
    };
}