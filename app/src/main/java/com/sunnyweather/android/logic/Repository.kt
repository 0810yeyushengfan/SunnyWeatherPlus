package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
//import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

//网络请求获取最新数据的仓库层的统一封装

object Repository {

    //寻找位置信息
    fun searchPlaces(query:String)= fire(Dispatchers.IO) {
            val placeResponse=SunnyWeatherNetwork.searchPlaces(query)
            if(placeResponse.status=="ok"){
                val place=placeResponse.places;
                Result.success(place)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
    }

    //刷新天气信息
    fun refreshWeather(lng:String,lat:String)= fire(Dispatchers.IO) {
            coroutineScope {
                val deferredRealtime=async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily=async {
                    SunnyWeatherNetwork.getDailyWeather(lng,lat)
                }
                val realtimeResponse=deferredRealtime.await()
                val dailyResponse=deferredDaily.await()
                if(realtimeResponse.status=="ok"&&dailyResponse.status=="ok"){
                    val weather= Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    Result.success(weather)
                }else{
                    Result.failure(RuntimeException("realtime response status is ${realtimeResponse.status}"+ "daily response status is ${dailyResponse.status}"))
                }
            }
    }

    private fun<T> fire(context:CoroutineContext,block:suspend()->Result<T>)=liveData<Result<T>>(context){
            val result=try{
                block()
            }catch (e:Exception){
                Result.failure<T>(e)
            }
            emit(result)
    }

    //对存储和读取SharedPreferences的数据方法进行封装
    fun savePlace(place:Place)=PlaceDao.savePlace(place)
    fun getSavePlace()=PlaceDao.getSavedPlace()
    fun isPlaceSaved()=PlaceDao.isPlaceSaved()

    //这是没有使用fire函数简便写法的方法
//    fun searchPlaces(query:String)= liveData(Dispatchers.IO) {
//        val result=try{
//                        val placeResponse=SunnyWeatherNetwork.searchPlaces(query)
//            if(placeResponse.status=="ok"){
//                val place=placeResponse.places;
//                Result.success(place)
//            }else{
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//
//        }catch (e:java.lang.Exception){
//            Result.failure<List<Place>>(e)
//        }
//        emit(result)
//    }

}