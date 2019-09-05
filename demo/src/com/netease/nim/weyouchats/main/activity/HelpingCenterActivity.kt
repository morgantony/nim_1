package com.netease.nim.weyouchats.main.activity

import android.os.Bundle
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_helping_center.*

class HelpingCenterActivity: UI(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helping_center)
        initView()
    }

    private fun initView(){

        titleBar.setLeftOnClickListener { finish() }
        ll_1.setOnClickListener {
            ToastHelper.showToast(this@HelpingCenterActivity, "此功能暂未开放")
        }
        ll_2.setOnClickListener {
            ToastHelper.showToast(this@HelpingCenterActivity, "此功能暂未开放")
        }
    }
}