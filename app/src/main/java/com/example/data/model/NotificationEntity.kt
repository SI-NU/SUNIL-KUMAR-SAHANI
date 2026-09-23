package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientMemberId: String?, // null for all members, or specific "AET-01"
    val title: String,
    val message: String,
    val type: String, // "MONTHLY_DUE_REMINDER", "INTEREST_REMINDER", "LOAN_UPDATE", "PAYOUT_UPDATE", "SECURITY_ALERT"
    val timestamp: Long,
    val isRead: Boolean = false
)
