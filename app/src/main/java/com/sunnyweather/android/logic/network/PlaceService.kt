package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//用于访问彩云天气城市搜索API的Retrofit接口

interface PlaceService {//Retrofit的接口文件建议以具体的功能种类名开头，并以Service结尾，这是一种比较好的命名习惯

    //这里使用了一个@GET注解，表示当调用searchPlaces()方法时Retrofit会发起一条GET请求，请求的地址就是我们在@GET注解中传入的具体参数。注意，这里只需要传入请求地址的相对路径即可
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")

    //searchPlaces()方法的返回值必须声明成Retrofit中内置的Call类型，并通过泛型来指定服务器响应的数据应该转换成什么对象,由于服务器响应的是一个包含PlaceResponse数据的JSON数组，因此这里我们将泛型声明成PlaceResponse
    //这里在searchPlaces()方法中添加了query这个参数，并使用@Query注解对它进行声明。这样当发起网络请求的时候，Retrofit就会自动按照带参数GET请求的格式将这个参数构建到请求地址当中
    fun searchPlaces(@Query("query") query: String): Call<PlaceResponse>


}