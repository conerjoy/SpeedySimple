# 关于

这是一个利用Retrofit+协程+ViewModel的便捷、精简发起网络请求的工具

# 如何使用

## 1、依赖

    implementation 'io.github.conerjoy:Speedy:1.0.5'
  
## 2、初始化

    Speedy.instance.init(this) {
        baseUrl("http://v.juhe.cn/")
        addConverterFactory(GsonConverterFactory.create())
    }
    //        Speedy.instance.init(this, mRetrofit)
    Speedy.instance.showToast = {
        ToastUtils.showShort(it)
    }
    Speedy.NETWORK_ERROR_TOAST = true // 全局生效
    
## 3、实现接口

    open class ResponseResult<T> : SpeedyBean<T> {
        var reason: String = ""
        var result: T? = null
        var error_code: Int = -1

        override fun isSuccess() : Boolean = error_code == 0

        override fun message(): String = reason
    }
    
## 4、使用

    class MainViewModel : BaseVM() {
        private val _result = MutableLiveData<String>()
        val result: LiveData<String> = _result

        fun getResult() {
            launch<ResponseResult<Any>> {
                invoke {
                    buildService<Test>().getSomething(mapOf("key" to "xxxx"))
                }.onSuccess {
                    _result use result.toString()
                }.onApiError {
                    LogUtils.e("业务错误")
                }.onError {
                    LogUtils.e("请求错误")
                    LogUtils.e(message)
                }.onComplete {
                    LogUtils.e("请求结束")
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
