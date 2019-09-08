package com.netease.nim.weyouchats.main.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode
import com.baidu.mapapi.model.LatLng
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.imageview.HeadImageView
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.UpdatePositionEntity
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.login.User
import kotlinx.android.synthetic.main.bdmap_activity.*
import org.jetbrains.anko.toast


/**
 * 太坑了，定位必须要二次确认权限 见 judgePermission()
 */
class BdMapActivity : UI() {
    // 定位相关
    var mLocClient: LocationClient? = null
    var myListener = MyLocationListenner()
    private var mCurrentMode: LocationMode? = null

    internal var mMapView: MapView? = null
    var mBaiduMap: BaiduMap? = null

    // UI相关
    internal var isFirstLoc = true// 是否首次定位
    internal var markerView: View? = null// 是否首次定位
    lateinit var user: User
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bdmap_activity)
        fl_fanhui.setOnClickListener {
            finish()
        }

        markerView = LayoutInflater.from(this).inflate(R.layout.location_touxiang, null)//加载布局
        user = Gson().fromJson(Preferences.getUserInfo(), User::class.java)

        val imageView = markerView?.findViewById<HeadImageView>(R.id.hv_location_touxiang)//布局里面的image
        if (user.icon != null) {
//            imageView?.loadAvatar(user.icon)
            Glide.with(this).load(user.icon).into(imageView)
        }
        judgePermission()
        mCurrentMode = LocationMode.NORMAL

        // 地图初始化
        mMapView = findViewById<View>(R.id.bmapView) as MapView
        mBaiduMap = mMapView!!.map
        // 开启定位图层
        mBaiduMap?.isMyLocationEnabled = true

        mBaiduMap?.setOnMarkerClickListener {
            toast("点击头像")
            true
        }

        // 定位初始化
        mLocClient = LocationClient(this)
        mLocClient?.registerLocationListener(myListener)
        val option = LocationClientOption()
        option.isOpenGps = true// 打开gps
        option.setCoorType("bd09ll") // 设置坐标类型
        option.setScanSpan(2000)// 设置发起定位请求的间隔时间为1000ms
        option.setIsNeedAddress(true)
        mLocClient?.locOption = option
        mLocClient?.start()

    }

    private fun getViewBitmap(addViewContent: View): Bitmap {
        addViewContent.isDrawingCacheEnabled = true
        addViewContent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        addViewContent.layout(0, 0, addViewContent.measuredWidth, addViewContent.measuredHeight)
        addViewContent.buildDrawingCache()
        val cacheBitmap = addViewContent.drawingCache
        return Bitmap.createBitmap(cacheBitmap)
    }


    /**
     * 定位SDK监听函数
     */
    inner class MyLocationListenner : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation?) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null)
                return
            val locData = MyLocationData.Builder()
                    .accuracy(location.radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100f).latitude(location.latitude)
                    .longitude(location.longitude).build()
            mBaiduMap?.setMyLocationData(locData)
            val wd = locData.latitude   //纬度
            val jd = locData.longitude  //经度

            val ll = LatLng(wd, jd)
            if (isFirstLoc) {

                isFirstLoc = false

                // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                // 设置缩放比例,更新地图状态
                val f = mBaiduMap?.maxZoomLevel// 19.0
                val u = MapStatusUpdateFactory.newLatLngZoom(ll,
                        f!!.minus(6))    //当前比例尺 500米

                //构建Marker图标
                val bitmap = BitmapDescriptorFactory.fromBitmap(getViewBitmap(markerView!!))
//                val bitmap = BitmapDescriptorFactory.fromResource(R.drawable.red_qipao)
                //构建MarkerOption，用于在地图上添加Marker
                val option = MarkerOptions()
                        .position(ll)
                        .anchor(0.5f, 0.5f)//覆盖物的对齐点，0.5f,0.5f为覆盖物的中心点
                        .icon(bitmap)
                //在地图上添加Marker，并显示
                mBaiduMap?.addOverlay(option)
                mBaiduMap?.animateMapStatus(u)     ////设置地图显示比例尺

                //地图位置显示
                Toast.makeText(this@BdMapActivity, location.addrStr,
                        Toast.LENGTH_SHORT).show()
            } else {
                mBaiduMap?.clear()
//                val bitmap = BitmapDescriptorFactory.fromResource(R.drawable.red_qipao)
                val bitmap = BitmapDescriptorFactory.fromBitmap(getViewBitmap(markerView!!))
                //构建MarkerOption，用于在地图上添加Marker
                val option = MarkerOptions()
                        .position(ll)
                        .anchor(0.5f, 1f)//覆盖物的对齐点，0.5f,0.5f为覆盖物的中心点
                        .icon(bitmap)
                //在地图上添加Marker，并显示
                mBaiduMap?.addOverlay(option)
            }

        }

        fun onReceivePoi(poiLocation: BDLocation) {}
    }

    override fun onPause() {
        mMapView!!.onPause()
        super.onPause()
    }

    override fun onResume() {
        mMapView!!.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        // 退出时销毁定位
        mLocClient?.stop()
        // 关闭定位图层
        mBaiduMap?.isMyLocationEnabled = false
        mMapView!!.onDestroy()
        mMapView = null
        super.onDestroy()
    }


    //6.0之后要动态获取权限，重要！！！
    protected fun judgePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

            // sd卡权限
            val SdCardPermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, SdCardPermission, 100)
            }

            //手机状态权限
            val readPhoneStatePermission = arrayOf(Manifest.permission.READ_PHONE_STATE)
            if (ContextCompat.checkSelfPermission(this, readPhoneStatePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, readPhoneStatePermission, 200)
            }

            //定位权限
            val locationPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (ContextCompat.checkSelfPermission(this, locationPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, locationPermission, 300)
            }

            val ACCESS_COARSE_LOCATION = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, ACCESS_COARSE_LOCATION, 400)
            }


            val READ_EXTERNAL_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, READ_EXTERNAL_STORAGE, 500)
            }

            val WRITE_EXTERNAL_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_STORAGE, 600)
            }

        } else {
            //doSdCardResult();
        }
        //LocationClient.reStart();
    }


}
