package com.sunnyweather.android.logic.dao

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.Place

//实现记录选中的城市功能定义的一个方法类
object PlaceDao {

    //使用SharedPreferences存储
    private fun sharedPreferences()=SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

    //存储数据
    fun savePlace(place: Place){
        sharedPreferences().edit{
            putString("place",Gson().toJson(place))//Gson类的toJson()方法能将place转换为一个json类的字符串
        }
    }

    //读取数据
    fun getSavedPlace():Place{
        val placeJson=sharedPreferences().getString("place","")
        return Gson().fromJson(placeJson,Place::class.java)//Gson类的fromJson()方法能将json类型的字符串转换为其他类
    }

    //判断当前是否已有数据被存储
    fun isPlaceSaved()= sharedPreferences().contains("place")



}