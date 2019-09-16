package com.netease.nim.weyouchats.main.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.widget.TextView
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.ChangePassWordEntity
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.main.adapter.EditTextAdapter
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_change_pw.*
import kotlinx.android.synthetic.main.activity_change_pw.et_new
import kotlinx.android.synthetic.main.activity_forget_pw.*
import kotlinx.android.synthetic.main.activity_helping_center.*

class HelpingCenterActivity: UI(){
    lateinit var toolbarView :TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helping_center)
        val options = NimToolBarOptions()
        options.titleId = R.string.my_suggest
        setToolBar(R.id.toolbar, options)
        initView()
    }

    private fun initView(){
         toolbarView = findView<TextView>(R.id.action_bar_right_clickable_textview)
        toolbarView.setOnClickListener {
            val msgStr=et_suggest.text.toString()
            if (!msgStr.isNullOrEmpty()){
                uploadSuggest(Preferences.getUserToken(),msgStr)
            }
        }

        et_suggest!!.addTextChangedListener(listen)
    }

    val listen = object : EditTextAdapter() {
        override fun afterTextChanged(se: Editable?) {
            super.afterTextChanged(se)
            if(!se.toString().isNullOrEmpty()){
                toolbarView.setTextColor(Color.parseColor("#FFFFFF"))
            }else{
                toolbarView.setTextColor(Color.parseColor("#BBBABA"))
            }

        }
    }

    private fun uploadSuggest( token:String,msg:String) {
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                .kefu(token, msg)

        builder.setCallBack(observable, object : CallBack<UpLoadUserInfoEntity>() {
            override fun onSuccess(response: UpLoadUserInfoEntity?) {
                if(response!!.code == 200){
                    ToastHelper.showToast(this@HelpingCenterActivity, "意见反馈成功")
                    finish()
                }else{
                    ToastHelper.showToast(this@HelpingCenterActivity, "意见反馈失败")
                }
            }
        })
    }
}