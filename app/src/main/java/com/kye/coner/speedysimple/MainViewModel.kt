package com.kye.coner.speedysimple

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.kye.coner.speedy.*
import com.kye.coner.speedy.vm.BaseVM
import kotlinx.coroutines.Dispatchers
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class MainViewModel : BaseVM() {
    private val _result = MutableLiveData<String>()
    val result: LiveData<String> = _result

    fun getResult() {
        launch<ResponseResult<Any>> {
            invoke {
                buildService<Test>().getSomething(mapOf("key" to "22ab70c7cede70e9fcf8bf442b669145"))
            }.onSuccess {
                _result use result.toString()
            }.onApiError(Dispatchers.IO) {
                LogUtils.e("业务错误")
            }.onError {
                LogUtils.e("请求错误")
                LogUtils.e(message)
            }.onComplete {
                LogUtils.e("请求结束")
            }.onErrorMessage {
//                ToastUtils.showShort(this)
            }.onSuccessMessage {
//                ToastUtils.showShort(this)
            }
        }
    }

    @JvmSuppressWildcards
    interface Test {
        @FormUrlEncoded
        @POST("toutiao/index")
        suspend fun getSomething(@FieldMap map: Map<String, Any>): ResponseResult<Any>
    }
}