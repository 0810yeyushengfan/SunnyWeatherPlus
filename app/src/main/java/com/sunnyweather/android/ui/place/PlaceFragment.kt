package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment :Fragment(){

    //懒加载
    //这里最需要注意的是，我们绝对不可以直接去创建ViewModel的实例，而是一定要通过ViewModelProvider来获取ViewModel的实例
    //之所以要这么写，是因为ViewModel有其独立的生命周期，并且其生命周期要长于Fragment。如果我们在onActivityCreated()方法中创建ViewModel的实例，那么每次onActivityCreated()方法执行的时候，ViewModel都会创建一个新的实例，这样当手机屏幕发生旋转的时候，就无法保留其中的数据了
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    //延迟初始化
    private lateinit var adapter: PlaceAdapter

    //利用视图绑定功能
    // 在Kotlin和一般的编程中，您经常会遇到属性名前面有下划线。这通常意味着不打算直接访问该属性
    private var _binding: FragmentPlaceBinding? = null
    //get()意味着这个属性是“get-only”。这意味着您可以获得该值，但一旦分配，您就不能将它分配给其他东西
    private val binding get() = _binding!!//!!：表示忽略语言的判空检查，即允许程序报NullPointerException（在kotlin中一般不建议这种写法，除非使用处一定不为空）

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //调用生成的绑定类中包含的静态 inflate() 方法。此操作会创建该绑定类的实例以供 Fragment 使用
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        //每个绑定类还包含一个 getRoot() 方法，用于为相应布局文件的根视图提供直接引用
        //通过调用getRoot()方法或使用Kotlin属性语法获取对根视图的引用，并从onCreateView()方法返回根视图，使其成为屏幕上的活动视图。
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //如果有历史记录城市，则直接打开此城市的天气
        //为了防止在打开滑动菜单切换城市时发生无限跳转，需要判断当前状态是否为MainActivity,只有是的时候才能进行直接打开历史城市的操作
        if(activity is MainActivity&&viewModel.isPlaceSaved()){//如果已经选择过城市
            val place=viewModel.getSavedPlace()//直接获取之前选择的城市并跳转该城市
            val intent= Intent(context,WeatherActivity::class.java).apply{//这里的context其实是this.getContext，this是当前Fragment
                putExtra("location_lng",place.location.lng)
                putExtra("location_lat",place.location.lat)
                putExtra("place_name",place.name)
            }
            startActivity(intent)
            activity?.finish()//这里其实是this.getActivity()，this是当前Fragment
            return
        }

        val layoutManager = LinearLayoutManager(activity)//这里其实是this.getActivity()，this是当前Fragment
        binding.recyclerView.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        binding.recyclerView.adapter = adapter

        binding.searchPlaceEdit.addTextChangedListener(object : TextWatcher {//匿名实现TextWatcher接口
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            //重写afterTextChange方法，在搜索框文字改变之后会回调此方法
            override fun afterTextChanged(editable: Editable?) {
                val content = editable.toString()//获取搜索框文字
                if (content.isNotEmpty()) {//String.isNotEmpty()方法，在String的length>0时返回true，=0时返回false
                    viewModel.searchPlaces(content)
                } else {
                    binding.recyclerView.visibility = View.GONE//隐藏搜索城市列表
                    binding.bgImageView.visibility = View.VISIBLE//显示背景图片
                    viewModel.placeList.clear()//清空之前的城市搜索
                    adapter.notifyDataSetChanged()//以没有搜索到城市来更新搜索城市列表
                }
            }

        })

        //这里调用了viewModel.placeLiveData的observe()方法来观察数据的变化。经过对PlaceViewModel的改造，现在placeLiveData变量已经变成了一个LiveData对象，任何LiveData对象都可以调用它的observe()方法来观察数据的变化。observe()方法接收两个参数：第一个参数是一个LifecycleOwner对象，第二个参数是一个Observer接口，当placeLiveData中包含的数据发生变化时，就会回调到这里，因此我们在这里将最新的搜索结果更新到界面上即可。
        //lifecycle-livedata-ktx就是一个专门为Kotlin语言设计的库，这个库在2.2.0版本中加入了对observe()方法的语法扩展，能够在调用observe()方法时对Observe接口使用Java函数式API的写法
        viewModel.placeLiveData.observe(viewLifecycleOwner){ result->   //在fragment中使用getViewLifecycleOwner来代替this
            val places=result.getOrNull()//获取结果内容，如果没有内容，返回null
            if(places!=null){
                binding.recyclerView.visibility=View.VISIBLE//显示搜索城市列表
                binding.bgImageView.visibility=View.GONE//隐藏背景图片
                viewModel.placeList.clear()//清空之前的城市搜索
                viewModel.placeList.addAll(places)//添加新的搜索到的城市
                adapter.notifyDataSetChanged()//以新的搜索到的城市来更新搜索城市列表
            }else{
                Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_LONG).show()
                binding.recyclerView.visibility = View.GONE//隐藏搜索城市列表
                binding.bgImageView.visibility = View.VISIBLE//显示背景图片
                viewModel.placeList.clear()//清空之前的城市搜索
                adapter.notifyDataSetChanged()//以没有搜索到城市来更新搜索城市列表
                result.exceptionOrNull()?.printStackTrace()//报异常
            }
        }
    }


//由于fragment的存在时间比视图长，所以在视图销毁时要先解除与fragment的视图绑定
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}