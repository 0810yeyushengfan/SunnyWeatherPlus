package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

data class RealtimeResponse(val status:String,val result:Result){

    //因为kotlin中也有Result类,而且在DailyResponse类中也要使用Result类，所以这里的Result要用内部类的形式使用
    data class Result(val realtime:Realtime)

    data class Realtime(val skycon:String,val temperature:Float,@SerializedName("air_quality")val airQuality:AirQuality)

    data class AirQuality(val aqi:AQI)

    data class AQI(val chn:Float)

}