package com.duy842.student_application_project.data.auth

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duy842.student_application_project.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue


@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // OK for tests
            .build()
        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_findByEmail_roundtrip() = runBlocking {
        val id = dao.insert(
            UserEntity(
                email = "a@b.com",
                passwordHash = "hash",
                salt = "salt"
            )
        )
        assertTrue(id > 0)

        val loaded = dao.findByEmail(email = "a@b.com")
        assertNotNull(loaded)
        assertEquals(id, loaded!!.id)
    }

    @Test
    fun count_reflects_number_of_rows() = runBlocking {
        assertEquals(0, dao.count())
        dao.insert(UserEntity(email = "x@y.com", passwordHash = "h1", salt = "s1"))
        dao.insert(UserEntity(email = "z@w.com", passwordHash = "h2", salt = "s2"))
        assertEquals(2, dao.count())
    }

    @Test
    fun insert_duplicate_exact_email_throws_constraint() = runBlocking {
        val e = "dup@test.com"
        val id1 = dao.insert(UserEntity(email = e, passwordHash = "h", salt = "s"))
        assertTrue(id1 > 0)

        // Second insert with the exact same email should violate UNIQUE index.
        val result = runCatching {
            dao.insert(UserEntity(email = e, passwordHash = "h2", salt = "s2"))
        }

        // We accept any failure type thrown by Room/SQLite for UNIQUE violation.
        assertTrue("Expected UNIQUE constraint failure", result.isFailure)
    }

}
