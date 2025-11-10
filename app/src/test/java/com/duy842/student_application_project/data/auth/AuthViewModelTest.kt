package com.duy842.student_application_project.data.auth

import com.duy842.student_application_project.ui.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestWatcher




/* ---------- Test Main dispatcher rule (so viewModelScope works in unit tests) ---------- */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    // Unconfined runs tasks immediately, so viewModelScope work completes without extra “advance”
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: org.junit.runner.Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: org.junit.runner.Description) {
        Dispatchers.resetMain()
    }
}

/* ---------- Fakes for fast, deterministic tests ---------- */

// Local fake session store (fine to keep this one here)
private class FakeSession : AuthSessionStore {
    private val _id = MutableStateFlow(-1L)
    override val currentUserId = _id
    override suspend fun setCurrentUser(id: Long) { _id.value = id }
    override suspend fun clear() { _id.value = -1L }
}

/* ---------- Tests ---------- */

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: AuthRepository
    private lateinit var session: FakeSession
    private lateinit var vm: AuthViewModel

    @Before
    fun setUp() {
        // Use the SHARED FakeUserDao from app/src/test/.../FakeUserDao.kt
        repo = AuthRepository(FakeUserDao())
        session = FakeSession()
        vm = AuthViewModel(repo, session)
    }

    @Test
    fun register_then_login_sets_logged_in_and_clears_error() = runTest {
        val email = "t${System.currentTimeMillis()}@example.com"

        vm.register(email, "pw123")
        advanceUntilIdle()

        vm.login(email, "pw123")
        advanceUntilIdle()

        assertTrue(vm.isLoggedIn.value)
        assertTrue(vm.currentUserId.value > 0)
        assertTrue(vm.error.value.isNullOrBlank())
    }

    @Test
    fun wrong_password_sets_error_and_keeps_logged_out() = runTest {
        val email = "u${System.currentTimeMillis()}@example.com"

        // Registration auto-logs in (by design)
        vm.register(email, "CorrectPw")
        advanceUntilIdle()

        // Ensure clean state before testing wrong-password path
        vm.logout()
        advanceUntilIdle()
        assertFalse(vm.isLoggedIn.value)

        // Now attempt wrong password
        vm.login(email, "WrongPw")
        advanceUntilIdle()

        // Expect: still logged out + error set
        assertFalse(vm.isLoggedIn.value)
        assertTrue(vm.currentUserId.value <= 0)
        assertFalse(vm.error.value.isNullOrBlank())
    }

    @Test
    fun logout_clears_session() = runTest {
        val email = "a${System.currentTimeMillis()}@b.com"

        vm.register(email, "pw")
        advanceUntilIdle()
        vm.login(email, "pw")
        advanceUntilIdle()
        assertTrue(vm.isLoggedIn.value)

        vm.logout()
        advanceUntilIdle()

        assertFalse(vm.isLoggedIn.value)
        assertTrue(vm.currentUserId.value <= 0)
    }
}
