package com.netease.nim.weyouchats.main.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.bingoogolapple.qrcode.core.BarcodeType
import cn.bingoogolapple.qrcode.core.QRCodeView
import cn.bingoogolapple.qrcode.zxing.ZXingView
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.media.imagepicker.Constants
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher
import com.netease.nim.uikit.common.media.imagepicker.option.DefaultImagePickerOption
import com.netease.nim.uikit.common.media.imagepicker.option.ImagePickerOption
import com.netease.nim.uikit.common.media.model.GLImage
import com.netease.nim.weyouchats.DemoCache
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.contact.activity.UserProfileActivity
import kotlinx.android.synthetic.main.activity_bga_scan.*
import org.jetbrains.anko.intentFor
import java.util.*

class ScanActivity : UI(), QRCodeView.Delegate {

    private var mZXingView: ZXingView? = null
    private var mDialog: Dialog? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bga_scan)
        titleBar.setLeftOnClickListener { v -> finish() }
        titleBar.setRightOnClickListener {
            showDialog()
        }
        mZXingView = findViewById(R.id.zxingview)
        mZXingView?.setDelegate(this)
    }

    override fun onStart() {
        super.onStart()

        mZXingView?.startCamera() // 打开后置摄像头开始预览，但是并未开始识别
        //        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
        mZXingView?.setType(BarcodeType.ALL, null) // 识别所有类型的码
        mZXingView?.scanBoxView?.isOnlyDecodeScanBoxArea = false // 识别整个屏幕中的码

        mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
    }

    override fun onStop() {
        mZXingView?.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        mZXingView?.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    //接收扫码成功的回调
    override fun onScanQRCodeSuccess(result: String?) {
        Log.e("999999", "result:$result")
        vibrate()
        mZXingView?.startSpot() // 开始识别
        result?.let {

            when {
                result.startsWith("http") -> {
                    val intent = Intent()
                    intent.data = Uri.parse(result)
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.action = Intent.ACTION_VIEW
                    startActivity(intent)
                }
                result.startsWith("wy://") -> {
                    //打开个人信息页面
                    UserProfileActivity.start(this, result.split("//")[1])
                }
                else -> startActivity(intentFor<BlankActivity>("result" to result))
            }

        }
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        var tipText = mZXingView?.scanBoxView?.tipText?:""
        val ambientBrightnessTip = "\n环境过暗，请打开闪光灯"
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView?.scanBoxView?.tipText = tipText + ambientBrightnessTip
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                mZXingView?.scanBoxView?.tipText = tipText
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错")
    }

    /**
     * 当不选择照片直接返回扫一扫 data: Intent? 会为空  加问号
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            if (data == null) {
                return
            }
            val images = data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS) as ArrayList<GLImage>
            if (images == null || images.isEmpty()) {
                return
            }
            val image = images[0]
            val picturePath =image.path
            Log.e("999999","$picturePath")

            mZXingView?.decodeQRCode(picturePath)

        }
    }

    companion object {
        private val TAG = ScanActivity::class.java.simpleName
        private val REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666
    }

    private fun showDialog() {
        if (mDialog == null) {
            initShareDialog()
        }
        mDialog?.show()
    }


    private fun initShareDialog() {
        mDialog = Dialog(this, R.style.dialog_bottom_full)
        mDialog?.setCanceledOnTouchOutside(true) //手指触碰到外界取消
        mDialog?.setCancelable(true)             //可取消 为true
        val window = mDialog?.window      // 得到dialog的窗体
        window!!.setGravity(Gravity.BOTTOM)
        window.setWindowAnimations(R.style.share_animation)

        val view = View.inflate(this, R.layout.dialog_locapic_flash, null) //获取布局视图
        view.findViewById<View>(R.id.choose_qrcde_from_gallery).setOnClickListener { view1 ->
           //选择相册二维码
            selectImageFromAlbum(this, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY)
        }

        view.findViewById<View>(R.id.open_flashlight).setOnClickListener { view1 ->
            //开灯
            if (view.findViewById<TextView>(R.id.open_flashlight).text.toString()=="打开闪光灯") {
                view.findViewById<TextView>(R.id.open_flashlight).text="关闭闪光灯"
                mZXingView?.openFlashlight() // 打开闪光灯
            } else {
                view.findViewById<TextView>(R.id.open_flashlight).text="打开闪光灯"
                mZXingView?.closeFlashlight() // 关闭闪光灯
            }
        }
        window.setContentView(view)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)//设置横向全屏
    }

    protected fun selectImageFromAlbum(activity: Activity, requestCode: Int) {
        val option = DefaultImagePickerOption.getInstance().setCrop(false)
        option.setPickType(ImagePickerOption.PickType.Image).setMultiMode(false).setSelectMax(1).isShowCamera = false
        ImagePickerLauncher.selectImage(activity, requestCode, option)
    }


//    fun onClick(v: View) {
//        when (v.id) {
//            R.id.start_preview -> mZXingView?.startCamera() // 打开后置摄像头开始预览，但是并未开始识别
//            R.id.stop_preview -> mZXingView?.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
//            R.id.start_spot -> mZXingView?.startSpot() // 开始识别
//            R.id.stop_spot -> mZXingView?.stopSpot() // 停止识别
//            R.id.start_spot_showrect -> mZXingView?.startSpotAndShowRect() // 显示扫描框，并且开始识别
//            R.id.stop_spot_hiddenrect -> mZXingView?.stopSpotAndHiddenRect() // 停止识别，并且隐藏扫描框
//            R.id.show_scan_rect -> mZXingView?.showScanRect() // 显示扫描框
//            R.id.hidden_scan_rect -> mZXingView?.hiddenScanRect() // 隐藏扫描框
//            R.id.decode_scan_box_area -> mZXingView?.scanBoxView.isOnlyDecodeScanBoxArea = true // 仅识别扫描框中的码
//            R.id.decode_full_screen_area -> mZXingView?.scanBoxView.isOnlyDecodeScanBoxArea = false // 识别整个屏幕中的码
//            R.id.open_flashlight -> {
//                if (open_flashlight.text.toString()=="打开闪光灯") {
//                    mZXingView?.openFlashlight() // 打开闪光灯
//                    open_flashlight.text="关闭闪光灯"
//                } else {
//                    mZXingView?.closeFlashlight() // 关闭闪光灯
//                    open_flashlight.text="打开闪光灯"
//                }
//            }
//            R.id.close_flashlight -> mZXingView?.closeFlashlight() // 关闭闪光灯
//            R.id.scan_one_dimension -> {
//                mZXingView?.changeToScanBarcodeStyle() // 切换成扫描条码样式
//                mZXingView?.setType(BarcodeType.ONE_DIMENSION, null) // 只识别一维条码
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_two_dimension -> {
//                mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
//                mZXingView?.setType(BarcodeType.TWO_DIMENSION, null) // 只识别二维条码
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_qr_code -> {
//                mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
//                mZXingView?.setType(BarcodeType.ONLY_QR_CODE, null) // 只识别 QR_CODE
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_code128 -> {
//                mZXingView?.changeToScanBarcodeStyle() // 切换成扫描条码样式
//                mZXingView?.setType(BarcodeType.ONLY_CODE_128, null) // 只识别 CODE_128
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_ean13 -> {
//                mZXingView?.changeToScanBarcodeStyle() // 切换成扫描条码样式
//                mZXingView?.setType(BarcodeType.ONLY_EAN_13, null) // 只识别 EAN_13
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_high_frequency -> {
//                mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
//                mZXingView?.setType(BarcodeType.HIGH_FREQUENCY, null) // 只识别高频率格式，包括 QR_CODE、UPC_A、EAN_13、CODE_128
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_all -> {
//                mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
//                mZXingView?.setType(BarcodeType.ALL, null) // 识别所有类型的码
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.scan_custom -> {
//                mZXingView?.changeToScanQRCodeStyle() // 切换成扫描二维码样式
//
//                val hintMap = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
//                val formatList = ArrayList<BarcodeFormat>()
//                formatList.add(BarcodeFormat.QR_CODE)
//                formatList.add(BarcodeFormat.UPC_A)
//                formatList.add(BarcodeFormat.EAN_13)
//                formatList.add(BarcodeFormat.CODE_128)
//                hintMap[DecodeHintType.POSSIBLE_FORMATS] = formatList // 可能的编码格式
//                hintMap[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE // 花更多的时间用于寻找图上的编码，优化准确性，但不优化速度
//                hintMap[DecodeHintType.CHARACTER_SET] = "utf-8" // 编码字符集
//                mZXingView?.setType(BarcodeType.CUSTOM, hintMap) // 自定义识别的类型
//
//                mZXingView?.startSpotAndShowRect() // 显示扫描框，并开始识别
//            }
//            R.id.choose_qrcde_from_gallery -> {
//                /*
//                从相册选取二维码图片，这里为了方便演示，使用的是
//                https://github.com/bingoogolapple/BGAPhotoPicker-Android
//                这个库来从图库中选择二维码图片，这个库不是必须的，你也可以通过自己的方式从图库中选择图片
//                 */
//                val photoPickerIntent = BGAPhotoPickerActivity.IntentBuilder(this)
//                        .cameraFileDir(null)
//                        .maxChooseCount(1)
//                        .selectedPhotos(null)
//                        .pauseOnScroll(false)
//                        .build()
//                startActivityForResult(photoPickerIntent, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY)
//            }
//        }
//    }

    //拿到图片路径，返回bitmap
    fun getDecodeAbleBitmap(picturePath: String): Bitmap? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(picturePath, options)
            var sampleSize = options.outHeight / 400
            if (sampleSize <= 0) {
                sampleSize = 1
            }
            options.inSampleSize = sampleSize
            options.inJustDecodeBounds = false

            return BitmapFactory.decodeFile(picturePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

}