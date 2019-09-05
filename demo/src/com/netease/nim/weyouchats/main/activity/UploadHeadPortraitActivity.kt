package com.netease.nim.weyouchats.main.activity

import android.content.Intent
import android.os.Bundle
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
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.UpLoadUserInfoEntity
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.activity_edit_info.*
import kotlinx.android.synthetic.main.activity_upload_head.*
import kotlinx.android.synthetic.main.activity_upload_head.titleBar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class UploadHeadPortraitActivity : UI() {

    private var imagePath: String = ""
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_head)
        initView()
        initEvent()
    }

    private fun initView() {
        user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        Glide.with(this).load(user.icon).into(iv_head!!)
    }

    private fun initEvent() {
        titleBar!!.setLeftOnClickListener { v -> finish() }

        if (!intent.getBooleanExtra("edit", false)) {
            titleBar!!.setRightText("")
        }
        titleBar!!.setRightOnClickListener { v ->
            if (!intent.getBooleanExtra("edit", false)) {
                return@setRightOnClickListener
            }
            ImagePickerLauncher.pickImage(this@UploadHeadPortraitActivity, REQUEST_PICK_ICON, R.string.head)
        }
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
            save()
        }
    }

    private fun save() {
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(true, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val headFile = File(imagePath)
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg; charset=UTF-8"), headFile)
        val part = MultipartBody.Part.createFormData("file", headFile.name, requestBody)//key(file)与服务器一致
        val observable = builder.createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                .upload(user.accid.toString(), part)

        builder.setCallBack(observable, object : CallBack<UpLoadUserInfoEntity>() {
            override fun onSuccess(response: UpLoadUserInfoEntity?) {
                if(response!!.code == 200){
                    user.icon = imagePath
                    Preferences.saveUserInfo(Gson().toJson(user))
                    Glide.with(this@UploadHeadPortraitActivity).load(imagePath).into(hv_robot)
                }else{
                    imagePath = ""
                    ToastHelper.showToast(this@UploadHeadPortraitActivity, "编辑失败，请稍后再试");
                }
            }

            override fun onFail(e: Throwable?) {
                super.onFail(e)
                imagePath = ""
            }
        })
    }

    companion object {

        private const val REQUEST_PICK_ICON = 104
    }

    override fun finish() {
        val intent =  Intent()
        intent.putExtra("head", imagePath)
        setResult(0, intent)
        super.finish()
    }
}
