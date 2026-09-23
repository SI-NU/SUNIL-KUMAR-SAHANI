package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY payoutMonthTurn ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members ORDER BY payoutMonthTurn ASC")
    suspend fun getAllMembersList(): List<MemberEntity>

    @Query("SELECT * FROM members WHERE id = :memberId")
    fun getMemberByIdFlow(memberId: String): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: String): MemberEntity?

    @Query("SELECT * FROM members WHERE phone = :phone LIMIT 1")
    suspend fun getMemberByPhone(phone: String): MemberEntity?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<MemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: String)

    @Query("UPDATE members SET isCurrentMonthPaid = :paid WHERE id = :memberId")
    suspend fun updateMonthlyPaidStatus(memberId: String, paid: Boolean)

    @Query("UPDATE members SET totalContributed = totalContributed + :amount WHERE id = :memberId")
    suspend fun addContribution(memberId: String, amount: Double)

    @Query("UPDATE members SET payoutReceived = :received, payoutDate = :date WHERE id = :memberId")
    suspend fun updatePayoutStatus(memberId: String, received: Boolean, date: Long?)
}
