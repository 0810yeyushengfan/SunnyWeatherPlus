package com.sunnyweather.android.ui.place

import androidx.lifecycle.*
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }

    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    //对存储和读取SharedPreferences的数据方法进行封装
    fun savePlace(place:Place)=Repository.savePlace(place)
    fun getSavedPlace()= Repository.getSavePlace()
    fun isPlaceSaved()=Repository.isPlaceSaved()



}