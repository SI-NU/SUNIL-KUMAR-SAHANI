package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String, // e.g. "TXN-88219"
    val memberId: String,
    val memberName: String,
    val type: String, // "CONTRIBUTION", "PAYOUT", "LOAN_DISBURSEMENT", "INTEREST_PAYMENT", "LOAN_REPAYMENT"
    val amount: Double,
    val timestamp: Long,
    val status: String = "SUCCESS",
    val description: String,
    val referenceNo: String,
    val cryptoHash: String,
    val prevHash: String = ""
)
