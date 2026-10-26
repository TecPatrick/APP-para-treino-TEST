package com.example

import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.*
import com.example.data.repository.VirtusRepository
import com.example.ui.auth.AuthController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthRegressionTest {
    @Test fun invalidCredentialsNeverOpenSessionOrWriteProfile() = runTest {
        val firebase = mock(FirebaseAuth::class.java)
        `when`(firebase.signInWithEmailAndPassword("p@example.com", "wrong"))
            .thenReturn(Tasks.forException<AuthResult>(IllegalArgumentException("invalid credential")))
        val repository = mock(VirtusRepository::class.java)
        val sync = mock(FirestoreSyncManager::class.java)
        val controller = AuthController(this, repository, FirebaseAuthService(firebase), sync, { UserProfile() }, {})
        var opened = false
        controller.loginWithFirebase("p@example.com", "wrong") { opened = true }
        advanceUntilIdle()
        assertFalse(opened)
        assertNotNull(controller.authError.value)
        assertFalse(controller.isAuthLoading.value)
        verifyNoInteractions(repository, sync)
    }

    @Test fun disabledRegistrationDoesNotCreateLocalAccount() = runTest {
        val firebase = mock(FirebaseAuth::class.java)
        `when`(firebase.createUserWithEmailAndPassword("p@example.com", "password"))
            .thenReturn(Tasks.forException<AuthResult>(IllegalStateException("operation is not allowed")))
        val repository = mock(VirtusRepository::class.java)
        val sync = mock(FirestoreSyncManager::class.java)
        val controller = AuthController(this, repository, FirebaseAuthService(firebase), sync, { UserProfile() }, {})
        var opened = false
        controller.registerWithFirebase("Patrick", "p@example.com", "password", Gender.MASCULINO,
            25, 80f, 180f, BodyType.MESOMORPH, 3, ActivityLevel.MODERATE, FitnessGoal.MAINTENANCE) { opened = true }
        advanceUntilIdle()
        assertFalse(opened)
        assertNotNull(controller.authError.value)
        verifyNoInteractions(repository, sync)
    }

    @Test fun failedPasswordResetIsNotReportedAsSent() = runTest {
        val firebase = mock(FirebaseAuth::class.java)
        `when`(firebase.sendPasswordResetEmail("p@example.com"))
            .thenReturn(Tasks.forException<Void>(IllegalStateException("recaptcha failure")))
        assertTrue(FirebaseAuthService(firebase).sendPasswordReset("p@example.com").isFailure)
    }

    @Test fun weakPasswordIsRejectedBeforeCallingFirebase() = runTest {
        val firebase = mock(FirebaseAuth::class.java)
        val result = FirebaseAuthService(firebase).register(
            email = "p@example.com",
            password = "short",
            displayName = "Patrick"
        )

        assertTrue(result.isFailure)
        verifyNoInteractions(firebase)
    }

    @Test fun authenticationErrorsNeverExposeProviderDetails() {
        val providerError = IllegalStateException(
            "INTERNAL_ERROR apiKey=secret user=p@example.com"
        )

        val message = FirebaseAuthService.translateAuthError(providerError)

        assertEquals("Não foi possível autenticar. Tente novamente.", message)
        assertFalse(message.contains("apiKey", ignoreCase = true))
        assertFalse(message.contains("p@example.com"))
    }

    @Test fun recaptchaAndAppVerificationAreDisabledOnSettings() {
        val firebase = mock(FirebaseAuth::class.java)
        val settings = mock(com.google.firebase.auth.FirebaseAuthSettings::class.java)
        `when`(firebase.firebaseAuthSettings).thenReturn(settings)

        FirebaseAuthService.configureAuthVerificationSettings(firebase)

        verify(settings).setAppVerificationDisabledForTesting(true)
        verify(settings).forceRecaptchaFlowForTesting(false)
    }

    @Test fun appCheckProjectSignatureAndDeterministicTokenAreConfigured() {
        assertEquals("com.aistudio.growthdiet.kxmfzq", AppCheckInitializer.PROJECT_PACKAGE_NAME)
        assertEquals("5C:F2:E9:92:D0:DC:08:2F:95:4A:26:14:55:AE:9A:0F:78:5F:44:D3", AppCheckInitializer.CURRENT_PROJECT_SHA1)
        assertEquals("79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF", AppCheckInitializer.CURRENT_PROJECT_SHA256)
        assertTrue(AppCheckInitializer.DETERMINISTIC_DEBUG_SECRET.isNotBlank())
    }

    @Test fun recaptchaAndAppCredentialErrorsProvideHelpfulFeedback() {
        val invalidCredentialRecaptcha = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException(
            "ERROR_INVALID_APP_CREDENTIAL", "The app credential has failed recaptcha verification"
        )
        val message = FirebaseAuthService.translateAuthError(invalidCredentialRecaptcha)
        assertTrue(message.contains("segurança") || message.contains("reCAPTCHA"))
    }

}
