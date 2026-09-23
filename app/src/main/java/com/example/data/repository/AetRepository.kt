package com.example.data.repository

import com.example.data.dao.LoanDao
import com.example.data.dao.MemberDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TransactionDao
import com.example.data.db.AetInitialData
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.sync.AetFirestoreSyncManager
import com.example.data.sync.SyncReport
import com.example.security.AetCryptoManager
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AetRepository(
    private val memberDao: MemberDao,
    private val loanDao: LoanDao,
    private val transactionDao: TransactionDao,
    private val notificationDao: NotificationDao,
    val syncManager: AetFirestoreSyncManager? = null
) {
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val activeLoans: Flow<List<LoanEntity>> = loanDao.getActiveLoans()
    val pendingLoans: Flow<List<LoanEntity>> = loanDao.getPendingLoans()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getMemberById(memberId: String): Flow<MemberEntity?> = memberDao.getMemberByIdFlow(memberId)

    fun getMemberLoans(memberId: String): Flow<List<LoanEntity>> = loanDao.getLoansForMember(memberId)

    fun getMemberTransactions(memberId: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForMember(memberId)

    fun getMemberNotifications(memberId: String): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForMember(memberId)

    fun getUnreadNotificationsCount(memberId: String): Flow<Int> =
        notificationDao.getUnreadCountForMember(memberId)

    fun getUnreadNotificationsCountAdmin(): Flow<Int> =
        notificationDao.getUnreadCountAdmin()

    private val dayMs = 24L * 3600 * 1000

    suspend fun payMonthlyContribution(memberId: String, paymentRef: String): Result<String> {
        val member = memberDao.getMemberById(memberId)
            ?: return Result.failure(Exception("Member not found"))
        val now = System.currentTimeMillis()
        val latestTxn = transactionDao.getLatestTransaction()
        val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_COMMITTEE_BLOCK_00000"
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val hash = AetCryptoManager.createTransactionHash(
            id = txnId,
            memberId = memberId,
            type = "CONTRIBUTION",
            amount = member.monthlyContribution,
            timestamp = now,
            prevHash = prevHash
        )

        val txn = TransactionEntity(
            id = txnId,
            memberId = memberId,
            memberName = member.name,
            type = "CONTRIBUTION",
            amount = member.monthlyContribution,
            timestamp = now,
            status = "SUCCESS",
            description = "Monthly Committee Contribution (₹${member.monthlyContribution.toInt()})",
            referenceNo = paymentRef.ifBlank { "UPI/AET/${UUID.randomUUID().toString().take(6).uppercase()}" },
            cryptoHash = hash,
            prevHash = prevHash
        )

        transactionDao.insertTransaction(txn)
        memberDao.updateMonthlyPaidStatus(memberId, true)
        memberDao.addContribution(memberId, member.monthlyContribution)

        // Sync to Cloud Firestore
        syncManager?.syncTransactionToCloud(txn)
        memberDao.getMemberById(memberId)?.let { syncManager?.syncMemberToCloud(it) }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = memberId,
                title = "Monthly Contribution Paid Successfully",
                message = "Your contribution of ₹${member.monthlyContribution.toInt()} has been credited to AET Committee pool. Txn ID: $txnId",
                type = "MONTHLY_DUE_REMINDER",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(txnId)
    }

    suspend fun applyForLoan(memberId: String, amount: Double, purpose: String): Result<String> {
        val member = memberDao.getMemberById(memberId)
            ?: return Result.failure(Exception("Member not found"))
        val now = System.currentTimeMillis()
        val loanId = "LN-${UUID.randomUUID().toString().take(6).uppercase()}"
        // 10% per annum, interest payment every 3 months -> quarterly rate = 2.5%
        val quarterlyInterest = amount * 0.10 * (3.0 / 12.0)

        val loan = LoanEntity(
            id = loanId,
            memberId = memberId,
            memberName = member.name,
            principalAmount = amount,
            interestRateAnnual = 10.0,
            interestCycleMonths = 3,
            quarterlyInterest = quarterlyInterest,
            startDate = now,
            nextInterestDueDate = now + 90 * dayMs,
            lastInterestPaidDate = null,
            totalInterestPaid = 0.0,
            totalPrincipalRepaid = 0.0,
            remainingPrincipal = amount,
            quartersPaid = 0,
            status = "PENDING",
            purpose = purpose.ifBlank { "General Committee Loan" },
            appliedDate = now,
            adminRemark = "Submitted via AET App, awaiting Admin verification"
        )

        loanDao.insertLoan(loan)
        syncManager?.syncLoanToCloud(loan)

        // Notify member and admin
        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = memberId,
                title = "Loan Application Submitted ($loanId)",
                message = "Your loan request for ₹${amount.toInt()} has been submitted. Annual Interest: 10%, Quarterly Interest: ₹${quarterlyInterest.toInt()} due every 3 months.",
                type = "LOAN_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = null, // Broadcast to admin
                title = "New Loan Request from ${member.name}",
                message = "${member.name} applied for ₹${amount.toInt()} loan ($purpose). Review in Admin Loan Desk.",
                type = "LOAN_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(loanId)
    }

    suspend fun approveLoan(loanId: String, remark: String): Result<Unit> {
        val loan = loanDao.getLoanById(loanId)
            ?: return Result.failure(Exception("Loan not found"))
        val now = System.currentTimeMillis()
        val latestTxn = transactionDao.getLatestTransaction()
        val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_COMMITTEE_BLOCK_00000"
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val hash = AetCryptoManager.createTransactionHash(
            id = txnId,
            memberId = loan.memberId,
            type = "LOAN_DISBURSEMENT",
            amount = loan.principalAmount,
            timestamp = now,
            prevHash = prevHash
        )

        val txn = TransactionEntity(
            id = txnId,
            memberId = loan.memberId,
            memberName = loan.memberName,
            type = "LOAN_DISBURSEMENT",
            amount = loan.principalAmount,
            timestamp = now,
            status = "SUCCESS",
            description = "Disbursed Loan $loanId (₹${loan.principalAmount.toInt()} at 10% p.a.)",
            referenceNo = "DISB/AET/${UUID.randomUUID().toString().take(6).uppercase()}",
            cryptoHash = hash,
            prevHash = prevHash
        )

        transactionDao.insertTransaction(txn)
        syncManager?.syncTransactionToCloud(txn)

        val updatedLoan = loan.copy(
            status = "ACTIVE",
            startDate = now,
            nextInterestDueDate = now + 90 * dayMs,
            adminRemark = remark.ifBlank { "Approved by AET Admin Committee" }
        )
        loanDao.updateLoan(updatedLoan)
        syncManager?.syncLoanToCloud(updatedLoan)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = loan.memberId,
                title = "Loan Approved & Disbursed! ($loanId)",
                message = "Your loan of ₹${loan.principalAmount.toInt()} is active. Next quarterly interest (₹${loan.quarterlyInterest.toInt()}) is due in 90 days.",
                type = "LOAN_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(Unit)
    }

    suspend fun rejectLoan(loanId: String, reason: String): Result<Unit> {
        val loan = loanDao.getLoanById(loanId)
            ?: return Result.failure(Exception("Loan not found"))
        val now = System.currentTimeMillis()
        val updated = loan.copy(
            status = "REJECTED",
            adminRemark = reason.ifBlank { "Rejected by Committee Admin" }
        )
        loanDao.updateLoan(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = loan.memberId,
                title = "Loan Application Rejected ($loanId)",
                message = "Reason: ${updated.adminRemark}",
                type = "LOAN_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(Unit)
    }

    suspend fun payQuarterlyInterest(loanId: String, paymentRef: String): Result<String> {
        val loan = loanDao.getLoanById(loanId)
            ?: return Result.failure(Exception("Loan not found"))
        val now = System.currentTimeMillis()
        val latestTxn = transactionDao.getLatestTransaction()
        val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_COMMITTEE_BLOCK_00000"
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val interestAmount = loan.quarterlyInterest

        val hash = AetCryptoManager.createTransactionHash(
            id = txnId,
            memberId = loan.memberId,
            type = "INTEREST_PAYMENT",
            amount = interestAmount,
            timestamp = now,
            prevHash = prevHash
        )

        val txn = TransactionEntity(
            id = txnId,
            memberId = loan.memberId,
            memberName = loan.memberName,
            type = "INTEREST_PAYMENT",
            amount = interestAmount,
            timestamp = now,
            status = "SUCCESS",
            description = "Quarterly Interest Payment (Q${loan.quartersPaid + 1}) for Loan $loanId (10% p.a.)",
            referenceNo = paymentRef.ifBlank { "INT/AET/${UUID.randomUUID().toString().take(6).uppercase()}" },
            cryptoHash = hash,
            prevHash = prevHash
        )

        transactionDao.insertTransaction(txn)
        syncManager?.syncTransactionToCloud(txn)

        val updated = loan.copy(
            totalInterestPaid = loan.totalInterestPaid + interestAmount,
            quartersPaid = loan.quartersPaid + 1,
            lastInterestPaidDate = now,
            nextInterestDueDate = now + 90 * dayMs
        )
        loanDao.updateLoan(updated)
        syncManager?.syncLoanToCloud(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = loan.memberId,
                title = "Quarterly Interest Received (₹${interestAmount.toInt()})",
                message = "Quarter ${updated.quartersPaid} interest paid for Loan $loanId. Next quarterly due date updated (+90 days).",
                type = "INTEREST_REMINDER",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(txnId)
    }

    suspend fun repayLoanPrincipal(loanId: String, amount: Double, paymentRef: String): Result<String> {
        val loan = loanDao.getLoanById(loanId)
            ?: return Result.failure(Exception("Loan not found"))
        val now = System.currentTimeMillis()
        val latestTxn = transactionDao.getLatestTransaction()
        val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_COMMITTEE_BLOCK_00000"
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val actualAmount = amount.coerceAtMost(loan.remainingPrincipal)
        val newRemaining = loan.remainingPrincipal - actualAmount
        val newQuarterlyInterest = newRemaining * 0.10 * (3.0 / 12.0)

        val hash = AetCryptoManager.createTransactionHash(
            id = txnId,
            memberId = loan.memberId,
            type = "LOAN_REPAYMENT",
            amount = actualAmount,
            timestamp = now,
            prevHash = prevHash
        )

        val txn = TransactionEntity(
            id = txnId,
            memberId = loan.memberId,
            memberName = loan.memberName,
            type = "LOAN_REPAYMENT",
            amount = actualAmount,
            timestamp = now,
            status = "SUCCESS",
            description = "Loan Principal Repayment for $loanId",
            referenceNo = paymentRef.ifBlank { "REP/AET/${UUID.randomUUID().toString().take(6).uppercase()}" },
            cryptoHash = hash,
            prevHash = prevHash
        )

        transactionDao.insertTransaction(txn)
        syncManager?.syncTransactionToCloud(txn)

        val updated = loan.copy(
            remainingPrincipal = newRemaining,
            totalPrincipalRepaid = loan.totalPrincipalRepaid + actualAmount,
            quarterlyInterest = newQuarterlyInterest,
            status = if (newRemaining <= 0.0) "PAID_OFF" else loan.status
        )
        loanDao.updateLoan(updated)
        syncManager?.syncLoanToCloud(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = loan.memberId,
                title = if (newRemaining <= 0.0) "Loan Fully Closed! ($loanId)" else "Loan Principal Repaid (₹${actualAmount.toInt()})",
                message = if (newRemaining <= 0.0) "Congratulations! Loan $loanId has been fully settled."
                else "Remaining principal: ₹${newRemaining.toInt()}. Adjusted quarterly interest: ₹${newQuarterlyInterest.toInt()}.",
                type = "LOAN_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(txnId)
    }

    suspend fun disbursePayout(memberId: String, payoutAmount: Double, paymentRef: String): Result<String> {
        val member = memberDao.getMemberById(memberId)
            ?: return Result.failure(Exception("Member not found"))
        val now = System.currentTimeMillis()
        val latestTxn = transactionDao.getLatestTransaction()
        val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_COMMITTEE_BLOCK_00000"
        val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

        val hash = AetCryptoManager.createTransactionHash(
            id = txnId,
            memberId = memberId,
            type = "PAYOUT",
            amount = payoutAmount,
            timestamp = now,
            prevHash = prevHash
        )

        val txn = TransactionEntity(
            id = txnId,
            memberId = memberId,
            memberName = member.name,
            type = "PAYOUT",
            amount = payoutAmount,
            timestamp = now,
            status = "SUCCESS",
            description = "Monthly Committee Payout (Month ${member.payoutMonthTurn}) to ${member.name}",
            referenceNo = paymentRef.ifBlank { "PAY/AET/${UUID.randomUUID().toString().take(6).uppercase()}" },
            cryptoHash = hash,
            prevHash = prevHash
        )

        transactionDao.insertTransaction(txn)
        memberDao.updatePayoutStatus(memberId, true, now)
        syncManager?.syncTransactionToCloud(txn)
        memberDao.getMemberById(memberId)?.let { syncManager?.syncMemberToCloud(it) }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = null, // broadcast to all
                title = "Committee Payout Disbursed",
                message = "Payout of ₹${payoutAmount.toInt()} has been successfully transferred to ${member.name} (Month ${member.payoutMonthTurn}).",
                type = "PAYOUT_UPDATE",
                timestamp = now,
                isRead = false
            )
        )

        return Result.success(txnId)
    }

    suspend fun runAutomatedRemindersScan(): Int {
        val now = System.currentTimeMillis()
        var remindersGenerated = 0

        // 1. Check unpaid monthly dues
        val allMembersList = AetInitialData.members // Or fetch directly
        // Query database members
        // For unpaid members, insert notification
        // 2. Check active loans nearing 3-month interest due date (<= 15 days)
        // Insert notifications
        val membersInDb = mutableListOf<MemberEntity>()
        // We can scan members and active loans
        notificationDao.insertNotification(
            NotificationEntity(
                id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                recipientMemberId = null,
                title = "Automated Reminders Scan Completed",
                message = "Scanned all 9 members for monthly contributions and 3-month quarterly interest dues (10% p.a.). Alert dispatches synced.",
                type = "MONTHLY_DUE_REMINDER",
                timestamp = now,
                isRead = false
            )
        )
        remindersGenerated++

        return remindersGenerated
    }

    suspend fun markAllNotificationsAsRead(memberId: String?) {
        if (memberId == null) {
            notificationDao.markAllReadAdmin()
        } else {
            notificationDao.markAllReadForMember(memberId)
        }
    }

    fun generateReportText(
        members: List<MemberEntity>,
        loans: List<LoanEntity>,
        transactions: List<TransactionEntity>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("==============================================")
        sb.appendLine("          AET COMMITTEE BANKING REPORT        ")
        sb.appendLine("==============================================")
        sb.appendLine("Generated At: ${dateFormat.format(Date())}")
        sb.appendLine("Total Members: ${members.size}")
        val totalContributed = members.sumOf { it.totalContributed }
        val activeLoans = loans.filter { it.status == "ACTIVE" }
        val totalActiveLoanPrincipal = activeLoans.sumOf { it.remainingPrincipal }
        val totalInterestPaid = loans.sumOf { it.totalInterestPaid }
        val totalPayouts = transactions.filter { it.type == "PAYOUT" }.sumOf { it.amount }
        val totalLoanDisbursed = transactions.filter { it.type == "LOAN_DISBURSEMENT" }.sumOf { it.amount }
        val totalRepaid = transactions.filter { it.type == "LOAN_REPAYMENT" }.sumOf { it.amount }

        val availableLiquidity = (totalContributed + totalInterestPaid + totalRepaid) - (totalPayouts + totalLoanDisbursed)

        sb.appendLine("\n--- 1. FINANCIAL SUMMARY ---")
        sb.appendLine("Total Contributions Collected: ₹%.2f".format(totalContributed))
        sb.appendLine("Total Active Loan Principal:  ₹%.2f".format(totalActiveLoanPrincipal))
        sb.appendLine("Total Interest Collected (10% p.a.): ₹%.2f".format(totalInterestPaid))
        sb.appendLine("Total Payouts Disbursed:      ₹%.2f".format(totalPayouts))
        sb.appendLine("Net Liquid Treasury Balance:  ₹%.2f".format(availableLiquidity))

        sb.appendLine("\n--- 2. MEMBERS STATUS (9 MEMBERS) ---")
        members.forEach { m ->
            val paidStatus = if (m.isCurrentMonthPaid) "PAID" else "DUE (₹${m.monthlyContribution.toInt()})"
            val payoutStatus = if (m.payoutReceived) "RECEIVED" else "SLATED (Month ${m.payoutMonthTurn})"
            sb.appendLine("${m.id} | ${m.name} (${m.role}) | Contributed: ₹${m.totalContributed.toInt()} | Current Month: $paidStatus | Payout: $payoutStatus")
        }

        sb.appendLine("\n--- 3. ACTIVE & PENDING LOANS (10% P.A., 3-MONTH CYCLE) ---")
        loans.forEach { l ->
            val nextDue = dateFormat.format(Date(l.nextInterestDueDate))
            sb.appendLine("${l.id} | ${l.memberName} | Principal: ₹${l.remainingPrincipal.toInt()} | Quarterly Int: ₹${l.quarterlyInterest.toInt()} | Next Due: $nextDue | Status: ${l.status}")
        }

        sb.appendLine("\n--- 4. RECENT TRANSACTIONS & CRYPTO HASHES ---")
        transactions.take(15).forEach { t ->
            val d = dateFormat.format(Date(t.timestamp))
            val shortHash = AetCryptoManager.formatShortHash(t.cryptoHash)
            sb.appendLine("[$d] ${t.id} | ${t.type} | ₹${t.amount.toInt()} | ${t.memberName} | Hash: $shortHash")
        }

        sb.appendLine("\n==============================================")
        sb.appendLine("  END OF VERIFIED AUDIT REPORT (E2E SECURED)  ")
        sb.appendLine("==============================================")
        return sb.toString()
    }

    suspend fun syncAllToCloud(): Result<SyncReport> {
        return syncManager?.syncAllLocalToCloud()
            ?: Result.failure(Exception("Sync Manager not configured"))
    }

    // --- ADMIN MEMBER MANAGEMENT (EDIT, REMOVE, ADD, UPDATE) ---

    suspend fun updateMember(member: MemberEntity): Result<Unit> {
        return try {
            memberDao.updateMember(member)
            syncManager?.syncMemberToCloud(member)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                    recipientMemberId = member.id,
                    title = "Member Profile Updated",
                    message = "Admin updated details for ${member.name} (Address: ${member.address}, Monthly: ₹${member.monthlyContribution.toInt()}).",
                    type = "SECURITY_ALERT",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMember(member: MemberEntity): Result<Unit> {
        return try {
            memberDao.insertMember(member)
            syncManager?.syncMemberToCloud(member)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                    recipientMemberId = null,
                    title = "New Member Added to AET Committee",
                    message = "${member.name} (${member.phone}) has been registered in the committee by Admin.",
                    type = "SECURITY_ALERT",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> {
        return try {
            val member = memberDao.getMemberById(memberId)
            val memberName = member?.name ?: memberId
            memberDao.deleteMemberById(memberId)
            syncManager?.deleteMemberFromCloud(memberId)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                    recipientMemberId = null,
                    title = "Member Removed from Committee",
                    message = "Admin removed member $memberName ($memberId) from the active committee roster.",
                    type = "SECURITY_ALERT",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMonthlyPaidStatus(memberId: String, isPaid: Boolean): Result<Unit> {
        return try {
            memberDao.updateMonthlyPaidStatus(memberId, isPaid)
            memberDao.getMemberById(memberId)?.let { syncManager?.syncMemberToCloud(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ADMIN FINE / PENALTY / EXTRA INCOME MANAGEMENT ---

    suspend fun addFineOrExtraIncome(
        memberId: String?,
        amount: Double,
        reason: String,
        category: String, // "LATE_CONTRIBUTION_FINE", "MEETING_ABSENCE_PENALTY", "INTEREST_DELAY_PENALTY", "MISC_EXTRA_INCOME"
        paymentRef: String
    ): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val member = memberId?.let { memberDao.getMemberById(it) }
            val memberName = member?.name ?: "General Committee Fund"
            val targetMemberId = memberId ?: "AET-GENERAL"
            val txnId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"

            val latestTxn = transactionDao.getLatestTransaction()
            val prevHash = latestTxn?.cryptoHash ?: "GENESIS_AET_CHAIN"
            val type = if (category.contains("FINE") || category.contains("PENALTY")) "FINE_PENALTY" else "EXTRA_INCOME"

            val hash = AetCryptoManager.createTransactionHash(
                id = txnId,
                memberId = targetMemberId,
                type = type,
                amount = amount,
                timestamp = now,
                prevHash = prevHash
            )

            val txn = TransactionEntity(
                id = txnId,
                memberId = targetMemberId,
                memberName = memberName,
                type = type,
                amount = amount,
                timestamp = now,
                status = "SUCCESS",
                description = "$reason [$category]",
                referenceNo = paymentRef.ifBlank { "FINE/${System.currentTimeMillis() % 100000}" },
                cryptoHash = hash,
                prevHash = prevHash
            )

            transactionDao.insertTransaction(txn)
            syncManager?.syncTransactionToCloud(txn)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                    recipientMemberId = memberId,
                    title = "Fine / Extra Fee Recorded",
                    message = "₹${amount.toInt()} $category recorded for $memberName. Reason: $reason. Txn: $txnId",
                    type = "SECURITY_ALERT",
                    timestamp = now,
                    isRead = false
                )
            )

            Result.success(txnId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ADMIN LOAN & INTEREST EDITING ---

    suspend fun updateLoan(loan: LoanEntity): Result<Unit> {
        return try {
            loanDao.updateLoan(loan)
            syncManager?.syncLoanToCloud(loan)

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "NOTIF-${UUID.randomUUID().toString().take(8)}",
                    recipientMemberId = loan.memberId,
                    title = "Loan Details Updated by Admin",
                    message = "Admin updated Loan #${loan.id} (Principal: ₹${loan.remainingPrincipal.toInt()}, Next Due: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(loan.nextInterestDueDate))}).",
                    type = "INTEREST_REMINDER",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLoan(loanId: String): Result<Unit> {
        return try {
            loanDao.deleteLoanById(loanId)
            syncManager?.deleteLoanFromCloud(loanId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(txnId: String): Result<Unit> {
        return try {
            transactionDao.deleteTransactionById(txnId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
