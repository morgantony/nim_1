package com.netease.nim.weyouchats.contact.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import com.netease.nim.uikit.common.ToastHelper

import com.netease.nim.weyouchats.DemoCache
import com.netease.nim.weyouchats.R
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.model.SimpleCallback
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.common.activity.ToolBarOptions
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.common.ui.dialog.DialogMaker
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon
import com.netease.nim.weyouchats.config.preference.Preferences
import com.netease.nim.weyouchats.contact.ContactHttpClient
import com.netease.nim.weyouchats.login.User
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.friend.FriendService
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import kotlinx.android.synthetic.main.add_friend_activity.*
import org.w3c.dom.Text

/**
 * 添加好友页面
 */
class AddFriendActivity : UI() {

    private var searchEdit: ClearableEditTextWithIcon? = null
    lateinit var mAdapter: SearchAdapter
    private var models = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friend_activity)
        mAdapter= SearchAdapter(this,models,R.layout.item_search)
        recyclerView.apply {
            setHasFixedSize(true)
            //item之间设置间距
//            addItemDecoration(SpaceItemDecoration(10.dp2px(this@SearchActivity)))
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        val options = NimToolBarOptions()
        options.titleId = R.string.add_buddy
        setToolBar(R.id.toolbar, options)

        //搜索结果列表item
//        mAdapter.onItemClickListener=BaseQuickAdapter.OnItemClickListener{adapter,view,position->
//            UserProfileActivity.start(this, (adapter.data[position] as User).accid)
//        }

        findViews()
        initActionbar()
    }

    private fun findViews() {
        searchEdit = findView(R.id.search_friend_edit)
        searchEdit!!.setDeleteImage(R.drawable.nim_grey_delete_icon)
    }

    private fun initActionbar() {
        val toolbarView = findView<TextView>(R.id.action_bar_right_clickable_textview)
        toolbarView.setText(R.string.search)
        toolbarView.setOnClickListener {
            if (TextUtils.isEmpty(searchEdit!!.text.toString())) {
                ToastHelper.showToast(this@AddFriendActivity, R.string.not_allow_empty)
            } else if (searchEdit!!.text.toString() == DemoCache.getAccount()) {
                ToastHelper.showToast(this@AddFriendActivity, R.string.add_friend_self_tip)
            } else {
                query()
            }
        }
    }

    private fun query() {
        DialogMaker.showProgressDialog(this, null, false)
        val account = searchEdit!!.text.toString().toLowerCase()

        ContactHttpClient.getInstance().searchFriend(account, object : ContactHttpClient.ContactHttpCallback<List<User>> {
            override fun onSuccess(user: List<User>?) {
                DialogMaker.dismissProgressDialog()
                if (user.isNullOrEmpty()) {
                    EasyAlertDialogHelper.showOneButtonDiolag(this@AddFriendActivity, R.string.user_not_exsit,
                            R.string.user_tips, R.string.ok, false, null)
                } else {
                    //展示列表
                    models.clear()
                    models.addAll(user)
                    mAdapter.notifyDataSetChanged()
                    //                    UserProfileActivity.start(AddFriendActivity.this, account);
                }
            }

            override fun onFailed(code: Int, errorMsg: String) {
                DialogMaker.dismissProgressDialog()
                ToastHelper.showToast(this@AddFriendActivity, "on failed:$code")
            }
        })

        //        NimUIKit.getUserInfoProvider().getUserInfoAsync(account, new SimpleCallback<NimUserInfo>() {
        //            @Override
        //            public void onResult(boolean success, NimUserInfo result, int code) {
        //                DialogMaker.dismissProgressDialog();
        //                if (success) {
        //                    if (result == null) {
        //                        EasyAlertDialogHelper.showOneButtonDiolag(AddFriendActivity.this, R.string.user_not_exsit,
        //                                R.string.user_tips, R.string.ok, false, null);
        //                    } else {
        //                        UserProfileActivity.start(AddFriendActivity.this, account);
        //                    }
        //                } else if (code == 408) {
        //                    ToastHelper.showToast(AddFriendActivity.this, R.string.network_is_not_available);
        //                } else if (code == ResponseCode.RES_EXCEPTION) {
        //                    ToastHelper.showToast(AddFriendActivity.this, "on exception");
        //                } else {
        //                    ToastHelper.showToast(AddFriendActivity.this, "on failed:" + code);
        //                }
        //            }
        //        });
    }

    companion object {

        fun start(context: Context) {
            val intent = Intent()
            intent.setClass(context, AddFriendActivity::class.java)
            context.startActivity(intent)
        }
    }

}

//搜索好友结果适配器
class SearchAdapter(var context: Context, datas: List<User>?, resId:Int) : BaseQuickAdapter<User, BaseViewHolder>(resId,datas) {
    override fun convert(helper: BaseViewHolder?, item: User?) {
        val tv_tianjia=helper?.getView<TextView>(R.id.tv_tianjia)
        helper?.setText(R.id.tv_mingzi,if(item?.name==null){item?.mobile}else{item.name})
        if (item?.icon!=null){
            Glide.with(context).load(item.icon).into(helper?.getView(R.id.hv_touxiang))
        }
        when {
            NIMClient.getService(FriendService::class.java).isMyFriend(item?.accid) -> tv_tianjia?.text = "已是好友"
            item?.accid==DemoCache.getAccount() -> tv_tianjia?.text = "编辑您的名片"
            else -> tv_tianjia?.text = "添加好友"
        }
        helper?.getView<TextView>(R.id.tv_tianjia)?.setOnClickListener {
            //添加好友
            UserProfileActivity.start(context, item?.accid)
        }
    }

}