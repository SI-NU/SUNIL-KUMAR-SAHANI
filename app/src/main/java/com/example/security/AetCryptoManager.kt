package com.example.security

import java.security.MessageDigest
import kotlin.random.Random

object AetCryptoManager {

    // Simulates secure ephemeral OTP storage
    private val activeOtps = mutableMapOf<String, OtpEntry>()

    data class OtpEntry(
        val code: String,
        val generatedAt: Long,
        val expiresAt: Long
    )

    /**
     * Computes SHA-256 hash for transaction ledger to guarantee tamper-proof records
     */
    fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a tamper-proof cryptographic ledger hash for a transaction
     */
    fun createTransactionHash(
        id: String,
        memberId: String,
        type: String,
        amount: Double,
        timestamp: Long,
        prevHash: String
    ): String {
        val payload = "$id|$memberId|$type|%.2f|$timestamp|$prevHash".format(amount)
        return computeSha256(payload)
    }

    /**
     * Generates a 6-digit OTP valid for 2 minutes
     */
    fun generateOtp(targetId: String): String {
        val code = "%06d".format(Random.nextInt(100000, 999999))
        val now = System.currentTimeMillis()
        activeOtps[targetId] = OtpEntry(
            code = code,
            generatedAt = now,
            expiresAt = now + 120_000L // 2 mins validity
        )
        return code
    }

    /**
     * Validates OTP (also allows universal demo OTP "123456" for convenience)
     */
    fun verifyOtp(targetId: String, inputCode: String): Boolean {
        if (inputCode == "123456") return true
        val entry = activeOtps[targetId] ?: return false
        val now = System.currentTimeMillis()
        if (now > entry.expiresAt) {
            activeOtps.remove(targetId)
            return false
        }
        val isValid = entry.code == inputCode
        if (isValid) {
            activeOtps.remove(targetId)
        }
        return isValid
    }

    /**
     * Formats a cryptographic hash for UI display (e.g. 8a3f...d9e1)
     */
    fun formatShortHash(hash: String): String {
        return if (hash.length > 16) {
            "${hash.take(8)}...${hash.takeLast(6)}"
        } else {
            hash
        }
    }
}
