package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.localization.AppLanguage
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.MemberTab

@Composable
fun MemberBottomBar(
    selectedTab: MemberTab,
    onTabSelected: (MemberTab) -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("member_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == MemberTab.DASHBOARD,
            onClick = { onTabSelected(MemberTab.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Dashboard" else "ड्यासबोर्ड") },
            modifier = Modifier.testTag("tab_member_dashboard")
        )
        NavigationBarItem(
            selected = selectedTab == MemberTab.LOANS,
            onClick = { onTabSelected(MemberTab.LOANS) },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Loans") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Loans (10%)" else "ऋण (१०%)") },
            modifier = Modifier.testTag("tab_member_loans")
        )
        NavigationBarItem(
            selected = selectedTab == MemberTab.PASSBOOK,
            onClick = { onTabSelected(MemberTab.PASSBOOK) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Passbook") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Passbook" else "पासबुक") },
            modifier = Modifier.testTag("tab_member_passbook")
        )
        NavigationBarItem(
            selected = selectedTab == MemberTab.COMMITTEE,
            onClick = { onTabSelected(MemberTab.COMMITTEE) },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Committee") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Committee" else "समिति") },
            modifier = Modifier.testTag("tab_member_committee")
        )
    }
}

@Composable
fun AdminBottomBar(
    selectedTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("admin_bottom_bar"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == AdminTab.TREASURY,
            onClick = { onTabSelected(AdminTab.TREASURY) },
            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Treasury") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Treasury" else "कोष खाता") },
            modifier = Modifier.testTag("tab_admin_treasury")
        )
        NavigationBarItem(
            selected = selectedTab == AdminTab.MEMBERS,
            onClick = { onTabSelected(AdminTab.MEMBERS) },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Members") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Members" else "सदस्यहरू") },
            modifier = Modifier.testTag("tab_admin_members")
        )
        NavigationBarItem(
            selected = selectedTab == AdminTab.LOANDESK,
            onClick = { onTabSelected(AdminTab.LOANDESK) },
            icon = { Icon(Icons.Default.RequestQuote, contentDescription = "Loan Desk") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Loan Desk" else "ऋण डेस्क") },
            modifier = Modifier.testTag("tab_admin_loans")
        )
        NavigationBarItem(
            selected = selectedTab == AdminTab.REPORTS,
            onClick = { onTabSelected(AdminTab.REPORTS) },
            icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Reports" else "प्रतिवेदन") },
            modifier = Modifier.testTag("tab_admin_reports")
        )
        NavigationBarItem(
            selected = selectedTab == AdminTab.REMINDERS,
            onClick = { onTabSelected(AdminTab.REMINDERS) },
            icon = { Icon(Icons.Default.Alarm, contentDescription = "Reminders") },
            label = { Text(if (language == AppLanguage.ENGLISH) "Reminders" else "रिमाइन्डर") },
            modifier = Modifier.testTag("tab_admin_reminders")
        )
    }
}
