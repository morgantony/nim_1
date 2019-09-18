package com.netease.nim.weyouchats.main.adapter

import android.content.Context
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.weyouchats.DemoCache
import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.contact.activity.UserProfileActivity
import com.netease.nim.weyouchats.main.model.TongXunLu
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.friend.FriendService

//通讯录item适配器
class TongXunLuAdapter(var context: Context, datas: List<TongXunLu>?, resId: Int) : BaseQuickAdapter<TongXunLu, BaseViewHolder>(resId, datas)  {
    override fun convert(helper: BaseViewHolder, item: TongXunLu?) {
        val tv_tianjia=helper.getView<TextView>(R.id.tv_tianjia)
        //加载图片，默认背景图
        if (item?.icon!=null){
            Glide.with(context).load(item.icon).into(helper.getView(R.id.hv_touxiang))  //头像
        }
        helper.setText(R.id.tv_mingzi,item?.mobileName)  //手机里存储的名字
        helper.setText(R.id.tv_nicheng,item?.name)   //微友名

        if(NIMClient.getService(FriendService::class.java).isMyFriend(item?.accid)){
            tv_tianjia?.text = "已是好友"
        }else{
            tv_tianjia?.text = "添加好友"
        }
        helper.getView<TextView>(R.id.tv_tianjia)?.setOnClickListener {
            //添加好友
            UserProfileActivity.start(context, item?.accid)
        }
    }
}