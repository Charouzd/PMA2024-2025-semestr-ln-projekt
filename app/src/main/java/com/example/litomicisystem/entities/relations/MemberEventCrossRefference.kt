package com.example.litomicisystem.entities.relations

import androidx.room.Entity

@Entity(primaryKeys = ["memberID","eventID"]
)
data class MemberEventCrossRefference (
    val memberID: Int,
    val eventID: Int

)