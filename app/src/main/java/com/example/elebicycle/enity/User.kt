package com.example.elebicycle.enity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(val username:String,val password:String) {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

}