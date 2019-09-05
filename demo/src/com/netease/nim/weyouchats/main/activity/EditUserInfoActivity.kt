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
import com.google.gson.Gson
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.media.imagepicker.Constants
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher
import com.netease.nim.uikit.common.media.model.GLImage
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity
import com.netease.nim.weyouchats.common.util.PickerUtils
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.activity_edit_info.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

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
        hv_robot.loadAvatar(user.icon)
        tv_name.text = if (user.name == null) "" else user.name
        tv_sex.text = if (user.gender == null) "" else user.gender
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
        ll_qr.setOnClickListener { v -> startActivity(Intent(this@EditUserInfoActivity, QRActivity::class.java)) }
        if (!intent.getBooleanExtra("edit", false)) {
            titleBar.setRightText("")
            et_note.isEnabled = false
            return
        }
        titleBar.setTitleText("编辑个人信息")
        titleBar.setRightOnClickListener { save() }
        ll_head.setOnClickListener { ImagePickerLauncher.pickImage(this@EditUserInfoActivity, REQUEST_PICK_ICON, R.string.head) }
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
        val params = HashMap<String, String>()
        params["accid"] = user.accid.toString()
        if(!TextUtils.isEmpty(tv_name.text.toString())) {
            params["name"] = tv_name.text.toString()
        }
        if(!TextUtils.isEmpty(et_note.text.toString())) {
            params["sign"] = et_note.text.toString()
        }
        if(!TextUtils.isEmpty(tv_email.text.toString())) {
            params["email"] = tv_email.text.toString()
        }
        if(!TextUtils.isEmpty(tv_birthday.text.toString())) {
            params["birth"] = tv_birthday.text.toString()
        }
        if(!TextUtils.isEmpty(user.mobile.toString())) {
            params["mobile"] = user.mobile.toString()//手机号暂时不能修改
        }
        if(!TextUtils.isEmpty(tv_sex.text.toString())) {
            params["gender"] = tv_sex.text.toString()
        }
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val headFile = File(imagePath)
        val observable = if(headFile.isFile){
            val requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"), headFile)
            val part = MultipartBody.Part.createFormData("file", headFile.name, requestBody)//key(file)与服务器一致
            builder
                    .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                    .upload(params, part)
        }else{
            builder
                    .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                    .upload(params)
        }

        builder.setCallBack(observable, object : CallBack<UpLoadUserInfoEntity>() {
            override fun onSuccess(response: UpLoadUserInfoEntity?) {
                if(response!!.code == 200){
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_ICON) {
            if (data == null) {
                return
            }
            val images = data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS) as ArrayList<*>
            if (images.isEmpty()) {
                return
            }
            val image = images[0] as GLImage
            imagePath = image.path
            Glide.with(this).load(image.path).into(hv_robot)
        }
    }

    companion object {

        private const val REQUEST_PICK_ICON = 104
    }
}
