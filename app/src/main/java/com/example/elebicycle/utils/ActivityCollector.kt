package com.example.elebicycle.utils

import android.app.Activity

//activity控制回收
object ActivityCollector {

        private val activities = ArrayList<Activity>()

        fun getActivities():ArrayList<Activity> = this.activities

        fun addActivity(activity: Activity) {
            activities.add(activity)
        }

        fun removeActivity(activity: Activity) {
            activities.remove(activity)
        }

        fun finishAll() {
            for (activity in activities) {
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
            activities.clear()
        }

}