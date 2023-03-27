package com.example.elebicycle.dao

import androidx.room.*
import com.example.elebicycle.enity.User

@Dao
interface UserDao {

    @Insert
    fun insertUser(user: User):Long

    @Update
    fun updateUser(newUser: User)

    @Query("select * from User")
    fun loadAllUsers(): List<User>

    @Query("select * from User where password = :password and username = :username")
    fun queryUser(username:String,password: String): List<User>

    @Delete
    fun deleteUser(user: User)

    @Query("delete from User where username = :name")
    fun deleteUserByName(name: String): Int

}