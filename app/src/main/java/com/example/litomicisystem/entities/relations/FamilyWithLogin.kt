package com.example.litomicisystem.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.litomicisystem.entities.Family
import com.example.litomicisystem.entities.UserLog

data class FamilyWithLogin(
    @Embedded val family: Family,
    @Relation(
        parentColumn = "id",
        entityColumn = "family2log"
    )
    val userLogs: List<UserLog>
)