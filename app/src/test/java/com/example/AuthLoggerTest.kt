package com.example

import com.example.util.AuthLogger
import com.example.util.AuthLogger.FailureCategory
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthLoggerTest {

    @Before
    fun setUp() {
        AuthLogger.clearHistory()
    }

    @Test
    fun distinguishesWrongPasswordFromInvalidRequest() {
        // 1. Wrong password scenario
        val wrongPasswordException = FirebaseAuthInvalidCredentialsException(
            "ERROR_WRONG_PASSWORD",
            "The password is invalid or user does not have a password."
        )
        val diagnosisWrongPass = AuthLogger.diagnoseAuthException(wrongPasswordException)

        assertEquals(FailureCategory.WRONG_PASSWORD, diagnosisWrongPass.category)
        assertTrue(diagnosisWrongPass.isWrongPassword)
        assertFalse(diagnosisWrongPass.isInvalidRequest)
        assertFalse(diagnosisWrongPass.isAppCheckOrRecaptcha)
        assertEquals("ERROR_WRONG_PASSWORD", diagnosisWrongPass.errorCode)

        // 2. Invalid request scenario (client validation)
        val clientInvalidReq = IllegalArgumentException("Digite um e-mail válido.")
        val diagnosisInvalidReq = AuthLogger.diagnoseAuthException(clientInvalidReq)

        assertEquals(FailureCategory.INVALID_REQUEST, diagnosisInvalidReq.category)
        assertFalse(diagnosisInvalidReq.isWrongPassword)
        assertTrue(diagnosisInvalidReq.isInvalidRequest)
        assertFalse(diagnosisInvalidReq.isAppCheckOrRecaptcha)
    }

    @Test
    fun identifiesAppCheckAndRecaptchaErrorsWithActionableGuidance() {
        val recaptchaException = FirebaseAuthException(
            "ERROR_CAPTCHA_CHECK_FAILED",
            "Recaptcha verification failed. App Check or token invalid."
        )
        val diagnosis = AuthLogger.diagnoseAuthException(recaptchaException)

        assertEquals(FailureCategory.APP_CHECK_OR_RECAPTCHA, diagnosis.category)
        assertTrue(diagnosis.isAppCheckOrRecaptcha)
        assertFalse(diagnosis.isWrongPassword)
        assertFalse(diagnosis.isInvalidRequest)
        assertTrue(diagnosis.actionableResolution.contains("79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF"))
    }

    @Test
    fun identifiesAppCheckMalformedCredentialError() {
        val ex = FirebaseAuthInvalidCredentialsException(
            "ERROR_INVALID_CREDENTIAL",
            "The supplied auth credential is incorrect, malformed or has expired."
        )
        val diagnosis = AuthLogger.diagnoseAuthException(ex)

        assertEquals(FailureCategory.APP_CHECK_OR_RECAPTCHA, diagnosis.category)
        assertTrue(diagnosis.isAppCheckOrRecaptcha)
        assertFalse(diagnosis.isInvalidRequest)
        assertTrue(diagnosis.actionableResolution.contains("SHA-256"))
    }

    @Test
    fun diagnosesInvalidLoginCredentialsAsWrongPasswordWhenNotMalformed() {
        val modernAuthException = FirebaseAuthInvalidCredentialsException(
            "INVALID_LOGIN_CREDENTIALS",
            "The provided credentials are not valid for this user."
        )
        val diagnosis = AuthLogger.diagnoseAuthException(modernAuthException)

        assertEquals(FailureCategory.WRONG_PASSWORD, diagnosis.category)
        assertTrue(diagnosis.isWrongPassword)
        assertFalse(diagnosis.isInvalidRequest)
    }

    @Test
    fun diagnosesUserNotFoundCorrectly() {
        val userNotFoundException = FirebaseAuthInvalidUserException(
            "ERROR_USER_NOT_FOUND",
            "There is no user record corresponding to this identifier."
        )
        val diagnosis = AuthLogger.diagnoseAuthException(userNotFoundException)

        assertEquals(FailureCategory.USER_NOT_FOUND, diagnosis.category)
        assertFalse(diagnosis.isWrongPassword)
        assertFalse(diagnosis.isInvalidRequest)
    }

    @Test
    fun diagnosesRateLimiting() {
        val rateLimitException = FirebaseAuthException(
            "ERROR_TOO_MANY_REQUESTS",
            "We have blocked all requests from this device due to unusual activity."
        )
        val diagnosis = AuthLogger.diagnoseAuthException(rateLimitException)

        assertEquals(FailureCategory.TOO_MANY_REQUESTS, diagnosis.category)
    }

    @Test
    fun sanitizesEmailAndIdentifiersPreservingPrivacy() {
        assertEquals("p*****k@gmail.com", AuthLogger.sanitizeIdentifier("patrick@gmail.com"))
        assertEquals("a*@example.org", AuthLogger.sanitizeIdentifier("ab@example.org"))
        assertEquals("<empty>", AuthLogger.sanitizeIdentifier(null))
        assertEquals("<empty>", AuthLogger.sanitizeIdentifier("   "))
    }

    @Test
    fun logsLoginFailureAndRetainsLastDiagnosis() {
        val ex = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "Invalid password.")
        val diagnosis = AuthLogger.logLoginFailure("PASSWORD", "user@test.com", ex)

        assertEquals(diagnosis, AuthLogger.getLastDiagnosis())
        assertTrue(AuthLogger.getRecentEvents().isNotEmpty())
        assertEquals("LOGIN_FAILURE", AuthLogger.getRecentEvents().last().eventType)
    }

    @Test
    fun logsSignOutStateProperly() {
        AuthLogger.logAuthStateChanged(null)
        val lastEvent = AuthLogger.getRecentEvents().last()
        assertEquals("AUTH_STATE_SIGNED_OUT", lastEvent.eventType)
    }
}
