package com.netease.nim.weyouchats.contact.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.business.session.constant.RequestCode
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.media.imagepicker.Constants
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher.selectImage
import com.netease.nim.uikit.common.media.imagepicker.option.DefaultImagePickerOption
import com.netease.nim.uikit.common.media.imagepicker.option.ImagePickerOption
import com.netease.nim.uikit.common.media.model.GLImage
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.config.DemoServers
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.contact.adapter.BxDialog
import com.netease.nim.weyouchats.contact.adapter.NoDoubleClickListener
import com.netease.nim.weyouchats.contact.adapter.PostAdapter
import com.netease.nim.weyouchats.main.model.Extras
import kotlinx.android.synthetic.main.black_list_item.*
import kotlinx.android.synthetic.main.jubao_submit.*
import kotlinx.android.synthetic.main.jubao_tijiao_right_click.*
import okhttp3.*
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 投诉提交界面
 */
class TouShuSubmitActivity : UI() {
    var intentStr = ""
    var otheraccount = ""
    private var mAdapter: PostAdapter? = null
    private var picList = ArrayList<GLImage>()  //保留五张图片，最终用于上传后台的集合
    private val PICK_IMAGE_COUNT = 5
    private var mSelectPath: ArrayList<GLImage>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jubao_submit)

        val options = NimToolBarOptions()
        options.titleId = R.string.jubao_sub
        setToolBar(R.id.toolbar, options)
        action_bar_right_clickable_textview.text = "提交"
        intentStr = intent.getStringExtra("type")
        otheraccount=intent.getStringExtra("account")
        tv_type.text = intentStr
        initEdit()
        //初始化recycleview
        initRecyclerView()
        initData()

        mAdapter?.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val pic = adapter.data[position] as GLImage
            if (pic.path == "-1") {
                //选择照片
                showSelector(RequestCode.PICK_IMAGE, true)

            } //加号按钮
            else {  //删除
                initDialog(position)
                if (!bxDialog?.isShowing!!) {  //防止重复弹出dialog
                    bxDialog?.show()
                }
            }
        }

    }

    /**
     * 打开图片选择器
     */
    private fun showSelector(requestCode: Int, multiSelect: Boolean) {
        val option = DefaultImagePickerOption.getInstance().setShowCamera(true).setPickType(
                ImagePickerOption.PickType.Image).setMultiMode(multiSelect).setSelectMax(PICK_IMAGE_COUNT)
        selectImage(this, requestCode, option)
    }

    private var bxDialog: BxDialog? = null
    private fun initDialog(pos: Int) {
        if (null == bxDialog) {
            bxDialog = BxDialog(this, R.style.myDialog).loadLayout(R.layout.dialog_layout)
            bxDialog?.setGCCanceledOnTouchOutside(true)
        }
        //是
        bxDialog?.setOnClickListener(R.id.btn_sure) {
            bxDialog?.dismiss()
            picList.removeAt(pos)
            val pic = GLImage("-1")
            if (!picList.contains(pic)) {
                picList.add(pic)
            }
            mAdapter?.notifyDataSetChanged()
        }
        //否
        bxDialog?.setOnClickListener(R.id.btn_cancel) {
            bxDialog?.dismiss()

        }

//        bxDialog?.setOnKeyListener(DialogUtils.keylistener)  //禁用物理返回键关闭dialog

    }

    private fun initEdit() {
        content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val str = s.toString()
                if (TextUtils.isEmpty(str)) {  //字符串判空
                    action_bar_right_clickable_textview.setTextColor(Color.parseColor("#80DCDCDC"))
                    action_bar_right_clickable_textview.setOnClickListener(null)
                } else {
                    action_bar_right_clickable_textview.setTextColor(ContextCompat.getColor(this@TouShuSubmitActivity, R.color.white))
                    action_bar_right_clickable_textview!!.setOnClickListener(object : NoDoubleClickListener() {
                        override fun onNoDoubleClick(v: View?) {
                            //关闭软键盘
                            closeKeyBord()
                            if(intentStr==""){
                                toast("请选择投诉类型")
                                return
                            }
                            if(content.text.toString().isEmpty()){
                                toast("请填写投诉内容")
                                return
                            }
                            //提交
                            uploadImage()
                        }
                    })
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

    }

        /**
         * 上传图片
         */
        @Throws(IOException::class, JSONException::class)
        fun uploadImage() {
            DialogMaker.showProgressDialog(this, "提交投诉中...", false)
            val MEDIA_TYPE_PNG = MediaType.parse("image/png")
            val multiBuilder = MultipartBody.Builder()
            //参数以添加header方式将参数封装，否则上传参数为空
            // 设置请求体
            multiBuilder.setType(MultipartBody.FORM)
            //这里是 封装上传图片参数
            if (picList.size > 0) {
                picList.forEachIndexed { index, glImage ->
                    if (glImage.path!="-1"){
                        val file = File(glImage.path)
                        multiBuilder.addFormDataPart("file$index",file.name,RequestBody.create(MEDIA_TYPE_PNG,file))
                    }
                }
            }else{
                toast("请至少选择一张图片")
                return
            }

            // 封装请求参数,这里最重要
            val params = HashMap<String, String>()
            //        params.put("client","Android");
            params.put("token", Preferences.getUserToken())
            params.put("bid", otheraccount)
            params.put("type", intentStr)
            params.put("text", content.text.toString())
            params.put("file", "avatarName.jpg")
            //参数以添加header方式将参数封装，否则上传参数为空
            if (!params.isEmpty()) {
                for (key in params.keys) {
                    multiBuilder.addPart(
                            Headers.of("Content-Disposition", "form-data; name=\"$key\""),
                            RequestBody.create(null, params[key]))
                }
            }
            val multiBody = multiBuilder.build()

            val sdcache:File = externalCacheDir
            val cacheSize:Long = 10 * 1024 * 1024
            val okHttpClient = OkHttpClient()
            okHttpClient.newBuilder().connectTimeout(15, TimeUnit.SECONDS)   //设置超时时间
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache( Cache(sdcache.absoluteFile, cacheSize))

            val request = Request.Builder().url(DemoServers.API_COUSMER + "user/report").post(multiBody).build()
            val call = okHttpClient.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //请求失败的处理
                    Log.e("sdfasdfadf", "失败请求")
                    runOnUiThread {
                        DialogMaker.dismissProgressDialog()
                    }

                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.e("sdfasdfadf", "成功")
                    runOnUiThread {
                        DialogMaker.dismissProgressDialog()
                    }
                    //投诉成功，跳回个人名片
                    val intent = Intent(this@TouShuSubmitActivity, UserProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP//关掉所要到的界面中间的activity
                    //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)//设置不要刷新将要跳转的界面
                    intent.putExtra(Extras.EXTRA_ACCOUNT,otheraccount)
                    startActivity(intent)
                }
            })
        }

    /**
     * 关闭软键盘
     */
    private fun closeKeyBord() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(content.windowToken, 0)
    }

    private fun initData() {
        val pic = GLImage("-1") //-1作为标识用来加载加号按钮
        picList.add(pic)
    }

    private fun initRecyclerView() {
        mAdapter = PostAdapter(R.layout.adapter_pic_post, picList)
        recycleView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                onPickImageActivityResult(requestCode, data)
//                mSelectPath = data!!.getStringArrayListExtra(MultiImageSelector.EXTRA_RESULT)
//                if (mSelectPath == null) return
//                //先清除掉集合中的加号按钮 后面再添加加号按钮
//                picList.forEachIndexed { index, pic ->
//                    if (pic.picStr == "-1") picList.remove(pic)
//                }
//                labelA@ for (p in mSelectPath!!) {
//                    //遍历mSelectPath，如果picStr里已经有了重复数据，不添加进picStr。
//                    labelB@ for (m in picList) {
//                        if (p == m.picStr) {
//                            continue@labelA     //直接返回labelA进行下一次循环，不执行后面代码
//                        }
//                    }
//
//                    val pic = Pic()
//                    pic.picStr = p
//                    picList.add(pic)
//                }
//                if (picList.size <= 8) {
//                    val pic = Pic()
//                    pic.picStr = "-1"  //-1作为标识用来加载加号按钮
//                    picList.add(pic)
//                }
//                recycleView.adapter?.notifyDataSetChanged()
            }
        }
    }

    /**
     * 图片选取回调
     */
    private fun onPickImageActivityResult(requestCode: Int, data: Intent?) {
        if (data == null) {
            ToastHelper.showToastLong(this, com.netease.nim.uikit.R.string.picker_image_error)
            return
        }
        sendImageAfterSelfImagePicker(data)
    }

    /**
     * 获取图片路径
     */
    private fun sendImageAfterSelfImagePicker(data: Intent) {
        mSelectPath = data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS) as ArrayList<GLImage>
        if (mSelectPath == null) return
        //先清除掉集合中的加号按钮 后面再添加加号按钮
        picList.forEachIndexed { index, pic ->
            if (pic.path == "-1") picList.remove(pic)
        }
        labelA@ for (p in mSelectPath!!) {
            //遍历mSelectPath，如果picStr里已经有了重复数据，不添加进picStr。
            labelB@ for (m in picList) {
                if (p.path == m.path) {
                    continue@labelA     //直接返回labelA进行下一次循环，不执行后面代码
                }
            }

            val pic = GLImage(p.path)
            picList.add(pic)
            if (picList.size==5) break@labelA    //最多上传五张图片，超过五张，结束循环
        }
        if (picList.size <= 4) {
            val pic = GLImage("-1")//-1作为标识用来加载加号按钮
            picList.add(pic)
        }
        recycleView.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogMaker.dismissProgressDialog()
    }
}