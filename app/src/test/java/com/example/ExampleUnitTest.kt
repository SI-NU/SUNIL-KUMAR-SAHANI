package com.example

import com.example.security.AetCryptoManager
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testQuarterlyInterestCalculation() {
        // 10% per annum on 3-month quarterly cycle:
        // Principal * 10% * (3/12) = Principal * 0.025
        val principal = 40000.0
        val annualRate = 0.10
        val quarterlyInterest = principal * annualRate * (3.0 / 12.0)

        assertEquals(1000.0, quarterlyInterest, 0.001)

        val principal2 = 100000.0
        val quarterlyInterest2 = principal2 * annualRate * (3.0 / 12.0)
        assertEquals(2500.0, quarterlyInterest2, 0.001)
    }

    @Test
    fun testNineMemberMonthlyPool() {
        // 9 members * 5,000 contribution = 45,000 monthly payout
        val memberCount = 9
        val monthlyContribution = 5000.0
        val poolTotal = memberCount * monthlyContribution
        assertEquals(45000.0, poolTotal, 0.001)
    }

    @Test
    fun testOtpGenerationAndVerification() {
        val phone = "+91 98111 22334"
        val otp = AetCryptoManager.generateOtp(phone)

        assertEquals(6, otp.length)
        assertTrue(otp.all { it.isDigit() })
        assertTrue(AetCryptoManager.verifyOtp(phone, otp))
        assertFalse(AetCryptoManager.verifyOtp(phone, "000000"))
    }

    @Test
    fun testSha256LedgerHashing() {
        val hash1 = AetCryptoManager.createTransactionHash("TXN-001", "AET-01", "CONTRIBUTION", 5000.0, 1700000000000L, "GENESIS")
        val hash2 = AetCryptoManager.createTransactionHash("TXN-001", "AET-01", "CONTRIBUTION", 5000.0, 1700000000000L, "GENESIS")

        // Deterministic
        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length)

        // Tamper evident
        val hashTampered = AetCryptoManager.createTransactionHash("TXN-001", "AET-01", "CONTRIBUTION", 5001.0, 1700000000000L, "GENESIS")
        assertNotEquals(hash1, hashTampered)
    }
}
