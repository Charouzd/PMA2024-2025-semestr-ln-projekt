package com.example.litomicisystem

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.litomicisystem.entities.Event
import com.example.litomicisystem.entities.Family
import com.example.litomicisystem.entities.Member
import com.example.litomicisystem.entities.UserLog
import com.example.litomicisystem.entities.relations.MemberEventCrossRefference

@Database(
    entities = [
        UserLog::class,
        Family::class,
        Member::class,
        Event::class,
        MemberEventCrossRefference::class
    ],
    version =1

)
abstract class SystemDatabase: RoomDatabase() {
    abstract val litostemDao: LitostemDao


    companion object{
        @Volatile
        private var INSTANCE: SystemDatabase? = null

        fun getInstance(context: Context): SystemDatabase {
            synchronized(this){
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SystemDatabase::class.java,
                    "litostem_db"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}