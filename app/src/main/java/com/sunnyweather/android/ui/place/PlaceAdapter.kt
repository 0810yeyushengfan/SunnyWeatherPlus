package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity

//为fragment_place中的recyclerview准备的适配器

class PlaceAdapter(private val fragment:PlaceFragment,private val placeList:List<Place>): RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val placeName: TextView =view.findViewById(R.id.placeName)
        val placeAddress:TextView=view.findViewById(R.id.placeAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.place_item,parent,false)
        val holder=ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val place = placeList[position]
            val activity = fragment.activity//调用fragment.getActivity()方法获取当前传入的fragment参数所在的活动
            //实现切换城市功能
            if (activity is WeatherActivity) {//若参数fragment所在的活动为WeatherActivity，证明是在已经进入了一个城市的天气后通过城市切换功能进行搜索的，只需要刷新当前页面即可，不需要跳转
                activity.drawerLayout.closeDrawers()
                activity.viewModel.locationLng = place.location.lng//赋值
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                activity.refreshWeather()//调用WeatherActivity的刷新天气函数
            } else {
                //若参数fragment所在的活动不是WeatherActivity，证明是从首页搜索城市，需要跳转至天气页面
                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }
                fragment.startActivity(intent)
                fragment.activity?.finish()
            }
            fragment.viewModel.savePlace(place)//记录选中的城市
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place=placeList[position]
        holder.placeName.text=place.name
        holder.placeAddress.text=place.address
    }

    override fun getItemCount(): Int {
        return  placeList.size
    }


}