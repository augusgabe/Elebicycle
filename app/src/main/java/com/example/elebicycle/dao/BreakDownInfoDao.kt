package com.example.elebicycle.dao

import androidx.room.*
import com.example.elebicycle.enity.BreakDown


@Dao
interface BreakDownInfoDao {
    @Insert
    fun insertBDInfo(bDInfo: BreakDown):Long

    @Update
    fun updateBDInfo(bDInfo: BreakDown)

    @Query("select * from Breakdown")
    fun loadAllBDInfo(): List<BreakDown>

    @Query("select * from Breakdown where ebNum = :ebNum")
    fun loadBDInfoByEbId(ebNum: String): List<BreakDown>

    @Delete
    fun deleteBDInfo(bDInfo: BreakDown)

    @Query("delete from Breakdown where ebNum = :ebNum")
    fun deleteBDInfoByName(ebNum: String): Int

    @Query("delete from Breakdown")
    fun deleteAllBDInfo(): Int
}