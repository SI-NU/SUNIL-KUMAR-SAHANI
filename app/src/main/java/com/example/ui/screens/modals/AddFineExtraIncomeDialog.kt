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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.example.data.model.MemberEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFineExtraIncomeDialog(
    members: List<MemberEntity>,
    preSelectedMember: MemberEntity?,
    onDismiss: () -> Unit,
    onSubmit: (memberId: String?, amount: Double, reason: String, category: String, paymentRef: String) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(preSelectedMember?.id) }
    var amountStr by remember { mutableStateOf("200") }
    var reason by remember { mutableStateOf("Late payment penalty for dues") }
    var paymentRef by remember { mutableStateOf("CASH/FINE/${System.currentTimeMillis() % 10000}") }
    var selectedCategory by remember { mutableStateOf("LATE_CONTRIBUTION_FINE") }

    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categoryList = listOf(
        "LATE_CONTRIBUTION_FINE" to "Late Monthly Contribution Fine (विलंब शुल्क)",
        "MEETING_ABSENCE_PENALTY" to "Meeting Absence Penalty (बैठक जुर्माना)",
        "INTEREST_DELAY_PENALTY" to "Interest Delay Penalty (ब्याज विलंब जुर्माना)",
        "ENTRY_MEMBERSHIP_FEE" to "New Member Entry Fee (प्रवेश शुल्क)",
        "MISC_EXTRA_INCOME" to "Miscellaneous Extra Income (विविध आमदनी)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("add_fine_extra_dialog"),
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
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF8B5CF6))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Record Fine / Extra Income",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Credits directly into AET Committee Treasury",
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

                // Target Member Dropdown
                val selectedMemberName = members.find { it.id == selectedMemberId }?.let { "${it.name} (${it.id})" }
                    ?: "General Committee Fund (No specific member)"

                ExposedDropdownMenuBox(
                    expanded = memberDropdownExpanded,
                    onExpandedChange = { memberDropdownExpanded = !memberDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMemberName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Impose On / Collected From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = memberDropdownExpanded,
                        onDismissRequest = { memberDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General Committee Fund (सामान्य खाता)") },
                            onClick = {
                                selectedMemberId = null
                                memberDropdownExpanded = false
                            }
                        )
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.name} (${m.id})") },
                                onClick = {
                                    selectedMemberId = m.id
                                    memberDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    val catLabel = categoryList.find { it.first == selectedCategory }?.second ?: selectedCategory
                    OutlinedTextField(
                        value = catLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fine / Fee Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categoryList.forEach { (catKey, catDesc) ->
                            DropdownMenuItem(
                                text = { Text(catDesc) },
                                onClick = {
                                    selectedCategory = catKey
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preset amount chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 200, 500, 1000).forEach { preset ->
                        FilterChip(
                            selected = amountStr == preset.toString(),
                            onClick = { amountStr = preset.toString() },
                            label = { Text("₹$preset", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Fine / Extra Amount (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fine_amount_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reason / Description
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Note (कारण)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fine_reason_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment / Receipt Reference
                OutlinedTextField(
                    value = paymentRef,
                    onValueChange = { paymentRef = it },
                    label = { Text("Reference / Receipt No") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fine_ref_input"),
                    singleLine = true
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
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMessage = "Please enter valid fine amount"
                                return@Button
                            }
                            if (reason.isBlank()) {
                                errorMessage = "Please provide a reason"
                                return@Button
                            }

                            onSubmit(
                                selectedMemberId,
                                amt,
                                reason.trim(),
                                selectedCategory,
                                paymentRef.trim()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_fine_btn")
                    ) {
                        Text("Record & Deposit")
                    }
                }
            }
        }
    }
}
