package com.example.litomicisystem.entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.litomicisystem.entities.Event
import com.example.litomicisystem.entities.Member

data class MemberWithEvent(
    @Embedded val member: Member,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(MemberEventCrossRefference::class, parentColumn = "memberID", entityColumn = "eventID" )
    )
    val events: List<Event>
)