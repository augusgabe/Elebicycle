package com.example.elebicycle.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.enity.BreakDown
import com.example.elebicycle.enity.EleBicycle
import com.example.elebicycle.enity.ReconnAdapter
import com.example.elebicycle.enity.ReconnInfo
import com.example.elebicycle.popupwindow.ReconnInfo_window
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import com.example.elebicycle.utils.MyApp.Companion.context
import java.text.DecimalFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class DataHandle(private val mainActivity: MainActivity,val view:View) {

    //数据展示
    private val CARD = "cc" //卡号
    private val RFID = "ID" //车编号
    private val BY = "by" //车编号
    private val BA = "ba" //电池编号
    private val TLight = "le"   //左右转向灯
    private val HO = "ho"   //蜂鸣器
    private val SENSOR_HE = "he" //温湿度
    private val SENSOR_IE = "ie" //光照度
    private val SENSOR_KE = "ke" //照明灯
    private val SENSOR_AL = "al" //电机转速
    private val RECONN_INFO = "te" //重发

    //控制
    private val CONTROL_SLA = "sl" //声光报警器
    private val CONTROL_EL = "el" //电磁锁

    val leftLight = view.findViewById<ImageView>(R.id.leftLight)
    val rightLight = view.findViewById<ImageView>(R.id.rightLight)

    //转向指示灯
    private val LEFT = 1
    private val RIGHT = 2
    private val CLOSE = 3


    var timer: Timer? = null
    var timerTask: TimerTask? = null
    var lc = 0
    var rc = 0

    val dataSend = DataSend(mainActivity,InfoViewModel.infoView!!)

    val handler = @SuppressLint("HandlerLeak") object: Handler(){
        override fun handleMessage( msg: Message){
            Log.d("Handler", msg.what.toString())

                when(msg.what){
                    LEFT->{

                        leftLight.setImageResource(R.drawable.left_on)
                        MainViewModel.is_left_on = true
                    }

                    RIGHT->{
                        //右转灯亮

                        rightLight.setImageResource(R.drawable.right_on)
                        MainViewModel.is_right_on = true
                    }

                    CLOSE->{

                        leftLight.setImageResource(R.drawable.left_off)
                        rightLight.setImageResource(R.drawable.right_off)

                        MainViewModel.is_left_on = false
                        MainViewModel.is_right_on = false

                    }
                }

            }
        }


@SuppressLint("SimpleDateFormat", "SetTextI18n", "UseSwitchCompatOrMaterialCode", "Recycle",
    "Range")
fun handle(recvData: String){
        try {
            val type = recvData.substring(3, 5) //设备类型
            //获取系统当前时间,(数据库相关)
            val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss") //
            val date = Date(System.currentTimeMillis())
            val time = simpleDateFormat.format(date)

            Log.d("databaseTest",time)
            when {

                //左右指示灯
                type.endsWith(TLight)->{
                    val forward = recvData[9]

                    when(forward){

                        //
                        '0'->{
                            //延时,亮0.7s灭0.3s
                            val msg = Message()
                            msg.what = CLOSE
                            handler.sendMessage(msg)

                        }

                        //指示灯左转
                        '1'->{
                            //延时
                            val msg = Message()
                            msg.what = LEFT
                            handler.sendMessage(msg)


                        }

                        //指示灯右转
                        '2'->{
                            val msg = Message()
                            msg.what = RIGHT
                            handler.sendMessage(msg)
                        }
                    }
                }

                //蜂鸣器Hwcho01010T
                type.endsWith(HO)->{
                    val state = recvData[9]

                    val bugle = view.findViewById<ImageView>(R.id.bugle)

                    when(state){
                        '0'->{
                            MainViewModel.is_bugle_on = false
                            bugle.setImageResource(R.drawable.bugle)
                        }

                        '1'->{
                            MainViewModel.is_bugle_on = true
                            bugle.setImageResource(R.drawable.bugle_on)
                        }
                    }

                }


                //照明灯
                type.endsWith(SENSOR_KE)->{
                    val state = recvData[7]
                    val light = view.findViewById<ImageView>(R.id.light)

                    when(state){
                        //照明灯灭
                        '0'->{
                            light.setImageResource(R.drawable.light)
                        }
                        //照明灯亮
                        '1'->{
                            light.setImageResource(R.drawable.light_on)
                        }
                    }
                }

                //重发数据Hwdte0123.41.64s23.41.63s23.41.64s
                type.endsWith(RECONN_INFO) ->{

                    //表示单片机们已经连接ok了
                    MainViewModel.isOthersConnectOK = true

                    var dataString = recvData.substring(7)

                    //把最后一个s去掉
                    dataString = dataString.removeSuffix("s")

                    // 将字符串分割成字符数组
                    val tokens = dataString.split("s")

                    val TempData = ArrayList<String>()
                    val VData = ArrayList<String>()

                    for (i in 0 until tokens.size){
                         TempData.add(tokens[i].substring(0,4))

                         VData.add(tokens[i].substring(4))

                        MainViewModel.reconnList.add(ReconnInfo(TempData[i],VData[i]))

                    }

                    MainViewModel.reconnAdapter = ReconnAdapter(MainViewModel.reconnList)


                    //开启弹窗
                    val reconnInfo_window = ReconnInfo_window(mainActivity)
                    reconnInfo_window.start()

                }


                // 温度加电压
                type.endsWith(SENSOR_HE) -> {
                    //获取Temp,Hum数据展示的控件
                    val temp:String = recvData.substring(7, 11).toFloat().toString()//去除数据前头的0
                    val connTvTemp = view.findViewById<TextView>(R.id.T_value)

                    val bicycleState = view.findViewById<TextView>(R.id.bicycleState)

                    //保存温度传感器第一次传来的值
                    if(InfoViewModel.firstTime){
                        InfoViewModel.baseTemp = temp
                        InfoViewModel.firstTime = false

                        connTvTemp?.text = "$temp ℃"

                    }else{

                        val tempChange = temp.toFloat()-InfoViewModel.baseTemp.toFloat()

                        //显示当前温度
                        connTvTemp?.text = "$temp ℃"
                        InfoViewModel.setTem(tempChange.toString()) //保存传来的值

                        //折线图广播
                        val intent = Intent("com.example.trafficdetection.currentTValue")
                        intent.putExtra("value",tempChange)
                        intent.setPackage(mainActivity.packageName)
                        mainActivity.sendBroadcast(intent)


                            //如果温差超过5则报警
                            if(tempChange > 5 ){
                                //示警灯变红
                                InfoViewModel.bicycle_showLight = "red"

                                ToastUtils.showCenterToast(context,"警告！电池温度异常，请及时检查！")

                                dataSend.sendFrame("Hwcmu01011T")

                                thread {
                                //故障处理，添加一个故障信息
                                //其次，组装一个故障信息类
                                //最后通过dao层工具，将故障信息类添加进数据库
                                //首先是获取车辆编号，依据车辆编号，查询是否添加了该车辆的故障信息类，若无则添加，有则进行更新
                                val bDInfo =  MainViewModel.bDInfoDao.loadBDInfoByEbId(InfoViewModel.bIdValue)

                                if(bDInfo.isEmpty()){

                                    val bDInfo1 = BreakDown(InfoViewModel.bIdValue,
                                        "电池温差过高",1, time)
                                    Log.d("Test","故障类型：${bDInfo1.bType}")

                                    //在线程中要手动创建looper才能使用handler,ToastUtils相当于一个handler,主线程中默认自动创建了
                                    bDInfo1.bid = MainViewModel.bDInfoDao.insertBDInfo(bDInfo1)


                                }else{
                                    val bDInfo1 = bDInfo[0]
                                    bDInfo1.let {
                                        it.bNum++
                                        it.bTime = time
                                        it.bType = "电池温差过高"
                                    }
                                    Log.d("Test","故障类型：${bDInfo1.bType}")

                                    MainViewModel.bDInfoDao.updateBDInfo(bDInfo1)


                                }


                                //3秒后自动关闭报警器

                                Thread.sleep(3000)

                                dataSend.sendFrame("Hwcmu01010T")


                                }

                        }

                    }

                    //处理电压
                    val V:String = recvData.substring(12, 16).toFloat().toString()//去除数据前头的0
                    val connTvV = view.findViewById<TextView>(R.id.V_value)

                    //显示当前电压
                    connTvV?.text = "${V} V"
                    InfoViewModel.setV(V) //保存传来的值

                    //折线图广播
                    val intent = Intent("com.example.trafficdetection.currentADValue")
                    intent.putExtra("value",V)
                    intent.setPackage(mainActivity.packageName)
                    mainActivity.sendBroadcast(intent)


                    //如果电压低于0.5V则报警
                    if(V.toFloat() < 0.5 ){
                        ToastUtils.showCenterToast(context,"警告！电池电压异常，请及时检查！")
                        //示警灯变红
                        InfoViewModel.bicycle_showLight = "red"

                        dataSend.sendFrame("Hwcmu01011T")

                        thread {
                        val bDInfo =  MainViewModel.bDInfoDao.loadBDInfoByEbId(InfoViewModel.bIdValue)

                        if(bDInfo.isEmpty()){

                            val bDInfo1 = BreakDown(InfoViewModel.bIdValue,
                                "电压过高",1, time)
                            Log.d("Test","故障类型：${bDInfo1.bType}")

                            //在线程中要手动创建looper才能使用handler,ToastUtils相当于一个handler,主线程中默认自动创建了
                            bDInfo1.bid = MainViewModel.bDInfoDao.insertBDInfo(bDInfo1)


                        }else{
                            val bDInfo1 = bDInfo[0]

                            bDInfo1.let {
                                it.bNum++
                                it.bTime = time
                                it.bType = "电压过高"
                            }
                            Log.d("Test","故障类型：${bDInfo1.bType}")

                            MainViewModel.bDInfoDao.updateBDInfo(bDInfo1)

                        }

                        //3秒后自动关闭报警器
                        Thread.sleep(3000)

                        dataSend.sendFrame("Hwcmu01010T")

                    }
                }
                    //显示电流，R为0.5欧
                    val r= 0.5

                    //构造方法的字符格式这里如果小数不足2位,会以0补足.
                    val decimalFormat = DecimalFormat(0.00.toString())
                    val I = decimalFormat.format((V.toFloat()/ r)) //format 返回的是字符串

                    InfoViewModel.batteryPower = "${I} A"

                    val connTvI = view.findViewById<TextView>(R.id.I_value)
                    connTvI.text = "${I} A"

                    //处理示警灯变色UI
                    if(InfoViewModel.bicycle_showLight=="red"){
                        bicycleState.text = "异常！"
                        //颜色改为红色
                        view.findViewById<ImageView>(R.id.image_bicycle_state)
                            .setImageResource(R.drawable.errorpot)

                        InfoViewModel.bicycle_showLight = "green"
                    }else{
                        bicycleState.text = "正常！"
                        //颜色改为红色
                        view.findViewById<ImageView>(R.id.image_bicycle_state)
                            .setImageResource(R.drawable.rightpot)
                    }

                }

                //光照度
                type.endsWith(SENSOR_IE) -> {

                    val connTvIllu = view.findViewById<TextView>(R.id.ie_value)
                    val illu:String= recvData.substring(9, 15).toInt().toString()

                    connTvIllu.text = "$illu lux"

                }

                //RFID上传车编号
//                type.endsWith(RFID) ->{
//                    Log.d("Test","执行rfid的数据帧")
//                    val info:String= recvData.substring(5, 8)
//                    val num_id = recvData.substring(12,20)
//
//                    when(info){
//                        //电车编号
//                        "car"->{
//                            val idNum = view.findViewById<TextView>(R.id.bId_value)
//                            idNum.text = num_id
//
//                            //将当前接入的车编号弄入缓存
//                            InfoViewModel.bIdValue = num_id
//
//                            //发送车编号
//                            val order = MessageFormat.format("Hwdby01T")
//                            dataSend.sendFrame(order)
//                        }
//
//                        "bat"->{
//
//                            val random = (0..999).random()
//                            var batnum = ""
//                            if (random.toString().length == 1) {
//                                batnum = "00$random"
//                            } else if (random.toString().length == 2) {
//                                batnum = "0$random"
//                            }else{
//                                batnum = random.toString()
//                            }
//
//                            //发送电池编号
//                            val order = MessageFormat.format("Hwdba01T")
//                            InfoViewModel.batteryNum = batnum
//
//                            dataSend.sendFrame(order)
//
//                        }
//                    }
//                }

                //车编号
                type.endsWith(BY) ->{
                    Log.d("Test","执行rfid的数据帧")
                    val num_id:String= recvData.substring(5, 7)
                    val idNum = view.findViewById<TextView>(R.id.bId_value)
                    idNum.text = num_id

                    //将当前接入的车编号弄入缓存
                    InfoViewModel.bIdValue = num_id

                }

                //电池编号
                type.endsWith(BA) ->{
                    Log.d("Test","执行rfid的数据帧")
                    val num_id:String= recvData.substring(5, 7)

                    //电池编号缓存
                    InfoViewModel.batteryNum = num_id

                }

                //转速Hwdal0103080T
                type.endsWith(SENSOR_AL)->{

                    val emSpeed_control = view.findViewById<SeekBar>(R.id.emSpeed_control)
                    val emSpeedValue = view.findViewById<TextView>(R.id.emSpeed_value)

                    val speed =  recvData.substring(9, 12).toFloat()
                    emSpeedValue.text =  speed.toInt().toString() +"r/s" //转速显示
                    emSpeed_control.progress = speed.toInt() //转速条进度调节


                }

            }
        } catch ( e:Exception) {
            e.printStackTrace()
        }
    }

}