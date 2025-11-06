package com.duy842.student_application_project.data.auth

import org.junit.Assert
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun newSalt_has_expected_length_and_varies() {
        val s1 = PasswordHasher.newSalt()          // default 16 bytes -> 32 hex chars
        val s2 = PasswordHasher.newSalt()
        Assert.assertEquals(32, s1.length)
        Assert.assertEquals(32, s2.length)
        Assert.assertNotEquals("Salt should be random", s1, s2)
        Assert.assertTrue(s1.matches(Regex("[0-9a-f]+")))
        Assert.assertTrue(s2.matches(Regex("[0-9a-f]+")))
    }

    @Test
    fun hash_then_verify_true_with_same_salt() {
        val raw = "Secret123!"
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(raw, salt)
        Assert.assertTrue(PasswordHasher.verify(raw, salt, hash))
    }

    @Test
    fun verify_fails_with_wrong_password() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("CorrectPw", salt)
        Assert.assertFalse(PasswordHasher.verify("WrongPw", salt, hash))
    }

    @Test
    fun verify_fails_with_wrong_salt() {
        val raw = "Secret123!"
        val salt1 = PasswordHasher.newSalt()
        val salt2 = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(raw, salt1)
        Assert.assertFalse(PasswordHasher.verify(raw, salt2, hash))
    }

    @Test
    fun hash_output_is_not_plaintext() {
        val pw = "Secret123!"
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(pw, salt)
        Assert.assertNotEquals(pw, hash)
        Assert.assertTrue(hash.matches(Regex("[0-9a-f]+")))
        Assert.assertEquals(64, hash.length) // SHA-256 hex
    }
}