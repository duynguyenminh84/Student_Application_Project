package com.duy842.student_application_project.data.auth

class AuthRepository(private val userDao: UserDao) {

    /** Normalize email: trim spaces and lowercase for consistent lookup. */
    private fun normalizeEmail(raw: String): String =
        raw.trim().lowercase()

    /** Normalize password: trim only, do NOT lowercase (passwords are case-sensitive). */
    private fun normalizePassword(raw: String): String =
        raw.trim()

    suspend fun register(email: String, password: String): Result<Long> {
        val normalizedEmail = normalizeEmail(email)
        val normalizedPwd = normalizePassword(password)

        if (normalizedEmail.isBlank() || normalizedPwd.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Email and password are required")
            )
        }

        // Check if a user with this email already exists
        val existing = userDao.findByEmail(normalizedEmail)
        if (existing != null) {
            // STRICT behaviour: do NOT log them in, force them to use Login button
            return Result.failure(
                IllegalStateException("Email already in use")
            )
        }

        // Email is new → create a brand new user
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(normalizedPwd, salt)

        val id = userDao.insert(
            UserEntity(
                email = normalizedEmail,
                passwordHash = hash,
                salt = salt
            )
        )

        return Result.success(id)
    }



    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalizedEmail = normalizeEmail(email)
        val normalizedPwd = normalizePassword(password)

        // Look up user using the same normalized email as in register()
        val user = userDao.findByEmail(normalizedEmail)
            ?: return Result.failure(
                IllegalArgumentException("Invalid email or password")
            )

        // Verify password hash
        return if (PasswordHasher.verify(normalizedPwd, user.salt, user.passwordHash)) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Invalid email or password"))
        }
    }
}
