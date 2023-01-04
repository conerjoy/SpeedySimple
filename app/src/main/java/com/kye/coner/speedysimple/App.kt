package com.kye.coner.speedysimple

import android.app.Application
import com.kye.coner.speedy.Speedy
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Speedy.instance.init {
            baseUrl("http://v.juhe.cn/")
            addConverterFactory(GsonConverterFactory.create())
        }
        Speedy.COMMON_NET_WEAK = "abc"
        Speedy.COMMON_NET_DISCONNECT = "qwer"
    }
}