package com.example.elebicycle.ui.record

import androidx.lifecycle.ViewModel
import com.example.elebicycle.enity.BreakDown
import com.example.elebicycle.enity.EleBicycle

object RecordViewModel : ViewModel() {

    var eleBicycleList = ArrayList<EleBicycle>()

    var bDInfoList = ArrayList<BreakDown>()
}