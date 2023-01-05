package com.kye.coner.speedy

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.kye.coner.speedy.util.ThrottleToastUtil.showToast
import com.kye.coner.speedy.bean.SpeedyBean
import com.kye.coner.speedy.bean.ResponseResultWrapper
import com.kye.coner.speedy.util.NetworkUtils
import kotlinx.coroutines.*
import retrofit2.Retrofit
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

/**
 * 便捷发起网络请求并处理请求结果
 *
 * @author wuc
 *
 *  @JvmSuppressWildcards
 *  interface Test {
 *      @FormUrlEncoded
 *      @POST("index")
 *      suspend fun getSomething(@FieldMap map: Map<String, Any>): ResponseResult<Any>
 *  }
 *
 *  launch<ResponseResult<Any>> {
 *       invoke { buildService<Test>().getSomething() }.onSuccess { }.onApiError {  }.onError {  }.onComplete {  }
 *  }
 */
class Speedy {
    /**
     * 快捷发起网络请求
     */
    fun <T : SpeedyBean<*>> launch(block: WorkHandler<T>) {
        CoroutineScope(Dispatchers.IO).launch { // 网络请求在子线程中执行
            launchRequest(block)
        }
    }

    /**
     * 利用协程+retrofit发起请求
     * @param block 发出请求的lambda
     */
    suspend fun <T : SpeedyBean<*>> launchRequest(block: WorkHandler<T>): WrapperBean<T> {
        return block.invoke {
            responseHandler(it)
        }.apply { // 先将onComplete(lambda函数)赋值
            withContext(completeDispatcher ?: Dispatchers.Main) {
                completeFun?.invoke()  // 触发onComplete(lambda函数)
            }
        }
    }

    /**
     * 处理请求结果的方法
     * @param block 发出请求并处理请求结果的流式lambda
     */
    private suspend fun <T : SpeedyBean<*>> responseHandler(block: RequestHandler<T>): WrapperBean<T> {
        return try {
            ResponseResultWrapper(block.invoke())
        } catch (e: Exception) { // 请求失败了，也包装一下
            ResponseResultWrapper(e)
        }
    }

    /**
     * 显示toast的lambda函数
     */
    var showToast: (String) -> Unit = { msg ->
        application?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }

    var application: Application? = null
    lateinit var SPEEDY_RETROFIT: Retrofit // 私有的retrofit对象

    fun init(app: Application, block: Retrofit.Builder.() -> Retrofit.Builder) {
        application = app
        SPEEDY_RETROFIT = block.invoke(Retrofit.Builder()).build()
    }

    fun init(app: Application, retrofit: Retrofit) {
        application = app
        SPEEDY_RETROFIT = retrofit
    }

    companion object {
        var COMMON_NET_WEAK = "网络不给力，请稍后重试"
        var COMMON_NET_DISCONNECT = "当前无网络，请检查后重试"

        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            Speedy()
        }
    }
}

val mWeakThrowable = ConnectException(Speedy.COMMON_NET_WEAK)
val mDisconnectThrowable = ConnectException(Speedy.COMMON_NET_DISCONNECT)

/**
 * 请求成功的扩展方法
 * 与onApiError、onError互斥
 */
inline fun <T : SpeedyBean<*>> WrapperBean<T>.onSuccess(dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: ResultHandler<T>): WrapperBean<T> {
    if (response?.isSuccess() == true) {
        CoroutineScope(dispatcher).launch {
            response?.let {
                handle(it)
            }
        }
    }
    return this
}

/**
 * 请求ApiError的扩展方法
 * 与onSuccess、onError互斥
 *
 * 注意：这个lambda中ResponseResult的参数data被kotlin编译后伪装成了非空类型，实际上基类由java编写，是不具备空安全能力的；在发生ApiException时，data可能是null
 */
inline fun <T : SpeedyBean<*>> WrapperBean<T>.onApiError(dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: ResultHandler<T>): WrapperBean<T> {
    if (response?.isSuccess() == false) {
        CoroutineScope(dispatcher).launch {
            response?.let {
                handle(it)
            }
        }
    }
    return this
}

inline fun <T> WrapperBean<T>.onError(crossinline handle: suspend Exception.() -> Unit): WrapperBean<T> {
    return onError(true, Dispatchers.Main, handle)
}

/**
 * 请求onError的扩展方法
 * 与onSuccess、ApiError互斥
 */
inline fun <T> WrapperBean<T>.onError(networkErrorToast: Boolean = true, dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: suspend Exception.() -> Unit): WrapperBean<T> {
    if (error != null) { // 有error对象则认定为其他错误
        if (error is TimeoutException || error is SocketTimeoutException) {
            if (networkErrorToast) {
                showToast(mWeakThrowable.message ?: Speedy.COMMON_NET_WEAK)
            }
            CoroutineScope(dispatcher).launch {
                handle(mWeakThrowable)
            }
        } else {
            if ((error is ConnectException) || (error is TimeoutException) || (error != null && error!!.message != null && error!!.message?.contains("Unable to resolve") == true)) {
                if (NetworkUtils.isConnected()) {
                    if (networkErrorToast) {
                        showToast(mWeakThrowable.message ?: Speedy.COMMON_NET_WEAK)
                    }
                    CoroutineScope(dispatcher).launch {
                        handle(mWeakThrowable)
                    }
                } else {
                    if (networkErrorToast) {
                        showToast(mDisconnectThrowable.message ?: Speedy.COMMON_NET_DISCONNECT)
                    }
                    CoroutineScope(dispatcher).launch {
                        handle(mDisconnectThrowable)
                    }
                }
            } else {
                CoroutineScope(dispatcher).launch {
                    error?.let { handle(it) }
                }
            }
        }
    }
    return this
}

/**
 * 单独用于展示msg的扩展方法
 */
inline fun <T : SpeedyBean<*>> WrapperBean<T>.onMessage(dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: String.() -> Unit): WrapperBean<T> {
    if (error != null) {
        error?.message?.let {
            if (it.isNullOrEmpty().not()) CoroutineScope(dispatcher).launch {
                handle(it)
            }
        }
    } else {
        if (response?.msg().isNullOrEmpty().not()) CoroutineScope(dispatcher).launch {
            handle(response?.msg() ?: "")
        }
    }
    return this
}

/**
 * 用于展示请求成功状态下msg的扩展方法
 */
inline fun <T : SpeedyBean<*>> WrapperBean<T>.onSuccessMessage(dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: String.() -> Unit): WrapperBean<T> {
    if (error == null) {
        if (response?.msg().isNullOrEmpty().not()) CoroutineScope(dispatcher).launch {
            handle(response?.msg() ?: "")
        }
    }
    return this
}

/**
 * 用于展示请求成功状态下msg的扩展方法
 */
inline fun <T> WrapperBean<T>.onErrorMessage(dispatcher: CoroutineDispatcher = Dispatchers.Main, crossinline handle: String.() -> Unit): WrapperBean<T> {
    if (error != null) {
        error?.message?.let {
            if (it.isNullOrEmpty().not()) CoroutineScope(dispatcher).launch {
                handle(it)
            }
        }
    }
    return this
}

/**
 * 请求执行完毕的扩展方法
 */
inline fun <T> WrapperBean<T>.onComplete(dispatcher: CoroutineDispatcher = Dispatchers.Main, noinline handle: () -> Unit): WrapperBean<T> {
    completeFun = handle
    completeDispatcher = dispatcher
    return this
}

/**
 * 请求结果包装类
 */
typealias WrapperBean<T> = ResponseResultWrapper<out T>

/**
 * 请求事务lambda
 */
typealias RequestHandler<T> = suspend () -> T

/**
 * 请求结果事务lambda
 */
typealias ResponseHandler<T> = suspend (RequestHandler<out T>) -> WrapperBean<out T>

/**
 * 内部封装处理请求事务lambda
 */
typealias WorkHandler<T> = suspend ResponseHandler<out T>.() -> WrapperBean<out T>

/**
 * 请求结果lambda
 */
typealias ResultHandler<T> = suspend T.() -> Unit

infix fun <T> MutableLiveData<T>.use(value: T?) = setValue(value)

infix fun <T> MutableLiveData<T>.post(value: T?) = postValue(value)

inline fun <reified S> buildService(): S = Speedy.instance.SPEEDY_RETROFIT.create(S::class.java)