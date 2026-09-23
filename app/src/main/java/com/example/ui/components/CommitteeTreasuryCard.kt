package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AetTreasurySummary
import com.example.ui.localization.AppLanguage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CommitteeTreasuryCard(
    summary: AetTreasurySummary,
    isAdmin: Boolean = false,
    language: AppLanguage = AppLanguage.ENGLISH,
    onAddFineClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val isEnglish = language == AppLanguage.ENGLISH

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("committee_treasury_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ADVANCE EDUCATIONAL TEAM -AET since 2082",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEnglish) "Public Treasury Ledger • Transparent Overview" else "सार्वजनिक कोष खाता • पारदर्शी वित्तीय विवरण",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isAdmin && onAddFineClick != null) {
                    OutlinedButton(
                        onClick = onAddFineClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_fine_header_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "Add Fine / Fee" else "जरिवाना / शुल्क",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Account Available Balance Highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (summary.netAvailableLiquidity >= 0)
                            Color(0xFF0F766E).copy(alpha = 0.12f)
                        else
                            Color(0xFFDC2626).copy(alpha = 0.12f)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = if (summary.netAvailableLiquidity >= 0) Color(0xFF0F766E) else Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEnglish) "AET Bank / Account Balance" else "AET बैंक तथा नगद मौज्दात",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormat.format(summary.netAvailableLiquidity),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (summary.netAvailableLiquidity >= 0) Color(0xFF0F766E) else Color(0xFFDC2626)
                        )
                        Text(
                            text = if (isEnglish) "Net Liquid Cash in Treasury Account" else "कोषमा बाँकी रहेको शुद्ध मौज्दात रकम",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isEnglish) "${summary.activeMembersCount} Members" else "${summary.activeMembersCount} सदस्यहरू",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isEnglish) "${summary.activeLoansCount} Active Loans" else "${summary.activeLoansCount} चालु ऋण",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // 4 Detailed Financial Pillars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Contribution & Active Loan Principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TreasuryMetricItem(
                        title = if (isEnglish) "Total Contributions Pool" else "कुल जम्मा रकम",
                        subtitle = if (isEnglish) "All Members Collected" else "सबै सदस्यहरूको बचत",
                        amount = summary.totalContributionsCollected,
                        icon = Icons.Default.Payments,
                        iconColor = Color(0xFF1E88E5),
                        badgeText = if (isEnglish) "Pool" else "जम्मा",
                        modifier = Modifier.weight(1f)
                    )

                    TreasuryMetricItem(
                        title = if (isEnglish) "Active Loans Deployed" else "लगानीमा रहेको ऋण",
                        subtitle = if (isEnglish) "Current Active Principal" else "बजारमा चालु ऋण",
                        amount = summary.activeLoansPrincipal,
                        icon = Icons.Default.TrendingUp,
                        iconColor = Color(0xFFD97706),
                        badgeText = if (isEnglish) "${summary.activeLoansCount} Active" else "${summary.activeLoansCount} सक्रिय",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Loan Interest Earned & Extra / Fine Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TreasuryMetricItem(
                        title = if (isEnglish) "Loan Interest Earned" else "ऋणको ब्याज आम्दानी",
                        subtitle = if (isEnglish) "10% p.a. Accumulated" else "वार्षिक १०% ब्याजबाट",
                        amount = summary.totalInterestEarned,
                        icon = Icons.Default.ArrowUpward,
                        iconColor = Color(0xFF10B981),
                        badgeText = if (isEnglish) "10% Yield" else "१०% ब्याज",
                        modifier = Modifier.weight(1f)
                    )

                    TreasuryMetricItem(
                        title = if (isEnglish) "Fines & Extra Income" else "जरिवाना तथा अतिरिक्त",
                        subtitle = if (isEnglish) "Late Fees & Charges" else "विलम्ब शुल्क र दण्ड",
                        amount = summary.totalFineAndExtraIncome,
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFF8B5CF6),
                        badgeText = if (isEnglish) "Extra" else "जरिवाना",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Informational Notice for Members
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEnglish)
                        "AET Transparent Ledger: All contributions, 10% loan interest, and fines are pooled in the common account."
                    else
                        "AET पारदर्शी खाता: सम्पूर्ण बचत, १०% ऋण ब्याज र जरिवाना साझा कोष खातामा सुरक्षित जम्मा हुन्छ।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TreasuryMetricItem(
    title: String,
    subtitle: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(iconColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currencyFormat.format(amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
