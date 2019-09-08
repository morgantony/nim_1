package com.netease.nim.weyouchats.common.entity

import com.bhm.sdk.rxlibrary.rxjava.BaseResponse
import com.google.gson.annotations.SerializedName
import com.netease.nim.weyouchats.main.activity.nearPersonListBean

class CommenEntity : BaseResponse() {
    @SerializedName("code")
    var code: Int = 0
    @SerializedName("msg")
    var msg: String? = null
    @SerializedName("data")
    var data: LocationBean? = null

}

class LocationBean : BaseResponse(){
    var latitude:Double=-1.00
    var accid:String?=null
    var list= arrayListOf<nearPersonListBean>()
}
