package com.example.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.CommitteeTreasuryCard
import com.example.ui.components.CryptoHashBadge
import com.example.ui.components.MemberAvatar
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberDashboardScreen(
    member: MemberEntity?,
    loans: List<LoanEntity>,
    transactions: List<TransactionEntity>,
    treasurySummary: AetTreasurySummary,
    onPayDuesClick: () -> Unit,
    onApplyLoanClick: () -> Unit,
    onViewPassbookClick: () -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (member == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading member profile...")
        }
        return
    }

    val activeLoan = loans.firstOrNull { it.status == "ACTIVE" }
    val pendingLoan = loans.firstOrNull { it.status == "PENDING" }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val myTotalLoansTaken = loans.sumOf { it.principalAmount }
    val myTotalInterestPaid = loans.sumOf { it.totalInterestPaid }
    val myTotalFinesPaid = transactions.filter { it.type == "FINE_PENALTY" }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("member_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ALL-MEMBER TRANSPARENT AET COMMITTEE FUND CARD (Public Ledger)
        item {
            CommitteeTreasuryCard(
                summary = treasurySummary,
                isAdmin = false
            )
        }

        // 2. Personal Profile Banner with Address
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_profile_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MemberAvatar(name = member.name, colorHex = member.avatarColorHex, sizeDp = 50)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${member.id} • ${member.phone} • ${member.role}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Round Turn: ${member.payoutMonthTurn}/9",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Address: ${member.address}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Personal Financial Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Contributed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${member.totalContributed.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF10B981))
                        }
                        Column {
                            Text("Monthly Share", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${member.monthlyContribution.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text("Payout Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (member.payoutReceived) "Received (₹45K)" else "Month ${member.payoutMonthTurn} Due",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (member.payoutReceived) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }

                    if (myTotalInterestPaid > 0 || myTotalFinesPaid > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (myTotalInterestPaid > 0) {
                                Text(
                                    text = "Interest Paid: ₹${myTotalInterestPaid.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (myTotalFinesPaid > 0) {
                                Text(
                                    text = "Fines Paid: ₹${myTotalFinesPaid.toInt()}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Month Contribution Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_dues_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (member.isCurrentMonthPaid) Color(0xFF064E3B).copy(alpha = 0.15f) else Color(0xFF7C2D12).copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (member.isCurrentMonthPaid) Color(0xFF10B981) else Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (member.isCurrentMonthPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (member.isCurrentMonthPaid) "Current Month Contribution: PAID" else "Current Month Contribution: PENDING",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (member.isCurrentMonthPaid) "₹5,000 recorded in AET pool" else "Due by 5th of this month (₹5,000)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!member.isCurrentMonthPaid) {
                        Button(
                            onClick = onPayDuesClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.testTag("pay_contribution_button")
                        ) {
                            Text("Pay ₹5,000")
                        }
                    } else {
                        StatusBadge("PAID")
                    }
                }
            }
        }

        // Active Loan Summary (10% per year, 3-month cycle)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("member_loan_summary_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "My Committee Loan (10% p.a.)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (activeLoan != null) {
                            StatusBadge("ACTIVE")
                        } else if (pendingLoan != null) {
                            StatusBadge("PENDING")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeLoan != null) {
                        val daysRemaining = ((activeLoan.nextInterestDueDate - System.currentTimeMillis()) / (24L * 3600 * 1000)).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Principal Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${activeLoan.remainingPrincipal.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("3-Month Interest (10% p.a.)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${activeLoan.quarterlyInterest.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFF59E0B))
                            }
                            Column {
                                Text("Next Due In", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$daysRemaining days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (daysRemaining <= 15) Color(0xFFEF4444) else Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Next 3-month interest due on: ${dateFormat.format(Date(activeLoan.nextInterestDueDate))}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (pendingLoan != null) {
                        Text(
                            text = "Your loan application for ₹${pendingLoan.principalAmount.toInt()} is currently under review by Admin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFF59E0B)
                        )
                    } else {
                        Text(
                            text = "You currently have no active loan. Committee members can apply for up to ₹1,00,000 at 10% annual interest with quarterly payments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onApplyLoanClick,
                            modifier = Modifier.fillMaxWidth().testTag("apply_loan_button")
                        ) {
                            Text("Apply for Committee Loan (10% p.a.)")
                        }
                    }
                }
            }
        }

        // Recent Transactions & Passbook Snippet
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Passbook",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewPassbookClick() }
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Text("No recent transactions found.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(transactions.take(4).size) { index ->
                val txn = transactions[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTransaction(txn) }
                        .testTag("txn_item_${txn.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = txn.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${dateFormat.format(Date(txn.timestamp))} • Ref: ${txn.referenceNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${if (txn.type == "PAYOUT" || txn.type == "LOAN_DISBURSEMENT") "+" else "-"}₹${txn.amount.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (txn.type == "PAYOUT" || txn.type == "LOAN_DISBURSEMENT") Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CryptoHashBadge(hash = txn.cryptoHash)
                    }
                }
            }
        }
    }
}
