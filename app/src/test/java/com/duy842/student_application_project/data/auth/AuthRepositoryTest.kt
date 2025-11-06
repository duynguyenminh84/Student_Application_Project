package com.duy842.student_application_project.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun register_success_inserts_and_returns_id() = runTest {
        val dao = FakeUserDao()
        val repo = AuthRepository(dao)

        val result = repo.register("User@Email.com", "Pw123!")
        assertTrue(result.isSuccess)

        val id = result.getOrThrow()
        assertTrue("id should be > 0", id > 0)

        // Count should be 1 and the stored hash should verify with the same salt
        assertEquals(1, dao.count())
        val stored = dao.findByEmail("user@email.com")!!
        assertTrue(PasswordHasher.verify("Pw123!", stored.salt, stored.passwordHash))
    }

    @Test
    fun register_fails_when_email_exists_case_insensitive() = runTest {
        val dao = FakeUserDao()
        val repo = AuthRepository(dao)

        val r1 = repo.register("A@B.Com", "x")
        assertTrue(r1.isSuccess)

        val r2 = repo.register("a@b.com", "y")
        assertTrue("Should fail duplicate email", r2.isFailure)
        assertEquals(1, dao.count())
    }

    @Test
    fun register_fails_when_email_or_password_blank() = runTest {
        val repo = AuthRepository(FakeUserDao())

        val r1 = repo.register("", "x")
        val r2 = repo.register("   ", "x")
        val r3 = repo.register("a@b.com", "")

        assertTrue(r1.isFailure)
        assertTrue(r2.isFailure)
        assertTrue(r3.isFailure)
    }

    @Test
    fun login_success_returns_user_entity() = runTest {
        val dao = FakeUserDao()
        val repo = AuthRepository(dao)

        // prepare user
        val id = repo.register("x@y.com", "Secret123").getOrThrow()

        val loggedIn = repo.login("x@y.com", "Secret123")
        assertTrue(loggedIn.isSuccess)

        val user = loggedIn.getOrThrow()
        assertEquals(id, user.id)
        assertEquals("x@y.com", user.email) // normalized and stored
        assertTrue(PasswordHasher.verify("Secret123", user.salt, user.passwordHash))
    }

    @Test
    fun login_fails_with_wrong_password() = runTest {
        val dao = FakeUserDao()
        val repo = AuthRepository(dao)

        repo.register("x@y.com", "Correct").getOrThrow()
        val res = repo.login("x@y.com", "Wrong")
        assertTrue(res.isFailure)
    }

    @Test
    fun login_fails_with_unknown_email() = runTest {
        val repo = AuthRepository(FakeUserDao())
        val res = repo.login("no@user.com", "pw")
        assertTrue(res.isFailure)
    }

    @Test
    fun login_normalizes_email_case() = runTest {
        val dao = FakeUserDao()
        val repo = AuthRepository(dao)

        repo.register("Case@Email.Com", "pw").getOrThrow()
        val res = repo.login("case@email.com", "pw")
        assertTrue(res.isSuccess)
    }
}
