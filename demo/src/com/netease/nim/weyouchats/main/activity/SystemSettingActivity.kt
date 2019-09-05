package com.netease.nim.weyouchats.main.activity

import android.os.Bundle
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.common.CommonUtils
import com.netease.nim.weyouchats.common.util.PickerUtils
import com.netease.nim.weyouchats.config.preference.UserPreferences
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import kotlinx.android.synthetic.main.activity_account_setting.titleBar
import kotlinx.android.synthetic.main.activity_system_setting.*

class SystemSettingActivity: UI(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system_setting)
        initView()
    }

    private fun initView(){
        switchButton1.check = UserPreferences.getRingToggle() //静音模式
        switchButton1.setOnChangedListener { _, checkState ->
            UserPreferences.setRingToggle(checkState)
            val info = UserPreferences.getStatusConfig()
            info.ring = checkState
            UserPreferences.setStatusConfig(info)
            NIMClient.updateStatusBarNotificationConfig(info)
        }
        switchButton2.check = UserPreferences.getNotificationToggle()//推送通知
        switchButton2.setOnChangedListener { _, checkState ->
            CommonUtils.setMessageNotify(this@SystemSettingActivity, switchButton2, checkState)
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