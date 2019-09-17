package com.netease.nim.weyouchats.contact.activity

import android.os.Bundle
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.jubao_list.*
import org.jetbrains.anko.intentFor

/**
 * 举报界面
 */
class JuBaoActivity: UI() {
    var account=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.jubao_list)
        account=intent.getStringExtra("account")
        val options = NimToolBarOptions()
        options.titleId = R.string.jubao_
        setToolBar(R.id.toolbar, options)

        initLis()
    }

    private fun initLis() {
        //投诉须知
        toushuxuzhi.setOnClickListener {
            startActivity(intentFor<TouShuXuZhiActivity>())
        }
        one.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "发布不适当内容对我造成骚扰",
                    "account" to account
            ))
        }
        two.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "存在欺诈骗钱行为",
                    "account" to account
            ))
        }
        three.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "此账号可能被盗用了",
                    "account" to account
            ))
        }
        four.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "存在侵权行为",
                    "account" to account
            ))
        }
        five.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "发布伪冒品信息",
                    "account" to account
            ))
        }
        six.setOnClickListener {
            startActivity(intentFor<TouShuSubmitActivity>(
                    "type" to "冒充他人",
                    "account" to account
            ))
        }
    }
}