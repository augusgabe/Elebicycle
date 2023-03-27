package com.example.elebicycle.enity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EleBicycle(var ebNum:String,var userName:String ,var state:String,var position:String,var useTime:String,
            var recordTime:String) {

        @PrimaryKey(autoGenerate = true)
        var ebId: Long = 0

 }