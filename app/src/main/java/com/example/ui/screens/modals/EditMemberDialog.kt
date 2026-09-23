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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Surface
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
fun EditMemberDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (MemberEntity) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var phone by remember { mutableStateOf(member.phone) }
    var address by remember { mutableStateOf(member.address) }
    var monthlyContributionStr by remember { mutableStateOf(member.monthlyContribution.toInt().toString()) }
    var payoutMonthTurnStr by remember { mutableStateOf(member.payoutMonthTurn.toString()) }
    var totalContributedStr by remember { mutableStateOf(member.totalContributed.toInt().toString()) }
    var payoutReceived by remember { mutableStateOf(member.payoutReceived) }
    var isCurrentMonthPaid by remember { mutableStateOf(member.isCurrentMonthPaid) }
    var selectedRole by remember { mutableStateOf(member.role) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("edit_member_dialog"),
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Member Profile",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Member ID: ${member.id}",
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

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_phone_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Address (Requested by User)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Member Address (निवास का पता)") },
                    placeholder = { Text("e.g. Connaught Place, New Delhi") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_address_input"),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Committee Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        listOf("MEMBER", "ADMIN", "TREASURER").forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    selectedRole = r
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Monthly Contribution & Payout Month
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = monthlyContributionStr,
                        onValueChange = { monthlyContributionStr = it },
                        label = { Text("Monthly Dues (₹)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_member_dues_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = payoutMonthTurnStr,
                        onValueChange = { payoutMonthTurnStr = it },
                        label = { Text("Payout Turn (1-9)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_member_turn_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Contributed (Adjustable by Admin)
                OutlinedTextField(
                    value = totalContributedStr,
                    onValueChange = { totalContributedStr = it },
                    label = { Text("Total Contributed Till Date (₹)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_member_total_contributed_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Checkboxes for Status Flags
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCurrentMonthPaid,
                                onCheckedChange = { isCurrentMonthPaid = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Month Contribution Paid (इस महीने जमा)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = payoutReceived,
                                onCheckedChange = { payoutReceived = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Round Payout Received (₹45,000 प्राप्त हुआ)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save & Cancel Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val dues = monthlyContributionStr.toDoubleOrNull()
                            val turn = payoutMonthTurnStr.toIntOrNull()
                            val total = totalContributedStr.toDoubleOrNull()

                            if (name.isBlank()) {
                                errorMessage = "Name cannot be blank"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Phone cannot be blank"
                                return@Button
                            }
                            if (address.isBlank()) {
                                errorMessage = "Address cannot be blank"
                                return@Button
                            }
                            if (dues == null || dues <= 0) {
                                errorMessage = "Enter valid monthly dues"
                                return@Button
                            }
                            if (turn == null || turn < 1) {
                                errorMessage = "Turn must be 1 or greater"
                                return@Button
                            }
                            if (total == null || total < 0) {
                                errorMessage = "Enter valid total contributed"
                                return@Button
                            }

                            val updated = member.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                role = selectedRole,
                                monthlyContribution = dues,
                                payoutMonthTurn = turn,
                                totalContributed = total,
                                payoutReceived = payoutReceived,
                                isCurrentMonthPaid = isCurrentMonthPaid,
                                payoutDate = if (payoutReceived && member.payoutDate == null) System.currentTimeMillis() else member.payoutDate
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_member_btn")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
