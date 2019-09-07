package com.netease.nim.weyouchats.main.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import android.widget.ToggleButton

import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptor
import com.baidu.mapapi.map.MapStatusUpdate
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.bdmap_activity.*

class BdMapActivity : UI() {
    // 定位相关
     var mLocClient: LocationClient?=null
    var myListener = MyLocationListenner()
    private var mCurrentMode: LocationMode? = null
    internal var mCurrentMarker: BitmapDescriptor? = null

    internal var mMapView: MapView? = null
     var mBaiduMap: BaiduMap?=null

    // UI相关
    internal var radioButtonListener: OnCheckedChangeListener? = null
    internal var requestLocButton: Button? = null
    internal var togglebtn: ToggleButton? = null
    internal var isFirstLoc = true// 是否首次定位

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SDKInitializer.initialize(applicationContext)
        setContentView(R.layout.bdmap_activity)
        fl_fanhui.setOnClickListener {
            finish()
        }
        judgePermission()
        //        requestLocButton = (Button) findViewById(R.id.button1);
        mCurrentMode = LocationMode.NORMAL
        //        requestLocButton.setText("普通");
        //        OnClickListener btnClickListener = new OnClickListener() {
        //            public void onClick(View v) {
        //                switch (mCurrentMode) {
        //                    case NORMAL:
        //                        requestLocButton.setText("跟随");
        //                        mCurrentMode = LocationMode.FOLLOWING;
        //                        mBaiduMap
        //                                .setMyLocationConfigeration(new MyLocationConfiguration(
        //                                        mCurrentMode, true, mCurrentMarker));
        //                        break;
        //                    case COMPASS:
        //                        requestLocButton.setText("普通");
        //                        mCurrentMode = LocationMode.NORMAL;
        //                        mBaiduMap
        //                                .setMyLocationConfigeration(new MyLocationConfiguration(
        //                                        mCurrentMode, true, mCurrentMarker));
        //                        break;
        //                    case FOLLOWING:
        //                        requestLocButton.setText("罗盘");
        //                        mCurrentMode = LocationMode.COMPASS;
        //                        mBaiduMap
        //                                .setMyLocationConfigeration(new MyLocationConfiguration(
        //                                        mCurrentMode, true, mCurrentMarker));
        //                        break;
        //                }
        //            }
        //        };
        //        requestLocButton.setOnClickListener(btnClickListener);
        //
        //        togglebtn = (ToggleButton) findViewById(R.id.togglebutton);
        //        togglebtn
        //                .setOnCheckedChangeListener((buttonView, isChecked) -> {
        //                    if (isChecked) {
        //                        // 普通地图
        //                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //                    } else {
        //                        // 卫星地图
        //                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //                    }
        //
        //                });

        // 地图初始化
        mMapView = findViewById<View>(R.id.bmapView) as MapView
        mBaiduMap = mMapView!!.map
        // 开启定位图层
        mBaiduMap?.isMyLocationEnabled = true
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
            if (isFirstLoc) {
                isFirstLoc = false
                val ll = LatLng(location.latitude,
                        location.longitude)
                // MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                // 设置缩放比例,更新地图状态
                val f = mBaiduMap?.maxZoomLevel// 19.0
                val u = MapStatusUpdateFactory.newLatLngZoom(ll,
                        f!!.minus(2))
                mBaiduMap?.animateMapStatus(u)
                //地图位置显示
                Toast.makeText(this@BdMapActivity, location.addrStr,
                        Toast.LENGTH_SHORT).show()
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
