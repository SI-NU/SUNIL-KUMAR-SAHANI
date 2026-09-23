package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String, // e.g. "LN-101"
    val memberId: String,
    val memberName: String,
    val principalAmount: Double,
    val interestRateAnnual: Double = 10.0, // 10% per year
    val interestCycleMonths: Int = 3, // every 3 months
    val quarterlyInterest: Double, // Principal * 0.025
    val startDate: Long,
    val nextInterestDueDate: Long, // timestamp for quarterly due
    val lastInterestPaidDate: Long? = null,
    val totalInterestPaid: Double = 0.0,
    val totalPrincipalRepaid: Double = 0.0,
    val remainingPrincipal: Double,
    val quartersPaid: Int = 0,
    val status: String, // "PENDING", "APPROVED", "ACTIVE", "PAID_OFF", "REJECTED"
    val purpose: String,
    val appliedDate: Long,
    val adminRemark: String = ""
)
