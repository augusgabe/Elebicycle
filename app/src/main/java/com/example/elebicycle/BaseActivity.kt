package com.example.elebicycle

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.elebicycle.utils.ActivityCollector

open class BaseActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)

        //状态栏文字自适应
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BaseActivity", javaClass.simpleName+"destroy")
        ActivityCollector.removeActivity(this)
    }

}