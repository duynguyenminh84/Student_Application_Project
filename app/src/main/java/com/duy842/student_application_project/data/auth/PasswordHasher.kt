package com.duy842.student_application_project.data.auth

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    private val rng = SecureRandom()

    fun newSalt(bytes: Int = 16): String {
        val arr = ByteArray(bytes)
        rng.nextBytes(arr)
        return arr.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest((password + saltHex).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
