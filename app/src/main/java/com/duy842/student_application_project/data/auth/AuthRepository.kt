package com.duy842.student_application_project.data.auth

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(email: String, password: String): Result<Long> {
        val normalized = email.trim().lowercase()
        val pwd = password.trim() // <-- trim password to avoid invisible spaces issues

        if (normalized.isBlank() || pwd.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password are required"))
        }

        val existing = userDao.findByEmail(normalized)
        if (existing != null) return Result.failure(IllegalStateException("Email already in use"))

        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(pwd, salt) // <-- use trimmed password
        val id = userDao.insert(
            UserEntity(email = normalized, passwordHash = hash, salt = salt)
        )
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalized = email.trim().lowercase()
        val pwd = password.trim() // <-- trim password here too

        val user = userDao.findByEmail(normalized)
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))

        return if (PasswordHasher.verify(pwd, user.salt, user.passwordHash)) // <-- verify with trimmed pwd
            Result.success(user)
        else
            Result.failure(IllegalArgumentException("Invalid email or password"))
    }
}
