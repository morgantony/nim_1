package com.netease.nim.weyouchats.main.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.netease.nim.avchatkit.AVChatProfile;
import com.netease.nim.avchatkit.activity.AVChatActivity;
import com.netease.nim.avchatkit.constant.AVChatExtras;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.drop.DropCover;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.common.ui.viewpager.FadeInOutPageTransformer;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.login.LoginActivity;
import com.netease.nim.weyouchats.login.LogoutHelper;
import com.netease.nim.weyouchats.main.adapter.MainTabPagerAdapter;
import com.netease.nim.weyouchats.main.helper.SystemMessageUnreadManager;
import com.netease.nim.weyouchats.main.model.MainTab;
import com.netease.nim.weyouchats.main.reminder.ReminderItem;
import com.netease.nim.weyouchats.main.reminder.ReminderManager;
import com.netease.nim.weyouchats.session.SessionHelper;
import com.netease.nim.weyouchats.team.TeamCreateHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;

/**
 * 主界面
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends UI implements ReminderManager.UnreadNumChangedCallback,  ViewPager.OnPageChangeListener, BottomNavigationBar.OnTabSelectedListener {

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private static final int REQUEST_CODE_NORMAL = 1;
    private static final int REQUEST_CODE_ADVANCED = 2;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;
    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };


//    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private int scrollState;
    private MainTabPagerAdapter adapter;

    private BottomNavigationBar bottomNavigationBar;
    private FragmentManager fm;
//    private TextBadgeItem badgeItem;

    private boolean isFirstIn;
    private Observer<Integer> sysMsgUnreadCountChangedObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer unreadCount) {
            SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unreadCount);
            ReminderManager.getInstance().updateContactUnreadNum(unreadCount);
        }
    };


    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        setToolBar(R.id.toolbar, R.string.app_name, R.drawable.actionbar_dark_logo);
//        setTitle(R.string.app_name);
        isFirstIn = true;

        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return;
        }
        init();
    }

    private void init() {
        observerSyncDataComplete();
        findViews();
        setupPager();
//        setupTabs();
        initView();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
        requestBasicPermission();
    }

    private void initView(){
        bottomNavigationBar = findViewById(R.id.bottom_nav_bar);
        fm = getSupportFragmentManager();

        /**
         * 导航基础设置 包括按钮选中效果 导航栏背景色等
         */
        bottomNavigationBar.setTabSelectedListener(this)
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor("#1ba4f3")//选中颜色
                .setInActiveColor("#939799")//未选中颜色
                .setBarBackgroundColor("#f5f9fa");//导航栏背景色
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
         *添加导航按钮
         */
        bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.img_xiaoxi, "消息"))
                .addItem(new BottomNavigationItem(R.drawable.img_people, "联系人"))
                .addItem(new BottomNavigationItem(R.drawable.img_pyq, "圈子"))//.setInActiveColor("#ffff00")
                .addItem(new BottomNavigationItem(R.drawable.img_my, "我的"))//.setBadgeItem(badgeItem)添加小红点数据
                .initialise();//initialise 一定要放在 所有设置的最后一项
        //设置默认导航栏
        onTabSelected(0);
    }

    private boolean parseIntent() {

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT);
            onLogout();
            return true;
        }

        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
            }

            return true;
        }

        if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT) && AVChatProfile.getInstance().isAVChatting()) {
            intent.removeExtra(AVChatActivity.INTENT_ACTION_AVCHAT);
            Intent localIntent = new Intent();
            localIntent.setClass(this, AVChatActivity.class);
            startActivity(localIntent);
            return true;
        }

        String account = intent.getStringExtra(AVChatExtras.EXTRA_ACCOUNT);
        if (intent.hasExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION) && !TextUtils.isEmpty(account)) {
            intent.removeExtra(AVChatExtras.EXTRA_FROM_NOTIFICATION);
            SessionHelper.startP2PSession(this, account);
            return true;
        }

        return false;
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance().observeSyncDataCompletedEvent(new Observer<Void>() {
            @Override
            public void onEvent(Void v) {
                DialogMaker.dismissProgressDialog();
            }
        });
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data)).setCanceledOnTouchOutside(false);
        }
    }

    private void findViews() {
//        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
    }

    private void setupPager() {
        adapter = new MainTabPagerAdapter(getSupportFragmentManager(), this, pager);
        pager.setOffscreenPageLimit(adapter.getCacheCount());
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);
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
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(sysMsgUnreadCountChangedObserver, register);
    }

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class).querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }

    //初始化未读红点动画
    private void initUnreadCover() {
        DropManager.getInstance().init(this, (DropCover) findView(R.id.unread_cover),
                new DropCover.IDropCompletedListener() {
                    @Override
                    public void onCompleted(Object id, boolean explosive) {
                        if (id == null || !explosive) {
                            return;
                        }

                        if (id instanceof RecentContact) {
                            RecentContact r = (RecentContact) id;
                            NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(), r.getSessionType());
                            return;
                        }

                        if (id instanceof String) {
                            if (((String) id).contentEquals("0")) {
                                NIMClient.getService(MsgService.class).clearAllUnreadCount();
                            } else if (((String) id).contentEquals("1")) {
                                NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
                            }
                        }
                    }
                });
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this)
                .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS)
                .request();
    }

    private void onLogout() {
        Preferences.saveUserToken("");
        // 清理缓存&注销监听
        LogoutHelper.logout();
        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    private void selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
        }
    }

    /**
     * 设置最近联系人的消息为已读
     * <p>
     * account, 聊天对象帐号，或者以下两个值：
     * {@link MsgService#MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * {@link MsgService#MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     */
    private void enableMsgNotification(boolean enable) {
        boolean msg = (pager.getCurrentItem() != MainTab.RECENT_CONTACTS.tabIndex);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
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

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        boolean temp = isFirstIn;
        isFirstIn = false;
        if (pager == null && temp) {
            return;
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init();
        }
        enableMsgNotification(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pager == null) {
            return;
        }
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        DropManager.getInstance().destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_NORMAL) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
            } else {
                ToastHelper.showToast(MainActivity.this, "请选择至少一个联系人！");
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
//        tabs.onPageSelected(position);
        selectPage();
        enableMsgNotification(false);
        bottomNavigationBar.selectTab(position, false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
//        tabs.onPageScrollStateChanged(state);
        scrollState = state;
        selectPage();
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(ReminderItem item) {
//        MainTab tab = MainTab.fromReminderId(item.getId());
//        if (tab != null) {
//            tabs.updateTab(tab.tabIndex, item);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            ToastHelper.showToast(this, "授权成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public void onTabSelected(int position) {
        pager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

}
