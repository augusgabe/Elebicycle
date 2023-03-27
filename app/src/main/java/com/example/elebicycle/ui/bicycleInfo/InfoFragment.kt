package com.example.elebicycle.ui.bicycleInfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.R
import com.example.elebicycle.databinding.InfoFragmentBinding
import com.example.elebicycle.popupwindow.BatteryInfo
import com.example.elebicycle.ui.setting.SettingViewModel
import com.example.elebicycle.utils.ActivityCollector
import com.example.elebicycle.utils.DataHandle
import com.example.elebicycle.utils.DataSend
import com.example.elebicycle.utils.ToastUtils
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*


class InfoFragment : Fragment() {

    private var _binding: InfoFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    lateinit var dataSend:DataSend
    lateinit var dataHandle:DataHandle


    private lateinit var viewModel: InfoViewModel


    private val POSITION = 1
    //用来在一些耗时逻辑里更新UI
    val handler = @SuppressLint("HandlerLeak") object: Handler(){
        override fun handleMessage( msg: Message){
            Log.d("Handler", msg.what.toString())
            when(msg.what){
               POSITION->{
                  //更新UI
                   binding.positionValue.setText(msg.arg1.toString())

               }

            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InfoFragmentBinding.inflate(inflater,container,false)

        mainActivity = activity as MainActivity

        viewModel = InfoViewModel

        initView()

        return binding.root


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.infoView = requireView()

        dataSend = DataSend(mainActivity,requireView())
        dataHandle = DataHandle(mainActivity, requireView())

        //当服务器传来数据帧，receDate值发生改变调用
        MainViewModel.receDate.observe(viewLifecycleOwner) {
            if (it != MainViewModel.oldReceDate) {
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                dataHandle.handle(it)
                MainViewModel.oldReceDate = it

            }

        }

        //解锁监听
        binding.unlock.setOnClickListener {

            //必须要求先登陆，再开锁

            if(SettingViewModel.username == ""){

                ToastUtils.showToast(context,"请先登陆！")

            } else {

                if(binding.bIdValue.text!="未连接"){

                    //电磁锁开锁或关锁
                    dataSend.sendCtrlCmd(viewModel.el_num,viewModel.el_state.value.toString(),"el")

                    //直流电机转速开始生效
                    controlAl()


                }else{
                    ToastUtils.showToast(context,"当前自行车未连接")
                }

            }

        }

        //响喇叭
        binding.bugle.setOnClickListener {
            if(InfoViewModel.islocked){
                ToastUtils.showToast(context,"此车已锁，请先开锁后再进行操作！")
            }else{

                //如果状态是false,就发送开启的数据帧否则发送关闭的数据帧
                if(MainViewModel.is_bugle_on){
                    //发送蜂鸣器关闭数据帧
                    dataSend.sendFrame("Hwcho01010T")
                    binding.bugle.setImageResource(R.drawable.bugle)
                    MainViewModel.is_bugle_on = false
                }else{
                    //发送蜂鸣器开启数据帧
                    dataSend.sendFrame("Hwcho01011T")
                    binding.bugle.setImageResource(R.drawable.bugle_on)
                    MainViewModel.is_bugle_on = true
                }

            }

        }

        //左右转向灯控制
        binding.leftLight.setOnClickListener{
            if(InfoViewModel.islocked){
                ToastUtils.showToast(context,"此车已锁，请先开锁后再进行操作！")
            }else{

                if(MainViewModel.is_left_on or MainViewModel.is_right_on){
                    dataSend.sendFrame("Hwcle01010T")
                    dataHandle.handle("Hwcle01010T")
                }else{

                    dataSend.sendFrame("Hwcle01011T")
                    dataHandle.handle("Hwcle01011T")
                }


            }

        }

        binding.rightLight.setOnClickListener{
            if(InfoViewModel.islocked){
                ToastUtils.showToast(context,"此车已锁，请先开锁后再进行操作！")
            }else{
                if(MainViewModel.is_left_on or MainViewModel.is_right_on){
                    dataSend.sendFrame("Hwcle01010T")
                    dataHandle.handle("Hwcle01010T")
                }else{
                    dataSend.sendFrame("Hwcle01012T")
                    dataHandle.handle("Hwcle01012T")
                }
            }


        }


        //控制阈值
        binding.setLightValueSure.setOnClickListener {
            var value = binding.setLightValue.text.toString()

            //左边补0操作
            if (value.length == 1) {
                value = "00000$value"
            } else if (value.length == 2) {
                value = "0000$value"
            }else if (value.length == 3){
                value = "000$value"
            }else if (value.length == 4){
                value = "00$value"
            }else if (value.length == 5){
                value = "0$value"
            }

            val order = MessageFormat.format("Hwclu0106{000000}T",value)
            dataSend.sendFrame(order)
            ToastUtils.showToast(context,"阈值设置成功！")
        }

        //进入登录页面
        binding.loginBtn.setOnClickListener {
            mainActivity.toLogin()
        }

        binding.batteryInfo.setOnClickListener {

            //电池信息
            val batteryInfo = BatteryInfo(mainActivity)

            batteryInfo.start()
        }

    }

    fun initView(){

        //修改计时器Chronometer的显示格式
        binding.timeValue.onChronometerTickListener = OnChronometerTickListener { cArg ->
            val time = System.currentTimeMillis() - cArg.base
            val d = Date(time)
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            binding.timeValue.text = sdf.format(d)
            InfoViewModel.rideTime = binding.timeValue.text.toString()
        }

        //各信息初始化
        binding.lockImage.setImageResource(viewModel.lockImage)
        binding.positionValue.setText(viewModel.position)
        binding.unlockText.text = viewModel.lockValue
        binding.bIdValue.text = viewModel.bIdValue //车编号

    }

    //控制直流电机
    @SuppressLint("SetTextI18n")
    fun controlAl(){
        var now_angle = "000"
        var timerSpeed:Timer? = null

        //获取进度条控件
        val emSpeedControl = binding.emSpeedControl
        val parseInt: Int = now_angle.toInt() //将数据帧进度百分比文本转化为数字
        emSpeedControl.progress = parseInt  //设置进度条百分比
        binding.emSpeedValue.text = "${parseInt}"+"r/s"
        mainActivity.sendMsg("Hwcal0103000T")

        //位置改变
        positionChange(now_angle)

        //进度条改变时的监听器
        emSpeedControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                var angle = "100"
                //改变进度条使文本相应改变
                if (progress.toString().length == 1) {
                    angle = "00$progress"
                } else if (progress.toString().length == 2) {
                    angle = "0$progress"
                }
                now_angle = angle
                binding.emSpeedValue.text = "${now_angle.toInt()}"+"r/s"

                //位置改变
                positionChange(now_angle)

            }

            //碰到进度条时的方法
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //发送对应的数据帧出去
                val tasklight: TimerTask = object : TimerTask() {
                        override fun run() {
                                val orderlight = "Hwcal0103" + now_angle + "T"
                                //发送数据
                                mainActivity.sendMsg(orderlight)

                        }
                    }
                //定时器,每隔500ms发一次
                timerSpeed = Timer()
                timerSpeed?.schedule(tasklight, 0, 500)

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

                val orderlight = "Hwcal0103" + now_angle + "T"
                //发送数据
                mainActivity.sendMsg(orderlight)

                InfoViewModel.now_angle.value = now_angle

                timerSpeed?.cancel()
                timerSpeed = null

                seekBar.isEnabled = false
                val msgRelift = Message()
                msgRelift.what = 0x117
                msgRelift.obj = seekBar
                mainActivity.handler.sendMessageDelayed(msgRelift, 1000)//延迟1秒后发送
            }
        })


    }

    //位置变化，只要直流电机转速不为0，则不断发送位置变化数据帧
    fun positionChange(now_angle:String) {

        if (now_angle == "000") {

            InfoViewModel.positionTimer?.cancel()
            InfoViewModel.positionTimer = null

            InfoViewModel.isChange = true

        }else{

            when(InfoViewModel.isChange){

                true->{
                    val tasklight: TimerTask = object : TimerTask() {
                        override fun run() {
                            val random = (1000..9999).random()
                            val order = MessageFormat.format("Hwdpo01{0}T", random.toString())
                            //发送数据
                            mainActivity.sendMsg(order)

                            InfoViewModel.position = random.toString()

                            //异步处理，UI无法在子线程里更新
                            val msg = Message()
                            msg.what = POSITION
                            msg.arg1 = random
                            handler.sendMessage(msg)
                        }
                    }

                    //定时器,每隔1s发一次
                    Log.d("position0",""+InfoViewModel.positionTimer)
                    InfoViewModel.positionTimer = Timer()

                    Log.d("position00",""+InfoViewModel.positionTimer)
                    InfoViewModel.positionTimer!!.schedule(tasklight, 0, 10000)

                    //将改变量置为否
                    InfoViewModel.isChange = false

                }

                false->{
                    //啥也不干
                }

            }

        }





    }



}