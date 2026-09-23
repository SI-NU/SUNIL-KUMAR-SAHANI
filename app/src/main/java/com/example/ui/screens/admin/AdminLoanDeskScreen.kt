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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminLoanDeskScreen(
    loans: List<LoanEntity>,
    onApproveLoan: (LoanEntity) -> Unit,
    onRejectLoan: (LoanEntity) -> Unit,
    onPayInterestClick: (LoanEntity) -> Unit,
    onRepayPrincipalClick: (LoanEntity) -> Unit,
    onEditLoan: (LoanEntity) -> Unit,
    onDeleteLoan: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val filteredLoans = remember(loans, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> loans.filter { it.status == "PENDING" }
            "ACTIVE" -> loans.filter { it.status == "ACTIVE" }
            "PAID_OFF" -> loans.filter { it.status == "PAID_OFF" }
            else -> loans
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_loan_desk_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Loan Portfolio & Approvals Desk",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Interest: 10% per year • 3-Month Cycle • Admin Full Edit & Interest Control",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Filter chips
        item {
            val filters = listOf(
                "ALL" to "All (${loans.size})",
                "PENDING" to "Pending (${loans.count { it.status == "PENDING" }})",
                "ACTIVE" to "Active (${loans.count { it.status == "ACTIVE" }})",
                "PAID_OFF" to "Closed (${loans.count { it.status == "PAID_OFF" }})"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_loan_$key")
                    )
                }
            }
        }

        if (filteredLoans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No loans found in '$selectedFilter' category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(filteredLoans.size) { index ->
            val loan = filteredLoans[index]
            val daysRemaining = ((loan.nextInterestDueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_loan_card_${loan.id}"),
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = loan.memberName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(loan.status)
                            }
                            Text(
                                text = "Loan ID: #${loan.id} • Member: ${loan.memberId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit & Delete Loan Icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditLoan(loan) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("edit_loan_btn_${loan.id}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Loan Terms",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteLoan(loan) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("delete_loan_btn_${loan.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Loan",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Principal Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${loan.remainingPrincipal.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Column {
                            Text("Annual Rate (p.a.)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${loan.interestRateAnnual}%", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF10B981))
                        }

                        Column {
                            Text("Quarterly Interest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${loan.quarterlyInterest.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFD97706))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Purpose: ${loan.purpose}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total Interest Paid: ₹${loan.totalInterestPaid.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }

                    if (loan.adminRemark.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Admin Note: ${loan.adminRemark}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Status specific actions
                    if (loan.status == "PENDING") {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onApproveLoan(loan) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("approve_loan_${loan.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve & Disburse", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onRejectLoan(loan) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reject_loan_${loan.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFDC2626))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reject", fontSize = 12.sp, color = Color(0xFFDC2626))
                            }
                        }
                    } else if (loan.status == "ACTIVE") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (daysRemaining <= 15) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (daysRemaining <= 15) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3-Month Interest: ₹${loan.quarterlyInterest.toInt()} due on ${dateFormat.format(Date(loan.nextInterestDueDate))} ($daysRemaining days)",
                                fontSize = 12.sp,
                                color = if (daysRemaining <= 15) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onPayInterestClick(loan) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_pay_interest_${loan.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Text("Record Interest ₹${loan.quarterlyInterest.toInt()}", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onRepayPrincipalClick(loan) },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("admin_repay_principal_${loan.id}")
                            ) {
                                Text("Principal Pay", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onEditLoan(loan) },
                                modifier = Modifier
                                    .weight(0.7f)
                                    .testTag("admin_quick_edit_loan_${loan.id}")
                            ) {
                                Text("Edit", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
