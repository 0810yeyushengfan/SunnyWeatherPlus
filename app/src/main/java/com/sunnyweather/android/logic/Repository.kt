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

//仓库层的主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取，并将获得的数据返回给调用方。因此，仓库层有点像是一个数据获取与缓存的中间层，在本地没有缓存数据的情况下就去网络层获取，如果本地已经有缓存了，就直接将缓存数据返回。
//这种搜索城市数据的请求并没有太多缓存的必要，每次都发起网络请求去获取最新的数据即可，因此这里就不进行本地缓存的实现了，只需要进行网络请求获取资源
//网络请求获取最新数据的仓库层的统一封装

object Repository {

    //寻找位置信息
    fun searchPlaces(query:String)= fire(Dispatchers.IO) {
            val placeResponse=SunnyWeatherNetwork.searchPlaces(query)
            if(placeResponse.status=="ok"){
                val place=placeResponse.places
                Result.success(place)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
    }

    //刷新天气信息
    //在仓库层我们并没有提供两个分别用于获取实时天气信息和未来天气信息的方法，而是提供了一个refreshWeather()方法用来刷新天气信息。因为对于调用方而言，需要调用两次请求才能获得其想要的所有天气数据明显是比较烦琐的行为，因此最好的做法就是在仓库层再进行一次统一的封装
    fun refreshWeather(lng:String,lat:String)= fire(Dispatchers.IO) {
            coroutineScope {//由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域
                //只需要分别在两个async函数中发起网络请求，然后再分别调用它们的await()方法，就可以保证只有在两个网络请求都成功响应之后，才会进一步执行程序
                val deferredRealtime=async {
                    SunnyWeatherNetwork.getRealtimeWeather(lng,lat)
                }
                val deferredDaily=async {
                    SunnyWeatherNetwork.getDailyWeather(lng,lat)
                }
                val realtimeResponse=deferredRealtime.await()
                val dailyResponse=deferredDaily.await()
                //在同时获取到RealtimeResponse和DailyResponse之后，如果它们的响应状态都是ok
                if(realtimeResponse.status=="ok"&&dailyResponse.status=="ok"){
                    //那么就将Realtime和Daily对象取出并封装到一个Weather对象中
                    val weather= Weather(realtimeResponse.result.realtime,dailyResponse.result.daily)
                    //然后使用Result.success()方法来包装这个Weather对象
                    Result.success(weather)
                }else{
                    //否则就使用Result.failure()方法来包装一个异常信息
                    Result.failure(RuntimeException("realtime response status is ${realtimeResponse.status}"+ "daily response status is ${dailyResponse.status}"))
                }
            }
    }

    //一个按照liveData()函数的参数接收标准定义的一个高阶函数
    //在liveData()函数的代码块中，我们是拥有挂起函数上下文的，可是当回调到Lambda表达式中，代码就没有挂起函数上下文了，但实际上Lambda表达式中的代码一定也是在挂起函数中运行的。为了解决这个问题，我们需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的
    private fun<T> fire(context:CoroutineContext,block:suspend()->Result<T>)=liveData<Result<T>>(context){//在fire()函数的内部会先调用一下liveData()函数，然后在liveData()函数的代码块中统一进行了try catch处理，并在try语句中调用传入的Lambda表达式中的代码，最终获取Lambda表达式的执行结果并调用emit()方法发射出去。
            val result=try{
                block()
            }catch (e:Exception){
                Result.failure<T>(e)//这里的泛型T为List<Weather/Place>,上面liveData的泛型为Result<List<Weather/Place>>，因为result数据被Result包装过之后才发给了liveData
            }
            emit(result)
    }

    //对存储和读取SharedPreferences的数据方法进行封装
    fun savePlace(place:Place)=PlaceDao.savePlace(place)
    fun getSavePlace()=PlaceDao.getSavedPlace()
    fun isPlaceSaved()=PlaceDao.isPlaceSaved()

    //这是没有使用fire函数简便写法的方法
//    //LiveData()函数是lifecycle-livedata-ktx库提供的一个非常强大且好用的功能，它可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了
//    fun searchPlaces(query:String)= liveData(Dispatchers.IO) {//我们还将liveData()函数的线程参数类型指定成了Dispatchers.IO，这样代码块中的所有代码就都运行在子线程中了。众所周知，Android是不允许在主线程中进行网络请求的，诸如读写数据库之类的本地数据操作也是不建议在主线程中进行的，因此非常有必要在仓库层进行一次线程转换
//        val result=try{
//            //调用SunnyWeatherNetwork的searchPlaces()函数来搜索城市数据
//            val placeResponse=SunnyWeatherNetwork.searchPlaces(query)//因为LiveData()函数提供了挂起函数的上下文，所以我们能够调用挂起函数
//            if(placeResponse.status=="ok"){//判断如果服务器响应的状态是ok
//                val place=placeResponse.places;
//                Result.success(place)//使用Kotlin内置的Result.success()方法来包装获取的城市数据列表
//            }else{
//                //使用Result.failure()方法来包装一个异常信息
//                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
//            }
//
//        }catch (e:Exception){
//            Result.failure<List<Place>>(e)
//        }
//        //这个emit()方法其实类似于调用LiveData的setValue()方法来通知数据变化，只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法
//        emit(result)//使用emit()方法将包装的结果发射出去
//    }

}