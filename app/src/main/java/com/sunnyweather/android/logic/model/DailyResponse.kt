package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class DailyResponse(val status:String,val result:Result){

    //因为kotlin中也有Result类,而且在RealtimeResponse类中也要使用Result类，所以这里的Result要用内部类的形式使用
    data class Result(val daily:Daily)

    data class Daily(val temperature:List<Temperature>,val skycon:List<Skycon>,@SerializedName("life_index") val lifeIndex:LifeIndex)

    data class Temperature(val max:Float,val min:Float)

    data class Skycon(val value:String,val date:Date)

    data class LifeIndex(val coldRisk:List<LifeDescription>,val carWashing:List<LifeDescription>,val ultraviolet:List<LifeDescription>,val dressing:List<LifeDescription>)

    data class LifeDescription(val desc:String)

}