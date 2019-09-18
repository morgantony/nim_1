package com.netease.nim.weyouchats.session.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import com.netease.nim.uikit.common.ToastHelper

import com.netease.nim.weyouchats.DemoCache
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.contact.activity.UserProfileActivity
import com.netease.nim.weyouchats.team.TeamCreateHelper
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.activity.ToolBarOptions
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.imageview.HeadImageView
import com.netease.nim.uikit.common.ui.widget.SwitchButton
import com.netease.nim.uikit.common.util.sys.NetworkUtil
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.weyouchats.main.activity.MainActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.team.model.CreateTeamResult

import java.util.ArrayList

/**
 * 创建群 普通群和高级群
 */
class MessageInfoActivity : UI() {
    // data
    private var account: String? = ""
    // view
    private var switchButton: SwitchButton? = null

    private val onChangedListener = SwitchButton.OnChangedListener { v, checkState ->
        if (!NetworkUtil.isNetAvailable(this@MessageInfoActivity)) {
            ToastHelper.showToast(this@MessageInfoActivity, R.string.network_is_not_available)
            switchButton!!.check = !checkState
            return@OnChangedListener
        }

        NIMClient.getService(FriendService::class.java).setMessageNotify(account, checkState).setCallback(object : RequestCallback<Void> {
            override fun onSuccess(param: Void?) {
                if (checkState) {
                    ToastHelper.showToast(this@MessageInfoActivity, "开启消息提醒成功")
                } else {
                    ToastHelper.showToast(this@MessageInfoActivity, "关闭消息提醒成功")
                }
            }

            override fun onFailed(code: Int) {
                if (code == 408) {
                    ToastHelper.showToast(this@MessageInfoActivity, R.string.network_is_not_available)
                } else {
                    ToastHelper.showToast(this@MessageInfoActivity, "on failed:$code")
                }
                switchButton!!.check = !checkState
            }

            override fun onException(exception: Throwable) {

            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_info_activity)

        val options = NimToolBarOptions()
        options.titleId = R.string.message_info
        options.navigateId = R.drawable.img_close_source
        setToolBar(R.id.toolbar, options)

        account = intent.getStringExtra(EXTRA_ACCOUNT)
        findViews()
    }

    override fun onResume() {
        super.onResume()
        updateSwitchBtn()
    }

    private fun findViews() {
        val userHead = findViewById<View>(R.id.user_layout).findViewById<View>(R.id.imageViewHeader) as HeadImageView
        val userName = findViewById<View>(R.id.user_layout).findViewById<View>(R.id.textViewName) as TextView
        userHead.loadBuddyAvatar(account)
        userName.text = UserInfoHelper.getUserDisplayName(account)
        userHead.setOnClickListener { openUserProfile() }

//        (findViewById<View>(R.id.create_team_layout).findViewById<View>(R.id.textViewName) as TextView).setText(R.string.create_normal_team)
        (findViewById<View>(R.id.create_team_layout).findViewById<View>(R.id.textViewName) as TextView).setText(R.string.create_advance_team)
        val addImage = findViewById<View>(R.id.create_team_layout).findViewById<View>(R.id.imageViewHeader) as HeadImageView
        addImage.setBackgroundResource(com.netease.nim.uikit.R.drawable.nim_team_member_add_selector)
        addImage.setOnClickListener { createTeamMsg() }

        (findViewById<View>(R.id.toggle_layout).findViewById<View>(R.id.user_profile_title) as TextView).setText(R.string.msg_notice)
        switchButton = findViewById<View>(R.id.toggle_layout).findViewById<View>(R.id.user_profile_toggle) as SwitchButton
        switchButton!!.setOnChangedListener(onChangedListener)
    }

    private fun updateSwitchBtn() {
        val notice = NIMClient.getService(FriendService::class.java).isNeedMessageNotify(account)
        switchButton!!.check = notice
    }

    private fun openUserProfile() {
        UserProfileActivity.start(this, account)
    }

    /**
     * 创建群聊
     */
    private fun createTeamMsg() {
        val memberAccounts = ArrayList<String>()
        memberAccounts.add(account!!)
        val option = TeamHelper.getCreateContactSelectOption(memberAccounts, 500)  //群组50人改为默认500人
//        NimUIKit.startContactSelector(this, option, REQUEST_CODE_NORMAL)// 创建讨论组
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_ADVANCED)// 创建群组（有权限管理）


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_NORMAL) {
                val selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA)
                if (selected != null && !selected.isEmpty()) {
                    TeamCreateHelper.createNormalTeam(this@MessageInfoActivity, selected, true, object : RequestCallback<CreateTeamResult> {
                        override fun onSuccess(param: CreateTeamResult) {
                            finish()
                        }

                        override fun onFailed(code: Int) {

                        }

                        override fun onException(exception: Throwable) {

                        }
                    })
                } else {
                    ToastHelper.showToast(DemoCache.getContext(), "请选择至少一个联系人！")
                }
            } else if (requestCode == REQUEST_CODE_ADVANCED) {   //创建高级群

                val selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA)
                if (selected != null && !selected.isEmpty()) {
                    TeamCreateHelper.createAdvancedTeam(this@MessageInfoActivity, selected)
                } else {
                    ToastHelper.showToast(DemoCache.getContext(), "请选择至少一个联系人！")
                }


            }
        }
    }

    companion object {
        private val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"
        private val REQUEST_CODE_NORMAL = 1    //普通群
        private val REQUEST_CODE_ADVANCED = 2  //高级群

        fun startActivity(context: Context, account: String) {
            val intent = Intent()
            intent.setClass(context, MessageInfoActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT, account)
            context.startActivity(intent)
        }
    }
}
