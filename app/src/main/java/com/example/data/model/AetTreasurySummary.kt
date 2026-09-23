package com.example.data.model

data class AetTreasurySummary(
    val totalContributionsCollected: Double = 0.0,
    val activeLoansPrincipal: Double = 0.0,
    val totalLoansDisbursed: Double = 0.0,
    val totalInterestEarned: Double = 0.0,
    val totalFineAndExtraIncome: Double = 0.0,
    val totalPayoutsDisbursed: Double = 0.0,
    val totalPrincipalRepaid: Double = 0.0,
    val netAvailableLiquidity: Double = 0.0,
    val activeMembersCount: Int = 9,
    val activeLoansCount: Int = 0
) {
    companion object {
        fun calculate(
            members: List<MemberEntity>,
            loans: List<LoanEntity>,
            transactions: List<TransactionEntity>
        ): AetTreasurySummary {
            val totalContributed = members.sumOf { it.totalContributed }
            val activeLoans = loans.filter { it.status == "ACTIVE" }
            val activeLoansPrincipal = activeLoans.sumOf { it.remainingPrincipal }
            val totalLoansDisbursed = loans.filter { it.status == "ACTIVE" || it.status == "PAID_OFF" }
                .sumOf { it.principalAmount }
            val totalInterestEarned = transactions
                .filter { it.type == "INTEREST_PAYMENT" }
                .sumOf { it.amount }
                .takeIf { it > 0.0 } ?: loans.sumOf { it.totalInterestPaid }

            val totalFineAndExtra = transactions
                .filter { it.type == "FINE_PENALTY" || it.type == "EXTRA_INCOME" }
                .sumOf { it.amount }

            val totalPayouts = transactions
                .filter { it.type == "PAYOUT" }
                .sumOf { it.amount }
                .takeIf { it > 0.0 } ?: (members.count { it.payoutReceived } * 45000.0)

            val totalRepaid = transactions
                .filter { it.type == "LOAN_REPAYMENT" }
                .sumOf { it.amount }

            // Liquid cash in AET committee account:
            // Total inflows: contributions + interest earned + fines/penalties + principal repaid
            // Total outflows: loans disbursed + rotational payouts
            val netLiquidity = (totalContributed + totalInterestEarned + totalFineAndExtra + totalRepaid) -
                    (totalLoansDisbursed + totalPayouts)

            return AetTreasurySummary(
                totalContributionsCollected = totalContributed,
                activeLoansPrincipal = activeLoansPrincipal,
                totalLoansDisbursed = totalLoansDisbursed,
                totalInterestEarned = totalInterestEarned,
                totalFineAndExtraIncome = totalFineAndExtra,
                totalPayoutsDisbursed = totalPayouts,
                totalPrincipalRepaid = totalRepaid,
                netAvailableLiquidity = netLiquidity,
                activeMembersCount = members.size,
                activeLoansCount = activeLoans.size
            )
        }
    }
}
