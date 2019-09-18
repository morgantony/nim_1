package com.netease.nim.weyouchats.main.activity

import android.os.Bundle
import android.util.Log
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.activity_blank_text_scan.*

//二维码扫描出来的是纯文本
class BlankActivity: UI() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank_text_scan)
        titleBar.setLeftOnClickListener { v -> finish() }
        val blank_=intent.getStringExtra("result")
        Log.e("999999","======$blank_")
        blank_text.text=blank_
    }
}