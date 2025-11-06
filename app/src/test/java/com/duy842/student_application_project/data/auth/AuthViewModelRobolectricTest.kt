package com.duy842.student_application_project.data.auth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.duy842.student_application_project.ui.auth.AuthViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], manifest = Config.NONE)
class AuthViewModelRobolectricTest {

    private lateinit var context: Application
    private lateinit var vm: AuthViewModel

    private var collectIsLoggedIn: Job? = null
    private var collectUserId: Job? = null

    @Before
    fun setUp() {
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            AuthPrefs.clear(context)
            vm = AuthViewModel(context)
            // Keep StateFlows hot (VM uses WhileSubscribed)
            collectIsLoggedIn = launch { vm.isLoggedIn.collect { } }
            collectUserId     = launch { vm.currentUserId.collect { } }
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            collectIsLoggedIn?.cancel()
            collectUserId?.cancel()
        }
    }

    private suspend fun waitUntil(timeoutMs: Long = 5_000, block: suspend () -> Boolean) {
        withTimeout(timeoutMs) { while (!block()) delay(20) }
    }

    private suspend fun dsUid(): Long = AuthPrefs.currentUserId(context).first()

    @Test
    fun register_then_login_updates_state() = runBlocking {
        val email = "t${System.currentTimeMillis()}@example.com"
        val pw = "Pw123!"
        vm.register(email, pw)
        vm.login(email, pw)
        waitUntil { vm.isLoggedIn.value || dsUid() > 0 || !vm.error.value.isNullOrBlank() }
        assertTrue("Unexpected error: ${vm.error.value}", vm.error.value.isNullOrBlank())
        assertTrue("Should be logged in", vm.isLoggedIn.value)
        assertTrue("UID must be > 0 (VM or DS)", vm.currentUserId.value > 0 || dsUid() > 0)
    }

    @Test
    fun login_wrong_password_sets_error() = runBlocking {
        val email = "u${System.currentTimeMillis()}@example.com"
        vm.register(email, "CorrectPw")
        vm.login(email, "WrongPw")
        waitUntil { !vm.error.value.isNullOrBlank() || vm.isLoggedIn.value }
        assertFalse(vm.isLoggedIn.value)
        assertTrue("Expected error message", !vm.error.value.isNullOrBlank())
    }


    @Test
    fun logout_resets_state() = runBlocking {
        val email = "a${System.currentTimeMillis()}@b.com"
        vm.register(email, "pw")
        vm.login(email, "pw")
        waitUntil { vm.isLoggedIn.value || dsUid() > 0 }
        vm.logout()
        waitUntil { !vm.isLoggedIn.value || dsUid() <= 0 }
        assertFalse("VM should report logged out", vm.isLoggedIn.value)
    }
}
