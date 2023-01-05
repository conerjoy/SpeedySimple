package com.kye.coner.speedysimple

import android.app.Application
import com.blankj.utilcode.util.ToastUtils
import com.kye.coner.speedy.Speedy
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Speedy.instance.init(this) {
            baseUrl("http://v.juhe.cn/")
            addConverterFactory(GsonConverterFactory.create())
        }
//        Speedy.instance.init(this, mRetrofit)
        Speedy.instance.showToast = {
            ToastUtils.showShort(it)
        }
    }
}