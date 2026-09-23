package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AetDatabase
import com.example.data.model.AetTreasurySummary
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.AetRepository
import com.example.data.sync.AetFirestoreSyncManager
import com.example.data.sync.SyncReport
import com.example.security.AetCryptoManager
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MemberTab {
    DASHBOARD,
    LOANS,
    PASSBOOK,
    COMMITTEE
}

enum class AdminTab {
    TREASURY,
    MEMBERS,
    LOANDESK,
    REPORTS,
    REMINDERS
}

class AetViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AetDatabase.getDatabase(application, viewModelScope)
    val syncManager = AetFirestoreSyncManager(
        context = application.applicationContext,
        memberDao = database.memberDao(),
        loanDao = database.loanDao(),
        transactionDao = database.transactionDao(),
        notificationDao = database.notificationDao()
    )
    val repository = AetRepository(
        memberDao = database.memberDao(),
        loanDao = database.loanDao(),
        transactionDao = database.transactionDao(),
        notificationDao = database.notificationDao(),
        syncManager = syncManager
    )

    val syncReport: StateFlow<SyncReport> = syncManager.syncReport

    init {
        // Ensure initial database population has run
        viewModelScope.launch {
            AetDatabase.populateInitialData(database)
        }
    }

    // Theme Mode
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Auth State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.ENGLISH) AppLanguage.NEPALI else AppLanguage.ENGLISH
    }

    private val _currentMemberId = MutableStateFlow<String?>("AET-02") // Default Sunil Sahani for instant viewing
    val currentMemberId: StateFlow<String?> = _currentMemberId.asStateFlow()

    // Navigation Tabs
    private val _currentMemberTab = MutableStateFlow(MemberTab.DASHBOARD)
    val currentMemberTab: StateFlow<MemberTab> = _currentMemberTab.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.TREASURY)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    // Data Streams
    val members: StateFlow<List<MemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans: StateFlow<List<LoanEntity>> = repository.allLoans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User
    val currentMember: StateFlow<MemberEntity?> = combine(members, currentMemberId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Filtered member data
    val memberLoans: StateFlow<List<LoanEntity>> = combine(loans, currentMemberId) { list, id ->
        list.filter { it.memberId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberTransactions: StateFlow<List<TransactionEntity>> = combine(transactions, currentMemberId) { list, id ->
        list.filter { it.memberId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unread notifications
    val unreadCount: StateFlow<Int> = combine(notifications, currentMemberId, isAdmin) { notifs, memId, admin ->
        if (admin) {
            notifs.count { !it.isRead }
        } else {
            notifs.count { !it.isRead && (it.recipientMemberId == null || it.recipientMemberId == memId) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Complete Committee Transparency Treasury Summary (Accessible by both Members and Admin)
    val treasurySummary: StateFlow<AetTreasurySummary> = combine(members, loans, transactions) { mList, lList, tList ->
        AetTreasurySummary.calculate(mList, lList, tList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AetTreasurySummary())

    // OTP Auth state
    private val _otpSentTarget = MutableStateFlow<String?>(null)
    val otpSentTarget: StateFlow<String?> = _otpSentTarget.asStateFlow()

    private val _generatedOtpCode = MutableStateFlow<String?>(null)
    val generatedOtpCode: StateFlow<String?> = _generatedOtpCode.asStateFlow()

    private val _otpCountdown = MutableStateFlow(0)
    val otpCountdown: StateFlow<Int> = _otpCountdown.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private var countdownJob: Job? = null

    // UI Dialogs
    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showApplyLoanDialog = MutableStateFlow(false)
    val showApplyLoanDialog: StateFlow<Boolean> = _showApplyLoanDialog.asStateFlow()

    private val _showPayDuesDialog = MutableStateFlow(false)
    val showPayDuesDialog: StateFlow<Boolean> = _showPayDuesDialog.asStateFlow()

    private val _selectedLoanForInterest = MutableStateFlow<LoanEntity?>(null)
    val selectedLoanForInterest: StateFlow<LoanEntity?> = _selectedLoanForInterest.asStateFlow()

    private val _selectedLoanForRepayment = MutableStateFlow<LoanEntity?>(null)
    val selectedLoanForRepayment: StateFlow<LoanEntity?> = _selectedLoanForRepayment.asStateFlow()

    private val _selectedMemberForPayout = MutableStateFlow<MemberEntity?>(null)
    val selectedMemberForPayout: StateFlow<MemberEntity?> = _selectedMemberForPayout.asStateFlow()

    private val _selectedTransactionReceipt = MutableStateFlow<TransactionEntity?>(null)
    val selectedTransactionReceipt: StateFlow<TransactionEntity?> = _selectedTransactionReceipt.asStateFlow()

    // Admin member management dialogs
    private val _selectedMemberForEdit = MutableStateFlow<MemberEntity?>(null)
    val selectedMemberForEdit: StateFlow<MemberEntity?> = _selectedMemberForEdit.asStateFlow()

    private val _showAddMemberDialog = MutableStateFlow(false)
    val showAddMemberDialog: StateFlow<Boolean> = _showAddMemberDialog.asStateFlow()

    private val _memberToDelete = MutableStateFlow<MemberEntity?>(null)
    val memberToDelete: StateFlow<MemberEntity?> = _memberToDelete.asStateFlow()

    // Admin fine / penalty dialog
    private val _selectedMemberForFine = MutableStateFlow<MemberEntity?>(null)
    val selectedMemberForFine: StateFlow<MemberEntity?> = _selectedMemberForFine.asStateFlow()
    private val _showAddFineDialog = MutableStateFlow(false)
    val showAddFineDialog: StateFlow<Boolean> = _showAddFineDialog.asStateFlow()

    // Admin loan edit dialog
    private val _selectedLoanForEdit = MutableStateFlow<LoanEntity?>(null)
    val selectedLoanForEdit: StateFlow<LoanEntity?> = _selectedLoanForEdit.asStateFlow()

    private val _loanToDelete = MutableStateFlow<LoanEntity?>(null)
    val loanToDelete: StateFlow<LoanEntity?> = _loanToDelete.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setMemberTab(tab: MemberTab) {
        _currentMemberTab.value = tab
    }

    fun setAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    fun openNotifications() {
        _showNotificationsDialog.value = true
    }

    fun closeNotifications() {
        _showNotificationsDialog.value = false
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(if (_isAdmin.value) null else _currentMemberId.value)
        }
    }

    fun openApplyLoanDialog() {
        _showApplyLoanDialog.value = true
    }

    fun closeApplyLoanDialog() {
        _showApplyLoanDialog.value = false
    }

    fun openPayDuesDialog() {
        _showPayDuesDialog.value = true
    }

    fun closePayDuesDialog() {
        _showPayDuesDialog.value = false
    }

    fun openPayInterestDialog(loan: LoanEntity) {
        _selectedLoanForInterest.value = loan
    }

    fun closePayInterestDialog() {
        _selectedLoanForInterest.value = null
    }

    fun openRepayLoanDialog(loan: LoanEntity) {
        _selectedLoanForRepayment.value = loan
    }

    fun closeRepayLoanDialog() {
        _selectedLoanForRepayment.value = null
    }

    fun openDisbursePayoutDialog(member: MemberEntity) {
        _selectedMemberForPayout.value = member
    }

    fun closeDisbursePayoutDialog() {
        _selectedMemberForPayout.value = null
    }

    fun showReceipt(transaction: TransactionEntity) {
        _selectedTransactionReceipt.value = transaction
    }

    fun closeReceipt() {
        _selectedTransactionReceipt.value = null
    }

    // Admin Member Edit Dialogs
    fun openEditMember(member: MemberEntity) {
        _selectedMemberForEdit.value = member
    }

    fun closeEditMember() {
        _selectedMemberForEdit.value = null
    }

    fun openAddMember() {
        _showAddMemberDialog.value = true
    }

    fun closeAddMember() {
        _showAddMemberDialog.value = false
    }

    fun confirmDeleteMember(member: MemberEntity) {
        _memberToDelete.value = member
    }

    fun cancelDeleteMember() {
        _memberToDelete.value = null
    }

    // Fine / Extra Income Dialogs
    fun openAddFine(member: MemberEntity? = null) {
        _selectedMemberForFine.value = member
        _showAddFineDialog.value = true
    }

    fun closeAddFine() {
        _selectedMemberForFine.value = null
        _showAddFineDialog.value = false
    }

    // Admin Loan Edit Dialogs
    fun openEditLoan(loan: LoanEntity) {
        _selectedLoanForEdit.value = loan
    }

    fun closeEditLoan() {
        _selectedLoanForEdit.value = null
    }

    fun confirmDeleteLoan(loan: LoanEntity) {
        _loanToDelete.value = loan
    }

    fun cancelDeleteLoan() {
        _loanToDelete.value = null
    }

    // --- Authentication Logic ---

    fun requestOtp(targetIdOrPhone: String) {
        _authError.value = null
        val code = AetCryptoManager.generateOtp(targetIdOrPhone)
        _otpSentTarget.value = targetIdOrPhone
        _generatedOtpCode.value = code
        _otpCountdown.value = 60

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 60 downTo 1) {
                _otpCountdown.value = i
                delay(1000)
            }
            _otpCountdown.value = 0
        }
    }

    fun verifyOtpAndLogin(target: String, code: String, asAdmin: Boolean) {
        if (!AetCryptoManager.verifyOtp(target, code)) {
            _authError.value = "Invalid OTP. Please enter the 6-digit code correctly."
            return
        }

        if (asAdmin) {
            _isAdmin.value = true
            _currentMemberId.value = "AET-01" // Rajesh Sharma (Admin)
            _isLoggedIn.value = true
            _authError.value = null
            _otpSentTarget.value = null
            _generatedOtpCode.value = null
            _snackbarMessage.value = "Logged in as AET Committee Admin"
        } else {
            // Match member by id or phone or default
            val member = members.value.find { it.id.equals(target, ignoreCase = true) || it.phone == target }
                ?: members.value.firstOrNull()
            _isAdmin.value = false
            _currentMemberId.value = member?.id ?: "AET-02"
            _isLoggedIn.value = true
            _authError.value = null
            _otpSentTarget.value = null
            _generatedOtpCode.value = null
            _snackbarMessage.value = "Welcome ${member?.name ?: "Member"}, logged into AET"
        }
    }

    fun directLoginMember(memberId: String) {
        _isAdmin.value = false
        _currentMemberId.value = memberId
        _isLoggedIn.value = true
        _snackbarMessage.value = "Logged in as ${members.value.find { it.id == memberId }?.name ?: memberId}"
    }

    fun directLoginAdmin() {
        _isAdmin.value = true
        _currentMemberId.value = "AET-01"
        _isLoggedIn.value = true
        _snackbarMessage.value = "Admin session verified. Welcome, Rajesh Sharma (President/Admin)"
    }

    fun logout() {
        _isLoggedIn.value = false
        _isAdmin.value = false
        _otpSentTarget.value = null
        _generatedOtpCode.value = null
    }

    // --- Financial Actions ---

    fun payMonthlyContribution(paymentRef: String) {
        val memberId = _currentMemberId.value ?: return
        recordMemberContribution(memberId, paymentRef)
    }

    fun recordMemberContribution(memberId: String, paymentRef: String) {
        viewModelScope.launch {
            val result = repository.payMonthlyContribution(memberId, paymentRef)
            result.onSuccess { txnId ->
                _snackbarMessage.value = "Contribution of ₹5,000 recorded for $memberId! Txn: $txnId"
                closePayDuesDialog()
            }.onFailure {
                _snackbarMessage.value = "Payment failed: ${it.message}"
            }
        }
    }

    fun applyLoan(amount: Double, purpose: String) {
        val memberId = _currentMemberId.value ?: return
        viewModelScope.launch {
            val result = repository.applyForLoan(memberId, amount, purpose)
            result.onSuccess { loanId ->
                _snackbarMessage.value = "Loan application $loanId submitted! 10% p.a. (Quarterly Interest: ₹${(amount * 0.025).toInt()})"
                closeApplyLoanDialog()
            }.onFailure {
                _snackbarMessage.value = "Application failed: ${it.message}"
            }
        }
    }

    fun approveLoan(loanId: String, remark: String) {
        viewModelScope.launch {
            val result = repository.approveLoan(loanId, remark)
            result.onSuccess {
                _snackbarMessage.value = "Loan $loanId approved and funds disbursed!"
            }.onFailure {
                _snackbarMessage.value = "Approval failed: ${it.message}"
            }
        }
    }

    fun rejectLoan(loanId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.rejectLoan(loanId, reason)
            result.onSuccess {
                _snackbarMessage.value = "Loan $loanId rejected."
            }.onFailure {
                _snackbarMessage.value = "Action failed: ${it.message}"
            }
        }
    }

    fun payQuarterlyInterest(loanId: String, paymentRef: String) {
        viewModelScope.launch {
            val result = repository.payQuarterlyInterest(loanId, paymentRef)
            result.onSuccess {
                _snackbarMessage.value = "Quarterly interest payment recorded! Next cycle extended by 3 months."
                closePayInterestDialog()
            }.onFailure {
                _snackbarMessage.value = "Payment failed: ${it.message}"
            }
        }
    }

    fun repayLoanPrincipal(loanId: String, amount: Double, paymentRef: String) {
        viewModelScope.launch {
            val result = repository.repayLoanPrincipal(loanId, amount, paymentRef)
            result.onSuccess {
                _snackbarMessage.value = "Repayment of ₹${amount.toInt()} recorded!"
                closeRepayLoanDialog()
            }.onFailure {
                _snackbarMessage.value = "Repayment failed: ${it.message}"
            }
        }
    }

    fun disbursePayout(memberId: String, amount: Double, paymentRef: String) {
        viewModelScope.launch {
            val result = repository.disbursePayout(memberId, amount, paymentRef)
            result.onSuccess {
                _snackbarMessage.value = "Payout of ₹${amount.toInt()} disbursed successfully!"
                closeDisbursePayoutDialog()
            }.onFailure {
                _snackbarMessage.value = "Payout failed: ${it.message}"
            }
        }
    }

    fun runAutomatedReminders() {
        viewModelScope.launch {
            repository.runAutomatedRemindersScan()
            _snackbarMessage.value = "Automated reminder scan complete! Dues and 3-month interest alerts sent."
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val result = repository.syncAllToCloud()
            result.onSuccess { report ->
                _snackbarMessage.value = "Cloud Sync Successful: ${report.syncedTransactionsCount} txns, ${report.syncedLoansCount} loans"
            }.onFailure { e ->
                _snackbarMessage.value = "Sync Notice: ${e.message ?: "Local cache preserved"}"
            }
        }
    }

    // --- ADMIN MEMBER ACTIONS ---

    fun saveMember(member: MemberEntity) {
        viewModelScope.launch {
            val result = repository.updateMember(member)
            result.onSuccess {
                _snackbarMessage.value = "Member ${member.name} (${member.id}) updated successfully!"
                closeEditMember()
            }.onFailure {
                _snackbarMessage.value = "Failed to update member: ${it.message}"
            }
        }
    }

    fun addNewMember(member: MemberEntity) {
        viewModelScope.launch {
            val result = repository.addMember(member)
            result.onSuccess {
                _snackbarMessage.value = "Member ${member.name} added to committee!"
                closeAddMember()
            }.onFailure {
                _snackbarMessage.value = "Failed to add member: ${it.message}"
            }
        }
    }

    fun executeDeleteMember() {
        val member = _memberToDelete.value ?: return
        viewModelScope.launch {
            val result = repository.deleteMember(member.id)
            result.onSuccess {
                _snackbarMessage.value = "Member ${member.name} (${member.id}) removed from committee."
                cancelDeleteMember()
            }.onFailure {
                _snackbarMessage.value = "Failed to remove member: ${it.message}"
            }
        }
    }

    fun toggleMonthlyPaidStatus(memberId: String, isPaid: Boolean) {
        viewModelScope.launch {
            val result = repository.updateMonthlyPaidStatus(memberId, isPaid)
            result.onSuccess {
                val statusText = if (isPaid) "PAID" else "PENDING"
                _snackbarMessage.value = "Member $memberId monthly contribution set to $statusText."
            }.onFailure {
                _snackbarMessage.value = "Failed to update status: ${it.message}"
            }
        }
    }

    // --- ADMIN FINE & EXTRA INCOME ACTIONS ---

    fun submitFineOrExtra(
        memberId: String?,
        amount: Double,
        reason: String,
        category: String,
        paymentRef: String
    ) {
        viewModelScope.launch {
            val result = repository.addFineOrExtraIncome(memberId, amount, reason, category, paymentRef)
            result.onSuccess { txnId ->
                _snackbarMessage.value = "₹${amount.toInt()} $category recorded! Txn: $txnId"
                closeAddFine()
            }.onFailure {
                _snackbarMessage.value = "Failed to record fee: ${it.message}"
            }
        }
    }

    // --- ADMIN LOAN EDITING & DELETION ---

    fun saveLoan(loan: LoanEntity) {
        viewModelScope.launch {
            val result = repository.updateLoan(loan)
            result.onSuccess {
                _snackbarMessage.value = "Loan #${loan.id} updated successfully!"
                closeEditLoan()
            }.onFailure {
                _snackbarMessage.value = "Failed to update loan: ${it.message}"
            }
        }
    }

    fun executeDeleteLoan() {
        val loan = _loanToDelete.value ?: return
        viewModelScope.launch {
            val result = repository.deleteLoan(loan.id)
            result.onSuccess {
                _snackbarMessage.value = "Loan #${loan.id} deleted."
                cancelDeleteLoan()
            }.onFailure {
                _snackbarMessage.value = "Failed to delete loan: ${it.message}"
            }
        }
    }

    fun deleteTransaction(txnId: String) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(txnId)
            result.onSuccess {
                _snackbarMessage.value = "Transaction $txnId deleted."
            }.onFailure {
                _snackbarMessage.value = "Failed to delete transaction: ${it.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.cleanUp()
    }
}
