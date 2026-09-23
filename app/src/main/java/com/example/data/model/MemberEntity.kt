package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String, // e.g. "AET-01" to "AET-09"
    val name: String,
    val phone: String,
    val address: String = "Delhi, India",
    val role: String, // "ADMIN", "MEMBER", "TREASURER"
    val monthlyContribution: Double = 5000.0,
    val payoutMonthTurn: Int, // 1 to 9 (rotation month)
    val payoutReceived: Boolean = false,
    val payoutDate: Long? = null,
    val totalContributed: Double = 0.0,
    val isCurrentMonthPaid: Boolean = false,
    val avatarColorHex: String = "#1E88E5",
    val pin: String = "1234"
)

