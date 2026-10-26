package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.VirtusRepository
import com.example.ui.auth.AuthController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthRegressionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: VirtusRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VirtusRepository(db.appDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun enterAsGuestSetsLocalUserSession() = runBlocking {
        val firebase = mock(FirebaseAuth::class.java)
        val sync = mock(FirestoreSyncManager::class.java)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        var profile = UserProfile()
        val controller = AuthController(scope, repository, FirebaseAuthService(firebase), sync, { profile }, {})

        var opened = false
        controller.enterAsGuest { opened = true }
        var count = 0
        while (!opened && count < 100) {
            Thread.sleep(20)
            count++
        }

        assertTrue(opened)
        val saved = repository.getUserProfileDirect(1)
        assertNotNull(saved)
        assertTrue(saved!!.isLoggedIn)
        assertEquals("guest_local", saved.firebaseUid)
    }

    @Test
    fun invalidGoogleCredentialFailsGracefully() = runBlocking {
        val firebase = mock(FirebaseAuth::class.java)
        val credential = mock(AuthCredential::class.java)
        `when`(firebase.signInWithCredential(credential))
            .thenReturn(Tasks.forException<AuthResult>(IllegalArgumentException("malformed credential")))

        val sync = mock(FirestoreSyncManager::class.java)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val controller = AuthController(scope, repository, FirebaseAuthService(firebase), sync, { UserProfile() }, {})

        var opened = false
        controller.loginWithGoogleCredential(credential) { opened = true }
        var count = 0
        while (controller.isAuthLoading.value && count < 100) {
            Thread.sleep(20)
            count++
        }

        assertFalse(opened)
        assertNotNull(controller.authError.value)
        assertFalse(controller.isAuthLoading.value)
    }

    @Test
    fun biometricLoginRejectsGuestAccount() = runBlocking {
        val firebase = mock(FirebaseAuth::class.java)
        val sync = mock(FirestoreSyncManager::class.java)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val guestProfile = UserProfile(firebaseUid = "guest_local", isLoggedIn = false)
        val controller = AuthController(scope, repository, FirebaseAuthService(firebase), sync, { guestProfile }, {})

        var opened = false
        controller.loginWithBiometrics { opened = true }
        var count = 0
        while (controller.isAuthLoading.value && count < 100) {
            Thread.sleep(20)
            count++
        }

        assertFalse(opened)
        assertNotNull(controller.authError.value)
    }

    @Test
    fun biometricLoginSucceedsForConfiguredProfile() = runBlocking {
        val firebase = mock(FirebaseAuth::class.java)
        val sync = mock(FirestoreSyncManager::class.java)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val validProfile = UserProfile(
            id = 1,
            firebaseUid = "google_user_123",
            name = "Carlos Atleta",
            email = "carlos@example.com",
            hasCompletedOnboarding = true,
            weightKg = 75f,
            isLoggedIn = false
        )
        repository.saveUserProfile(validProfile)

        val controller = AuthController(scope, repository, FirebaseAuthService(firebase), sync, { validProfile }, {})

        var opened = false
        controller.loginWithBiometrics { opened = true }
        var count = 0
        while (!opened && count < 100) {
            Thread.sleep(20)
            count++
        }

        assertTrue("Expected biometric login to succeed, but authError was: ${controller.authError.value}", opened)
        val saved = repository.getUserProfileDirect(1)
        assertNotNull(saved)
        assertTrue(saved!!.isLoggedIn)
    }

    @Test
    fun authenticationErrorsNeverExposeProviderDetails() {
        val providerError = IllegalStateException(
            "INTERNAL_ERROR apiKey=secret user=p@example.com"
        )

        val message = FirebaseAuthService.translateAuthError(providerError)

        assertFalse(message.contains("apiKey", ignoreCase = true))
        assertFalse(message.contains("p@example.com"))
    }

    @Test
    fun appCheckDeterministicSecretIsConfigured() {
        assertTrue(AppCheckInitializer.DETERMINISTIC_DEBUG_SECRET.isNotBlank())
    }
}
