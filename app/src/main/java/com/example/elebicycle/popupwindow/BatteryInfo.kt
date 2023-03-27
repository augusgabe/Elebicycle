package com.example.elebicycle.popupwindow

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import com.example.elebicycle.utils.ActivityCollector
import com.example.elebicycle.utils.LineChart
import lecho.lib.hellocharts.view.LineChartView


class BatteryInfo(val mainActivity: MainActivity) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    lateinit var tempLineChart:LineChartView
    lateinit var vLineChart:LineChartView
    lateinit var linChart_t:LineChart
    lateinit var linChart_v:LineChart

    //阈值设定界面
    @SuppressLint("InflateParams", "CutPasteId")
    fun start(){

        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.batteryiinfo, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.CENTER, 5, 5)

        //初始化lineChart
        tempLineChart = view.findViewById<LineChartView>(R.id.lineChart_t) //温度折线图
        vLineChart = view.findViewById<LineChartView>(R.id.lineChart_V) //电压折线图

        initCharts()
        //返回按钮点击事件
        val backBtn = view.findViewById<View>(R.id.turnback) as ImageView
        val batteryNum = view.findViewById<TextView>(R.id.batteryNum)
        val batteryPower = view.findViewById<TextView>(R.id.batteryPower)

        batteryNum.text = InfoViewModel.batteryNum
        batteryPower.text = InfoViewModel.batteryPower

        backBtn.setOnClickListener {
            //都关咯
            InfoViewModel.isTempLineChartOpen = false

            //弹窗消失
            popupWindow?.dismiss()
            popupWindow = null

            //注销广播并清除折线图的数据
            linChart_t.destroy()
            linChart_v.destroy()
        }


    }

    fun initCharts(){

        linChart_t = LineChart(mainActivity)
        linChart_v = LineChart(mainActivity)

        linChart_t.apply {
            setChart(tempLineChart)  //设置表
            //设置Y轴相关属性
            setYHight(13,3)
            setYName("温度: ℃")
            setWhichValue("temp") //设置接收广播时哪个值
            setBroadcast("com.example.trafficdetection.currentTValue") //设置广播接收的动作
            showChart()//展示折线图
        }

        linChart_v.apply {
            setChart(vLineChart)
            setYHight(3,1)
            setYName("电压: V")
            setWhichValue("V")
            setBroadcast("com.example.trafficdetection.currentADValue") //设置广播接收的动作
            showChart()
        }


    }
}