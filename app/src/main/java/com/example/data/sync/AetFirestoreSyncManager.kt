package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.dao.LoanDao
import com.example.data.dao.MemberDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TransactionDao
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.TransactionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class SyncState {
    OFFLINE,
    IDLE,
    SYNCING,
    SYNCED,
    ERROR
}

data class SyncReport(
    val state: SyncState = SyncState.IDLE,
    val lastSyncTime: Long = 0L,
    val syncedTransactionsCount: Int = 0,
    val syncedLoansCount: Int = 0,
    val syncedMembersCount: Int = 0,
    val message: String = "Ready to sync"
)

class AetFirestoreSyncManager(
    private val context: Context,
    private val memberDao: MemberDao,
    private val loanDao: LoanDao,
    private val transactionDao: TransactionDao,
    private val notificationDao: NotificationDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null

    private val _syncReport = MutableStateFlow(SyncReport())
    val syncReport: StateFlow<SyncReport> = _syncReport.asStateFlow()

    private var loansListener: ListenerRegistration? = null
    private var transactionsListener: ListenerRegistration? = null

    init {
        initializeFirestore()
    }

    private fun initializeFirestore() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                Log.i("AetSync", "Firebase Firestore initialized successfully.")
                _syncReport.value = _syncReport.value.copy(
                    state = SyncState.IDLE,
                    message = "Cloud Sync Connected"
                )
                startRealtimeListeners()
            } else {
                Log.w("AetSync", "FirebaseApp not initialized. Operating in local-first Room mode.")
                _syncReport.value = _syncReport.value.copy(
                    state = SyncState.OFFLINE,
                    message = "Local Room Engine (Offline Mode)"
                )
            }
        } catch (e: Exception) {
            Log.e("AetSync", "Firestore init skipped or unavailable: ${e.message}")
            _syncReport.value = _syncReport.value.copy(
                state = SyncState.OFFLINE,
                message = "Local Room Engine"
            )
        }
    }

    /**
     * Uploads all local Room entities to Cloud Firestore
     */
    suspend fun syncAllLocalToCloud(): Result<SyncReport> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(Exception("Firestore not initialized on device"))

        try {
            _syncReport.value = _syncReport.value.copy(state = SyncState.SYNCING, message = "Uploading data to Firestore...")

            val members: List<MemberEntity> = memberDao.getAllMembersList()
            val loans: List<LoanEntity> = loanDao.getAllLoansList()
            val transactions: List<TransactionEntity> = transactionDao.getAllTransactionsList()

            // 1. Sync 9 Members
            for (member in members) {
                val memberMap = mapOf<String, Any?>(
                    "id" to member.id,
                    "name" to member.name,
                    "phone" to member.phone,
                    "address" to member.address,
                    "role" to member.role,
                    "monthlyContribution" to member.monthlyContribution,
                    "totalContributed" to member.totalContributed,
                    "isCurrentMonthPaid" to member.isCurrentMonthPaid,
                    "payoutMonthTurn" to member.payoutMonthTurn,
                    "payoutReceived" to member.payoutReceived,
                    "avatarColorHex" to member.avatarColorHex,
                    "lastUpdated" to System.currentTimeMillis()
                )
                db.collection("committee_members")
                    .document(member.id)
                    .set(memberMap, SetOptions.merge())
                    .await()
            }

            // 2. Sync Loans
            for (loan in loans) {
                val loanMap = mapOf<String, Any?>(
                    "id" to loan.id,
                    "memberId" to loan.memberId,
                    "memberName" to loan.memberName,
                    "principalAmount" to loan.principalAmount,
                    "remainingPrincipal" to loan.remainingPrincipal,
                    "interestRateAnnual" to loan.interestRateAnnual,
                    "quarterlyInterest" to loan.quarterlyInterest,
                    "status" to loan.status,
                    "appliedDate" to loan.appliedDate,
                    "startDate" to loan.startDate,
                    "nextInterestDueDate" to loan.nextInterestDueDate,
                    "quartersPaid" to loan.quartersPaid,
                    "totalInterestPaid" to loan.totalInterestPaid,
                    "purpose" to loan.purpose,
                    "adminRemark" to loan.adminRemark,
                    "lastUpdated" to System.currentTimeMillis()
                )
                db.collection("committee_loans")
                    .document(loan.id)
                    .set(loanMap, SetOptions.merge())
                    .await()
            }

            // 3. Sync Transactions (Immutable Ledger)
            for (txn in transactions) {
                val transactionMap = mapOf<String, Any?>(
                    "id" to txn.id,
                    "memberId" to txn.memberId,
                    "memberName" to txn.memberName,
                    "type" to txn.type,
                    "amount" to txn.amount,
                    "timestamp" to txn.timestamp,
                    "description" to txn.description,
                    "referenceNo" to txn.referenceNo,
                    "cryptoHash" to txn.cryptoHash,
                    "prevHash" to txn.prevHash,
                    "status" to txn.status
                )
                db.collection("committee_transactions")
                    .document(txn.id)
                    .set(transactionMap, SetOptions.merge())
                    .await()
            }

            val report = SyncReport(
                state = SyncState.SYNCED,
                lastSyncTime = System.currentTimeMillis(),
                syncedMembersCount = members.size,
                syncedLoansCount = loans.size,
                syncedTransactionsCount = transactions.size,
                message = "Synced ${transactions.size} txns & ${loans.size} loans to Firestore"
            )
            _syncReport.value = report
            Result.success(report)
        } catch (e: Exception) {
            Log.e("AetSync", "Error during Firestore sync: ${e.message}", e)
            val errorReport = _syncReport.value.copy(
                state = SyncState.ERROR,
                message = "Sync failed: ${e.localizedMessage}"
            )
            _syncReport.value = errorReport
            Result.failure(e)
        }
    }

    /**
     * Real-time listener that propagates Firestore updates into local Room database
     */
    private fun startRealtimeListeners() {
        val db = firestore ?: return

        try {
            // Listen for transactions added by other committee devices
            transactionsListener = db.collection("committee_transactions")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("AetSync", "Transaction listen error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val memberId = doc.getString("memberId") ?: ""
                                    val memberName = doc.getString("memberName") ?: ""
                                    val type = doc.getString("type") ?: "CONTRIBUTION"
                                    val amount = doc.getDouble("amount") ?: 0.0
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    val description = doc.getString("description") ?: ""
                                    val referenceNo = doc.getString("referenceNo") ?: ""
                                    val cryptoHash = doc.getString("cryptoHash") ?: ""
                                    val prevHash = doc.getString("prevHash") ?: "GENESIS"
                                    val status = doc.getString("status") ?: "COMPLETED"

                                    val txn = TransactionEntity(
                                        id = id,
                                        memberId = memberId,
                                        memberName = memberName,
                                        type = type,
                                        amount = amount,
                                        timestamp = timestamp,
                                        description = description,
                                        referenceNo = referenceNo,
                                        cryptoHash = cryptoHash,
                                        prevHash = prevHash,
                                        status = status
                                    )
                                    transactionDao.insertTransaction(txn)
                                } catch (e: Exception) {
                                    Log.w("AetSync", "Failed to deserialize txn doc: ${e.message}")
                                }
                            }
                        }
                    }
                }

            // Listen for loans updated/added in Cloud
            loansListener = db.collection("committee_loans")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("AetSync", "Loan listen error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch {
                            for (doc in snapshots.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val memberId = doc.getString("memberId") ?: ""
                                    val memberName = doc.getString("memberName") ?: ""
                                    val principalAmount = doc.getDouble("principalAmount") ?: 0.0
                                    val remainingPrincipal = doc.getDouble("remainingPrincipal") ?: principalAmount
                                    val interestRateAnnual = doc.getDouble("interestRateAnnual") ?: 10.0
                                    val quarterlyInterest = doc.getDouble("quarterlyInterest") ?: (remainingPrincipal * 0.025)
                                    val status = doc.getString("status") ?: "PENDING"
                                    val appliedDate = doc.getLong("appliedDate") ?: System.currentTimeMillis()
                                    val startDate = doc.getLong("startDate") ?: System.currentTimeMillis()
                                    val nextInterestDueDate = doc.getLong("nextInterestDueDate") ?: (System.currentTimeMillis() + 90L * 24 * 3600 * 1000)
                                    val quartersPaid = doc.getLong("quartersPaid")?.toInt() ?: 0
                                    val totalInterestPaid = doc.getDouble("totalInterestPaid") ?: 0.0
                                    val purpose = doc.getString("purpose") ?: ""
                                    val adminRemark = doc.getString("adminRemark") ?: ""

                                    val loan = LoanEntity(
                                        id = id,
                                        memberId = memberId,
                                        memberName = memberName,
                                        principalAmount = principalAmount,
                                        remainingPrincipal = remainingPrincipal,
                                        interestRateAnnual = interestRateAnnual,
                                        interestCycleMonths = 3,
                                        quarterlyInterest = quarterlyInterest,
                                        status = status,
                                        appliedDate = appliedDate,
                                        startDate = startDate,
                                        nextInterestDueDate = nextInterestDueDate,
                                        quartersPaid = quartersPaid,
                                        totalInterestPaid = totalInterestPaid,
                                        purpose = purpose,
                                        adminRemark = adminRemark
                                    )
                                    loanDao.insertLoan(loan)
                                } catch (e: Exception) {
                                    Log.w("AetSync", "Failed to deserialize loan doc: ${e.message}")
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("AetSync", "Could not start realtime listeners: ${e.message}")
        }
    }

    /**
     * Single transaction sync convenience
     */
    fun syncTransactionToCloud(txn: TransactionEntity) {
        val db = firestore ?: return
        scope.launch {
            try {
                val transactionMap = mapOf<String, Any?>(
                    "id" to txn.id,
                    "memberId" to txn.memberId,
                    "memberName" to txn.memberName,
                    "type" to txn.type,
                    "amount" to txn.amount,
                    "timestamp" to txn.timestamp,
                    "description" to txn.description,
                    "referenceNo" to txn.referenceNo,
                    "cryptoHash" to txn.cryptoHash,
                    "prevHash" to txn.prevHash,
                    "status" to txn.status
                )
                db.collection("committee_transactions")
                    .document(txn.id)
                    .set(transactionMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("AetSync", "Async upload for txn ${txn.id} failed: ${e.message}")
            }
        }
    }

    /**
     * Single loan sync convenience
     */
    fun syncLoanToCloud(loan: LoanEntity) {
        val db = firestore ?: return
        scope.launch {
            try {
                val loanMap = mapOf<String, Any?>(
                    "id" to loan.id,
                    "memberId" to loan.memberId,
                    "memberName" to loan.memberName,
                    "principalAmount" to loan.principalAmount,
                    "remainingPrincipal" to loan.remainingPrincipal,
                    "interestRateAnnual" to loan.interestRateAnnual,
                    "interestCycleMonths" to loan.interestCycleMonths,
                    "quarterlyInterest" to loan.quarterlyInterest,
                    "status" to loan.status,
                    "appliedDate" to loan.appliedDate,
                    "startDate" to loan.startDate,
                    "nextInterestDueDate" to loan.nextInterestDueDate,
                    "quartersPaid" to loan.quartersPaid,
                    "totalInterestPaid" to loan.totalInterestPaid,
                    "purpose" to loan.purpose,
                    "adminRemark" to loan.adminRemark,
                    "lastUpdated" to System.currentTimeMillis()
                )
                db.collection("committee_loans")
                    .document(loan.id)
                    .set(loanMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("AetSync", "Async upload for loan ${loan.id} failed: ${e.message}")
            }
        }
    }

    /**
     * Single member sync convenience
     */
    fun syncMemberToCloud(member: MemberEntity) {
        val db = firestore ?: return
        scope.launch {
            try {
                val memberMap = mapOf<String, Any?>(
                    "id" to member.id,
                    "name" to member.name,
                    "phone" to member.phone,
                    "address" to member.address,
                    "role" to member.role,
                    "monthlyContribution" to member.monthlyContribution,
                    "totalContributed" to member.totalContributed,
                    "isCurrentMonthPaid" to member.isCurrentMonthPaid,
                    "payoutMonthTurn" to member.payoutMonthTurn,
                    "payoutReceived" to member.payoutReceived,
                    "avatarColorHex" to member.avatarColorHex,
                    "lastUpdated" to System.currentTimeMillis()
                )
                db.collection("committee_members")
                    .document(member.id)
                    .set(memberMap, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("AetSync", "Async upload for member ${member.id} failed: ${e.message}")
            }
        }
    }

    fun deleteMemberFromCloud(memberId: String) {
        val db = firestore ?: return
        scope.launch {
            try {
                db.collection("committee_members").document(memberId).delete().await()
            } catch (e: Exception) {
                Log.w("AetSync", "Async delete for member $memberId failed: ${e.message}")
            }
        }
    }

    fun deleteLoanFromCloud(loanId: String) {
        val db = firestore ?: return
        scope.launch {
            try {
                db.collection("committee_loans").document(loanId).delete().await()
            } catch (e: Exception) {
                Log.w("AetSync", "Async delete for loan $loanId failed: ${e.message}")
            }
        }
    }

    fun cleanUp() {
        loansListener?.remove()
        transactionsListener?.remove()
    }
}
