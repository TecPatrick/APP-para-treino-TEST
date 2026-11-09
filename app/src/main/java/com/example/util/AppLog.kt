package com.example.util

import android.util.Log
import com.example.BuildConfig

/**
 * Centralized privacy-safe logger that only outputs in debug builds (`BuildConfig.DEBUG == true`)
 * and masks sensitive identifiers such as Firebase UIDs and email addresses.
 */
object AppLog {

    private val emailRegex = Regex("""([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})""")
    private val uidKeyRegex = Regex("""(?i)\b(uid|userId|firebaseUid)\s*[:=]\s*([A-Za-z0-9_-]{5,})""")

    /**
     * Masks a UID so only the first 4 characters are visible (e.g. `abcd***`).
     */
    fun maskUid(uid: String?): String {
        if (uid.isNullOrBlank()) return "<empty>"
        val trimmed = uid.trim()
        return if (trimmed.length <= 4) "***" else "${trimmed.take(4)}***"
    }

    /**
     * Masks an email address as `p***@domain.com`.
     */
    fun maskEmail(email: String?): String {
        if (email.isNullOrBlank()) return "<empty>"
        val trimmed = email.trim()
        val atIndex = trimmed.indexOf('@')
        if (atIndex <= 0 || atIndex == trimmed.lastIndex) {
            return maskUid(trimmed)
        }
        val firstChar = trimmed.first()
        val domain = trimmed.substring(atIndex + 1)
        return "$firstChar***@$domain"
    }

    /**
     * Scrubs accidental email addresses or `uid=...` tokens from free-form log strings.
     */
    fun sanitizeMessage(message: String): String {
        val emailMasked = emailRegex.replace(message) { match ->
            val local = match.groupValues[1]
            val domain = match.groupValues[2]
            "${local.firstOrNull() ?: '*'}***@$domain"
        }
        return uidKeyRegex.replace(emailMasked) { match ->
            val key = match.groupValues[1]
            val rawUid = match.groupValues[2]
            "$key=${maskUid(rawUid)}"
        }
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, sanitizeMessage(message))
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, sanitizeMessage(message))
        }
    }

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, sanitizeMessage(message))
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(tag, sanitizeMessage(message), throwable)
            } else {
                Log.w(tag, sanitizeMessage(message))
            }
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, sanitizeMessage(message), throwable)
            } else {
                Log.e(tag, sanitizeMessage(message))
            }
        }
    }
}
