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
}
