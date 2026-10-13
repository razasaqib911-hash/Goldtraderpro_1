package com.example.data.vault

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureVaultStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trader_pro_vault_prefs", Context.MODE_PRIVATE)

    // Salted encryption key for vault obfuscation
    private val keyBytes = "TraderProSecureVaultKey2026!#$".padEnd(32, '0').substring(0, 32).toByteArray(StandardCharsets.UTF_8)
    private val ivBytes = "1234567890123456".toByteArray(StandardCharsets.UTF_8)

    fun saveDerivToken(token: String) {
        val encrypted = encryptString(token)
        prefs.edit().putString(KEY_DERIV_TOKEN, encrypted).apply()
    }

    fun getDerivToken(): String {
        val encrypted = prefs.getString(KEY_DERIV_TOKEN, "") ?: ""
        return decryptString(encrypted)
    }

    fun saveDailyLossCap(cap: Double) {
        prefs.edit().putFloat(KEY_DAILY_LOSS_CAP, cap.toFloat()).apply()
    }

    fun getDailyLossCap(): Double {
        return prefs.getFloat(KEY_DAILY_LOSS_CAP, 300.0f).toDouble()
    }

    fun saveRiskRewardRatio(ratio: Double) {
        prefs.edit().putFloat(KEY_RR_RATIO, ratio.toFloat()).apply()
    }

    fun getRiskRewardRatio(): Double {
        return prefs.getFloat(KEY_RR_RATIO, 2.0f).toDouble()
    }

    fun saveEnforceKillzones(enforce: Boolean) {
        prefs.edit().putBoolean(KEY_ENFORCE_KILLZONES, enforce).apply()
    }

    fun getEnforceKillzones(): Boolean {
        return prefs.getBoolean(KEY_ENFORCE_KILLZONES, true)
    }

    private fun encryptString(plain: String): String {
        if (plain.isBlank()) return ""
        return try {
            val key = SecretKeySpec(keyBytes, "AES")
            val iv = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key, iv)
            val encrypted = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            plain
        }
    }

    private fun decryptString(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            val key = SecretKeySpec(keyBytes, "AES")
            val iv = IvParameterSpec(ivBytes)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val decoded = Base64.decode(encoded, Base64.NO_WRAP)
            String(cipher.doFinal(decoded), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            encoded
        }
    }

    companion object {
        private const val KEY_DERIV_TOKEN = "vault_deriv_token"
        private const val KEY_DAILY_LOSS_CAP = "vault_daily_loss_cap"
        private const val KEY_RR_RATIO = "vault_risk_reward_ratio"
        private const val KEY_ENFORCE_KILLZONES = "vault_enforce_killzones"
    }
}
