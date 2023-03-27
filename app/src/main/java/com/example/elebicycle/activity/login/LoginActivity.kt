package com.example.elebicycle.activity.login

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.example.elebicycle.BaseActivity
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.activity.register.RegisterActivity
import com.example.elebicycle.databinding.ActivityLoginBinding
import com.example.elebicycle.enity.User
import com.example.elebicycle.ui.bicycleInfo.InfoViewModel
import com.example.elebicycle.ui.setting.SettingViewModel
import com.example.elebicycle.utils.*
import com.example.elebicycle.utils.MyApp.Companion.context
import kotlin.concurrent.thread

class LoginActivity  : BaseActivity(){

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()

        //获取userDao实例
        val userDao = AppDatabase.getDatabase(this).userDao()

        binding.turnbackLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.login.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            Log.d("Test","登录")
            // 如果账号是admin且密码是123456，就认为登录成功
            //如果从数据库中查询的结果不为空
            thread {
                Looper.prepare()
                if (userDao.queryUser(username, password).isNotEmpty()) {
                    Log.d("Test","查询的实例"+userDao.queryUser(username,password))

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    SettingViewModel.username = username
                    SettingViewModel.password = password

                    if(username== "admin"){
                        SettingViewModel.authriority = "管理员"
                    }else{
                        SettingViewModel.authriority = "用户"
                    }

                    //发一条数据帧给服务器告诉服务器用户姓名
                    MainViewModel.globalSendMsg("Hwdus01${username}")


                    finish()
                } else {

                    ToastUtils.showToast(context, "账户名或密码错误！")
                    Log.d("Test","登录1")
                }
                Looper.loop()
            }

        }

        binding.register.setOnClickListener {
            Log.d("Test","注册")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
//            ToastUtils.showToast(context, "进入注册新账号")
        }

    }


}