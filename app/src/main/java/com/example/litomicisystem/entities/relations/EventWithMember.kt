package com.example.litomicisystem.entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.litomicisystem.entities.Event
import com.example.litomicisystem.entities.Member


data class EventWithMember(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(MemberEventCrossRefference::class, parentColumn = "eventID", entityColumn = "memberID")
    )
    val members: List<Member> = emptyList()
)