package com.example.litomicisystem.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class Member (
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    val name: String,
    val lastName: String,
    val birthday: String,
    val gdpr:Boolean,
    val healthCondition: String,
    val familyID: Int

)