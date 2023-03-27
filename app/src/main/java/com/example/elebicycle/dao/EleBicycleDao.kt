package com.example.elebicycle.dao

import androidx.room.*
import com.example.elebicycle.enity.EleBicycle

@Dao
interface EleBicycleDao {
    @Insert
    fun insertBicycle(bicycle: EleBicycle):Long

    @Update
    fun updateEleBicycle(newBicycle: EleBicycle)

    @Query("select * from EleBicycle")
    fun loadAllBicycles(): List<EleBicycle>

    @Query("select * from EleBicycle where ebNum = :ebNum")
    fun loadBicyclesByEbId(ebNum: String): List<EleBicycle>

    //通过用户来查询行程记录
    @Query("select * from EleBicycle where userName = :userName")
    fun loadBicyclesByUser(userName: String): List<EleBicycle>

    @Delete
    fun deleteUser(bicycle: EleBicycle)

    @Query("delete from EleBicycle where ebNum = :ebNum")
    fun deleteBicycleByName(ebNum: String): Int

    @Query("delete from EleBicycle")
    fun deleteAllBicycle(): Int
}