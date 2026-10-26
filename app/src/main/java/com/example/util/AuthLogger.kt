package com.example.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Dedicated logging and diagnostic utility for Firebase Authentication.
 *
 * Responsibilities:
 * 1. Monitors and logs Firebase Auth state changes (sign-in, sign-out, token refresh, user profile).
 * 2. Formats and logs login/auth failures with full exception details and Firebase error codes.
 * 3. Accurately distinguishes between 'wrong password', 'invalid request', 'App Check / reCAPTCHA',
 *    and other authentication failure scenarios.
 * 4. Provides actionable diagnostic hints for developers and logs structured output for Logcat.
 */
object AuthLogger {

    const val TAG = "VirtusAuthLogger"

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val recentEvents = CopyOnWriteArrayList<AuthLogEntry>()
    private const val MAX_RECENT_EVENTS = 50

    @Volatile
    private var lastDiagnosis: AuthDiagnosis? = null

    @Volatile
    private var isListenerAttached = false

    enum class FailureCategory {
        WRONG_PASSWORD,
        INVALID_REQUEST,
        USER_NOT_FOUND,
        APP_CHECK_OR_RECAPTCHA,
        TOO_MANY_REQUESTS,
        NETWORK_FAILURE,
        USER_DISABLED,
        ACCOUNT_COLLISION,
        WEAK_PASSWORD,
        UNKNOWN
    }

    data class AuthDiagnosis(
        val category: FailureCategory,
        val errorCode: String?,
        val isWrongPassword: Boolean,
        val isInvalidRequest: Boolean,
        val isAppCheckOrRecaptcha: Boolean,
        val summary: String,
        val actionableResolution: String,
        val rawExceptionClass: String,
        val rawMessage: String?,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class AuthLogEntry(
        val timestamp: Long,
        val eventType: String,
        val details: String,
        val isError: Boolean = false
    )

    /**
     * Attaches an AuthStateListener to monitor authentication state transitions.
     * Safe to call multiple times; duplicate listeners are avoided.
     */
    @Synchronized
    fun attachAuthStateListener(auth: FirebaseAuth = FirebaseAuth.getInstance()): FirebaseAuth.AuthStateListener {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            logAuthStateChanged(user)
        }
        if (!isListenerAttached) {
            auth.addAuthStateListener(listener)
            isListenerAttached = true
            Log.i(TAG, "📡 [AUTH_LISTENER] Firebase AuthStateListener attached successfully.")
        }
        return listener
    }

    /**
     * Logs auth state transition (signed in vs signed out).
     */
    fun logAuthStateChanged(user: FirebaseUser?) {
        val timestamp = System.currentTimeMillis()
        if (user != null) {
            val sanitizedEmail = sanitizeIdentifier(user.email)
            val providerIds = user.providerData.joinToString { it.providerId }
            val logMessage = buildString {
                appendLine("======================================================================")
                appendLine("👤 [AUTH STATE] User is SIGNED IN")
                appendLine("├─ UID: ${user.uid}")
                appendLine("├─ Email: $sanitizedEmail (Verified: ${user.isEmailVerified})")
                appendLine("├─ Name: ${user.displayName ?: "<not set>"}")
                appendLine("├─ Providers: [$providerIds]")
                appendLine("└─ Is Anonymous: ${user.isAnonymous}")
                appendLine("======================================================================")
            }
            Log.i(TAG, logMessage)
            recordEvent("AUTH_STATE_SIGNED_IN", "UID=${user.uid}, email=$sanitizedEmail, providers=$providerIds")
        } else {
            val logMessage = buildString {
                appendLine("======================================================================")
                appendLine("🚪 [AUTH STATE] User is SIGNED OUT (No active session)")
                appendLine("======================================================================")
            }
            Log.i(TAG, logMessage)
            recordEvent("AUTH_STATE_SIGNED_OUT", "No authenticated user")
        }
    }

    /**
     * Logs the beginning of an authentication attempt.
     */
    fun logLoginAttempt(method: String, identifier: String?) {
        val sanitized = sanitizeIdentifier(identifier)
        Log.d(TAG, "🚀 [AUTH ATTEMPT] Method: $method | Target: $sanitized")
        recordEvent("LOGIN_ATTEMPT", "Method=$method, Target=$sanitized")
    }

    /**
     * Logs a successful authentication attempt.
     */
    fun logLoginSuccess(method: String, user: FirebaseUser) {
        val sanitized = sanitizeIdentifier(user.email)
        Log.i(TAG, "✅ [AUTH SUCCESS] Method: $method | UID: ${user.uid} | Email: $sanitized")
        recordEvent("LOGIN_SUCCESS", "Method=$method, UID=${user.uid}, Target=$sanitized")
        lastDiagnosis = null
    }

    /**
     * Diagnoses and logs an authentication failure in detail.
     * Highlights whether the error was caused by a wrong password or an invalid request.
     */
    fun logLoginFailure(
        method: String,
        identifier: String?,
        throwable: Throwable
    ): AuthDiagnosis {
        val diagnosis = diagnoseAuthException(throwable)
        lastDiagnosis = diagnosis
        val sanitized = sanitizeIdentifier(identifier)

        val logOutput = buildString {
            appendLine("======================================================================")
            appendLine("❌ [AUTH FAILURE] Method: $method")
            appendLine("├─ Target: $sanitized")
            appendLine("├─ Scenario Diagnosis: ${diagnosis.category.name}")
            appendLine("├─ Is Wrong Password? ${if (diagnosis.isWrongPassword) "YES (Check user credentials)" else "NO"}")
            appendLine("├─ Is Invalid Request? ${if (diagnosis.isInvalidRequest) "YES (Client validation or malformed payload)" else "NO"}")
            appendLine("├─ Is App Check / Recaptcha Error? ${if (diagnosis.isAppCheckOrRecaptcha) "YES (Verify SHA-256 in Firebase Console!)" else "NO"}")
            appendLine("├─ Firebase Error Code: ${diagnosis.errorCode ?: "N/A"}")
            appendLine("├─ Exception Class: ${diagnosis.rawExceptionClass}")
            appendLine("├─ Message: ${diagnosis.rawMessage ?: "<null>"}")
            appendLine("├─ Summary: ${diagnosis.summary}")
            appendLine("└─ Actionable Hint: ${diagnosis.actionableResolution}")
            appendLine("======================================================================")
        }

        if (diagnosis.isAppCheckOrRecaptcha || diagnosis.category == FailureCategory.NETWORK_FAILURE) {
            Log.e(TAG, logOutput, throwable)
        } else if (diagnosis.isWrongPassword) {
            Log.w(TAG, logOutput)
        } else {
            Log.e(TAG, logOutput, throwable)
        }

        recordEvent(
            "LOGIN_FAILURE",
            "Method=$method, Category=${diagnosis.category}, Code=${diagnosis.errorCode}, Target=$sanitized",
            isError = true
        )

        return diagnosis
    }

    /**
     * Logs registration attempts and failures.
     */
    fun logRegistrationAttempt(email: String) {
        val sanitized = sanitizeIdentifier(email)
        Log.d(TAG, "📝 [REGISTER ATTEMPT] Email: $sanitized")
        recordEvent("REGISTER_ATTEMPT", "Email=$sanitized")
    }

    fun logRegistrationSuccess(user: FirebaseUser) {
        val sanitized = sanitizeIdentifier(user.email)
        Log.i(TAG, "✅ [REGISTER SUCCESS] UID: ${user.uid} | Email: $sanitized")
        recordEvent("REGISTER_SUCCESS", "UID=${user.uid}, Email=$sanitized")
        lastDiagnosis = null
    }

    fun logRegistrationFailure(email: String?, throwable: Throwable): AuthDiagnosis {
        val diagnosis = diagnoseAuthException(throwable)
        lastDiagnosis = diagnosis
        val sanitized = sanitizeIdentifier(email)

        Log.e(
            TAG,
            "❌ [REGISTER FAILURE] Email: $sanitized | Category: ${diagnosis.category} | " +
                "Code: ${diagnosis.errorCode} | ${diagnosis.summary}",
            throwable
        )
        recordEvent("REGISTER_FAILURE", "Category=${diagnosis.category}, Code=${diagnosis.errorCode}", isError = true)
        return diagnosis
    }

    /**
     * Logs explicit sign-out.
     */
    fun logSignOut(user: FirebaseUser?) {
        val uid = user?.uid ?: "unknown"
        Log.i(TAG, "🚪 [SIGN OUT] Active session ended for UID: $uid")
        recordEvent("SIGN_OUT", "UID=$uid")
    }

    /**
     * Diagnoses a throwable into structured categories, explicitly separating:
     * - WRONG_PASSWORD (invalid credentials for existing account)
     * - INVALID_REQUEST (client input validation or malformed request payload)
     * - APP_CHECK_OR_RECAPTCHA (SHA-256 missing / Play Integrity / reCAPTCHA challenge issue)
     * - USER_NOT_FOUND (user does not exist)
     * - TOO_MANY_REQUESTS (rate limiting)
     * - NETWORK_FAILURE (no connection to Firebase servers)
     */
    fun diagnoseAuthException(throwable: Throwable): AuthDiagnosis {
        val errorCode = extractFirebaseErrorCode(throwable)
        val rawMessage = throwable.message.orEmpty()
        val lowerMessage = rawMessage.lowercase(Locale.ROOT)
        val exceptionClass = throwable.javaClass.name

        // Check for App Check / Play Integrity / Recaptcha issues first
        val isRecaptchaOrAppCheck = isAppCheckOrRecaptchaError(errorCode, lowerMessage, throwable)
        if (isRecaptchaOrAppCheck) {
            return AuthDiagnosis(
                category = FailureCategory.APP_CHECK_OR_RECAPTCHA,
                errorCode = errorCode,
                isWrongPassword = false,
                isInvalidRequest = false,
                isAppCheckOrRecaptcha = true,
                summary = "Firebase App Check or reCAPTCHA verification failed.",
                actionableResolution = "Add SHA-256 fingerprint (79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF) " +
                    "to Firebase Console -> Project Settings -> Android Apps, or whitelist App Check debug token in Firebase Console.",
                rawExceptionClass = exceptionClass,
                rawMessage = rawMessage
            )
        }

        // Check for client-side or server invalid request (syntax/format error)
        val isInvalidReq = isInvalidRequestError(errorCode, lowerMessage, throwable)
        if (isInvalidReq) {
            return AuthDiagnosis(
                category = FailureCategory.INVALID_REQUEST,
                errorCode = errorCode ?: "CLIENT_INVALID_REQUEST",
                isWrongPassword = false,
                isInvalidRequest = true,
                isAppCheckOrRecaptcha = false,
                summary = "The login request was malformed, incomplete, or failed parameter validation before/at server dispatch.",
                actionableResolution = "Verify email format, non-empty fields, and parameter encodings before calling Firebase Auth APIs.",
                rawExceptionClass = exceptionClass,
                rawMessage = rawMessage
            )
        }

        // Check for wrong password
        val isWrongPass = isWrongPasswordError(errorCode, lowerMessage, throwable)
        if (isWrongPass) {
            return AuthDiagnosis(
                category = FailureCategory.WRONG_PASSWORD,
                errorCode = errorCode ?: "ERROR_WRONG_PASSWORD",
                isWrongPassword = true,
                isInvalidRequest = false,
                isAppCheckOrRecaptcha = false,
                summary = "The request was syntactically valid and reached Firebase, but the password provided was incorrect for this user.",
                actionableResolution = "Prompt user to verify password or trigger the sendPasswordReset flow.",
                rawExceptionClass = exceptionClass,
                rawMessage = rawMessage
            )
        }

        // Check other common Firebase Auth scenarios
        when {
            throwable is FirebaseAuthInvalidUserException || errorCode == "ERROR_USER_NOT_FOUND" -> {
                return AuthDiagnosis(
                    category = FailureCategory.USER_NOT_FOUND,
                    errorCode = errorCode ?: "ERROR_USER_NOT_FOUND",
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "No user account exists for the given email address.",
                    actionableResolution = "Prompt user to register or verify spelling of email.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            errorCode == "ERROR_USER_DISABLED" -> {
                return AuthDiagnosis(
                    category = FailureCategory.USER_DISABLED,
                    errorCode = errorCode,
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "The user account has been disabled by an administrator.",
                    actionableResolution = "Contact support or enable the account in Firebase Console -> Authentication -> Users.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            errorCode == "ERROR_TOO_MANY_REQUESTS" || lowerMessage.contains("too many requests") -> {
                return AuthDiagnosis(
                    category = FailureCategory.TOO_MANY_REQUESTS,
                    errorCode = errorCode ?: "ERROR_TOO_MANY_REQUESTS",
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "Access temporarily disabled due to many consecutive failed attempts.",
                    actionableResolution = "Wait several minutes or reset password to unblock immediately.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            errorCode == "ERROR_NETWORK_REQUEST_FAILED" ||
                lowerMessage.contains("network") ||
                lowerMessage.contains("unknownhost") ||
                lowerMessage.contains("timeout") -> {
                return AuthDiagnosis(
                    category = FailureCategory.NETWORK_FAILURE,
                    errorCode = errorCode ?: "ERROR_NETWORK_REQUEST_FAILED",
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "A network error occurred while communicating with Firebase Authentication.",
                    actionableResolution = "Check device internet connection and DNS settings.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            throwable is FirebaseAuthUserCollisionException || errorCode == "ERROR_EMAIL_ALREADY_IN_USE" -> {
                return AuthDiagnosis(
                    category = FailureCategory.ACCOUNT_COLLISION,
                    errorCode = errorCode ?: "ERROR_EMAIL_ALREADY_IN_USE",
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "An account already exists with this email address.",
                    actionableResolution = "Direct the user to the Sign In screen or perform password recovery.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            throwable is FirebaseAuthWeakPasswordException || errorCode == "ERROR_WEAK_PASSWORD" -> {
                return AuthDiagnosis(
                    category = FailureCategory.WEAK_PASSWORD,
                    errorCode = errorCode ?: "ERROR_WEAK_PASSWORD",
                    isWrongPassword = false,
                    isInvalidRequest = true,
                    isAppCheckOrRecaptcha = false,
                    summary = "The supplied password does not satisfy Firebase password policy (minimum 6-8 chars).",
                    actionableResolution = "Ensure the password meets minimum complexity guidelines.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
            else -> {
                return AuthDiagnosis(
                    category = FailureCategory.UNKNOWN,
                    errorCode = errorCode,
                    isWrongPassword = false,
                    isInvalidRequest = false,
                    isAppCheckOrRecaptcha = false,
                    summary = "Unclassified authentication error: ${throwable.message}",
                    actionableResolution = "Review raw exception details and Firebase Console service status.",
                    rawExceptionClass = exceptionClass,
                    rawMessage = rawMessage
                )
            }
        }
    }

    private fun extractFirebaseErrorCode(throwable: Throwable): String? {
        if (throwable is FirebaseAuthException) {
            return throwable.errorCode
        }
        val cause = throwable.cause
        if (cause is FirebaseAuthException) {
            return cause.errorCode
        }
        // Check for error code patterns in message like [ERROR_WRONG_PASSWORD] or [INVALID_LOGIN_CREDENTIALS]
        val message = throwable.message ?: return null
        val regex = Regex("(ERROR_[A-Z_]+|INVALID_LOGIN_CREDENTIALS)")
        return regex.find(message)?.value
    }

    private fun isWrongPasswordError(errorCode: String?, lowerMessage: String, throwable: Throwable): Boolean {
        if (errorCode == "ERROR_WRONG_PASSWORD") return true

        // In modern Firebase Auth with email enumeration protection enabled,
        // invalid password and invalid email both return INVALID_LOGIN_CREDENTIALS.
        // If the user already exists or the exception is FirebaseAuthInvalidCredentialsException
        // without an invalid email format syntax error, it represents wrong credentials.
        if (errorCode == "INVALID_LOGIN_CREDENTIALS" || errorCode == "ERROR_INVALID_CREDENTIAL") {
            if (!lowerMessage.contains("badly formatted") &&
                !lowerMessage.contains("invalid email") &&
                !lowerMessage.contains("recaptcha") &&
                !lowerMessage.contains("app_not_authorized")
            ) {
                return true
            }
        }

        if (throwable is FirebaseAuthInvalidCredentialsException) {
            if (lowerMessage.contains("password") ||
                lowerMessage.contains("senha") ||
                lowerMessage.contains("credential") ||
                lowerMessage.contains("invalid_login_credentials")
            ) {
                return true
            }
        }

        return lowerMessage.contains("wrong password") || lowerMessage.contains("senha incorreta")
    }

    private fun isInvalidRequestError(errorCode: String?, lowerMessage: String, throwable: Throwable): Boolean {
        if (throwable is IllegalArgumentException) return true

        // Exclude the specific Firebase Auth "malformed" message that often indicates App Check/Recaptcha failure
        if (lowerMessage.contains("supplied auth credential is incorrect, malformed or has expired")) {
            return false
        }

        when (errorCode) {
            "ERROR_INVALID_EMAIL",
            "ERROR_MISSING_EMAIL",
            "ERROR_INVALID_REQ_TYPE",
            "ERROR_INVALID_ARGUMENT",
            "ERROR_EMPTY_FIELDS",
            "CLIENT_INVALID_REQUEST" -> return true
        }

        if (lowerMessage.contains("badly formatted") ||
            lowerMessage.contains("malformed") ||
            lowerMessage.contains("e-mail válido") ||
            lowerMessage.contains("invalid email") ||
            lowerMessage.contains("empty email") ||
            lowerMessage.contains("empty password") ||
            lowerMessage.contains("argument")
        ) {
            return true
        }

        return false
    }

    private fun isAppCheckOrRecaptchaError(errorCode: String?, lowerMessage: String, throwable: Throwable): Boolean {
        when (errorCode) {
            "ERROR_CAPTCHA_CHECK_FAILED",
            "ERROR_WEB_CONTEXT_CANCELED",
            "ERROR_APP_NOT_AUTHORIZED",
            "ERROR_PLAY_INTEGRITY_FAILED",
            "ERROR_APP_CHECK_TOKEN_INVALID" -> return true
        }

        return lowerMessage.contains("recaptcha") ||
            lowerMessage.contains("app-check") ||
            lowerMessage.contains("app check") ||
            lowerMessage.contains("play integrity") ||
            lowerMessage.contains("playintegrity") ||
            lowerMessage.contains("safetynet") ||
            lowerMessage.contains("safety net") ||
            lowerMessage.contains("app_not_authorized") ||
            lowerMessage.contains("fingerprint") ||
            lowerMessage.contains("certificate_hash") ||
            lowerMessage.contains("malformed") && lowerMessage.contains("credential") ||
            lowerMessage.contains("incorrect") && lowerMessage.contains("malformed") && lowerMessage.contains("expired")
    }

    /**
     * Masks email or identifiers to preserve user privacy in release logs
     * while retaining enough detail for debugging.
     */
    fun sanitizeIdentifier(identifier: String?): String {
        if (identifier.isNullOrBlank()) return "<empty>"
        val trimmed = identifier.trim()
        val atIndex = trimmed.indexOf('@')
        return if (atIndex > 1) {
            val user = trimmed.substring(0, atIndex)
            val domain = trimmed.substring(atIndex)
            val maskedUser = if (user.length <= 2) {
                user.first() + "*"
            } else {
                user.first() + "*".repeat(user.length - 2) + user.last()
            }
            "$maskedUser$domain"
        } else if (trimmed.length > 4) {
            trimmed.take(2) + "***" + trimmed.takeLast(2)
        } else {
            "***"
        }
    }

    private fun recordEvent(eventType: String, details: String, isError: Boolean = false) {
        val entry = AuthLogEntry(System.currentTimeMillis(), eventType, details, isError)
        recentEvents.add(entry)
        while (recentEvents.size > MAX_RECENT_EVENTS) {
            recentEvents.removeAt(0)
        }
    }

    fun getLastDiagnosis(): AuthDiagnosis? = lastDiagnosis

    fun getRecentEvents(): List<AuthLogEntry> = recentEvents.toList()

    fun clearHistory() {
        recentEvents.clear()
        lastDiagnosis = null
    }
}
