package com.netease.nim.weyouchats.main.model

import java.io.Serializable

//通讯录接口返回
 class TongXunLu :Serializable{
    var uid: String=""
    var accid: String=""
    var mobile: String=""
    var mobileName: String=""
    var token: String=""
    var name: String=""
    var icon: String=""
    var sign: String=""
    var email: String=""
    var birth: String=""
    var gender: String=""
    var latitude: String=""
    var longitude: String=""
    var hasPosition: Boolean=false
}

 class  txl :Serializable{
     var mobileName: String? = ""
     var mobile: String? = ""
 }