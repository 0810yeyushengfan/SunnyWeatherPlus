package com.sunnyweather.android.ui.place

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
import com.sunnyweather.android.databinding.FragmentPlaceBinding

class PlaceFragment :Fragment(){

    //懒加载
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    //延迟初始化
    private lateinit var adapter: PlaceAdapter

    //利用视图绑定功能
    private var _binding: FragmentPlaceBinding? = null
    //在Kotlin和一般的编程中，您经常会遇到属性名前面有下划线。这通常意味着不打算直接访问该属性
    //get()意味着这个属性是“get-only”。这意味着您可以获得该值，但一旦分配，您就不能将它分配给其他东西
    private val binding get() = _binding!!//!!：表示忽略语言的判空检查，即允许程序报NullPointerException（在kotlin中一般不建议这种写法，除非使用处一定不为空）

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
                    binding.recyclerView.visibility = View.GONE
                    binding.bgImageView.visibility = View.VISIBLE
                    viewModel.placeList.clear()
                    adapter.notifyDataSetChanged()
                }
            }

        })
        viewModel.placeLiveData.observe(viewLifecycleOwner,{ result->
            val places=result.getOrNull()//获取结果内容，如果没有内容，返回null
            if(places!=null){
                binding.recyclerView.visibility=View.VISIBLE
                binding.bgImageView.visibility=View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            }else{
                Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_LONG).show()
                binding.recyclerView.visibility = View.GONE
                binding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }


//由于fragment的存在时间比视图长，所以在视图销毁时要先解除与fragment的视图绑定
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}