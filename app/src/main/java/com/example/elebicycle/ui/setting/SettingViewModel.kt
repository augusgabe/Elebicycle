package com.example.elebicycle.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object SettingViewModel :ViewModel() {
    private var ip = "192.168.110.53"
    private var port = "6004"

    var username = ""
    var isLogined = MutableLiveData("")
    var oldLogined = ""
    var password = ""
    var authriority = ""


    fun getIp():String{
        return ip
    }

    fun setIp(ip:String){
        this.ip = ip
    }

    fun getPort():String{
        return port
    }

    fun setPort(port:String){
        this.port = port
    }
}