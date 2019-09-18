package com.netease.nim.weyouchats.main.activity

import android.os.Bundle
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.CommonUtils
import com.netease.nim.weyouchats.common.util.PickerUtils
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.config.preference.UserPreferences
import com.netease.nim.weyouchats.main.model.SettingTemplate
import com.netease.nim.weyouchats.main.model.SettingType
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.mixpush.MixPushService
import com.netease.nimlib.sdk.msg.MsgService
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_system_setting.*

class SystemSettingActivity: UI(){
    private var notificationItem: SettingTemplate? = null
    private val TAG_NOTICE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_setting)
        initView()
    }

    private fun initView(){

        notificationItem = SettingTemplate(TAG_NOTICE, getString(R.string.msg_notice), SettingType.TYPE_TOGGLE,
                UserPreferences.getNotificationToggle())

        switchButton1.check = UserPreferences.getRingToggle() //消息静音开关
        switchButton1.setOnChangedListener { _, checkState ->
            UserPreferences.setRingToggle(checkState)
            val info = UserPreferences.getStatusConfig()
            info.ring = checkState
            UserPreferences.setStatusConfig(info)
            NIMClient.updateStatusBarNotificationConfig(info)
        }

        switchButton2.check = Preferences.gettoggle()//推送通知通知栏是否显示
        switchButton2.setOnChangedListener { _, checkState ->
            Preferences.savetoggle(checkState)
            NIMClient.toggleNotification(checkState)
        }

        titleBar.setLeftOnClickListener { finish() }
        ll_3.setOnClickListener {
            PickerUtils.showExitDialog(this@SystemSettingActivity, "是否清除聊天记录？"
                    , "否", "是") {
                NIMClient.getService(MsgService::class.java).clearMsgDatabase(true)
                ToastHelper.showToast(this@SystemSettingActivity, R.string.clear_msg_history_success)
            }
        }
        ll_4.setOnClickListener {
            PickerUtils.showExitDialog(this@SystemSettingActivity, "是否清除图片、视频缓存？"
                    , "否", "是") {
                CommonUtils.clearCache(this@SystemSettingActivity)
            }
        }
    }

}