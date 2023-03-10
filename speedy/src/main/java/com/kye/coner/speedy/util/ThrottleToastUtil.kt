package com.kye.coner.speedy.util

import com.kye.coner.speedy.Speedy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * toast节流器，用于在规定时间内只提示一次
 */
object ThrottleToastUtil {
    private var timestamp = 0L
    private const val TIME_INTERVAL = 5 * 1000

    @JvmStatic
    fun showToast(msg: String) {
        if (System.currentTimeMillis() - timestamp > TIME_INTERVAL) {
            synchronized(ThrottleToastUtil::class.java) {
                if (System.currentTimeMillis() - timestamp > TIME_INTERVAL) {
                    timestamp = System.currentTimeMillis()
                    CoroutineScope(Dispatchers.Main).launch {
                        Speedy.instance.showToast.invoke(msg)
                    }
                }
            }
        }
    }
}