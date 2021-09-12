package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//统一的网络数据源访问入口的封装

object SunnyWeatherNetwork {

    private val weatherService = ServiceCreator.create<WeatherService>()

    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()

    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()

    //使用ServiceCreator创建了一个PlaceService接口的动态代理对象
    private val placeService = ServiceCreator.create<PlaceService>()

    //定义了一个searchPlaces()函数，并在这里调用刚刚在PlaceService接口中定义的searchPlaces()方法，并利用它的返回值调用自定义的await()方法，以发起搜索城市数据请求
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    //当外部调用SunnyWeatherNetwork的searchPlaces()函数时，Retrofit就会立即发起网络请求，同时当前的协程也会被阻塞住。直到服务器响应我们的请求之后，await()函数会将解析出来的数据模型对象取出并返回，同时恢复当前协程的执行，searchPlaces()函数在得到await()函数的返回值后会将该数据再返回到上一层
    private suspend fun <T> Call<T>.await(): T {//首先await()函数仍然是一个挂起函数，然后我们给它声明了一个泛型T，并将await()函数定义成了Call<T>的扩展函数，这样所有返回值是Call类型的Retrofit网络请求接口就都可以直接调用await()函数了
        return suspendCoroutine { continuation ->//await()函数中使用了suspendCoroutine函数来挂起当前协程
            //调用enqueue()方法，Retrofit就会根据注解中配置的服务器接口地址去进行网络请求，服务器响应的数据会回调到enqueue()方法中传入的Callback实现里面,需要注意的是，当发起请求的时候，Retrofit会自动在内部开启子线程，当数据回调到Callback中之后，Retrofit又会自动切换回主线程，整个操作过程中我们都不用考虑线程切换问题
            enqueue(object : Callback<T> {//由于扩展函数的原因，我们现在拥有了Call对象的上下文，那么这里就可以直接调用enqueue()方法让Retrofit发起网络请求
                //网络请求成功之后回调此方法,并将结果放入response中
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    //在Callback的onResponse()方法中，调用response.body()方法将会得到Retrofit解析后的对象，也就是PlaceResponse类型的数据
                    val body = response.body()
                //，在onResponse()回调当中，我们调用body()方法解析出来的对象是可能为空的。如果为空的话，要手动抛出一个异常
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }
                //网络请求失败之后回调此方法，并将异常信息放入t中
                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}