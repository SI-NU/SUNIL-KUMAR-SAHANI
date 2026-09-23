package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AetTreasurySummary
import com.example.data.model.LoanEntity
import com.example.data.model.MemberEntity
import com.example.data.model.TransactionEntity
import com.example.data.sync.SyncReport
import com.example.data.sync.SyncState
import com.example.ui.components.CommitteeTreasuryCard
import com.example.ui.components.StatCard

@Composable
fun AdminTreasuryScreen(
    members: List<MemberEntity>,
    loans: List<LoanEntity>,
    transactions: List<TransactionEntity>,
    treasurySummary: AetTreasurySummary = AetTreasurySummary(),
    syncReport: SyncReport = SyncReport(),
    onSyncNow: () -> Unit = {},
    onAddFineClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalContributed = members.sumOf { it.totalContributed }
    val activeLoans = loans.filter { it.status == "ACTIVE" }
    val totalActiveLoanPrincipal = activeLoans.sumOf { it.remainingPrincipal }
    val totalInterestEarned = loans.sumOf { it.totalInterestPaid }
    val totalPayoutsDisbursed = transactions.filter { it.type == "PAYOUT" }.sumOf { it.amount }
    val totalLoansDisbursed = transactions.filter { it.type == "LOAN_DISBURSEMENT" }.sumOf { it.amount }
    val totalPrincipalRepaid = transactions.filter { it.type == "LOAN_REPAYMENT" }.sumOf { it.amount }

    // Net Treasury Available
    val liquidCash = (totalContributed + totalInterestEarned + totalPrincipalRepaid) - (totalPayoutsDisbursed + totalLoansDisbursed)

    val currentMonthPaidCount = members.count { it.isCurrentMonthPaid }
    val complianceRate = if (members.isNotEmpty()) (currentMonthPaidCount.toFloat() / members.size.toFloat()) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_treasury_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Room-to-Firestore Cloud Sync Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firestore_sync_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (syncReport.state) {
                        SyncState.SYNCED -> Color(0xFF065F46).copy(alpha = 0.2f)
                        SyncState.SYNCING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        SyncState.ERROR -> Color(0xFF991B1B).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    when (syncReport.state) {
                                        SyncState.SYNCED -> Color(0xFF10B981)
                                        SyncState.SYNCING -> MaterialTheme.colorScheme.primary
                                        SyncState.ERROR -> Color(0xFFEF4444)
                                        else -> Color(0xFF6B7280)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (syncReport.state) {
                                    SyncState.SYNCED -> Icons.Default.CloudDone
                                    SyncState.SYNCING -> Icons.Default.Sync
                                    else -> Icons.Default.CloudSync
                                },
                                contentDescription = "Sync",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Room-to-Firestore Sync",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (syncReport.state) {
                                                SyncState.SYNCED -> Color(0xFF10B981)
                                                SyncState.SYNCING -> MaterialTheme.colorScheme.primary
                                                SyncState.ERROR -> Color(0xFFEF4444)
                                                else -> Color(0xFF6B7280)
                                            }
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = syncReport.state.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = syncReport.message,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSyncNow,
                        enabled = syncReport.state != SyncState.SYNCING,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("sync_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Master Fund Header & Transparent Breakdown
        item {
            CommitteeTreasuryCard(
                summary = treasurySummary,
                isAdmin = true,
                onAddFineClick = onAddFineClick
            )
        }

        // Current Month Collection Progress
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collection_progress_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Dues Collection Status",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currentMonthPaidCount of ${members.size} Paid (${(complianceRate * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (complianceRate >= 0.8f) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { complianceRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF10B981),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Collected: ₹${(currentMonthPaidCount * 5000)} / ₹${(members.size * 5000)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${members.size - currentMonthPaidCount} Pending",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (members.size - currentMonthPaidCount > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // 2x2 Grid of Financial Breakdown Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Contributions",
                    value = "₹${totalContributed.toInt()}",
                    subtitle = "${members.size} Members × ₹5K/mo",
                    icon = Icons.Default.AccountBalance,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Loans Disbursed",
                    value = "₹${totalLoansDisbursed.toInt()}",
                    subtitle = "${activeLoans.size} active loans",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Payouts Disbursed",
                    value = "₹${totalPayoutsDisbursed.toInt()}",
                    subtitle = "${transactions.count { it.type == "PAYOUT" }} monthly rounds",
                    icon = Icons.Default.TrendingUp,
                    iconTint = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Interest Collected",
                    value = "₹${totalInterestEarned.toInt()}",
                    subtitle = "10% annual rate",
                    icon = Icons.Default.PieChart,
                    iconTint = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Loan Portfolio Health & 3-Month Interest Projections
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quarterly Interest Projections (10% Annual Rate)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val upcomingQuarterlyInterest = activeLoans.sumOf { it.quarterlyInterest }
                    Text(
                        text = "Every 3 months, AET collects 2.5% on active loan principals (10% p.a.). Expected upcoming quarterly interest inflow from current portfolio: ₹${upcomingQuarterlyInterest.toInt()}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
