package com.example.data.db

import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.security.AetCryptoManager

object AetInitialData {

    val members = listOf(
        MemberEntity(
            id = "AET-01",
            name = "Rajesh Sharma",
            phone = "+91 98765 43210",
            address = "Connaught Place, New Delhi",
            role = "ADMIN",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 1,
            payoutReceived = true,
            payoutDate = System.currentTimeMillis() - 60L * 24 * 3600 * 1000,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#1E88E5",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-02",
            name = "Sunil Sahani",
            phone = "+91 98111 22334",
            address = "Karol Bagh, New Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 2,
            payoutReceived = true,
            payoutDate = System.currentTimeMillis() - 30L * 24 * 3600 * 1000,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#43A047",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-03",
            name = "Amit Verma",
            phone = "+91 98222 33445",
            address = "Rohini Sector 7, Delhi",
            role = "TREASURER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 3,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#FB8C00",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-04",
            name = "Manoj Tiwari",
            phone = "+91 98333 44556",
            address = "Lajpat Nagar, New Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 4,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#8E24AA",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-05",
            name = "Vikram Patel",
            phone = "+91 98444 55667",
            address = "Chandni Chowk, Old Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 5,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 10000.0,
            isCurrentMonthPaid = false, // Due for current month
            avatarColorHex = "#E53935",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-06",
            name = "Deepak Gupta",
            phone = "+91 98555 66778",
            address = "Pitampura, North West Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 6,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#00897B",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-07",
            name = "Rohan Singh",
            phone = "+91 98666 77889",
            address = "Janakpuri, West Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 7,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#3949AB",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-08",
            name = "Sanjay Yadav",
            phone = "+91 98777 88990",
            address = "Dwarka Sector 10, New Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 8,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 10000.0,
            isCurrentMonthPaid = false, // Due for current month
            avatarColorHex = "#D81B60",
            pin = "1234"
        ),
        MemberEntity(
            id = "AET-09",
            name = "Pooja Mehta",
            phone = "+91 98888 99001",
            address = "Saket, South Delhi",
            role = "MEMBER",
            monthlyContribution = 5000.0,
            payoutMonthTurn = 9,
            payoutReceived = false,
            payoutDate = null,
            totalContributed = 15000.0,
            isCurrentMonthPaid = true,
            avatarColorHex = "#00ACC1",
            pin = "1234"
        )
    )

    val now = System.currentTimeMillis()
    val dayMs = 24L * 3600 * 1000

    val loans = listOf(
        LoanEntity(
            id = "LN-101",
            memberId = "AET-04",
            memberName = "Manoj Tiwari",
            principalAmount = 40000.0,
            interestRateAnnual = 10.0,
            interestCycleMonths = 3,
            quarterlyInterest = 40000.0 * 0.10 * (3.0 / 12.0), // 1000.0
            startDate = now - 75 * dayMs,
            nextInterestDueDate = now + 15 * dayMs, // Due in 15 days!
            lastInterestPaidDate = null,
            totalInterestPaid = 0.0,
            totalPrincipalRepaid = 0.0,
            remainingPrincipal = 40000.0,
            quartersPaid = 0,
            status = "ACTIVE",
            purpose = "Small Business Inventory Expansion",
            appliedDate = now - 75 * dayMs,
            adminRemark = "Approved by Committee on 75 days ago"
        ),
        LoanEntity(
            id = "LN-102",
            memberId = "AET-05",
            memberName = "Vikram Patel",
            principalAmount = 60000.0,
            interestRateAnnual = 10.0,
            interestCycleMonths = 3,
            quarterlyInterest = 60000.0 * 0.10 * (3.0 / 12.0), // 1500.0
            startDate = now - 45 * dayMs,
            nextInterestDueDate = now + 45 * dayMs,
            lastInterestPaidDate = null,
            totalInterestPaid = 0.0,
            totalPrincipalRepaid = 0.0,
            remainingPrincipal = 60000.0,
            quartersPaid = 0,
            status = "ACTIVE",
            purpose = "Agricultural Equipment & Solar Pump",
            appliedDate = now - 45 * dayMs,
            adminRemark = "Approved with joint guarantee"
        ),
        LoanEntity(
            id = "LN-103",
            memberId = "AET-08",
            memberName = "Sanjay Yadav",
            principalAmount = 30000.0,
            interestRateAnnual = 10.0,
            interestCycleMonths = 3,
            quarterlyInterest = 30000.0 * 0.10 * (3.0 / 12.0), // 750.0
            startDate = now,
            nextInterestDueDate = now + 90 * dayMs,
            lastInterestPaidDate = null,
            totalInterestPaid = 0.0,
            totalPrincipalRepaid = 0.0,
            remainingPrincipal = 30000.0,
            quartersPaid = 0,
            status = "PENDING",
            purpose = "Home Renovation & Electrical Works",
            appliedDate = now - 2 * dayMs,
            adminRemark = "Pending Committee approval in upcoming monthly meeting"
        )
    )

    fun createInitialTransactions(): List<TransactionEntity> {
        val list = mutableListOf<TransactionEntity>()
        var prevHash = "GENESIS_AET_COMMITTEE_BLOCK_00000"

        fun addTxn(
            id: String,
            memberId: String,
            memberName: String,
            type: String,
            amount: Double,
            daysAgo: Long,
            desc: String,
            ref: String
        ) {
            val ts = now - daysAgo * dayMs
            val hash = AetCryptoManager.createTransactionHash(id, memberId, type, amount, ts, prevHash)
            list.add(
                TransactionEntity(
                    id = id,
                    memberId = memberId,
                    memberName = memberName,
                    type = type,
                    amount = amount,
                    timestamp = ts,
                    status = "SUCCESS",
                    description = desc,
                    referenceNo = ref,
                    cryptoHash = hash,
                    prevHash = prevHash
                )
            )
            prevHash = hash
        }

        addTxn("TXN-1001", "AET-01", "Rajesh Sharma", "CONTRIBUTION", 5000.0, 60, "Month 1 Monthly Contribution", "UPI/AET/90011")
        addTxn("TXN-1002", "AET-02", "Sunil Sahani", "CONTRIBUTION", 5000.0, 60, "Month 1 Monthly Contribution", "UPI/AET/90012")
        addTxn("TXN-1003", "AET-01", "Rajesh Sharma", "PAYOUT", 45000.0, 58, "Month 1 Committee Payout (Round 1)", "NEFT/AET/45001")
        addTxn("TXN-1004", "AET-04", "Manoj Tiwari", "LOAN_DISBURSEMENT", 40000.0, 75, "Loan Disbursed (#LN-101) at 10% p.a.", "BANK/AET/40101")
        addTxn("TXN-1005", "AET-02", "Sunil Sahani", "PAYOUT", 45000.0, 28, "Month 2 Committee Payout (Round 2)", "NEFT/AET/45002")
        addTxn("TXN-1006", "AET-05", "Vikram Patel", "LOAN_DISBURSEMENT", 60000.0, 45, "Loan Disbursed (#LN-102) at 10% p.a.", "BANK/AET/40102")
        addTxn("TXN-1007", "AET-01", "Rajesh Sharma", "CONTRIBUTION", 5000.0, 5, "Month 3 Monthly Contribution", "UPI/AET/90031")
        addTxn("TXN-1008", "AET-02", "Sunil Sahani", "CONTRIBUTION", 5000.0, 4, "Month 3 Monthly Contribution", "UPI/AET/90032")
        addTxn("TXN-1009", "AET-03", "Amit Verma", "CONTRIBUTION", 5000.0, 3, "Month 3 Monthly Contribution", "UPI/AET/90033")
        addTxn("TXN-1010", "AET-04", "Manoj Tiwari", "INTEREST_PAYMENT", 1000.0, 2, "Loan #LN-101 1st Quarter 3-Month Interest (10% p.a.)", "UPI/AET/INT101")
        addTxn("TXN-1011", "AET-05", "Vikram Patel", "FINE_PENALTY", 200.0, 2, "Late Monthly Contribution Fine (10-Day Delay)", "CASH/AET/FINE01")
        addTxn("TXN-1012", "AET-08", "Sanjay Yadav", "FINE_PENALTY", 500.0, 1, "Meeting Absence & Delay Penalty Fee", "UPI/AET/FINE02")

        return list
    }

    val notifications = listOf(
        NotificationEntity(
            id = "NOTIF-01",
            recipientMemberId = "AET-04",
            title = "Quarterly Loan Interest Due Reminder",
            message = "Loan #LN-101: 3-month interest payment of ₹1,000 (10% p.a. on ₹40,000) is due in 15 days.",
            type = "INTEREST_REMINDER",
            timestamp = now - 1 * dayMs,
            isRead = false
        ),
        NotificationEntity(
            id = "NOTIF-02",
            recipientMemberId = "AET-05",
            title = "Monthly Contribution Pending",
            message = "Monthly contribution of ₹5,000 for Current Month is pending. Please clear dues.",
            type = "MONTHLY_DUE_REMINDER",
            timestamp = now - 2 * dayMs,
            isRead = false
        ),
        NotificationEntity(
            id = "NOTIF-03",
            recipientMemberId = "AET-08",
            title = "Monthly Contribution Pending",
            message = "Monthly contribution of ₹5,000 for Current Month is pending. Please submit payment.",
            type = "MONTHLY_DUE_REMINDER",
            timestamp = now - 3 * dayMs,
            isRead = false
        ),
        NotificationEntity(
            id = "NOTIF-04",
            recipientMemberId = null,
            title = "Upcoming Committee Payout Turn",
            message = "Month 3 Payout of ₹45,000 is scheduled for Amit Verma (AET-03).",
            type = "PAYOUT_UPDATE",
            timestamp = now - 4 * dayMs,
            isRead = true
        ),
        NotificationEntity(
            id = "NOTIF-05",
            recipientMemberId = null,
            title = "AET Security Audit & E2E Verification",
            message = "SHA-256 Ledger integrity check passed. All transactions and balances cryptographically verified.",
            type = "SECURITY_ALERT",
            timestamp = now - 5 * dayMs,
            isRead = true
        )
    )
}
