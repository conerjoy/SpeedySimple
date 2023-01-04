package com.kye.coner.speedy.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kye.coner.speedy.*
import com.kye.coner.speedy.bean.SpeedyBean
import kotlinx.coroutines.launch

open class BaseVM : ViewModel() {
    val LOADING = MutableLiveData<Boolean>() // loading监听
    val ERROR = MutableLiveData<Boolean>() // 请求失败监听，true 请求失败  false 请求apierror

    /**
     * 利用协程+retrofit发起请求
     * @param block 发出请求的lambda
     */
    fun <T: SpeedyBean<*>> launch(block: WorkHandler<T>) {
        viewModelScope.launch {
            launchInScope(block)
        }
    }

    /**
     * 利用协程+retrofit发起请求
     * @param block 发出请求的lambda
     */
    suspend fun <T: SpeedyBean<*>> launchInScope(block: WorkHandler<T>) {
        LOADING use true
        Speedy.instance.launchRequest(block)
            .onError {
                ERROR use true
            }.onApiError {
                ERROR use false
            }.apply {
                LOADING use false
            }
    }
}