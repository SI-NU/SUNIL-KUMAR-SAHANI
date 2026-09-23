package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AdminBottomBar
import com.example.ui.components.AetTopBar
import com.example.ui.components.MemberBottomBar
import com.example.ui.screens.admin.AdminLoanDeskScreen
import com.example.ui.screens.admin.AdminMembersScreen
import com.example.ui.screens.admin.AdminRemindersScreen
import com.example.ui.screens.admin.AdminReportsScreen
import com.example.ui.screens.admin.AdminTreasuryScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.member.MemberCommitteeScreen
import com.example.ui.screens.member.MemberDashboardScreen
import com.example.ui.screens.member.MemberLoansScreen
import com.example.ui.screens.member.MemberPassbookScreen
import com.example.ui.screens.modals.AddFineExtraIncomeDialog
import com.example.ui.screens.modals.AddMemberDialog
import com.example.ui.screens.modals.ApplyLoanDialog
import com.example.ui.screens.modals.ConfirmDeleteDialog
import com.example.ui.screens.modals.DisbursePayoutDialog
import com.example.ui.screens.modals.EditLoanDialog
import com.example.ui.screens.modals.EditMemberDialog
import com.example.ui.screens.modals.PayDuesDialog
import com.example.ui.screens.modals.PayInterestDialog
import com.example.ui.screens.modals.RepayPrincipalDialog
import com.example.ui.screens.modals.TransactionReceiptDialog
import com.example.ui.screens.notifications.NotificationsDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AetViewModel
import com.example.ui.viewmodel.MemberTab

class MainActivity : ComponentActivity() {

    private val viewModel: AetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AetMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AetMainApp(viewModel: AetViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentMember by viewModel.currentMember.collectAsStateWithLifecycle()
    val currentMemberId by viewModel.currentMemberId.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    val members by viewModel.members.collectAsStateWithLifecycle()
    val allLoans by viewModel.loans.collectAsStateWithLifecycle()
    val memberLoans by viewModel.memberLoans.collectAsStateWithLifecycle()
    val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()
    val memberTransactions by viewModel.memberTransactions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val currentMemberTab by viewModel.currentMemberTab.collectAsStateWithLifecycle()
    val currentAdminTab by viewModel.currentAdminTab.collectAsStateWithLifecycle()

    // OTP Auth state
    val otpSentTarget by viewModel.otpSentTarget.collectAsStateWithLifecycle()
    val generatedOtpCode by viewModel.generatedOtpCode.collectAsStateWithLifecycle()
    val otpCountdown by viewModel.otpCountdown.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    // Dialogs state
    val showNotifications by viewModel.showNotificationsDialog.collectAsStateWithLifecycle()
    val showApplyLoan by viewModel.showApplyLoanDialog.collectAsStateWithLifecycle()
    val showPayDues by viewModel.showPayDuesDialog.collectAsStateWithLifecycle()
    val selectedLoanForInterest by viewModel.selectedLoanForInterest.collectAsStateWithLifecycle()
    val selectedLoanForRepayment by viewModel.selectedLoanForRepayment.collectAsStateWithLifecycle()
    val selectedMemberForPayout by viewModel.selectedMemberForPayout.collectAsStateWithLifecycle()
    val selectedReceipt by viewModel.selectedTransactionReceipt.collectAsStateWithLifecycle()
    val syncReport by viewModel.syncReport.collectAsStateWithLifecycle()
    val treasurySummary by viewModel.treasurySummary.collectAsStateWithLifecycle()

    // New Admin Management Dialog States
    val selectedMemberForEdit by viewModel.selectedMemberForEdit.collectAsStateWithLifecycle()
    val showAddMemberDialog by viewModel.showAddMemberDialog.collectAsStateWithLifecycle()
    val memberToDelete by viewModel.memberToDelete.collectAsStateWithLifecycle()
    val showAddFineDialog by viewModel.showAddFineDialog.collectAsStateWithLifecycle()
    val selectedMemberForFine by viewModel.selectedMemberForFine.collectAsStateWithLifecycle()
    val selectedLoanForEdit by viewModel.selectedLoanForEdit.collectAsStateWithLifecycle()
    val loanToDelete by viewModel.loanToDelete.collectAsStateWithLifecycle()

    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    if (!isLoggedIn) {
        AuthScreen(
            members = members,
            otpSentTarget = otpSentTarget,
            generatedOtpCode = generatedOtpCode,
            otpCountdown = otpCountdown,
            authError = authError,
            onRequestOtp = { viewModel.requestOtp(it) },
            onVerifyOtp = { target, code, asAdmin -> viewModel.verifyOtpAndLogin(target, code, asAdmin) },
            onDirectLoginMember = { viewModel.directLoginMember(it) },
            onDirectLoginAdmin = { viewModel.directLoginAdmin() }
        )
    } else {
        Scaffold(
            topBar = {
                AetTopBar(
                    isDarkMode = isDarkMode,
                    isAdmin = isAdmin,
                    currentMember = currentMember,
                    unreadCount = unreadCount,
                    syncReport = syncReport,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onOpenNotifications = { viewModel.openNotifications() },
                    onSyncNow = { viewModel.syncNow() },
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                if (isAdmin) {
                    AdminBottomBar(
                        selectedTab = currentAdminTab,
                        onTabSelected = { viewModel.setAdminTab(it) }
                    )
                } else {
                    MemberBottomBar(
                        selectedTab = currentMemberTab,
                        onTabSelected = { viewModel.setMemberTab(it) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isAdmin) {
                    when (currentAdminTab) {
                        AdminTab.TREASURY -> AdminTreasuryScreen(
                            members = members,
                            loans = allLoans,
                            transactions = allTransactions,
                            treasurySummary = treasurySummary,
                            syncReport = syncReport,
                            onSyncNow = { viewModel.syncNow() },
                            onAddFineClick = { viewModel.openAddFine() }
                        )
                        AdminTab.MEMBERS -> AdminMembersScreen(
                            members = members,
                            onRecordContribution = { viewModel.recordMemberContribution(it.id, "ADMIN/RECORD/CASH") },
                            onDisbursePayout = { viewModel.openDisbursePayoutDialog(it) },
                            onEditMember = { viewModel.openEditMember(it) },
                            onDeleteMember = { viewModel.confirmDeleteMember(it) },
                            onAddMember = { viewModel.openAddMember() },
                            onAddFine = { viewModel.openAddFine(it) },
                            onToggleStatus = { member, paid -> viewModel.toggleMonthlyPaidStatus(member.id, paid) }
                        )
                        AdminTab.LOANDESK -> AdminLoanDeskScreen(
                            loans = allLoans,
                            onApproveLoan = { viewModel.approveLoan(it.id, "Approved by Admin Committee") },
                            onRejectLoan = { viewModel.rejectLoan(it.id, "Application declined by Committee") },
                            onPayInterestClick = { viewModel.openPayInterestDialog(it) },
                            onRepayPrincipalClick = { viewModel.openRepayLoanDialog(it) },
                            onEditLoan = { viewModel.openEditLoan(it) },
                            onDeleteLoan = { viewModel.confirmDeleteLoan(it) }
                        )
                        AdminTab.REPORTS -> AdminReportsScreen(
                            members = members,
                            loans = allLoans,
                            transactions = allTransactions,
                            repository = viewModel.repository
                        )
                        AdminTab.REMINDERS -> AdminRemindersScreen(
                            members = members,
                            loans = allLoans,
                            notifications = notifications,
                            onTriggerScan = { viewModel.runAutomatedReminders() }
                        )
                    }
                } else {
                    when (currentMemberTab) {
                        MemberTab.DASHBOARD -> MemberDashboardScreen(
                            member = currentMember,
                            loans = memberLoans,
                            transactions = memberTransactions,
                            treasurySummary = treasurySummary,
                            onPayDuesClick = { viewModel.openPayDuesDialog() },
                            onApplyLoanClick = { viewModel.openApplyLoanDialog() },
                            onViewPassbookClick = { viewModel.setMemberTab(MemberTab.PASSBOOK) },
                            onSelectTransaction = { viewModel.showReceipt(it) }
                        )
                        MemberTab.LOANS -> MemberLoansScreen(
                            loans = memberLoans,
                            onApplyLoanClick = { viewModel.openApplyLoanDialog() },
                            onPayInterestClick = { viewModel.openPayInterestDialog(it) },
                            onRepayPrincipalClick = { viewModel.openRepayLoanDialog(it) }
                        )
                        MemberTab.PASSBOOK -> MemberPassbookScreen(
                            transactions = memberTransactions,
                            onSelectTransaction = { viewModel.showReceipt(it) }
                        )
                        MemberTab.COMMITTEE -> MemberCommitteeScreen(
                            members = members,
                            currentMemberId = currentMemberId
                        )
                    }
                }
            }
        }
    }

    // Modals & Dialogs
    if (showNotifications) {
        NotificationsDialog(
            notifications = notifications,
            onDismiss = { viewModel.closeNotifications() },
            onMarkAllRead = { viewModel.markNotificationsRead() }
        )
    }

    if (showApplyLoan) {
        ApplyLoanDialog(
            onDismiss = { viewModel.closeApplyLoanDialog() },
            onSubmit = { amount, purpose -> viewModel.applyLoan(amount, purpose) }
        )
    }

    if (showPayDues) {
        PayDuesDialog(
            onDismiss = { viewModel.closePayDuesDialog() },
            onSubmit = { ref -> viewModel.payMonthlyContribution(ref) }
        )
    }

    selectedLoanForInterest?.let { loan ->
        PayInterestDialog(
            loan = loan,
            onDismiss = { viewModel.closePayInterestDialog() },
            onSubmit = { ref -> viewModel.payQuarterlyInterest(loan.id, ref) }
        )
    }

    selectedLoanForRepayment?.let { loan ->
        RepayPrincipalDialog(
            loan = loan,
            onDismiss = { viewModel.closeRepayLoanDialog() },
            onSubmit = { amount, ref -> viewModel.repayLoanPrincipal(loan.id, amount, ref) }
        )
    }

    selectedMemberForPayout?.let { member ->
        DisbursePayoutDialog(
            member = member,
            onDismiss = { viewModel.closeDisbursePayoutDialog() },
            onSubmit = { amount, ref -> viewModel.disbursePayout(member.id, amount, ref) }
        )
    }

    selectedReceipt?.let { txn ->
        TransactionReceiptDialog(
            transaction = txn,
            onDismiss = { viewModel.closeReceipt() }
        )
    }

    // Admin Member Management Dialogs
    selectedMemberForEdit?.let { member ->
        EditMemberDialog(
            member = member,
            onDismiss = { viewModel.closeEditMember() },
            onSave = { updated -> viewModel.saveMember(updated) }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            existingMembersCount = members.size,
            onDismiss = { viewModel.closeAddMember() },
            onAdd = { newMember -> viewModel.addNewMember(newMember) }
        )
    }

    memberToDelete?.let { member ->
        ConfirmDeleteDialog(
            title = "Remove Committee Member",
            message = "Are you sure you want to remove ${member.name} (${member.id}) from AET Committee? This will remove their record from local Room DB and Cloud Firestore.",
            confirmButtonText = "Delete Member",
            onDismiss = { viewModel.cancelDeleteMember() },
            onConfirm = { viewModel.executeDeleteMember() }
        )
    }

    // Fine & Extra Income Dialog
    if (showAddFineDialog) {
        AddFineExtraIncomeDialog(
            members = members,
            preSelectedMember = selectedMemberForFine,
            onDismiss = { viewModel.closeAddFine() },
            onSubmit = { memberId, amount, reason, category, paymentRef ->
                viewModel.submitFineOrExtra(memberId, amount, reason, category, paymentRef)
            }
        )
    }

    // Admin Loan Management Dialogs
    selectedLoanForEdit?.let { loan ->
        EditLoanDialog(
            loan = loan,
            onDismiss = { viewModel.closeEditLoan() },
            onSave = { updated -> viewModel.saveLoan(updated) }
        )
    }

    loanToDelete?.let { loan ->
        ConfirmDeleteDialog(
            title = "Remove Loan Record",
            message = "Are you sure you want to remove Loan #${loan.id} for ${loan.memberName}? This will delete the loan record from Room and Firestore.",
            confirmButtonText = "Delete Loan",
            onDismiss = { viewModel.cancelDeleteLoan() },
            onConfirm = { viewModel.executeDeleteLoan() }
        )
    }
}

// Keep Greeting for backwards compatibility with tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
