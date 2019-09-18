package com.netease.nim.weyouchats.main.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.bhm.sdk.bhmlibrary.views.TitleBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.netease.nim.uikit.business.session.actions.PickImageAction
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.media.imagepicker.Constants
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher
import com.netease.nim.uikit.common.media.model.GLImage
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.uikit.common.ui.imageview.HeadImageView
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.contact.ContactHttpClient
import com.netease.nim.weyouchats.contact.helper.UserUpdateHelper
import com.netease.nim.weyouchats.login.User
import com.netease.nimlib.sdk.AbortableFuture
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.nos.NosService
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum
import java.io.File
import java.util.*

class UploadHeadPortraitActivity : UI() {
    private var imagePath: String? = ""
    private var user: User? = null
    private var uploadAvatarFuture: AbortableFuture<String>? = null

    private var titleBar: TitleBar? = null
    private var iv_head: ImageView? = null

    private val outimeTask = { cancelUpload(R.string.user_info_update_failed) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_head)
        initView()
        initEvent()
    }

    private fun initView() {
        iv_head = findView(R.id.iv_head)
        titleBar = findView(R.id.titleBar)
        user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        imagePath = user!!.icon
        Glide.with(this).load(user!!.icon).into(iv_head!!)
    }

    private fun initEvent() {
        titleBar!!.setLeftOnClickListener {  finish() }

        if (!intent.getBooleanExtra("edit", false)) {
            titleBar!!.setRightText("")
        }
        titleBar!!.setRightOnClickListener {
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
            val images = data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS) as ArrayList<GLImage>
            if (images == null || images.isEmpty()) {
                return
            }
            val image = images[0]
            imagePath = image.path
            save()
        }
    }

    private fun save() {
        val file = File(imagePath!!)
        //不可取消请求（物理返回键或者触摸屏幕）
        DialogMaker.showProgressDialog(this, null, null, false) { cancelUpload(R.string.user_info_update_cancel) }.setCanceledOnTouchOutside(false)
        //30s后关闭dialog取消请求
        Handler().postDelayed(outimeTask, 30000)

        uploadAvatarFuture = NIMClient.getService(NosService::class.java).upload(file, PickImageAction.MIME_JPEG)
        uploadAvatarFuture?.setCallback(object : RequestCallbackWrapper<String>() {
            override fun onResult(code: Int, url: String, exception: Throwable?) {
                if (code == ResponseCode.RES_SUCCESS.toInt() && !TextUtils.isEmpty(url)) {
                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, object : RequestCallbackWrapper<Void>() {
                        override fun onResult(code: Int, result: Void?, exception: Throwable?) {
                            if (code == ResponseCode.RES_SUCCESS.toInt()) {
                                user!!.icon = url
                                Preferences.saveUserInfo(Gson().toJson(user))

//                                Glide.with(this@UploadHeadPortraitActivity).load(url).into(iv_head!!)
                                /**
                                 * 监听图片加载成功或失败
                                 */
                                Glide.with(this@UploadHeadPortraitActivity).load(url).into(object : SimpleTarget<Drawable>() {
                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        super.onLoadFailed(errorDrawable)
                                        iv_head?.setBackgroundColor(Color.parseColor("#F5F9FA"))
                                    }
                                    override fun onResourceReady(resource: Drawable?, transition: Transition<in Drawable>?) {
                                        iv_head?.setImageDrawable(resource)
                                        runOnUiThread {
                                            ToastHelper.showToast(this@UploadHeadPortraitActivity, R.string.head_update_success)
                                            //通过云信上传头像成功后通知后台同步云信的头像到服务器
                                            updateUserinfoByYx()
                                            onUpdateDone()
                                        }
                                    }
                                })

                            } else {
                                imagePath = ""
                                ToastHelper.showToast(this@UploadHeadPortraitActivity, R.string.head_update_failed)
                            }
                        }
                    }) // 更新资料
                } else {
                    imagePath = ""
                    ToastHelper.showToast(this@UploadHeadPortraitActivity, R.string.user_info_update_failed)
                    onUpdateDone()
                }
            }
        })
    }

    private fun updateUserinfoByYx() {
        val token = Preferences.getUserToken()
        ContactHttpClient.getInstance().updateUserinfoByYx(token, object : ContactHttpClient.ContactHttpCallback<User> {
            override fun onSuccess(user: User?) {
                Log.e("999999", "服务器同步云信成功")
            }

            override fun onFailed(code: Int, errorMsg: String?) {
                Log.e("999999", "服务器同步云信失败==code==errorMsg$code$errorMsg")
            }
        })
    }

    private fun cancelUpload(resId: Int) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture!!.abort()
            ToastHelper.showToast(this@UploadHeadPortraitActivity, resId)
            onUpdateDone()
        }
    }

    private fun onUpdateDone() {
        uploadAvatarFuture = null
        DialogMaker.dismissProgressDialog()
    }

    override fun finish() {
        val intent = Intent()
        intent.putExtra("head", imagePath)
        setResult(0, intent)
        super.finish()
    }

    companion object {

        private val REQUEST_PICK_ICON = 104
    }
}
