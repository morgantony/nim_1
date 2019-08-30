package com.netease.nim.weyouchats.login

import java.io.Serializable


class User : Serializable {

    var accid: String? = null

    var token: String? = null

    var mobile: String? = null

    var name: String? = null

    var icon: String? = null

    var sign: String? = null

    var email: String? = null

    var birth: String? = null

    var gender: String? = null

    var uid: Int? = -1

}
