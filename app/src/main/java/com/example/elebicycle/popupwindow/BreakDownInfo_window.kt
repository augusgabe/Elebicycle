package com.example.elebicycle.popupwindow

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.elebicycle.MainActivity
import com.example.elebicycle.R
import com.example.elebicycle.enity.BreakDown

class BreakDownInfo_window(val mainActivity: MainActivity,val bDInfo: BreakDown) {

    //弹窗相关
    private var popupWindow: PopupWindow? = null

    fun start(){
        //创建弹出窗口视图，和阈值设置界面布局联系
        val view: View =
            LayoutInflater.from(mainActivity).inflate(R.layout.break_down_window, null)
        popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        //设置弹窗位置
        popupWindow?.showAtLocation(view, Gravity.CENTER, 5, 5)

        //返回按钮点击事件
        val backBtn = view.findViewById<View>(R.id.turnback_break) as ImageView
        val ebNum = view.findViewById<TextView>(R.id.break_info_ebNum)
        val bNum = view.findViewById<TextView>(R.id.break_info_bNum)
        val bType = view.findViewById<TextView>(R.id.break_info_bType)
        val bTime = view.findViewById<TextView>(R.id.break_info_bTime)

        ebNum.text = bDInfo.ebNum
        bNum.text = bDInfo.bNum.toString()
        bType.text = bDInfo.bType
        bTime.text = bDInfo.bTime


        backBtn.setOnClickListener {
            //弹窗消失
            popupWindow?.dismiss()
            popupWindow = null

        }


    }



}