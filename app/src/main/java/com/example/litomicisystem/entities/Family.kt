package com.example.litomicisystem.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Family(
    @PrimaryKey(autoGenerate = true)
        val id: Int=0,
        val phone: String,
        val street: String,
        val city: String,
        val psc: String,
        val email : String
)