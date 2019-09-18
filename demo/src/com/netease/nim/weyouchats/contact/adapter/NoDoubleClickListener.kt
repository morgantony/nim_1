package com.netease.nim.weyouchats.contact.adapter

import android.view.View

/**
 * 防止二次点击多次提交
 */
abstract class NoDoubleClickListener :View.OnClickListener{
    val MIN_CLICK_DELAY_TIME:Long = 2000
    private var lastClickTime:Long = 0

    abstract fun  onNoDoubleClick(v:View?)
    override fun onClick(view: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime-lastClickTime>MIN_CLICK_DELAY_TIME){
            lastClickTime = currentTime
            onNoDoubleClick(view)
        }
    }
}