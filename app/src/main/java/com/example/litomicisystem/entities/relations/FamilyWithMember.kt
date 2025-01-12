package com.example.litomicisystem.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.litomicisystem.entities.Family
import com.example.litomicisystem.entities.Member

data class FamilyWithMember(
    @Embedded val familyID: Family,
    @Relation(
        parentColumn = "id",
        entityColumn = "familyID"
    )
    val logins: List<Member>
)