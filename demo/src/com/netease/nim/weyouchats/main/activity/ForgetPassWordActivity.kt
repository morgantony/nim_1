package com.netease.nim.weyouchats.main.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.google.gson.Gson
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.ChangePassWordEntity
import com.netease.nim.weyouchats.common.util.countdown.CountdownUtils
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_change_pw.*
import kotlinx.android.synthetic.main.activity_change_pw.et_new
import kotlinx.android.synthetic.main.activity_change_pw.et_new_sure
import kotlinx.android.synthetic.main.activity_forget_pw.*

class ForgetPassWordActivity: UI(){

    private var countdownUtils: CountdownUtils? = null //倒计时工具

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_pw)
        initView()
    }

    private fun initView(){
        countdownUtils = CountdownUtils(tv_code, "重新发送", 60)
        var user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        if(user != null){
            tv_phone.text = "您已绑定手机号：" + user.mobile
        }else {
            tv_phone.visibility = View.GONE
        }
        titleBar.setLeftOnClickListener { finish() }
        tv_code.setOnClickListener { getCode(user!!.mobile) }
        titleBar.setRightOnClickListener{
            if(TextUtils.isEmpty(et_new.text)){
                ToastHelper.showToast(this@ForgetPassWordActivity, "请输入新密码")
                return@setRightOnClickListener
            }
            if(et_new.text.length > 12 || et_new.text.length < 6){
                ToastHelper.showToast(this@ForgetPassWordActivity, "请输入6-12位字母+数字组合")
                return@setRightOnClickListener
            }
            if(TextUtils.isEmpty(et_new_sure.text)){
                ToastHelper.showToast(this@ForgetPassWordActivity, "请输入确认密码")
                return@setRightOnClickListener
            }
            if(et_new_sure.text.toString() != et_new.text.toString()){
                ToastHelper.showToast(this@ForgetPassWordActivity, "新密码和确认密码不一致")
                return@setRightOnClickListener
            }
            if(TextUtils.isEmpty(et_code.text)){
                ToastHelper.showToast(this@ForgetPassWordActivity, "请输入验证码")
                return@setRightOnClickListener
            }

            sure(user!!.mobile)
        }
    }

    private fun getCode(mobile: String?) {
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                .getCode(mobile, 1)

        builder.setCallBack(observable, object : CallBack<ChangePassWordEntity>() {
            override fun onSuccess(response: ChangePassWordEntity?) {
                if(response!!.code == 200){
                    tv_code.text = ""
                    countdownUtils!!.init()
                    countdownUtils!!.runTimer()
                    ToastHelper.showToast(this@ForgetPassWordActivity, "验证码已发送");
                }else{
                    ToastHelper.showToast(this@ForgetPassWordActivity, "获取失败，请稍后再试");
                }
            }
        })
    }

    private fun sure(mobile: String?){
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                    .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                    .findPassWord(mobile, et_new.text.toString(), et_code.text.toString())

        builder.setCallBack(observable, object : CallBack<ChangePassWordEntity>() {
            override fun onSuccess(response: ChangePassWordEntity?) {
                if(response!!.code == 200){
                    ToastHelper.showToast(this@ForgetPassWordActivity, "找回成功，请重新登录")
                    MainActivity.logout(this@ForgetPassWordActivity, false)
                    finish()
                }else{
                    ToastHelper.showToast(this@ForgetPassWordActivity, "找回失败，请稍后再试");
                }
            }
        })
    }
}