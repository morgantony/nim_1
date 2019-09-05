package com.netease.nim.weyouchats.main.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.bhm.sdk.bhmlibrary.utils.DateUtils
import com.bhm.sdk.onresult.ActivityResult
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity
import com.netease.nim.weyouchats.common.util.PickerUtils
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.activity_edit_info.*

class EditUserInfoActivity : UI() {

    private var imagePath: String = ""
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_info)
        initView()
        initEvent()
    }

    private fun initView() {
        user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
        Glide.with(this@EditUserInfoActivity).load(user.icon).apply(requestOptions).into(hv_robot)
        imagePath = user.icon.toString()
        tv_name.text = if (user.name == null) "" else user.name
        if (user.gender == null){
            tv_sex.text = ""
        } else if(user.gender.toString() == "1" || user.gender.toString() == "男"){
            tv_sex.text = "男"
        }else{
            tv_sex.text = "女"
        }

        tv_birthday.text = if (user.birth == null) "" else user.birth
        tv_email.text = if (user.email == null) "" else user.email
        tv_id.text = if (user.accid == null) "" else user.accid
        tv_phone.text = if (user.mobile == null) "" else user.mobile
        et_note.setText(if (TextUtils.isEmpty(user.sign)) "这个人很懒什么都没留下" else user.sign)
        et_note.setSelection(et_note.text.length)
    }

    @SuppressLint("SetTextI18n")
    private fun initEvent() {
        titleBar.setLeftOnClickListener { finish() }
        ll_qr.setOnClickListener { startActivity(Intent(this@EditUserInfoActivity, QRActivity::class.java)) }
        ll_head.setOnClickListener {
            val intent1 = Intent(this@EditUserInfoActivity, UploadHeadPortraitActivity::class.java)
            intent1.putExtra("edit", intent.getBooleanExtra("edit", false))
            ActivityResult(this@EditUserInfoActivity).startForResult(intent1) { resultCode, data ->
                if (resultCode == 0 && null != data && null != data.getStringExtra("head")) {
                    val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
                    Glide.with(this@EditUserInfoActivity).load(data.getStringExtra("head")).apply(requestOptions).into(hv_robot)
                    imagePath = data.getStringExtra("head")
                }
            }
        }
        if (!intent.getBooleanExtra("edit", false)) {
            titleBar.setRightText("")
            et_note.isEnabled = false
            return
        }
        titleBar.setTitleText("编辑个人信息")
        titleBar.setRightOnClickListener { save() }
        ll_name.setOnClickListener {
            val intent = Intent(this@EditUserInfoActivity, EditUserItemActivity::class.java)
            intent.putExtra("type", 1)
            intent.putExtra("name", tv_name.text)
            ActivityResult(this@EditUserInfoActivity).startForResult(intent) { resultCode, data ->
                if (resultCode == 0 && null != data && null != data.getStringExtra("name")) {
                    tv_name.text = data.getStringExtra("name")
                }
            }
        }
        ll_sex.setOnClickListener {
            val dialog = CustomAlertDialog(this@EditUserInfoActivity)
            dialog.setTitle("性别")
            dialog.addItem("男"
            ) { tv_sex.text = "男" }
            dialog.addItem("女"
            ) { tv_sex.text = "女" }
            dialog.show()
        }
        ll_birthday.setOnClickListener {
            val startDate = intArrayOf(1900, 1, 1, 0, 0, 0)
            val endDate = intArrayOf(Integer.parseInt(DateUtils.getYear()), Integer.parseInt(DateUtils.getMonth()), Integer.parseInt(DateUtils.getDay()), 0, 0, 0)

            PickerUtils.getDefaultDatePickerView(this,
                    startDate, endDate, endDate) { i, i1, i2, i3, i4, _, _ ->
                tv_birthday.text = PickerUtils.changeTime(i, i1, i2, i3, i4, false, "-")
            }.show()
        }
        ll_email.setOnClickListener {
            val intent = Intent(this@EditUserInfoActivity, EditUserItemActivity::class.java)
            intent.putExtra("type", 2)
            intent.putExtra("name", tv_email.text)
            ActivityResult(this@EditUserInfoActivity).startForResult(intent) { resultCode, data ->
                if (resultCode == 0 && null != data && null != data.getStringExtra("name")) {
                    tv_email.text = data.getStringExtra("name")
                }
            }
        }
    }

    private fun save() {
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                .upload(user.accid.toString(),
                        tv_name.text.toString(),
                        et_note.text.toString(),
                        tv_email.text.toString(),
                        tv_birthday.text.toString(),
                        user.mobile.toString(),
                        if("男" == tv_sex.text.toString())"1" else "2")

        builder.setCallBack(observable, object : CallBack<UpLoadUserInfoEntity>() {
            override fun onSuccess(response: UpLoadUserInfoEntity?) {
                if(response!!.code == 200){
                    user.sign = et_note.text.toString()
                    user.birth = tv_birthday.text.toString()
                    user.email = tv_email.text.toString()
                    user.gender = tv_sex.text.toString()
                    user.name = tv_name.text.toString()
                    Preferences.saveUserInfo(Gson().toJson(user))
                    val intent =  Intent()
                    intent.putExtra("head", imagePath)
                    intent.putExtra("name", tv_name.text.toString())
                    intent.putExtra("sign", et_note.text.toString())
                    setResult(0, intent)
                    finish()
                }else{
                    ToastHelper.showToast(this@EditUserInfoActivity, "编辑失败，请稍后再试");
                }
            }
        })
    }
}
