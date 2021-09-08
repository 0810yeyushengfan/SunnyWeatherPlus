package com.sunnyweather.android.logic.network


import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.DailyResponse
import com.sunnyweather.android.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

//用于访问天气信息API的Retrofit接口
interface WeatherService {

//    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
//    fun getRealtimeWeather(@Path("lng")lng:String,@Path("lat")lat:String): Call<RealtimeResponse>
//
//    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng,{lat}/daily.json")
//    fun getDailyWeather(@Path("lng")lng: String,@Path("lat")lat: String):Call<DailyResponse>

    //此处需注意，我用上面的代码运行会出现不能访问天气接口的错误，用下面的代码就会正常运行，我并不知道这两个的区别在哪，因此写下注释，以便后面查询错误的位置
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>

    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<DailyResponse>


}