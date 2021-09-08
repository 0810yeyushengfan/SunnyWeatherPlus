package com.sunnyweather.android.ui.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private  val viewModel by lazy{ViewModelProvider(this).get(WeatherViewModel::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng")?:""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName=intent.getStringExtra("place_name")?:""
        }
        viewModel.weatherLiveData.observe(this, { result->
            val weather=result.getOrNull()
            if(weather!=null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
    }
    private fun showWeatherInfo(weather: Weather){
        val placeName= findViewById(R.id.placeName) as TextView
        placeName.text=viewModel.placeName
        val realtime=weather.realtime
        val daily=weather.daily
        //填充now.xml布局中的数据
        val currentTempText="${realtime.temperature.toInt()}℃"
        val currentTemp=findViewById(R.id.currentTemp) as TextView
        currentTemp.text=currentTempText
        val currentSky=findViewById(R.id.currentSky) as TextView
        currentSky.text= getSky(realtime.skycon).info
        val currentPM25Text="空气指数${realtime.airQuality.aqi.chn.toInt()}"
        val currentAQI=findViewById(R.id.currentAQI) as TextView
        currentAQI.text=currentPM25Text
        val nowLayout=findViewById(R.id.nowLayout) as RelativeLayout
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        //填充forecast.xml布局中的数据
        val forecastLayout=findViewById(R.id.forecastLayout) as LinearLayout
        forecastLayout.removeAllViews()
        val days=daily.skycon.size
        for(i in 0 until days){
            val skycon=daily.skycon[i]
            val temperature =daily.temperature[i]
            val view=LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false)
            val dateInfo=view.findViewById(R.id.dataInfo) as TextView
            val skyIcon=view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo=view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo=view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text=simpleDateFormat.format(skycon.date)
            val sky= getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text=sky.info
            val tempText="${temperature.min.toInt()}~${temperature.max.toInt()}℃"
            temperatureInfo.text=tempText
            forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex=daily.lifeIndex
        val coldRiskText=findViewById(R.id.coldRiskText) as TextView
        val dressingText=findViewById(R.id.dressingText) as TextView
        val ultravioleText=findViewById(R.id.ultravioletText) as TextView
        val carWashingText=findViewById(R.id.carWashingText) as TextView
        val weatherLayout=findViewById(R.id.weatherLayout) as ScrollView
        coldRiskText.text=lifeIndex.coldRisk[0].desc
        dressingText.text=lifeIndex.dressing[0].desc
        ultravioleText.text=lifeIndex.ultraviolet[0].desc
        carWashingText.text=lifeIndex.carWashing[0].desc
        weatherLayout.visibility= View.VISIBLE
    }
}