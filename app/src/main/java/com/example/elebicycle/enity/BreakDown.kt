package com.example.elebicycle.enity

import androidx.room.Entity
import androidx.room.PrimaryKey

//故障信息实体类
@Entity
data class BreakDown(val ebNum:String, var bType:String, var bNum:Int, var bTime:String) {

    @PrimaryKey(autoGenerate = true)
    var bid: Long = 0

}