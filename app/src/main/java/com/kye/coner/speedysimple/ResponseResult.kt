package com.kye.coner.speedysimple

import com.kye.coner.speedy.bean.SpeedyBean

open class ResponseResult<T> : SpeedyBean<T> {
//    var code: Int = 0
//    var msg: String = ""
//    var data: T? = null
//    var success: Boolean = false

    var reason: String = ""
    var result: T? = null
    var error_code: Int = -1
    var msg: String = ""

    override fun isSuccess() : Boolean = error_code == 0

    override fun msg(): String = msg
}