package com.netease.nim.weyouchats.contact.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nim.uikit.common.media.model.GLImage
import com.netease.nim.weyouchats.R

/**
 * 上传图片adapter
 */

class PostAdapter(layoutResId: Int, data: ArrayList<GLImage>?) : BaseQuickAdapter<GLImage, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: GLImage) {
        val img_item_pic=helper.getView<ImageView>(R.id.img_item_pic)
        val ll_root_item=helper.getView<LinearLayout>(R.id.ll_root_item)
        val tv_del_pic=helper.getView<TextView>(R.id.tv_del_pic)

        //对根布局设置 设置图片宽高为 屏幕宽度的三分之一 单位px
        val lParam = LinearLayout.LayoutParams(ll_root_item.layoutParams)
        val width=ScreenUtils.getScreenWidth(mContext)
        lParam.width =(width-30) /3
        lParam.height = (width-30) /3
        img_item_pic.scaleType=ImageView.ScaleType.CENTER_CROP
        lParam.marginEnd=5
        lParam.marginStart=5
        lParam.leftMargin=5
        lParam.rightMargin=5
        ll_root_item.layoutParams = lParam

        //对ImageView设置
//        val lParam = LinearLayout.LayoutParams(img_item_pic.layoutParams)
//        val width=ScreenUtils.getScreenWidth(mContext)
//        lParam.width =(width-30) /3
//        lParam.height = (width-30) /3
//        img_item_pic.scaleType=ImageView.ScaleType.CENTER_CROP
//        lParam.margin= 5
//        img_item_pic.layoutParams = lParam

        //加载图片
        if (item.path=="-1"){
            img_item_pic.setImageResource(R.drawable.ic_add_photo)
            tv_del_pic.visibility= View.GONE
        }else{
            tv_del_pic.visibility= View.VISIBLE
            Glide.with(mContext).load(item.path).into(img_item_pic)
        }
    }
}

