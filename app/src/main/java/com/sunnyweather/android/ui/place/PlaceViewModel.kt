package com.sunnyweather.android.ui.place

import androidx.lifecycle.*
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place


//通常来讲，比较好的编程规范是给每一个Activity和Fragment都创建一个对应的ViewModel
//ViewModel的一个重要作用就是可以帮助Activity分担一部分工作，它是专门用于存放与界面相关的数据的。也就是说，只要是界面上能看得到的数据，它的相关变量都应该存放在ViewModel中，而不是Activity中，这样可以在一定程度上减少Activity中的逻辑
//ViewModel还有一个非常重要的特性。我们都知道，当手机发生横竖屏旋转的时候，Activity会被重新创建，同时存放在Activity中的数据也会丢失。而ViewModel的生命周期和Activity不同，它可以保证在手机屏幕发生旋转的时候不会被重新创建，只有当Activity退出的时候才会跟着Activity一起销毁。因此，将与界面相关的变量存放在ViewModel当中，这样即使旋转手机屏幕，界面上显示的数据也不会丢失
//LiveData之所以能够成为Activity与ViewModel之间通信的桥梁，并且还不会有内存泄漏的风险，靠的就是Lifecycles组件。LiveData在内部使用了Lifecycles组件来自我感知生命周期的变化，从而可以在Activity销毁的时候及时释放引用，避免产生内存泄漏的问题。
//另外，由于要减少性能消耗，当Activity处于不可见状态的时候（比如手机息屏，或者被其他的Activity遮挡），如果LiveData中的数据发生了变化，是不会通知给观察者的。只有当Activity重新恢复可见状态时，才会将数据通知给观察者，而LiveData之所以能够实现这种细节的优化，依靠的还是Lifecycles组件。
//还有一个小细节，如果在Activity处于不可见状态的时候，LiveData发生了多次数据变化，当Activity恢复可见状态时，只有最新的那份数据才会通知给观察者，前面的数据在这种情况下相当于已经过期了，会被直接丢弃。
//PlaceViewModel的整体工作流程:首先，当外部调用PlaceViewModel的searchPlaces()方法来获取用户数据时，并不会发起任何请求或者函数调用，只会将传入的query值设置到searchLiveData当中。一旦searchLiveData的数据发生变化，那么观察searchLiveData的switchMap()方法就会执行，并且调用我们编写的转换函数。然后在转换函数中调用Repository.searchPlaces()方法获取真正的搜索城市结果。同时，switchMap()方法会将Repository.searchPlaces()方法返回的LiveData对象转换成一个可观察的LiveData对象，对于PlaceFragment而言，只要去观察这个LiveData对象就可以了
class PlaceViewModel : ViewModel() {

    //LiveData是Jetpack提供的一种响应式编程组件，它可以包含任何类型的数据，并在数据发生变化的时候通知给观察者。LiveData特别适合与ViewModel结合在一起使用，虽然它也可以单独用在别的地方，但是在绝大多数情况下，它是使用在ViewModel当中的。
    //这里我们将searchLiveData变量修改成了一个MutableLiveData对象，并指定它的泛型为String，表示它包含的是字符串数据。MutableLiveData是一种可变的LiveData
    //将searchLiveData声明成了private，以保证数据的封装性
    private val searchLiveData = MutableLiveData<String>()

    //placeList是在Fragment中需要使用的一个List<Place>类型的数据(主要用于RecyclerView的显示)，需要放在对应的PlaceViewModel中
    val placeList = ArrayList<Place>()

    //如果ViewModel中的某个LiveData对象是调用另外的方法获取的，那么我们就可以借助switchMap()方法，将这个LiveData对象转换成另外一个可观察的LiveData对象
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->//switchMap()方法同样接收两个参数：第一个参数传入我们新增的searchLiveData，switchMap()方法会对它进行观察；第二个参数是一个转换函数，注意，我们必须在这个转换函数中返回一个LiveData对象，因为switchMap()方法的工作原理就是要将转换函数中返回的LiveData对象转换成另一个可观察的LiveData对象。那么很显然，我们只需要在转换函数中调用Repository的searchPlaces()方法来得到LiveData对象，并将它返回就可以了
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