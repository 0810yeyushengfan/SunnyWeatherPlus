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
    //在@GET注解指定的接口地址当中，这里使用了两个{lng}和{lat}占位符，然后又在getRealtimeWeather()方法中添加了两个lng和lat参数，并使用@Path("lng")和@Path("lat")注解来声明这两个参数。这样当调用getRealtimeWeather()方法发起请求时，Retrofit就会自动将lng和lat参数的值替换到占位符的位置，从而组成一个合法的请求地址。
    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/realtime.json")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>

    @GET("v2.5/${SunnyWeatherApplication.TOKEN}/{lng},{lat}/daily.json")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<DailyResponse>


}