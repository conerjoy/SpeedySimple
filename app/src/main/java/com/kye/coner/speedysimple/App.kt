package com.kye.coner.speedysimple

import android.app.Application
import com.blankj.utilcode.util.ToastUtils
import com.kye.coner.speedy.Speedy
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Speedy.instance.init(this) {
            baseUrl("http://v.juhe.cn/")
            addConverterFactory(GsonConverterFactory.create())
            client(OkHttpClient.Builder().build())
        }
//        Speedy.instance.init(this, mRetrofit)
        Speedy.instance.showToast = {
            ToastUtils.showShort(it)
        }
        Speedy.NETWORK_ERROR_TOAST = true // 全局生效
    }
}