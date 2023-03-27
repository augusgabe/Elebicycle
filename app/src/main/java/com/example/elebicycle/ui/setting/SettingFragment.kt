package com.example.elebicycle.ui.setting

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.databinding.SettingFragmentBinding
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import com.example.elebicycle.utils.DataHandle
import com.example.elebicycle.utils.DataSend
import com.example.elebicycle.utils.ToastUtils
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread

class SettingFragment : Fragment() {

    private var _binding: SettingFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var mainActivity : MainActivity

    private lateinit var viewModel: SettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingFragmentBinding.inflate(inflater, container, false)
        mainActivity = activity as MainActivity

        binding.user.text = SettingViewModel.username
        binding.authority.text = SettingViewModel.authriority

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val infoViewModel = InfoViewModel
        val setViewModel = SettingViewModel

        //当服务器传来数据帧，receDate值发生改变调用
        MainViewModel.receDate.observe(viewLifecycleOwner, Observer {

            val dataHandle = DataHandle(mainActivity, infoViewModel.infoView!!)
            if(it != MainViewModel.oldReceDate){
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                dataHandle.handle(it)
                MainViewModel.oldReceDate = it

            }

        } )

        //登陆时发送数据帧
        SettingViewModel.isLogined.observe(viewLifecycleOwner, Observer {

            val dataSend = DataSend(mainActivity, infoViewModel.infoView!!)

            if(it != SettingViewModel.oldLogined){
                //弊端：当连续接收同样的数据时，无变化，但是每次的数据都需要处理，导致不能处理无变化的数据
                val order1 = "Hwdus"+SettingViewModel.username+"T"
                val order2 = "Hwdpa"+SettingViewModel.password+"T"

                dataSend.sendFrame(order1)
                dataSend.sendFrame(order2)

                SettingViewModel.oldLogined = it

            }


        } )

        binding.connBtn.setOnClickListener {
            val mainActivity = activity as MainActivity

            //服务器IP
            val ip = binding.connEtIp.text.toString().trim()
            //保存到SetViewModel中
            setViewModel.setIp(ip)
            if (!checkIP(ip)) {
                ToastUtils.showToast(context, "请输入正确的服务器IP")
            }
            //端口号
            val port = binding.connEtPort.text.toString().trim()
            setViewModel.setPort(port)

            if (TextUtils.isEmpty(port)) {
                ToastUtils.showToast(context, "请输入正确的端口号")
            }
            mainActivity.connectA53Server(ip,port)

            //间接处理监听用户名
            SettingViewModel.isLogined.value = SettingViewModel.username
        }

        //关闭socket连接
        binding.disconnBtn.setOnClickListener {
            mainActivity.closeSocket()
            ToastUtils.showToast(mainActivity,"已断开服务器连接")
        }


    }

    /**
     * 校验服务器IP
     **/
    private fun checkIP(ip:String):Boolean {
        var isIP:Boolean = false

        if (!TextUtils.isEmpty(ip)) {
            //IP地址验证规则
            val regex:String = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."+
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$"

            val pattern: Pattern = Pattern.compile(regex)
            val matcher: Matcher = pattern.matcher(ip)
            val rs:Boolean = matcher.matches()// 字符串是否与正则表达式相匹配
            isIP = rs
        }
        return isIP
    }

    override fun onDestroy() {
        super.onDestroy()
        //保证binding有效生命周期是在onCreateView()函数和onDestroyView()函数之间
        _binding = null
    }

}