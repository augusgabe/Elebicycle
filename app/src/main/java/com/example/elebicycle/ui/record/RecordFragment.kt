package com.example.elebicycle.ui.record

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.databinding.RecordFragmentBinding
import com.example.elebicycle.enity.BreakDown
import com.example.elebicycle.enity.EleBicycle
import com.example.elebicycle.enity.EleBicycleAdapter
import com.example.elebicycle.popupwindow.BreakDownInfo_window
import com.example.elebicycle.ui.setting.SettingViewModel
import com.example.elebicycle.utils.ToastUtils
import kotlin.concurrent.thread

class RecordFragment : Fragment() {

    private var _binding: RecordFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    private lateinit var eleBicycleAdapter: EleBicycleAdapter

    private val UPDATE_LIST = 1

    val handler = @SuppressLint("HandlerLeak") object: Handler(){
        override fun handleMessage( msg: Message){
            Log.d("Handler", msg.what.toString())
            when(msg.what){

                //更新列表
                UPDATE_LIST->{
                    eleBicycleAdapter = EleBicycleAdapter(RecordViewModel.eleBicycleList)
                    binding.bicycleRecyclerView.adapter = eleBicycleAdapter
                }


            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RecordFragmentBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity
        val layoutManager = LinearLayoutManager(context)

        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = true;//列表再底部开始展示，反转后由上面开始展示
        layoutManager.reverseLayout = true;//列表翻转

        binding.bicycleRecyclerView.layoutManager = layoutManager

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        //初始化列表
        initElebicycleInfo()

        //依据车辆编号搜索车辆历史故障信息
        binding.searchBtn.setOnClickListener {

            //数据库操作是耗时操作，不允许在主线程中使用
            thread {
                Looper.prepare()
                //获取车辆编号
                val ebNum:String  = binding.bicycleId.text.toString()

                //数据库搜索
                val bDInfo = MainViewModel.bDInfoDao.loadBDInfoByEbId(ebNum)

                if(bDInfo.isEmpty()){
                    ToastUtils.showToast(context,"查询不到该编号车辆故障信息！")
                }else{

                    //开启弹窗,传入一个故障类
                    val breakdown_Info_window = BreakDownInfo_window(mainActivity,bDInfo[0])

                    breakdown_Info_window.start()

                }

                Looper.loop()
            }


        }

        binding.deleteBtn.setOnClickListener {
            deleteList()
        }

    }

    fun  initElebicycleInfo(){

        //从数据库中加载所有的行车记录列表
        thread {
            //先清空
            RecordViewModel.eleBicycleList.clear()

            //通过用户名查询获得的行程记录
            RecordViewModel.eleBicycleList =
                MainViewModel.eleBicycleDao.
                    loadBicyclesByUser(SettingViewModel.username) as ArrayList<EleBicycle>

            val msg = Message()
            msg.what = UPDATE_LIST
            handler.sendMessage(msg)

        }

    }

    //依据编号搜索行车记录
//    fun updateList(){
//
//        val ebNum = binding.bicycleId.text.toString()
//
//        thread {
//            //先清空
//            RecordViewModel.eleBicycleList.clear()
//
//            RecordViewModel.eleBicycleList =
//                MainViewModel.eleBicycleDao.loadBicyclesByEbId(ebId) as ArrayList<EleBicycle>
//
//            val msg = Message()
//            msg.what = UPDATE_LIST
//            handler.sendMessage(msg)
//
//        }
//
//    }

    fun deleteList(){

        thread {
            //删除所有车
            MainViewModel.eleBicycleDao.deleteAllBicycle()

            //清空列表
            RecordViewModel.eleBicycleList.clear()

            val msg = Message()
            msg.what = UPDATE_LIST
            handler.sendMessage(msg)
        }
    }
}