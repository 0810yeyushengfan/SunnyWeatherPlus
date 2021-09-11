package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    //懒加载
    val viewModel by lazy{ViewModelProvider(this).get(WeatherViewModel::class.java)}

    //实现城市切换功能
    lateinit var drawerLayout:DrawerLayout

    //实现刷新天气功能
    private lateinit var swipeRefresh:SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //设置背景图与状态栏融合到一起的效果
        val decorView=window.decorView
        decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor= Color.TRANSPARENT
        setContentView(R.layout.activity_weather)
        swipeRefresh=findViewById(R.id.swipeRefresh) as SwipeRefreshLayout

        if(viewModel.locationLng.isEmpty()){
            viewModel.locationLng=intent.getStringExtra("location_lng")?:""
        }
        if(viewModel.locationLat.isEmpty()){
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
        }
        if(viewModel.placeName.isEmpty()){
            viewModel.placeName=intent.getStringExtra("place_name")?:""
        }

        //观察天气信息
        viewModel.weatherLiveData.observe(this){ result->
            val weather=result.getOrNull()
            if(weather!=null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing=false//已经获取完毕天气，停止刷新行为
        }

        //手动切换城市功能
        val navBtn=findViewById(R.id.navBtn) as Button
        drawerLayout=findViewById(R.id.drawerLayout) as DrawerLayout
        navBtn.setOnClickListener{
            drawerLayout.openDrawer(GravityCompat.START)//打开滑动菜单
        }

        //定义滑动菜单的监听器
        drawerLayout.addDrawerListener(object:DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }

            //当滑动菜单隐藏的时候也要隐藏输入法
            override fun onDrawerClosed(drawerView: View) {
                val manager=getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })

        //手动刷新天气功能
        swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
    }

    //刷新天气函数
    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        swipeRefresh.isRefreshing=true//获取信息,开始刷新行为
    }

    //获取并展示天气信息函数
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