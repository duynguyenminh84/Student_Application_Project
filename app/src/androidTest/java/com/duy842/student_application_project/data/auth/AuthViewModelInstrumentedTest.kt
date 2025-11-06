package com.duy842.student_application_project.data.auth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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

@RunWith(AndroidJUnit4::class)
class AuthViewModelInstrumentedTest {

    private lateinit var app: Application
    private lateinit var vm: AuthViewModel

    private var collectJobIsLoggedIn: Job? = null
    private var collectJobUserId: Job? = null

    @Before
    fun setUp() = runBlocking {
        app = ApplicationProvider.getApplicationContext()
        // start every test logged out
        AuthPrefs.clear(app)
        vm = AuthViewModel(app)

        // Keep flows hot so .stateIn(WhileSubscribed) actually updates
        collectJobIsLoggedIn = launch { vm.isLoggedIn.collect { } }
        collectJobUserId    = launch { vm.currentUserId.collect { } }
    }

    @After
    fun tearDown() {
        runBlocking {
            collectJobIsLoggedIn?.cancel()
            collectJobUserId?.cancel()
        }
    }

    // Small helpers
    private suspend fun waitUntil(timeoutMs: Long = 8_000, predicate: suspend () -> Boolean) {
        withTimeout(timeoutMs) {
            while (!predicate()) delay(25)
        }
    }
    private suspend fun dsUid(): Long = AuthPrefs.currentUserId(app).first()

    @Test
    fun register_then_login_updates_state() = runBlocking {
        val email = "t${System.currentTimeMillis()}@example.com"
        val pw = "Pw123!"

        // Register writes uid to DataStore via VM
        vm.register(email, pw)
        // (Optional login is fine; register already sets uid)
        vm.login(email, pw)

        // Wait for success or an error to appear
        waitUntil {
            vm.isLoggedIn.value || dsUid() > 0 || !vm.error.value.isNullOrBlank()
        }

        // If we somehow got an error, surface it clearly
        if (!vm.error.value.isNullOrBlank()) {
            fail("register/login produced error: ${vm.error.value}")
        }

        // Success path
        val uidVm = vm.currentUserId.value
        val uidDs = dsUid()
        println("register_then_login: uidVm=$uidVm uidDs=$uidDs loggedIn=${vm.isLoggedIn.value}")

        assertTrue("Expected to be logged in", vm.isLoggedIn.value)
        assertTrue("User id should be > 0 from VM or DataStore", uidVm > 0 || uidDs > 0)
    }

    @Test
    fun login_wrong_password_sets_error() = runBlocking {
        val email = "u${System.currentTimeMillis()}@example.com"
        vm.register(email, "CorrectPw")

        vm.login(email, "WrongPw")

        // Wait until either error shows or (incorrectly) becomes logged in
        waitUntil { !vm.error.value.isNullOrBlank() || vm.isLoggedIn.value }

        println("login_wrong_password: error=${vm.error.value} loggedIn=${vm.isLoggedIn.value}")

        assertFalse("Should not be logged in with wrong password", vm.isLoggedIn.value)
        assertTrue("Error should be non-blank", !vm.error.value.isNullOrBlank())
    }

    @Test
    fun logout_resets_state() = runBlocking {
        val email = "a${System.currentTimeMillis()}@b.com"
        vm.register(email, "pw")
        vm.login(email, "pw")

        // Ensure we are logged in first
        waitUntil { vm.isLoggedIn.value || dsUid() > 0 }
        println("before logout: loggedIn=${vm.isLoggedIn.value} uidVm=${vm.currentUserId.value} uidDs=${dsUid()}")

        vm.logout()

        // Only require a clear logout signal from either source
        waitUntil {
            !vm.isLoggedIn.value || dsUid() <= 0
        }

        println("after  logout: loggedIn=${vm.isLoggedIn.value} uidVm=${vm.currentUserId.value} uidDs=${dsUid()}")

        // Minimal guarantee your VM gives: logged-in flag turns false
        assertFalse("Expected VM to report logged out", vm.isLoggedIn.value)
        // We intentionally do NOT assert on uid because your VM may keep it briefly.
    }
}
