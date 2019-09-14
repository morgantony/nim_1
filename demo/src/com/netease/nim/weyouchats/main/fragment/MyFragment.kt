package com.netease.nim.weyouchats.main.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.bhm.sdk.onresult.ActivityResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.netease.nim.uikit.common.ui.imageview.HeadImageView
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import com.netease.nim.weyouchats.main.activity.AboutActivity
import com.netease.nim.weyouchats.main.activity.AccountSettingActivity
import com.netease.nim.weyouchats.main.activity.EditUserInfoActivity
import com.netease.nim.weyouchats.main.activity.HelpingCenterActivity
import com.netease.nim.weyouchats.main.activity.QRActivity
import com.netease.nim.weyouchats.main.activity.SystemSettingActivity
import com.netease.nim.weyouchats.main.activity.UploadHeadPortraitActivity
import com.netease.nim.weyouchats.main.model.MainTab
import kotlinx.android.synthetic.main.activity_my.*
import org.jetbrains.anko.intentFor

class MyFragment : MainTabFragment() {

    private var headImageView: HeadImageView? = null
    private var tv_name: TextView? = null
    private var tv_des: TextView? = null
    private var iv_qr: ImageView? = null
    private var iv_edit: ImageView? = null   //已隐藏
    private var ll_info: LinearLayout? = null
    private var ll_account_setting: LinearLayout? = null
    private var ll_about: LinearLayout? = null
    private var ll_help: LinearLayout? = null
    private var ll_system_setting: LinearLayout? = null

    init {
        this.containerId = MainTab.MY.fragmentId
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onCurrent()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onInit() {
        findViews()
        initEvent()
    }

    private fun findViews() {
        headImageView = findView(R.id.hv_robot)
        tv_name = findView(R.id.tv_name)
        iv_qr = findView(R.id.iv_qr)
        iv_edit = findView(R.id.iv_edit)   //已隐藏
        ll_info = findView(R.id.ll_info)
        tv_des = findView(R.id.tv_des)
        ll_account_setting = findView(R.id.ll_account_setting)
        ll_about = findView(R.id.ll_about)
        ll_help = findView(R.id.ll_help)
        ll_system_setting = findView(R.id.ll_system_setting)
    }

    private fun initData() {
        val user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        if (null != user) {
            val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
            Glide.with(this).load(user.icon).apply(requestOptions).into(headImageView!!)
            tv_name!!.text = user.name
            if (!TextUtils.isEmpty(user.sign)) {
                tv_des!!.text = user.sign
            }
        }
    }

    private fun initEvent() {
        headImageView!!.setOnClickListener {   //点击头像跳转头像
            startActivity(context.intentFor<UploadHeadPortraitActivity>(
                    "edit" to true
            ))
        }
        iv_qr!!.setOnClickListener { v -> startActivity(Intent(activity, QRActivity::class.java)) }
        //个人信息
        ll_info!!.setOnClickListener { v ->

            val intent = Intent(activity, EditUserInfoActivity::class.java)
            intent.putExtra("edit", true)
            ActivityResult(activity).startForResult(intent) { _, data ->
                if (data != null) {
                    //更新头像和网名等
                    val head = data.getStringExtra("head")
                    val name = data.getStringExtra("name")
                    val sign = data.getStringExtra("sign")
                    val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
                    Glide.with(this).load(head).apply(requestOptions).into(hv_robot)
                    if (!TextUtils.isEmpty(name)) {
                        tv_name?.text = name
                    }
                    if (!TextUtils.isEmpty(sign)) {
                        tv_des?.text = sign
                    }
                }
            }

//            startActivity(Intent(activity, EditUserInfoActivity::class.java))
        }
        //已隐藏
        iv_edit!!.setOnClickListener { v ->
            val intent = Intent(activity, EditUserInfoActivity::class.java)
            intent.putExtra("edit", true)
            ActivityResult(activity).startForResult(intent) { resultCode, data ->
                if (data != null) {
                    //更新头像和网名等
                    val head = data.getStringExtra("head")
                    val name = data.getStringExtra("name")
                    val sign = data.getStringExtra("sign")
                    val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
                    Glide.with(this).load(head).apply(requestOptions).into(headImageView!!)
                    if (!TextUtils.isEmpty(name)) {
                        tv_name!!.text = name
                    }
                    if (!TextUtils.isEmpty(sign)) {
                        tv_des!!.text = sign
                    }
                }
            }
        }
        ll_account_setting!!.setOnClickListener { v -> startActivity(Intent(activity, AccountSettingActivity::class.java)) }
        ll_about!!.setOnClickListener { v -> startActivity(Intent(activity, AboutActivity::class.java)) }
        ll_help!!.setOnClickListener { v -> startActivity(Intent(activity, HelpingCenterActivity::class.java)) }
        ll_system_setting!!.setOnClickListener { v -> startActivity(Intent(activity, SystemSettingActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }
}
