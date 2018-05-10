package com.bhm.sdk.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bhm.sdk.demo.adapter.MainUIAdapter;
import com.bhm.sdk.rxlibrary.demo.R;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected RecyclerView main_recycle_view;
    private MainUIAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initView(){
        main_recycle_view = (RecyclerView) findViewById(R.id.main_recycle_view);
        LinearLayoutManager ms= new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.VERTICAL);
        main_recycle_view.setLayoutManager(ms);
        main_recycle_view.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        main_recycle_view.setHasFixedSize(false);
        adapter = new MainUIAdapter(getItems());
        main_recycle_view.setAdapter(adapter);
    }

    private List<String> getItems(){
        List<String> list = new ArrayList<>();
        list.add("RxJava2+Retrofit2,Get请求");
        list.add("RxJava2+Retrofit2,post请求");
        list.add("RxJava2+Retrofit2,文件下载");
        list.add("RxJava2+Retrofit2,文件上传");
        list.add("");
        list.add("RxBus");
        return list;
    }

    private void initListener(){
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                openUI(position);
            }
        });
    }

    private void openUI(int position){
        Intent intent = new Intent();
        switch (position){
            case 0:
                intent.setClass(this, TitleBarXMLActivity.class);
                break;
            case 1:
                intent.setClass(this, TitleBarExtendsBaseActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
