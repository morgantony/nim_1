package com.netease.nim.weyouchats.main.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.bingoogolapple.qrcode.core.BGAQRCodeUtil
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder

import com.bhm.sdk.bhmlibrary.views.TitleBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.google.gson.Gson
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.util.ScreenShotUtils
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.activity_qr.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QRActivity : UI() {

    private var titleBar: TitleBar? = null
    private var iv_qr: ImageView? = null
    private var mDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        initView()
        initEvent()
    }

    private fun initView() {
        titleBar = findView(R.id.titleBar)
        iv_qr = findView(R.id.iv_qr)
        val iv_head = findView<ImageView>(R.id.iv_head)
        val tv_name = findView<TextView>(R.id.tv_name)
        val user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)
        if (null != user) {
            Glide.with(this).load(user.icon).into(iv_head)
            tv_name.text = user.name
            createChineseQRCodeWithLogo("wy://${user.accid}")
        }
        tv_wechat_num.text="微友号:${user.accid}"
    }

    lateinit var bt:Bitmap
    private fun createChineseQRCodeWithLogo(str:String) {
        //异步生成二维码保存在相册，然后ui线程显示二维码
       doAsync {
           val logoBitmap = BitmapFactory.decodeResource(this@QRActivity.resources, R.drawable.ic_logo)
            bt= QRCodeEncoder.syncEncodeQRCode(str, BGAQRCodeUtil.dp2px(this@QRActivity, 150f),
                   Color.parseColor("#f7389fff"),
                   Color.WHITE,
                   logoBitmap)
           uiThread {
               if (bt != null) {
                   iv_qr?.setImageBitmap(bt)
               } else {
                   Toast.makeText(this@QRActivity, "生成带logo的中文二维码失败", Toast.LENGTH_SHORT).show()
               }
           }
       }

    }

    override fun onDestroy() {
        super.onDestroy()
        bt.recycle()
    }

    private fun initEvent() {
        titleBar!!.setLeftOnClickListener { v -> finish() }
        titleBar!!.setRightOnClickListener {
            showDialog()
        }
//        iv_qr!!.setOnClickListener { v ->
//
//        }
    }

    private fun showDialog() {
        if (mDialog == null) {
            initShareDialog()
        }
        mDialog!!.show()
    }

    private fun initShareDialog() {
        mDialog = Dialog(this, R.style.dialog_bottom_full)
        mDialog!!.setCanceledOnTouchOutside(true) //手指触碰到外界取消
        mDialog!!.setCancelable(true)             //可取消 为true
        val window = mDialog!!.window      // 得到dialog的窗体
        window!!.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.share_animation)

        val view = View.inflate(this, R.layout.dialog_qr, null) //获取布局视图
        view.findViewById<View>(R.id.tv_save).setOnClickListener { view1 ->
            //保存二维码
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
            try {
                if (bt == null) {
                    ToastHelper.showToast(this@QRActivity, "获取不到二维码")
                } else {
                    doAsync {
                        ScreenShotUtils.shotScreen(this@QRActivity)
                        uiThread {
                            ToastHelper.showToast(this@QRActivity, "保存成功")
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
        view.findViewById<View>(R.id.tv_sao).setOnClickListener { view1 ->
            //扫描二维码  跳转扫描二维码界面
            if (mDialog != null && mDialog!!.isShowing) {
                startActivity(intentFor<ScanActivity>())
                mDialog!!.dismiss()
            }
        }
//        view.findViewById<View>(R.id.tv_reset).setOnClickListener { view1 ->
//            //重置二维码
//            if (mDialog != null && mDialog!!.isShowing) {
//                mDialog!!.dismiss()
//            }
//        }
        window.setContentView(view)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)//设置横向全屏
    }
}
