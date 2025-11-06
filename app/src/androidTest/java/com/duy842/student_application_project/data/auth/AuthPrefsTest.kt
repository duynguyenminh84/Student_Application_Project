package com.duy842.student_application_project.data.auth


import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthPrefsTest {

    @Test
    fun set_and_clear_current_user_roundtrip() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()

        // initially -1 (logged out)
        assertEquals(-1L, AuthPrefs.currentUserId(ctx).first())

        // set user id
        AuthPrefs.setCurrentUser(ctx, 42L)
        assertEquals(42L, AuthPrefs.currentUserId(ctx).first())

        // clear (logout)
        AuthPrefs.clear(ctx)
        assertEquals(-1L, AuthPrefs.currentUserId(ctx).first())
    }
}
