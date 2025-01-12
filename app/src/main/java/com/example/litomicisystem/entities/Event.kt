package com.example.litomicisystem.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Event (
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val eventName: String,
    val starts: String,
    val ends: String,
    val destination: String,
    val meetingPoint: String,
    val departure: String,
    val price: Float,
    val capacity: Int,
    val checklist: String,
    val description: String,
    val type: Short


    )