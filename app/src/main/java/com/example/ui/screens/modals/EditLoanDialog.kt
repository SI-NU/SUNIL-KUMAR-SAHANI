package com.example.ui.screens.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LoanEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLoanDialog(
    loan: LoanEntity,
    onDismiss: () -> Unit,
    onSave: (LoanEntity) -> Unit
) {
    var remainingPrincipalStr by remember { mutableStateOf(loan.remainingPrincipal.toInt().toString()) }
    var interestRateStr by remember { mutableStateOf(loan.interestRateAnnual.toString()) }
    var quartersPaidStr by remember { mutableStateOf(loan.quartersPaid.toString()) }
    var totalInterestPaidStr by remember { mutableStateOf(loan.totalInterestPaid.toInt().toString()) }
    var selectedStatus by remember { mutableStateOf(loan.status) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var adminRemark by remember { mutableStateOf(loan.adminRemark) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("edit_loan_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFD97706))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Loan & Interest Terms",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Loan #${loan.id} • ${loan.memberName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Loan Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loan Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        listOf("ACTIVE", "PENDING", "PAID_OFF", "REJECTED").forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    selectedStatus = st
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remaining Principal
                OutlinedTextField(
                    value = remainingPrincipalStr,
                    onValueChange = { remainingPrincipalStr = it },
                    label = { Text("Remaining Principal Balance (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_loan_principal_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Annual Interest Rate % & Quarters Paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text("Annual Rate (%)") },
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = quartersPaidStr,
                        onValueChange = { quartersPaidStr = it },
                        label = { Text("Quarters Paid (3M)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Interest Paid to Date
                OutlinedTextField(
                    value = totalInterestPaidStr,
                    onValueChange = { totalInterestPaidStr = it },
                    label = { Text("Total Interest Paid to AET Account (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_loan_interest_paid_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Admin Remark
                OutlinedTextField(
                    value = adminRemark,
                    onValueChange = { adminRemark = it },
                    label = { Text("Admin Remark / Settlement Note") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_loan_remark_input"),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val principal = remainingPrincipalStr.toDoubleOrNull()
                            val rate = interestRateStr.toDoubleOrNull()
                            val quarters = quartersPaidStr.toIntOrNull()
                            val interestPaid = totalInterestPaidStr.toDoubleOrNull()

                            if (principal == null || principal < 0) {
                                errorMessage = "Enter valid remaining principal"
                                return@Button
                            }
                            if (rate == null || rate < 0) {
                                errorMessage = "Enter valid interest rate"
                                return@Button
                            }
                            if (quarters == null || quarters < 0) {
                                errorMessage = "Enter valid quarters count"
                                return@Button
                            }
                            if (interestPaid == null || interestPaid < 0) {
                                errorMessage = "Enter valid total interest paid"
                                return@Button
                            }

                            val quarterlyInterest = principal * (rate / 100.0) * (3.0 / 12.0)
                            val updated = loan.copy(
                                remainingPrincipal = principal,
                                interestRateAnnual = rate,
                                quarterlyInterest = quarterlyInterest,
                                quartersPaid = quarters,
                                totalInterestPaid = interestPaid,
                                status = selectedStatus,
                                adminRemark = adminRemark.trim()
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_loan_btn")
                    ) {
                        Text("Save Loan")
                    }
                }
            }
        }
    }
}
