package com.example.elebicycle.popupwindow

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.enity.BreakDown
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import lecho.lib.hellocharts.view.LineChartView

class ReconnInfo_window(val mainActivity: MainActivity) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    fun start(){
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.reconnect_info, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        //返回按钮点击事件
        val backBtn = view.findViewById<View>(R.id.turnback_reconn) as ImageView
        val recyclerView = view.findViewById<RecyclerView>(R.id.reconn_recyclerview)

        recyclerView.layoutManager = LinearLayoutManager(mainActivity)
        recyclerView.adapter = MainViewModel.reconnAdapter

        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.CENTER, 5, 5)

        backBtn.setOnClickListener {
            //弹窗消失
            popupWindow?.dismiss()
            popupWindow = null

        }


    }



}