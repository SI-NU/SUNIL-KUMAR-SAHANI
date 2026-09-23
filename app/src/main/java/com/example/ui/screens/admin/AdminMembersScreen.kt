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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.MemberEntity
import com.example.ui.components.MemberAvatar
import com.example.ui.components.StatusBadge

@Composable
fun AdminMembersScreen(
    members: List<MemberEntity>,
    onRecordContribution: (MemberEntity) -> Unit,
    onDisbursePayout: (MemberEntity) -> Unit,
    onEditMember: (MemberEntity) -> Unit,
    onDeleteMember: (MemberEntity) -> Unit,
    onAddMember: () -> Unit,
    onAddFine: (MemberEntity) -> Unit,
    onToggleStatus: (MemberEntity, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_members_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with "Add New Member" button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Committee Member Roster",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Admin Full Access • Total ${members.size} Members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddMember,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("admin_add_member_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Member", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // List of Members
        items(members.size) { index ->
            val member = members[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_member_card_${member.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Avatar, Name, Role, and Edit/Delete Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            MemberAvatar(name = member.name, colorHex = member.avatarColorHex, sizeDp = 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = member.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (member.role == "ADMIN") Color(0xFFDC2626).copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = member.role,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (member.role == "ADMIN") Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "${member.id} • ${member.phone} • Turn: Month ${member.payoutMonthTurn}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Quick Edit & Delete icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditMember(member) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("edit_member_btn_${member.id}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Member",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteMember(member) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("delete_member_btn_${member.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Member",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address Row (Directly satisfies user prompt: "user address remove user ka sab kuch change aaur update karne ka access admin ko do")
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(if (member.isCurrentMonthPaid) "PAID" else "DUE")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Financial metrics row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Contributed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${member.totalContributed.toInt()}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF10B981))
                        }

                        Column {
                            Text("Current Month Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (member.isCurrentMonthPaid) "₹0 (Cleared)" else "₹${member.monthlyContribution.toInt()} (Pending)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (member.isCurrentMonthPaid) MaterialTheme.colorScheme.onSurface else Color(0xFFEF4444)
                            )
                        }

                        Column {
                            Text("Payout Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (member.payoutReceived) "Paid (₹45,000)" else "Month ${member.payoutMonthTurn} Slated",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (member.payoutReceived) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle payment status
                        if (!member.isCurrentMonthPaid) {
                            Button(
                                onClick = { onRecordContribution(member) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("record_paid_${member.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record ₹${member.monthlyContribution.toInt()} Paid", fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onToggleStatus(member, false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_pending_${member.id}")
                            ) {
                                Text("Set as Pending", fontSize = 11.sp)
                            }
                        }

                        // Add Fine / Penalty Button
                        OutlinedButton(
                            onClick = { onAddFine(member) },
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("add_fine_member_${member.id}")
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Fine", fontSize = 11.sp, color = Color(0xFF8B5CF6))
                        }

                        // Disburse Payout (if turn eligible)
                        if (!member.payoutReceived) {
                            Button(
                                onClick = { onDisbursePayout(member) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("disburse_payout_${member.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Payout ₹45K", fontSize = 11.sp)
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
