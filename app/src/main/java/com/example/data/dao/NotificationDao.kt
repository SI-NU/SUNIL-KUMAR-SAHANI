package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE recipientMemberId IS NULL OR recipientMemberId = :memberId ORDER BY timestamp DESC")
    fun getNotificationsForMember(memberId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0 AND (recipientMemberId IS NULL OR recipientMemberId = :memberId)")
    fun getUnreadCountForMember(memberId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCountAdmin(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientMemberId IS NULL OR recipientMemberId = :memberId")
    suspend fun markAllReadForMember(memberId: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllReadAdmin()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getNotificationCount(): Int
}
