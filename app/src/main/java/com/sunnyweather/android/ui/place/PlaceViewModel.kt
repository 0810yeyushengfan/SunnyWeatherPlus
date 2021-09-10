package com.sunnyweather.android.ui.place

import androidx.lifecycle.*
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place


//通常来讲，比较好的编程规范是给每一个Activity和Fragment都创建一个对应的ViewModel
//ViewModel的一个重要作用就是可以帮助Activity分担一部分工作，它是专门用于存放与界面相关的数据的。也就是说，只要是界面上能看得到的数据，它的相关变量都应该存放在ViewModel中，而不是Activity中，这样可以在一定程度上减少Activity中的逻辑
//ViewModel还有一个非常重要的特性。我们都知道，当手机发生横竖屏旋转的时候，Activity会被重新创建，同时存放在Activity中的数据也会丢失。而ViewModel的生命周期和Activity不同，它可以保证在手机屏幕发生旋转的时候不会被重新创建，只有当Activity退出的时候才会跟着Activity一起销毁。因此，将与界面相关的变量存放在ViewModel当中，这样即使旋转手机屏幕，界面上显示的数据也不会丢失
class PlaceViewModel : ViewModel() {

    //LiveData是Jetpack提供的一种响应式编程组件，它可以包含任何类型的数据，并在数据发生变化的时候通知给观察者。LiveData特别适合与ViewModel结合在一起使用，虽然它也可以单独用在别的地方，但是在绝大多数情况下，它是使用在ViewModel当中的。
    //这里我们将searchLiveData变量修改成了一个MutableLiveData对象，并指定它的泛型为String，表示它包含的是字符串数据。MutableLiveData是一种可变的LiveData
    private val searchLiveData = MutableLiveData<String>()

    //placeList是在Fragment中需要使用的一个List<Place>类型的数据(主要用于RecyclerView的显示)，需要放在对应的PlaceViewModel中
    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    //setValue()方法用于给LiveData设置数据，但是只能在主线程中调用
    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    //对存储和读取SharedPreferences的数据方法进行封装
    fun savePlace(place:Place)=Repository.savePlace(place)
    fun getSavedPlace()= Repository.getSavePlace()
    fun isPlaceSaved()=Repository.isPlaceSaved()



}