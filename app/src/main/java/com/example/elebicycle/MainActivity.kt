package com.example.elebicycle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.elebicycle.activity.login.LoginActivity
import com.example.elebicycle.databinding.ActivityMainBinding
import com.example.elebicycle.utils.ToastUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.system.exitProcess


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isExit:Boolean = false
    private lateinit var mViewModel: MainViewModel

    //线程池
    private lateinit var mThreadPool: ExecutorService
    internal lateinit var handler: Handler

    //socket
    private var socket: Socket? = null
    // 输入流对象
    private  var isStream: InputStream? = null
    // 输出流对象
    private  var outputStream: OutputStream? = null

    private val CONN_SUCCESS = 1//连接成功
    private val CONN_FAIL = 2//连接失败
    private val CONN_TIME_OUT = 3//连接超时
    private val RECV_DATA = 4;//接收到的数据

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.let{
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayShowTitleEnabled(false)
        }

//        supportActionBar?.hide()

        //设置状态栏透明
        makeStatusBarTransparent(this);
//        //状态栏文字自适应
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }

        val navView: BottomNavigationView = binding.appBarMain.include.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // 将每个菜单 ID 作为一组 Id 传递，因为应将每个菜单视为顶级目标。
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_info, R.id.navigation_record, R.id.navigation_setting
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mViewModel = MainViewModel
        //线程池中开启线程
        mThreadPool = Executors.newCachedThreadPool()

        handler = @SuppressLint("HandlerLeak") object: Handler(){
            override fun handleMessage( msg: Message){
                Log.d("Handler", msg.what.toString())
                when(msg.what){

                    CONN_SUCCESS->{
                        ToastUtils.showToast(this@MainActivity, "连接成功")

                        //判断是否是重连的
                        if(MainViewModel.isFirstConnect){
                           MainViewModel.isFirstConnect = false
                        }else{

                            thread{
                                while(!MainViewModel.isOthersConnectOK){
                                    sendMsg("HwdreconnT")

                                    //延时3秒
                                    Thread.sleep(3000)
                                }
                            }

                        }

                        //将成功连接的socket保存到全局中
                        MainViewModel.global_socket = socket
                    }

                    CONN_FAIL->{
                        ToastUtils.showToast(this@MainActivity, "连接失败");
                    }
                    CONN_TIME_OUT->{
                        ToastUtils.showToast(this@MainActivity, "连接超时");
                    }

                    // 获取数据
                    RECV_DATA->{
                        Log.d("T","获取的数据"+(msg.obj as String))
                        checkDataShow(msg.obj as String)
                    }
                    //延迟1秒，可调灯再次生效
                    0x117->{
                        val bar = msg.obj as SeekBar
                        bar.isEnabled = true
                    }
                    else ->{
                        println("未知连接")
                    }
                }
            }
        }

//        MainViewModel.toService = intent.getStringExtra("msg").toString()
       //如果全局socket不为空，则将其赋予当前的socket
        if(MainViewModel.global_socket!=null){
            socket = MainViewModel.global_socket
        }

    }


    fun toLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

        //设置状态栏透明
    fun makeStatusBarTransparent(activity: Activity) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            val option: Int = window.decorView
                .systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.decorView.setSystemUiVisibility(option)
            window.statusBarColor = Color.TRANSPARENT
        }

    /***
     *
     *  发送消息
     *
     * ***/
    internal fun sendMsg( msg:String) {
        Log.d("sendMsg", "sendMsg 发送消息 msg =" + msg);
        // 利用线程池直接开启一个线程 & 执行该线程
        mThreadPool.execute {
            if (socket != null) {
                try {
                    // 步骤1：从Socket 获得输出流对象OutputStream
                    // 该对象作用：发送数据
                    outputStream = socket?.getOutputStream()
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

    fun checkDataShow(receDate:String){
        Log.d("Rece","checkDate执行")
        mViewModel.setReceDate(receDate) // 让MainViewModel中数据变化，各Fragment再对receDate进行监听
        Log.d("mViewModel","实例 "+mViewModel)
    }

    /***
     * 连接A53服务器
     * **/
    fun connectA53Server(ip: String,port: String){
        //隐藏软键盘
        hideSoftKeyboard(this@MainActivity)

        Log.d("Main", "ip=$ip")
        Log.d("Main", "port=$port")

        //连接服务器
        mThreadPool.execute{
            while (true){

                try {
                    //尝试连接服务器
                    connectSocket(ip,port)

                    // 监测服务器关闭并重连
                    while (true) {

                        val buffer = ByteArray(1024)
                        val len = socket?.inputStream?.read(buffer)

                        //如果数据返回为0，代表服务器关闭了，先将线程休眠5秒后尝试重新连接
                        if (len == -1) {
                            runOnUiThread {
                                ToastUtils.showCenterToast(this,"服务器故障!")

                            }

                            closeSocket()
                            Thread.sleep(5000)
                            runOnUiThread {
                                ToastUtils.showCenterToast(this,"尝试重新连接中...")
                            }

                            break

                        } else{//这里就是对接收的数据进行处理

                            var recvData:String = ""
                            try {
                                recvData =String (buffer, 0, len!!, Charsets.UTF_8).removeSuffix("\r\n")
                            } catch ( e:Exception) {
                                e.printStackTrace()
                            }

                            //获取数据
                            val message =  Message()
                            message.what = RECV_DATA
                            message.obj = recvData
                            handler.sendMessage(message)

                        }


                    }


                }catch (e:IOException){
                    runOnUiThread {
                        ToastUtils.showCenterToast(this,"连接错误, ${e.message}")
                    }

                    closeSocket()

                    Thread.sleep(5000)

                    runOnUiThread {
                        ToastUtils.showCenterToast(this,"尝试重新连接中...")
                    }
                }catch (e: InterruptedException) {

                    closeSocket()
                    exitProcess(0)
                }

            }
        }


    }

    /**
     * 连接socket
     ***/
    private fun connectSocket(ip:String, port:String) {
        // 利用线程池直接开启一个线程 & 执行该线程
            try {

                // 创建Socket对象 & 指定服务端的IP 及 端口号
                Log.d("Main", "socket创建")
                socket = Socket(ip, Integer.parseInt(port))

                Log.d("Main", "socket 指定")
                //如果连接
                if (socket!!.isConnected) {


                    val msg = Message()
                    msg.what = CONN_SUCCESS
                    handler.sendMessage(msg)
                } else {
                    val msg = Message()
                    msg.what = CONN_FAIL
                    handler.sendMessage(msg)
                }

            } catch (e: SocketTimeoutException) {//连接超时报错
                val msg = Message()
                msg.what = CONN_TIME_OUT
                handler.sendMessage(msg)
            } catch (e: IOException) {

                Log.d("Main", "IO错误$socket")
                Log.d("Wrong", e.toString())
                val msg = Message()
                msg.what = CONN_FAIL
                handler.sendMessage(msg)
            }




    }

    /***
     *
     * 关闭socket
     *
     * **/
    fun closeSocket() {
        try {
            Log.d("Main","socket 关闭")
            //关闭输入流通道
            isStream?.close()
//            关闭输出流通道
            outputStream?.close()
            // 关闭Socket连接
            socket?.close();
        } catch ( e: IOException) {
            e.printStackTrace();
        }

    }

    /**
     * 隐藏软键盘
     */
    private  fun hideSoftKeyboard( activity: Activity) {
        val view: View? = activity.currentFocus;
        if (view != null) {
            val inputMethodManager: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     *
     * 退出应用
     *
     * ***/
    private fun exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            Timer().schedule(object: TimerTask() {
                override fun run() {
                    isExit = false;
                }
            }, 2000)
        } else {
            this.finish();
            exitProcess(0);
        }
    }

    override  fun onKeyDown( keyCode:Int, event: KeyEvent):Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit()
        }
        return false;
    }

}