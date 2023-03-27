package com.example.elebicycle.enity

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elebicycle.R

class EleBicycleAdapter(var eleBicycleList: List<EleBicycle>):
    RecyclerView.Adapter<EleBicycleAdapter.ViewHolder>(){

    //设置接口监听
//    private var mOnItemClickListener: OnItemClickListener? = null
//
//    fun setOnItemClickListener(clickListener: OnItemClickListener?) {
//        mOnItemClickListener = clickListener
//    }

    //内部类
    inner class ViewHolder(view: View):
        RecyclerView.ViewHolder(view){

        val ebNum: TextView = view.findViewById(R.id.ebNum)
        val useTime:TextView = view.findViewById(R.id.useTime)
        val position:TextView = view.findViewById(R.id.position_stay)
        val byState:TextView = view.findViewById(R.id.byState)
        val recordTime:TextView = view.findViewById(R.id.recordTime)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<EleBicycle>){
        this.eleBicycleList = list
        //通知数据改变
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bicycle_item,parent,false)

//        val viewHolder = mOnItemClickListener?.let { ViewHolder(view, it) }
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val eleBicycle = eleBicycleList[position]
        holder.ebNum.text = eleBicycle.ebNum
        holder.useTime.text = eleBicycle.useTime
        holder.position.text = eleBicycle.position
        holder.byState.text = eleBicycle.state
        holder.recordTime.text = eleBicycle.recordTime

    }

    override fun getItemCount() = eleBicycleList.size
}