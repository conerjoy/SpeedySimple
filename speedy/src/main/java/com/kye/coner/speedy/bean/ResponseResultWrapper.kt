package com.kye.coner.speedy.bean

import kotlinx.coroutines.CoroutineDispatcher
import java.lang.Exception

/**
 * 请求结果的包装类
 */
class ResponseResultWrapper<T> {
    var t: T? = null // 请求结果
    var error: Exception? = null // 请求异常的错误
    var completeFun: (() -> Unit)? = null // 请求完成的回调lambda
    var mDispatcher: CoroutineDispatcher? = null // 请求完成的回调lambda 执行线程

    constructor() : super()
    constructor(error: Exception?) : super() {
        this.error = error
    }

    constructor(sup: T) : super() {
        t = sup
    }
}