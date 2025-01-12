package com.example.litomicisystem

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.litomicisystem.entities.Event
import com.example.litomicisystem.entities.Family
import com.example.litomicisystem.entities.Member

import com.example.litomicisystem.entities.UserLog
import com.example.litomicisystem.entities.relations.EventWithMember
import com.example.litomicisystem.entities.relations.FamilyWithLogin
import com.example.litomicisystem.entities.relations.FamilyWithMember
import com.example.litomicisystem.entities.relations.MemberEventCrossRefference
import com.example.litomicisystem.entities.relations.MemberWithEvent

@Dao
interface LitostemDao {

// CREATE DATA
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUserLog(userLog: UserLog)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertFamily(family: Family)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertEvent(event: Event)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMember(member: Member)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMemberEventCrossRefference(crossReference: MemberEventCrossRefference)


//  DELETE DATA
    @Delete
    fun deleteFamily(family: Family)
    @Delete
    fun deleteUserLog(userLog: UserLog)
    @Delete
    fun deleteEvent(event: Event)
    @Delete
    fun deleteMember(member: Member)
    @Delete
    fun deleteCrossRef(memberEventCrossReference: MemberEventCrossRefference)
    @Query("DELETE FROM MemberEventCrossRefference WHERE eventID = :eventId AND memberID = :memberId")
    fun deleteMemberEventCrossReference(eventId: Int, memberId: Int)

// UPDATE DATA
    @Update
   fun updateFamily(family: Family)
    @Update
    fun updateUserLog(userLog: UserLog)
    @Update
    fun updateEvent(event: Event)
    @Update
    fun updateMember(member: Member)
    @Update
    fun updateMemberEventCrossRef(memberEventCrossReference: MemberEventCrossRefference)

// QUERIES
    @Query("SELECT * FROM family WHERE id = :familyId")
    fun getFamilyById(familyId: Int): Family?
    @Query("SELECT * FROM userlog WHERE email = :userLogId")
    fun getUserLogById(userLogId: String): UserLog?
    @Query("SELECT * FROM event WHERE id = :eventId")
    fun getEventById(eventId: Int): Event?
    @Query("SELECT * FROM member WHERE id = :memberId")
    fun getMemberById(memberId: Int): Member?
    @Query("SELECT * FROM membereventcrossrefference WHERE memberId = :memberId AND eventId = :eventId")
    fun getMemberEventCrossRef(memberId: Int, eventId: Int): MemberEventCrossRefference?
    @Query("SELECT * FROM family ORDER BY id DESC LIMIT 1")
    fun getLastAddedFamily(): Family?
    @Query("SELECT * FROM Member WHERE familyID = :familyId")
    fun getMembersByFamilyId(familyId: Int): List<Member>
    @Query("SELECT * FROM event")
    fun getAllEvents(): List<Event> // Přizpůsobte `Event` vašemu modelu tabulky
    @Query("SELECT * FROM family ORDER BY id DESC LIMIT 1")
    fun getFamilyWithMaxId(): Family?
    @Query("""
    SELECT m.* 
    FROM Member m
    INNER JOIN MemberEventCrossRefference mecr ON m.id = mecr.memberID
    WHERE mecr.eventID = :eventId
""")
    fun getMembersForEvent(eventId: Int): List<Member>
    @Query("SELECT * FROM UserLog")
    fun getAllUserLogs(): List<UserLog>

// TRANSACTIONS
    @Transaction
    @Query("SELECT * FROM family WHERE id = :familyID")
    fun getFamilyWithUsers(familyID: Int): List<FamilyWithLogin>

    @Transaction
    @Query("SELECT * FROM family WHERE id=:familyID")
    fun getFamilyWithMembers(familyID: Int): List<FamilyWithMember>

    @Transaction
    @Query("SELECT * FROM event WHERE id=:eventID")
    fun getMembersOfEvent(eventID: Int): List<EventWithMember>

    @Transaction
    @Query("SELECT * FROM member WHERE id=:memberID")
    fun getEventsOfMember(memberID: Int): List<MemberWithEvent>

// FULL CLEAR
    @Query("DELETE FROM family")
    fun deleteAllFamilies()
    @Query("SELECT * FROM member WHERE familyID= :accountId")
    fun getMembersByAccountId(accountId: Int): List<Member>
    @Query("DELETE FROM userlog")
    fun deleteAllUserLogs()
    @Query("DELETE FROM event")
    fun deleteAllEvents()
    @Query("DELETE FROM member")
    fun deleteAllMembers()
    @Query("DELETE FROM membereventcrossrefference")
    fun deleteAllMemberEventCrossRefs()
    @Query("DELETE FROM sqlite_sequence WHERE name ='family'")
    fun resetFamilyAutoIncrement()
    @Query("DELETE FROM sqlite_sequence WHERE name = 'userlog'")
    fun resetUserLogAutoIncrement()
    @Query("DELETE FROM sqlite_sequence WHERE name = 'event'")
    fun resetEventAutoIncrement()
    @Query("DELETE FROM sqlite_sequence WHERE name = 'member'")
    fun resetMemberAutoIncrement()
    @Query("DELETE FROM sqlite_sequence WHERE name = 'membereventcrossrefference'")
    fun resetMemberEventCrossRefAutoIncrement()

    @Transaction
    @Query("DELETE FROM sqlite_sequence")
    fun fullClear()

}