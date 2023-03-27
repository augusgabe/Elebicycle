package com.example.elebicycle

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elebicycle.enity.EleBicycleAdapter
import com.example.elebicycle.enity.ReconnAdapter
import com.example.elebicycle.enity.ReconnInfo
import com.example.elebicycle.utils.AppDatabase
import com.example.elebicycle.utils.MyApp
import java.io.IOException
import java.net.Socket
import java.util.ArrayList

//数据共享处，设置为单一类
object MainViewModel:ViewModel() {

    //接收的数据帧数据
    var receDate = MutableLiveData("")
    //用于做对比，防止默认执行观察
    var oldReceDate = ""

    //数据库
    internal var db: SQLiteDatabase? = null

    //行车记录的Dao
    val eleBicycleDao =  AppDatabase.getDatabase(MyApp.context).eleBicycleDao()
    val bDInfoDao =  AppDatabase.getDatabase(MyApp.context).bDInfoDao()
    val userDao =  AppDatabase.getDatabase(MyApp.context).userDao()

    var is_left_on = false//左转向灯的标志量
    var is_right_on = false//左转向灯的标志量

    //蜂鸣器开关监测
    var is_bugle_on = false

    fun setReceDate(Date:String){
        receDate.value=Date
    }

    var reconnList = ArrayList<ReconnInfo>()
    var reconnAdapter: ReconnAdapter? = null

    //重发数据相关
    var isFirstConnect = true //判断是否首次发送
    var isOthersConnectOK = false //重连时可能由于单片机与安卓端连接服务器顺序不一致导致,如果只发一次就可能收不到单片机发的东西

    //传给服务器的数据,用户登陆时
    var hasLogin =  MutableLiveData(false)

    //全局socket变量
    var global_socket:Socket? = null

    //全局方法，可以向连接好的socket发送数据
    fun globalSendMsg(msg:String){
        if (global_socket != null) {
            try {
                // 步骤1：从Socket 获得输出流对象OutputStream
                // 该对象作用：发送数据
                val outputStream = global_socket?.getOutputStream()
                // 步骤2：写入需要发送的数据到输出流对象中
                outputStream?.write(msg.toByteArray(Charsets.UTF_8))
                // 步骤3：发送数据到服务端
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}