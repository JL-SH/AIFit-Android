package com.jlsh.aifit.feature.user.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.testutil.fakeUserProfileEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserProfileDaoTest {

    private lateinit var db: AiFitDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AiFitDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.userProfileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ─── upsert + getById ─────────────────────────────────────────────────────

    @Test
    fun `upsert y getById retornan la misma entidad`() = runTest {
        val entity = fakeUserProfileEntity(id = "user-1")

        dao.upsert(entity)
        val result = dao.getById("user-1")

        assertNotNull(result)
        assertEquals(entity, result)
    }

    @Test
    fun `getById con id inexistente retorna null`() = runTest {
        val result = dao.getById("no-existe")

        assertNull(result)
    }

    @Test
    fun `upsert actualiza entidad existente con mismo id`() = runTest {
        val original = fakeUserProfileEntity(id = "me", name = "Original")
        val updated = fakeUserProfileEntity(id = "me", name = "Actualizado")

        dao.upsert(original)
        dao.upsert(updated)

        val result = dao.getById("me")
        assertEquals("Actualizado", result?.name)
    }

    @Test
    fun `upsert preserva goalType y fitnessLevel correctamente`() = runTest {
        val entity = fakeUserProfileEntity(
            id = "me",
            goalType = "GAIN_MUSCLE",
            fitnessLevel = "ADVANCED",
        )

        dao.upsert(entity)
        val result = dao.getById("me")

        assertEquals("GAIN_MUSCLE", result?.goalType)
        assertEquals("ADVANCED", result?.fitnessLevel)
    }

    @Test
    fun `upsert con campos opcionales null los guarda correctamente`() = runTest {
        val entity = fakeUserProfileEntity(id = "me", goalType = null, fitnessLevel = null)

        dao.upsert(entity)
        val result = dao.getById("me")

        assertNull(result?.goalType)
        assertNull(result?.fitnessLevel)
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    fun `delete elimina la entidad y getById retorna null`() = runTest {
        val entity = fakeUserProfileEntity(id = "user-to-delete")
        dao.upsert(entity)

        dao.delete("user-to-delete")
        val result = dao.getById("user-to-delete")

        assertNull(result)
    }

    @Test
    fun `delete con id inexistente no lanza excepción`() = runTest {
        // Should not throw exception
        dao.delete("id-que-no-existe")
    }

    @Test
    fun `delete solo elimina la entidad con el id especificado`() = runTest {
        val entity1 = fakeUserProfileEntity(id = "user-1")
        val entity2 = fakeUserProfileEntity(id = "user-2")
        dao.upsert(entity1)
        dao.upsert(entity2)

        dao.delete("user-1")

        assertNull(dao.getById("user-1"))
        assertNotNull(dao.getById("user-2"))
    }
}

