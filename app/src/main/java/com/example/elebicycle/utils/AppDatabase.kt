package com.example.elebicycle.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.elebicycle.dao.BreakDownInfoDao
import com.example.elebicycle.dao.EleBicycleDao
import com.example.elebicycle.dao.UserDao
import com.example.elebicycle.enity.BreakDown
import com.example.elebicycle.enity.EleBicycle
import com.example.elebicycle.enity.User

// 使用 @Database 注解指定数据库版本号为 2，包含了两个实体类 User 和 EleBicycle
@Database(version = 2, entities = [User::class, EleBicycle::class,BreakDown::class])
abstract class AppDatabase : RoomDatabase() {

    // 定义获取 DAO 的抽象方法，分别用于访问 User 和 EleBicycle 实体类对应的数据表
    abstract fun userDao(): UserDao
    abstract fun eleBicycleDao(): EleBicycleDao
    abstract fun bDInfoDao(): BreakDownInfoDao

    // 数据库迁移操作，将数据库从版本 1 升级到版本 2，在版本 2 中添加了一张名为 EleBicycle 的数据表
    companion object {
        // 定义一个 Migration 对象，实现版本 1 到版本 2 的迁移操作
        val MIGRATION_2_3 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                //创建用户表
                database.execSQL("create table if not exists User(" +
                        "uId integer primary key autoincrement not null," +
                        "username text not null, " +
                        "password text not null )")

                //创建故障信息表
                database.execSQL("create table if not exists Breakdown(" +
                        "bId integer primary key autoincrement not null," +
                        "ebNum text not null, " +
                        "bType text not null, " +
                        "bNum integer not null, " +
                        "bTime text not null )")

                //创建了名为 EleBicycle 的详细行程表
                database.execSQL("create table if not exists EleBicycle(" +
                        "ebId integer primary key autoincrement not null," +
                        "ebNum text not null, " + //车编号
                        "userName text not null, " + //使用者
                        "state text not null," +
                        "position text not null," +
                        "useTime text not null," +
                        "recordTime text not null )")

            }
        }

        // 使用单例模式，全局只存在一个 AppDatabase 实例
        private var instance: AppDatabase? = null

        // 获取 AppDatabase 实例的方法
        fun getDatabase(context: Context): AppDatabase {
            // 如果实例已存在，则返回该实例；否则创建一个新的实例
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "app_database")
                .addMigrations(MIGRATION_2_3) // 添加数据库迁移操作
                .build().apply {
                    instance = this // 将新创建的实例赋值给单例模式中的 instance
                }
        }
    }
}
