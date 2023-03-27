package com.example.elebicycle.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.dao.EleBicycleDao
import com.example.elebicycle.enity.EleBicycle
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import com.example.elebicycle.ui.setting.SettingViewModel
import com.example.elebicycle.utils.MyApp.Companion.context

import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class DataSend(val mainActivity: MainActivity,val infoView:View) {

    private val CONTROL_SLA = "sl" //声光报警器
    private val CONTROL_EL = "el" //电磁锁
    private val CONTROL_LE = "le" //电磁锁

    private val OPEN = 1
    private val CLOSE = 2

    val lockImage = infoView.findViewById<ImageView>(R.id.lockImage)
    val unlockText = infoView.findViewById<TextView>(R.id.unlock_text)
    val timeValue = infoView.findViewById<Chronometer>(R.id.time_value)
    val seekbar = infoView.findViewById<SeekBar>(R.id.emSpeed_control)

    val handler = @SuppressLint("HandlerLeak") object: Handler(){
        override fun handleMessage( msg: Message){
            Log.d("Handler", msg.what.toString())
            when(msg.what){
                OPEN->{
                    //图标改为开锁
                    lockImage.setImageResource(R.drawable.unlock)
                    unlockText.text = "已开锁"
                    ToastUtils.showToast(context,"开锁成功！")

                    InfoViewModel.lockImage = R.drawable.unlock
                    InfoViewModel.lockValue = "已开锁"
                    InfoViewModel.islocked = false
                    timeValue.apply {
                        base =  System.currentTimeMillis()
                        start()
                        }

                }

                CLOSE->{
                    //图标改为关锁
                    lockImage.setImageResource(R.drawable.lock)
                    unlockText.text = "申请开锁"
                    ToastUtils.showToast(context,"锁已关闭！")

                    InfoViewModel.lockImage = R.drawable.lock
                    InfoViewModel.lockValue = "申请开锁"
                    InfoViewModel.islocked = true//锁已关
                    seekbar.progress = 0

                    //位置停止变化
                    InfoViewModel.positionTimer?.cancel()
                    InfoViewModel.positionTimer = null

                    timeValue.apply {
                        Log.d("test",InfoViewModel.rideTime)
                        stop()
                    }
                }
            }

        }
    }


    /***
     *
     * 发送命令
     *
     * **/
    @SuppressLint("UseSwitchCompatOrMaterialCode", "SimpleDateFormat")
    fun sendCtrlCmd(num:String, status:String, type:String) {

        when(type){
          //控制电插锁
            CONTROL_EL->{
                if (!TextUtils.isEmpty(num)) {
                    if (status == "on") {
                        //关闭
                        val order = MessageFormat.format("Hwcel{0}03offT", num);
                        mainActivity.sendMsg(order)
                        InfoViewModel.el_state.value="off"
                        ToastUtils.showToast(context, "电磁锁关闭指令已发送")

                        //位置信息不让能改变了
                        InfoViewModel.isChange =  false

                        thread {
                                Thread.sleep(1000)
                                val msg = Message()
                                msg.what = 2
                                handler.sendMessage(msg)
                            }

                        //记录行车数据，将其存入数据库中
                        //先获取了UserDao的实例

//                      获取系统当前时间,(数据库相关)
                        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                        val date = Date(System.currentTimeMillis())
                        val time = simpleDateFormat.format(date)

                        val eleBicycleDao = MainViewModel.eleBicycleDao

                        val eleBicycle = EleBicycle(InfoViewModel.bIdValue,
                            SettingViewModel.username,InfoViewModel.byState,InfoViewModel.position,
                            InfoViewModel.rideTime,time)

                        //用户行程联系表



                        thread {
                            //在线程中要手动创建looper才能使用handler,ToastUtils相当于一个handler,主线程中默认自动创建了
                            eleBicycle.ebId = eleBicycleDao.insertBicycle(eleBicycle)
                        }


                    } else if (status == "off") {
                        //打开
                        val order = MessageFormat.format("Hwcel{0}02onT", num)
                        mainActivity.sendMsg(order)
                        InfoViewModel.el_state.value="on"
                        ToastUtils.showToast(context, "电磁锁打开指令已发送")

                        //位置可以开始改变
                        InfoViewModel.isChange = true

                        //延时后更改UI，模拟申请解锁
                        thread {
                            Thread.sleep(1000)
                            val msg = Message()
                            msg.what = 1
                            handler.sendMessage(msg)
                        }
                    }
                } else {
                    ToastUtils.showToast(context, "设备编号获取中...")
                }
            }



        }
    }

    //发送任意数据帧
    fun sendFrame(dataFrame:String){
        mainActivity.sendMsg(dataFrame)
    }
}