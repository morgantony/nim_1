package com.netease.nim.weyouchats.main.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.bhm.sdk.bhmlibrary.utils.DisplayUtil
import com.bhm.sdk.bhmlibrary.views.ShadowView
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.util.PickerUtils
import kotlinx.android.synthetic.main.activity_account_setting.*

class AccountSettingActivity: UI(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)
        initView()
    }

    private fun initView(){
        ShadowView.newBuilder()
                .setTargetView(tv_logout)
                .setColor(Color.parseColor("#ee4b62"))//View颜色
                .setRadius(DisplayUtil.dp2px(this, 4f))
                .build()

        titleBar.setLeftOnClickListener { finish() }
        ll_update_pw.setOnClickListener {
            startActivity(Intent(this@AccountSettingActivity, ChangePassWordActivity::class.java))
        }
        tv_logout.setOnClickListener {
            PickerUtils.showExitDialog(this@AccountSettingActivity) {
                logout()
            }
        }
    }

    private fun logout(){
        //这里不需要调接口的吗？
        MainActivity.logout(this@AccountSettingActivity, false)
        finish()
    }
}