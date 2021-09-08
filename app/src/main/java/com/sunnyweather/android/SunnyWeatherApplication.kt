package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//在使用MVVM分层架构设计的时候，为了防止出现缺Context的现象，给项目提供一种全局获取Context的方式
class SunnyWeatherApplication : Application(){
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN="baN3iPcUGx1r0IFa"
    }
    override fun onCreate(){
        super.onCreate()
        context=applicationContext

    }
}