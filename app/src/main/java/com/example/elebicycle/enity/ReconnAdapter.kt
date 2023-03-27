package com.example.elebicycle.enity

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elebicycle.R

class ReconnAdapter(var reconnList: List<ReconnInfo>):
    RecyclerView.Adapter<ReconnAdapter.ViewHolder>(){

    //内部类
    inner class ViewHolder(view: View):
        RecyclerView.ViewHolder(view){

        val temp: TextView = view.findViewById(R.id.reconn_temp)
        val v:TextView = view.findViewById(R.id.reconn_v)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<ReconnInfo>){
        this.reconnList = list
        //通知数据改变
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reconn_item,parent,false)

        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reconnInfo = reconnList[position]
        holder.temp.text = reconnInfo.Temp
        holder.v.text = reconnInfo.V

    }

    override fun getItemCount()=reconnList.size

}