package com.netease.nim.weyouchats.main.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.widget.Toast

import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.main.model.MainTab
import com.netease.nim.weyouchats.main.viewholder.FuncViewHolder
import com.netease.nim.uikit.business.contact.ContactsFragment
import com.netease.nim.uikit.business.contact.core.item.AbsContactItem
import com.netease.nim.uikit.business.contact.core.viewholder.AbsContactViewHolder
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.uikit.api.model.contact.ContactsCustomization
import com.netease.nim.uikit.business.contact.core.item.ItemTypes
import com.netease.nim.weyouchats.contact.activity.BlackListActivity
import com.netease.nim.weyouchats.main.activity.SystemMessageActivity
import com.netease.nim.weyouchats.main.activity.TeamListActivity
import kotlinx.android.synthetic.main.contacts_list.*


/**
 * 集成通讯录列表
 *
 *
 * Created by huangjun on 2015/9/7.
 */
class ContactListFragment : MainTabFragment() {

    private var fragment: ContactsFragment? = null

    init {
        containerId = MainTab.CONTACT.fragmentId
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onCurrent() // 触发onInit，提前加载
    }

    override fun onInit() {
        addContactFragment()  // 集成通讯录页面
    }

    // 将通讯录列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private fun addContactFragment() {
        //好友验证
        ll_new_friend.setOnClickListener {
            SystemMessageActivity.start(context)
        }
        //通讯录
        ll_tongxunllu.setOnClickListener {
            myToast("敬请期待")
        }
        //群聊
        ll_qunliao.setOnClickListener {
            TeamListActivity.start(context, ItemTypes.TEAMS.NORMAL_TEAM)   //普通群聊
//            TeamListActivity.start(context, ItemTypes.TEAMS.ADVANCED_TEAM)  //可以设置权限的高级群聊
        }
        //黑名单
        ll_heimingdan.setOnClickListener {
            BlackListActivity.start(context)
        }
        fragment = ContactsFragment()
        fragment!!.containerId = R.id.contact_fragment

        val activity = activity as UI

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = activity.addFragment(fragment) as ContactsFragment

        // 功能项定制
        fragment!!.setContactsCustomization(object : ContactsCustomization {
            override fun onGetFuncViewHolderClass(): Class<out AbsContactViewHolder<out AbsContactItem>> {
                return FuncViewHolder::class.java
            }

            override fun onGetFuncItems(): List<AbsContactItem> {
                return FuncViewHolder.FuncItem.provide()
            }

            override fun onFuncItemClick(item: AbsContactItem) {
                FuncViewHolder.FuncItem.handle(getActivity(), item)
            }
        })
    }

    override fun onCurrentTabClicked() {
        // 点击切换到当前TAB
        if (fragment != null) {
            fragment!!.scrollToTop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FuncViewHolder.unRegisterUnreadNumChangedCallback()
    }
}
