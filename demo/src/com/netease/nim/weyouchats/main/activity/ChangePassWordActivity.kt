package com.netease.nim.weyouchats.main.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.ChangePassWordEntity
import com.netease.nim.weyouchats.config.preference.Preferences
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_change_pw.*

class ChangePassWordActivity: UI(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pw)
        initView()
    }

    private fun initView(){
        titleBar.setLeftOnClickListener { finish() }
        tv_forget.setOnClickListener { startActivity(Intent(this@ChangePassWordActivity, ForgetPassWordActivity::class.java)) }
        titleBar.setRightOnClickListener{
            if(TextUtils.isEmpty(et_old.text)){
                ToastHelper.showToast(this@ChangePassWordActivity, "请输入旧密码")
                return@setRightOnClickListener
            }
            if(TextUtils.isEmpty(et_new.text)){
                ToastHelper.showToast(this@ChangePassWordActivity, "请输入新密码")
                return@setRightOnClickListener
            }
            if(et_new.text.length > 12 || et_new.text.length < 6){
                ToastHelper.showToast(this@ChangePassWordActivity, "请输入6-12位字母+数字组合")
                return@setRightOnClickListener
            }
            if(TextUtils.isEmpty(et_new_sure.text)){
                ToastHelper.showToast(this@ChangePassWordActivity, "请输入确认密码")
                return@setRightOnClickListener
            }

            if(et_new_sure.text.toString() != et_new.text.toString()){
                ToastHelper.showToast(this@ChangePassWordActivity, "新密码和确认密码不一致")
                return@setRightOnClickListener
            }

            sure()
        }
    }

    private fun sure(){
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                    .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                    .changePassWord(Preferences.getUserToken(), et_old.text.toString(), et_new.text.toString())

        builder.setCallBack(observable, object : CallBack<ChangePassWordEntity>() {
            override fun onSuccess(response: ChangePassWordEntity?) {
                if(response!!.code == 200){
                    ToastHelper.showToast(this@ChangePassWordActivity, "修改成功，请重新登录")
                    MainActivity.logout(this@ChangePassWordActivity, false)
                    finish()
                }else{
                    ToastHelper.showToast(this@ChangePassWordActivity, "修改失败，请稍后再试")
                }
            }
        })
    }
}