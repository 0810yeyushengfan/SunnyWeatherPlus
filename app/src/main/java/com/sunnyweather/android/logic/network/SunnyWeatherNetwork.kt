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

    private val weatherService = ServiceCreator.create(WeatherService::class.java)

    suspend fun getDailyWeather(lng: String, lat: String) = weatherService.getDailyWeather(lng, lat).await()

    suspend fun getRealtimeWeather(lng: String, lat: String) = weatherService.getRealtimeWeather(lng, lat).await()

    private val placeService = ServiceCreator.create(PlaceService::class.java)

    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            //调用enqueue()方法，Retrofit就会根据注解中配置的服务器接口地址去进行网络请求，服务器响应的数据会回调到enqueue()方法中传入的Callback实现里面,需要注意的是，当发起请求的时候，Retrofit会自动在内部开启子线程，当数据回调到Callback中之后，Retrofit又会自动切换回主线程，整个操作过程中我们都不用考虑线程切换问题
            enqueue(object : Callback<T> {
                //网络请求成功之后回调此方法,并将结果放入response中
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    //在Callback的onResponse()方法中，调用response.body()方法将会得到Retrofit解析后的对象，也就是PlaceResponse类型的数据
                    val body = response.body()
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