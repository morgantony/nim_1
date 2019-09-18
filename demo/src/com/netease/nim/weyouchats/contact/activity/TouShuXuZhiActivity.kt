package com.netease.nim.weyouchats.contact.activity

import android.os.Bundle
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions
import com.netease.nim.uikit.common.activity.UI
import com.netease.nim.weyouchats.R
import kotlinx.android.synthetic.main.jubao_list.*

/**
 * 投诉须知界面
 */
class TouShuXuZhiActivity: UI() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toushuxuzhi)

        val options = NimToolBarOptions()
        options.titleId = R.string.toushuxuzhi_
        setToolBar(R.id.toolbar, options)

    }

}