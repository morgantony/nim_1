package com.netease.nim.weyouchats.main.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.ashokvarma.bottomnavigation.BottomNavigationItem
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.bhm.sdk.onresult.ActivityResult
import com.bhm.sdk.rxlibrary.rxjava.RxBuilder
import com.bhm.sdk.rxlibrary.rxjava.callback.CallBack
import com.bhm.sdk.rxlibrary.rxjava.callback.RxUpLoadCallBack
import com.bhm.sdk.rxlibrary.utils.RxLoadingDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zhouwei.library.CustomPopWindow
import com.netease.nim.avchatkit.AVChatProfile
import com.netease.nim.avchatkit.activity.AVChatActivity
import com.netease.nim.avchatkit.constant.AVChatExtras
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.uikit.common.ui.drop.DropCover
import com.netease.nim.uikit.common.ui.drop.DropManager
import com.netease.nim.uikit.support.permission.MPermission
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.HttpApi
import com.netease.nim.weyouchats.common.entity.UpdatePositionEntity
import com.netease.nim.weyouchats.common.maputil.Const
import com.netease.nim.weyouchats.common.ui.viewpager.FadeInOutPageTransformer
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.contact.activity.AddFriendActivity
import com.netease.nim.weyouchats.login.LoginActivity
import com.netease.nim.weyouchats.login.LogoutHelper
import com.netease.nim.weyouchats.main.adapter.MainTabPagerAdapter
import com.netease.nim.weyouchats.main.helper.SystemMessageUnreadManager
import com.netease.nim.weyouchats.main.model.MainTab
import com.netease.nim.weyouchats.main.reminder.ReminderItem
import com.netease.nim.weyouchats.main.reminder.ReminderManager
import com.netease.nim.weyouchats.session.SessionHelper
import com.netease.nim.weyouchats.team.TeamCreateHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.SystemMessageObserver
import com.netease.nimlib.sdk.msg.SystemMessageService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import kotlinx.android.synthetic.main.activity_my.*
import kotlinx.android.synthetic.main.main.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast

/**
 * 主界面
 * Created by huangjun on 2015/3/25.
 */
open class MainActivity : UI(), ReminderManager.UnreadNumChangedCallback, ViewPager.OnPageChangeListener, BottomNavigationBar.OnTabSelectedListener {


    lateinit var  popWindow : CustomPopWindow
    //    private PagerSlidingTabStrip tabs;
    private var fragmentFlag = 0
    private var pager: ViewPager? = null
    private var scrollState: Int = 0
    private var adapter: MainTabPagerAdapter? = null

    private var bottomNavigationBar: BottomNavigationBar? = null
    private var fm: FragmentManager? = null
    //    private TextBadgeItem badgeItem;

    private var isFirstIn: Boolean = false

    private var isFirstUpload: Boolean = false

    var mLocationClient: LocationClient? = null    //LocationClient类是定位SDK的核心类
    var myListener: BDLocationListener = MyLocationListener()

    private val sysMsgUnreadCountChangedObserver = Observer<Int> { unreadCount ->
        SystemMessageUnreadManager.getInstance().sysMsgUnreadCount = unreadCount!!
        ReminderManager.getInstance().updateContactUnreadNum(unreadCount)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        //        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
        //        setTitle(R.string.app_name);
        isFirstIn = true

        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return
        }
        init()
    }

    private fun init() {
        observerSyncDataComplete()
        findViews()
        setupPager()
        //        setupTabs();
        initView()
        registerMsgUnreadInfoObserver(true)
        registerSystemMessageObservers(true)
        requestSystemMessageUnreadCount()
        initUnreadCover()
        requestBasicPermission()
        initPop()
        judgePermission()   //百度地图权限的二次确认



    }

    //初始化定位 开始定位
    private fun initLocations() {
        //声明LocationClient类
        mLocationClient = LocationClient(this)
        //注册监听函数
        mLocationClient?.registerLocationListener(myListener)

        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll")
        //可选，默认gcj02，设置返回的定位结果坐标系

//        val span = 0  //定位一次
//        option.setScanSpan(span)
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true)
        //可选，设置是否需要地址信息，默认不需要

        option.isOpenGps = true
        //可选，默认false,设置是否使用gps

        option.isLocationNotify = true
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true)
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true)
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false)
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false)
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false)
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        //配置LocationClient属性
        mLocationClient?.locOption = option

        mLocationClient?.start() //开始定位
    }
    private fun initPop() {
        val contentView = LayoutInflater.from(this).inflate(R.layout.pop_layout1, null)
        //处理popWindow 显示内容
        handleLogic(contentView)
        popWindow= CustomPopWindow.PopupWindowBuilder(this)
                .setView(contentView)
                .setFocusable(true)
                .setOutsideTouchable(true)
                .setAnimationStyle(R.style.CustomPopWindowStyle)
                .create()
    }

    private fun initView() {
        bottomNavigationBar = findViewById(R.id.bottom_nav_bar)
        fm = supportFragmentManager

        /**
         * 导航基础设置 包括按钮选中效果 导航栏背景色等
         */
        bottomNavigationBar!!.setTabSelectedListener(this)
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor("#1ba4f3")//选中颜色
                .setInActiveColor("#939799")//未选中颜色
                .setBarBackgroundColor("#f5f9fa")//导航栏背景色
        //        badgeItem = new TextBadgeItem()
        //                .setBorderWidth(2)//Badge的Border(边界)宽度
        //                .setBorderColor(Color.BLUE)//Badge的Border颜色
        //                .setBackgroundColor(Color.RED)
        //                .setTextColor(Color.BLACK)//文本颜色
        //                .setGravity(Gravity.RIGHT| Gravity.TOP)//位置，默认右上角
        //                .setAnimationDuration(2000)
        //                .setHideOnSelect(true)//当选中状态时消失，非选中状态显示
        //                .setText("99");

        /**
         * 添加导航按钮
         */
        bottomNavigationBar!!.addItem(BottomNavigationItem(R.drawable.img_xiaoxi, "消息"))
                .addItem(BottomNavigationItem(R.drawable.img_people, "联系人"))
//                .addItem(BottomNavigationItem(R.drawable.img_pyq, "圈子"))//.setInActiveColor("#ffff00")
                .addItem(BottomNavigationItem(R.drawable.img_my, "我的"))//.setBadgeItem(badgeItem)添加小红点数据
                .initialise()//initialise 一定要放在 所有设置的最后一项
        bottomNavigationBar!!.elevation = 3f
        //设置默认导航栏
        onTabSelected(0)

        //titlebar"我的"右上角按钮
        main_titleBar.setRightOnClickListener {
            val intent = Intent(this, EditUserInfoActivity::class.java)
            intent.putExtra("edit", true)
            ActivityResult(this).startForResult(intent) { _, data ->
                if (data != null) {
                    //更新头像和网名等
                    val head = data.getStringExtra("head")
                    val name = data.getStringExtra("name")
                    val sign = data.getStringExtra("sign")
                    val requestOptions = RequestOptions().centerCrop().error(R.drawable.nim_avatar_default)
                    Glide.with(this).load(head).apply(requestOptions).into(hv_robot)
                    if (!TextUtils.isEmpty(name)) {
                        tv_name.text = name
                    }
                    if (!TextUtils.isEmpty(sign)) {
                        tv_des.text = sign
                    }
                }
            }
        }

        fl_tianjia.setOnClickListener {
            if (fragmentFlag == 1) {   //是联系人界面
                popWindow.showAsDropDown(fl_tianjia, 0, 35)
            }
        }
    }



    private fun parseIntent(): Boolean {

        val intent = intent
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT)
            onLogout()
            return true
        }

        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            val message = intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT) as IMMessage
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT)
            when (message.sessionType) {
                SessionTypeEnum.P2P -> SessionHelper.startP2PSession(this, message.sessionId)
                SessionTypeEnum.Team -> SessionHelper.startTeamSession(this, message.sessionId)
            }

            return true
        }

        if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT) && AVChatProfile.getInstance().isAVChatting) {
            intent.removeExtra(AVChatActivity.INTENT_ACTION_AVCHAT)
            val localIntent = Intent()
            localIntent.setClass(this, AVChatActivity::class.java)
            startActivity(localIntent)
            return true
        }

        val account = intent.getStringExtra(AVChatExtras.EXTRA_ACCOUNT)
        if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) && !TextUtils.isEmpty(account)) {
            intent.removeExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION)
            SessionHelper.startP2PSession(this, account)
            return true
        }

        return false
    }

    private fun observerSyncDataComplete() {
        val syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent { DialogMaker.dismissProgressDialog() }
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(this@MainActivity, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false)
        }
    }

    private fun findViews() {
        //        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager)
    }

    private fun setupPager() {
        adapter = MainTabPagerAdapter(supportFragmentManager, this, pager)
        pager!!.offscreenPageLimit = adapter!!.cacheCount
        pager!!.setPageTransformer(true, FadeInOutPageTransformer())
        pager!!.adapter = adapter
        pager!!.setOnPageChangeListener(this)
    }

    /*private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {
            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.tab_layout_main;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnTabClickListener(adapter);
        tabs.setOnTabDoubleTapListener(adapter);
    }*/


    /**
     * 注册未读消息数量观察者
     */
    private fun registerMsgUnreadInfoObserver(register: Boolean) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this)
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this)
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private fun registerSystemMessageObservers(register: Boolean) {
        NIMClient.getService(SystemMessageObserver::class.java).observeUnreadCountChange(sysMsgUnreadCountChangedObserver, register)
    }

    /**
     * 查询系统消息未读数
     */
    private fun requestSystemMessageUnreadCount() {
        val unread = NIMClient.getService(SystemMessageService::class.java).querySystemMessageUnreadCountBlock()
        SystemMessageUnreadManager.getInstance().sysMsgUnreadCount = unread
        ReminderManager.getInstance().updateContactUnreadNum(unread)
    }

    //初始化未读红点动画
    private fun initUnreadCover() {
        DropManager.getInstance().init(this, findView<View>(R.id.unread_cover) as DropCover,
                DropCover.IDropCompletedListener { id, explosive ->
                    if (id == null || !explosive) {
                        return@IDropCompletedListener
                    }

                    if (id is RecentContact) {
                        NIMClient.getService(MsgService::class.java).clearUnreadCount(id.contactId, id.sessionType)
                        return@IDropCompletedListener
                    }

                    if (id is String) {
                        if (id.contentEquals("0")) {
                            NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
                        } else if (id.contentEquals("1")) {
                            NIMClient.getService(SystemMessageService::class.java).resetSystemMessageUnreadCount()
                        }
                    }
                })
    }

    private fun requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS)
        MPermission.with(this@MainActivity)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(*BASIC_PERMISSIONS)
                .request()
    }

    private fun onLogout() {
        Preferences.saveUserToken("")
        // 清理缓存&注销监听
        LogoutHelper.logout()
        // 启动登录
        LoginActivity.start(this)
        finish()
    }

    private fun selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter!!.onPageSelected(pager!!.currentItem)
        }
    }

    /**
     * 设置最近联系人的消息为已读
     *
     *
     * account, 聊天对象帐号，或者以下两个值：
     * [MsgService.MSG_CHATTING_ACCOUNT_ALL] 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * [MsgService.MSG_CHATTING_ACCOUNT_NONE] 目前没有与任何人对话，需要状态栏消息通知
     */
    private fun enableMsgNotification(enable: Boolean) {
        val msg = pager!!.currentItem != MainTab.RECENT_CONTACTS.tabIndex
        if (enable or msg) {
            NIMClient.getService(MsgService::class.java).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None)
        } else {
            NIMClient.getService(MsgService::class.java).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None)
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(MainActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper.getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(MainActivity.this, advancedOption, REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        parseIntent()
    }

    public override fun onResume() {
        super.onResume()
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        val temp = isFirstIn
        isFirstIn = false
        initLocations()
        if (pager == null && temp) {
            return
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init()
        }
        enableMsgNotification(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    public override fun onPause() {
        super.onPause()
        if (pager == null) {
            return
        }
        enableMsgNotification(true)
    }

    public override fun onDestroy() {
        super.onDestroy()
        registerMsgUnreadInfoObserver(false)
        registerSystemMessageObservers(false)
        DropManager.getInstance().destroy()
        mLocationClient?.stop()   //销毁LocationClient
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE_NORMAL) {
            val selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA)
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(this@MainActivity, selected, false, null)
            } else {
                ToastHelper.showToast(this@MainActivity, "请选择至少一个联系人！")
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            val selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA)
            TeamCreateHelper.createAdvancedTeam(this@MainActivity, selected)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        //        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter!!.onPageScrolled(position)
    }

    override fun onPageSelected(position: Int) {
        //        tabs.onPageSelected(position);
        selectPage()
        enableMsgNotification(false)
        bottomNavigationBar!!.selectTab(position, false)
        when (position) {
            0 -> {
                main_titleBar.setTitleText("消息")
                fl_tianjia.visibility = View.GONE
                main_titleBar.setIsRightViewShow(false)
                fragmentFlag = 0
            }
            1 -> {
                main_titleBar.setTitleText("联系人")
                fl_tianjia.visibility = View.VISIBLE
                main_titleBar.setIsRightViewShow(false)
                fragmentFlag = 1
            }
            //后续开发
//            2->{
//                main_titleBar.setTitleText("圈子")
//            fl_tianjia.visibility=View.GONE
//            main_titleBar.setIsRightViewShow(false)
//                fragmentFlag=2
//            }
            2 -> {
                main_titleBar.setTitleText("")
                fl_tianjia.visibility = View.GONE
                main_titleBar.setIsRightViewShow(true)
                fragmentFlag = 3
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        //        tabs.onPageScrollStateChanged(state);
        scrollState = state
        selectPage()
    }

    //未读消息数量观察者实现
    override fun onUnreadNumChanged(item: ReminderItem) {
        //        MainTab tab = MainTab.fromReminderId(item.getId());
        //        if (tab != null) {
        //            tabs.updateTab(tab.tabIndex, item);
        //        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    fun onBasicPermissionSuccess() {
        try {
            ToastHelper.showToast(this, "授权成功")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS)
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    fun onBasicPermissionFailed() {
        try {
            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS)
    }

    override fun displayHomeAsUpEnabled(): Boolean {
        return false
    }

    override fun onTabSelected(position: Int) {
        pager!!.currentItem = position
    }

    override fun onTabUnselected(position: Int) {

    }

    override fun onTabReselected(position: Int) {

    }

    companion object {

        private val EXTRA_APP_QUIT = "APP_QUIT"
        private val REQUEST_CODE_NORMAL = 1
        private val REQUEST_CODE_ADVANCED = 2
        const val BASIC_PERMISSION_REQUEST_CODE = 100
        private val BASIC_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        @JvmStatic
        fun start(context: Context) {
            start(context, null)
        }


        @JvmStatic
        fun start(context: Context, extras: Intent? = null) {
            val intent = Intent()
            intent.setClass(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (extras != null) {
                intent.putExtras(extras)
            }
            context.startActivity(intent)
        }

        // 注销
        @JvmStatic
        fun logout(context: Context, quit: Boolean) {
            val extra = Intent()
            extra.putExtra(EXTRA_APP_QUIT, quit)
            start(context, extra)
        }
    }

    private fun handleLogic(contentView: View) {
        val listener = View.OnClickListener { v ->
            if (popWindow != null) {
                popWindow.dissmiss()
            }
            when (v.id) {
                R.id.tv_one -> {
                    //添加好友
                    AddFriendActivity.start(this)
                }
                R.id.tv_two -> {
                    //附近的人
                    toast("附近的人")
                    startActivity(intentFor<BdMapActivity>())
                }
                R.id.tv_three -> {
                    //扫一扫
                    toast("扫一扫")
                    startActivity(intentFor<ScanActivity>())
                }
            }
        }
        contentView.findViewById<View>(R.id.tv_one).setOnClickListener(listener)
        contentView.findViewById<View>(R.id.tv_two).setOnClickListener(listener)
        contentView.findViewById<View>(R.id.tv_three).setOnClickListener(listener)
    }

    private fun updatePosition(jd: Double, wd: Double) {
        val builder = RxBuilder.newBuilder(this)
                .setLoadingDialog(RxLoadingDialog.getDefaultDialog())
                .setDialogAttribute(false, true, false)
                .setIsLogOutPut(true)//默认是false
                .setIsDefaultToast(true, rxManager)
                .bindRx()
        val observable = builder
                .createApi(HttpApi::class.java, HttpApi.HOST, RxUpLoadCallBack())//rxUpLoadListener不能为空
                .updatePosition(Preferences.getUserToken(), jd.toString(), wd.toString())

        builder.setCallBack(observable, object : CallBack<UpdatePositionEntity>() {
            override fun onSuccess(response: UpdatePositionEntity?) {
                if (response!!.code == 200) {
//                    Log.e("888888","位置更新成功")
                } else {
                    toast("位置更新失败")
                }
            }
        })
    }


    //6.0之后要动态获取权限，重要！！！
    private fun judgePermission() {

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

    //百度地图定位回调监听
   inner class MyLocationListener : BDLocationListener {
        override fun onReceiveLocation(location: BDLocation) {
            //获取定位结果
            val sb = StringBuffer(256)

            sb.append("time : ")
            sb.append(location.time)    //获取定位时间

            sb.append("\nerror code : ")
            sb.append(location.locType)    //获取类型类型

            sb.append("\nlatitude : ")
            sb.append(location.latitude.toString() + "")    //获取纬度信息

            sb.append("\nlontitude : ")
            sb.append(location.longitude.toString() + "")    //获取经度信息

            sb.append("\nradius : ")
            sb.append(location.radius)    //获取定位精准度

            if (location.locType == BDLocation.TypeGpsLocation) {

                // GPS定位结果
                sb.append("\nspeed : ")
                sb.append(location.speed)    // 单位：公里每小时

                sb.append("\nsatellite : ")
                sb.append(location.satelliteNumber)    //获取卫星数

                sb.append("\nheight : ")
                sb.append(location.altitude)    //获取海拔高度信息，单位米

                sb.append("\ndirection : ")
                sb.append(location.direction)    //获取方向信息，单位度

                sb.append("\naddr : ")
                sb.append(location.addrStr)    //获取地址信息

                sb.append("\ndescribe : ")
                sb.append("gps定位成功")

            } else if (location.locType == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ")
                sb.append(location.addrStr)    //获取地址信息

                sb.append("\noperationers : ")
                sb.append(location.operators)    //获取运营商信息

                sb.append("\ndescribe : ")
                sb.append("网络定位成功")

            } else if (location.locType == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ")
                sb.append("离线定位成功，离线定位结果也是有效的")

            } else if (location.locType == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ")
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因")

            } else if (location.locType == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ")
                sb.append("网络不同导致定位失败，请检查网络是否通畅")

            } else if (location.locType == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ")
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机")

            }

            sb.append("\nlocationdescribe : ")
            sb.append(location.locationDescribe)    //位置语义化信息

            val list = location.poiList    // POI数据
            if (list != null) {
                sb.append("\npoilist size = : ")
                sb.append(list.size)
                for (p in list) {
                    sb.append("\npoi= : ")
                    sb.append(p.id + " " + p.name + " " + p.rank)
                }
            }

            Log.i("BaiduLocationApiDem", sb.toString())

            //坐标位置不变的话 就不用更新位置
            try {
                val a=Const.LONGITUDE.toString()==location.longitude.toString()
                val b=Const.LATITUDE.toString()==location.latitude.toString()
                if(a&&b){
                    return
                }
                if(Const.LONGITUDE.toString()=="4.9E-324"){   //定位失败的返回坐标
                    return
                }
                if(Const.LATITUDE.toString()=="4.9E-324"){
                    return
                }
            }catch (e:java.lang.Exception){

            }
            //现在已经定位成功，可以将定位的数据保存下来，Const就是保存数据的类
            Const.LONGITUDE = location.longitude
            Const.LATITUDE = location.latitude
//            Log.e("888888","${Const.LONGITUDE}   ${Const.LATITUDE}")
            updatePosition(Const.LONGITUDE, Const.LATITUDE)
        }

    }
}
