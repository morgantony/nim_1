package com.netease.nim.weyouchats.main.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.activity_edit_name.*

class EditUserItemActivity : UI() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_name)
        initView()
        initEvent()
    }

    private fun initView() {
        if (null != intent && null != intent.getStringExtra("name")) {
            val name = intent.getStringExtra("name")
            et_name.setText(name)
            et_name.setSelection(name.length)
        }
        if(intent!!.getIntExtra("type", 0) == 1){
            //修改昵称
            titleBar.setTitleText("修改昵称")
        }else if(intent!!.getIntExtra("type", 0) == 2){
            //修改昵称
            titleBar.setTitleText("修改邮箱")
        }
    }

    private fun initEvent() {
        titleBar.setLeftOnClickListener { finish() }
        titleBar.setRightOnClickListener {
            if (TextUtils.isEmpty(et_name.text)) {
                if(intent!!.getIntExtra("type", 0) == 1){
                    ToastHelper.showToast(this@EditUserItemActivity, "请输入昵称")
                }else if(intent!!.getIntExtra("type", 0) == 2){
                    ToastHelper.showToast(this@EditUserItemActivity, "请输入邮箱")
                }
                return@setRightOnClickListener
            }
            val intent = Intent()
            intent.putExtra("name", et_name.text.toString())
            setResult(0, intent)
            finish()
        }
    }
}
