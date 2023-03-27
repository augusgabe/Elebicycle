package com.example.elebicycle.activity.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import com.example.elebicycle.BaseActivity
import com.example.elebicycle.MainActivity
import com.example.elebicycle.MainViewModel
import com.example.elebicycle.activity.login.LoginActivity
import com.example.elebicycle.databinding.ActivityRegisterBinding
import com.example.elebicycle.enity.User
import com.example.elebicycle.utils.ActivityCollector
import com.example.elebicycle.utils.AppDatabase
import com.example.elebicycle.utils.ToastUtils
import kotlin.concurrent.thread

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()

        //先获取了UserDao的实例
        val userDao = AppDatabase.getDatabase(this).userDao()

        binding.register1.setOnClickListener {

            val username = binding.usernameRegister.text.toString()
            val password = binding.passwordRegister.text.toString()

            val user = User(username,password)

            /*将User对象插入数据库中，并将insertUser()方法返回的主键id值赋值给原来的User对象
             之所以要这么做，是因为使用@Update和@Delete注解去更新和删除数据时都是基于这个id值
            来操作的*/
            thread {
                //在线程中要手动创建looper才能使用handler,ToastUtils相当于一个handler,主线程中默认自动创建了
                Looper.prepare()
                if(userDao.queryUser(username, password).isNotEmpty()){

                    ToastUtils.showToast(this,"注册失败，该用户已注册！")

                }else{

                    user.uid = userDao.insertUser(user)
                    ToastUtils.showToast(this,"注册成功！")

                    //发一条数据帧给服务器告诉服务器用户姓名
                    MainViewModel.globalSendMsg("Hwdzc01${username}.${password}.110112")

                }

                Looper.loop()
            }
            /*由于数据库操作属于耗时操作，Room默认是不允许在主线程中进行数据库操作的，因此
               上述代码中我们将增删改查的功能都放到了子线程中*/

        }

        binding.returnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }




    }
}