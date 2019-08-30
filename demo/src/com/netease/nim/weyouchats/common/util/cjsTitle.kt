package com.netease.nim.weyouchats.common.util

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.Window
import com.netease.nim.weyouchats.R

fun titleW(context: Context, window: Window){
    if (Build.VERSION.SDK_INT >= 21) {
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        window.statusBarColor = ContextCompat.getColor(context, R.color.color_FF2195D3)
    }
}