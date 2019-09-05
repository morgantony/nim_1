package com.netease.nim.weyouchats.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.netease.nim.weyouchats.R
import com.netease.nim.weyouchats.main.model.MainTab
import com.netease.nim.uikit.common.fragment.TabFragment
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast


abstract class MainTabFragment : TabFragment() {

    private var loaded = false

    private var tabData: MainTab? = null

    protected abstract fun onInit()

    protected fun inited(): Boolean {
        return loaded
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.main_tab_fragment_container, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun attachTabData(tabData: MainTab) {
        this.tabData = tabData
    }

    override fun onCurrent() {
        super.onCurrent()

        if (!loaded && loadRealLayout()) {
            loaded = true
            onInit()
        }
    }

    private fun loadRealLayout(): Boolean {
        val root = view as ViewGroup?
        if (root != null) {
            root.removeAllViewsInLayout()
            View.inflate(root.context, tabData!!.layoutId, root)
        }
        return root != null
    }

    fun myToast(msg:String){
        context?.runOnUiThread { toast(msg) }
    }
}
