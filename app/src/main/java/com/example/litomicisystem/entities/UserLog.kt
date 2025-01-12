package com.example.litomicisystem.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserLog(
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val password: String,
    @ColumnInfo(defaultValue = "0")
    val position: Int,
    val family2log: Int?
)