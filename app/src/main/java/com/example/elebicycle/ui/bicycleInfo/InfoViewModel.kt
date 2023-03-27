package com.example.elebicycle.ui.bicycleInfo

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elebicycle.R
import java.util.*

@SuppressLint("StaticFieldLeak")
object InfoViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    //界面初始化相关变量,默认
    var bIdValue = "未连接"
    var lockValue = "申请开锁"
    var lockImage = R.drawable.lock
    var rideTime = "00:00:00"
    var position = "湖南工商大学"
    var batteryPower = "0A"//电池电流大小
    var batteryNum = "000"//电池编号
    var byState = "正常"

    //是否解锁
    var islocked = true

    var infoView: View? = null

    var el_state = MutableLiveData<String>("off") //电磁锁状态

    var el_num = "01"

    //左右转向灯控制
    var left_light_state = MutableLiveData("off")
    var right_light_state = MutableLiveData("off")

    //折线图
    var firstTime = true
    var baseTemp = "0" //基础温度
    private var tem="0"  //温度
    private var V="0"  //电压

    var isTempLineChartOpen = false

    //车状态示警灯
    var bicycle_showLight = "green"

    var now_angle = MutableLiveData("000")//直流电机当前进度
    var positionTimer: Timer? = null
    var isChange = true  //是否要改变

    fun getTem():String {
        return this.tem
    }
    fun setTem( tem:String) {
        this.tem = tem
    }

    fun getV():String {
        return this.V
    }
    fun setV( V:String) {
        this.V = V
    }

}