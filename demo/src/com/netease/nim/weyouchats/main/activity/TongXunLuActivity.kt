package com.netease.nim.weyouchats.main.activity

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.contact.ContactHttpClient
import com.netease.nim.weyouchats.main.adapter.TongXunLuAdapter
import com.netease.nim.weyouchats.main.model.TongXunLu
import com.netease.nim.weyouchats.main.model.txl
import kotlinx.android.synthetic.main.activity_tongxunlu.*
import org.jetbrains.anko.doAsync
import java.util.*

/**
 * 通讯录界面
 */
class TongXunLuActivity : UI() {

    private var list: RecyclerView? = null
    private var arrayAdapter: TongXunLuAdapter? = null

    private val con = ArrayList<TongXunLu>()  //download

    private val contxl = ArrayList<txl>()  //upload

    private var layoutManager: RecyclerView.LayoutManager? = null

    lateinit var loaderManager: LoaderManager

    //callBack 实现
    private val mLoaderCallback = object : LoaderManager.LoaderCallbacks<Cursor> {

        private val Contact_PROJECTION = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {  //可为空的参数  记得加问号Bundle?
            return CursorLoader(this@TongXunLuActivity,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, Contact_PROJECTION, null, null, null)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {

            //首先要清空数据源，避免重复数据
            contxl.clear()
            while (data.moveToNext()) {
                val displayName = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val t=txl()
                t.mobileName=displayName
                t.mobile=number
                contxl.add(t)
            }
            data.close()
            loaderManager.destroyLoader(0)
            //当前子线程，切回主线程。不然物理返回键后 ANR
            runOnUiThread {
                uploadTxl()
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            contxl.clear()
        }

    }

    private fun uploadTxl() {
        DialogMaker.showProgressDialog(this, null, false)
        val token = Preferences.getUserToken()
       val jsonArray = Gson().toJson(contxl, object : TypeToken<ArrayList<txl>>() {}.type)

        ContactHttpClient.getInstance().uploadAddressBook(jsonArray,token, object : ContactHttpClient.ContactHttpCallback<List<TongXunLu>> {
            override fun onSuccess(user: List<TongXunLu>) {
                DialogMaker.dismissProgressDialog()
                if (user.isNullOrEmpty()) {
//                    EasyAlertDialogHelper.showOneButtonDiolag(this@TongXunLuActivity, R.string.user_not_exsit,
//                            R.string.user_tips, R.string.ok, false, null)
                } else {
                    con.clear()
                    con.addAll(user)
                    if (con.isEmpty()){
                        ll_img.visibility= View.VISIBLE
                        list?.visibility=View.GONE
                    }else{
                        ll_img.visibility= View.GONE
                        list?.visibility=View.VISIBLE
                        arrayAdapter?.notifyDataSetChanged()
                    }
                    //展示列表
//                    models.clear()
//                    models.addAll(user)
//                    mAdapter.notifyDataSetChanged()
                    //                    UserProfileActivity.start(AddFriendActivity.this, account);
                }
            }

            override fun onFailed(code: Int, errorMsg: String) {
                DialogMaker.dismissProgressDialog()
                ToastHelper.showToast(this@TongXunLuActivity, "on failed:$code")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tongxunlu)

        list = findViewById(R.id.contact_list)
        layoutManager = LinearLayoutManager(this)
        list!!.layoutManager = layoutManager
        list!!.setHasFixedSize(true)
        arrayAdapter = TongXunLuAdapter(this, con, R.layout.item_tongxunlu)
        list!!.adapter = arrayAdapter

        //获取LoaderManager
        loaderManager = supportLoaderManager
        //动态申请权限
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CONTACTS), 0)
        } else {
            loaderManager.initLoader(0, null, mLoaderCallback)
        }

        val options = NimToolBarOptions()
        options.titleString = "通讯录"
        setToolBar(R.id.toolbar, options)

    }

    override fun onResume() {
        super.onResume()
        //loaderManager.initLoader(0,null,mLoaderCallback);
    }

    override fun onDestroy() {
        super.onDestroy()
        loaderManager.destroyLoader(0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e("LoaderManager", "====onSaveInstanceState")
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loaderManager.initLoader(0, null, mLoaderCallback)
            }
        }
    }

}
