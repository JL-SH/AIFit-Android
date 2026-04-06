package com.jlsh.aifit.feature.diet.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.testutil.FAKE_USER_ID
import com.jlsh.aifit.testutil.fakeDietPlanEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DietPlanDaoTest {

    private lateinit var db: AiFitDatabase
    private lateinit var dao: DietPlanDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AiFitDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.dietPlanDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertAll y getAllByUserId retornan las entidades`() = runTest {
        val entity = fakeDietPlanEntity(id = "dp-1")
        dao.upsertAll(listOf(entity))

        val result = dao.getAllByUserId(FAKE_USER_ID)

        assertEquals(1, result.size)
        assertEquals("dp-1", result[0].id)
    }

    @Test
    fun `getById retorna la entidad correcta`() = runTest {
        val entity = fakeDietPlanEntity(id = "dp-1")
        dao.upsertAll(listOf(entity))

        val result = dao.getById("dp-1")

        assertNotNull(result)
        assertEquals("dp-1", result!!.id)
        assertEquals("Test Diet", result.name)
    }

    @Test
    fun `getById retorna null cuando no existe`() = runTest {
        val result = dao.getById("nonexistent")

        assertNull(result)
    }

    @Test
    fun `deleteById elimina la entidad`() = runTest {
        val entity = fakeDietPlanEntity(id = "dp-1")
        dao.upsertAll(listOf(entity))

        dao.deleteById("dp-1")

        val result = dao.getById("dp-1")
        assertNull(result)
    }

    @Test
    fun `upsertAll actualiza entidad existente con mismo id`() = runTest {
        val original = fakeDietPlanEntity(id = "dp-1", name = "Original")
        dao.upsertAll(listOf(original))

        val updated = fakeDietPlanEntity(id = "dp-1", name = "Updated")
        dao.upsertAll(listOf(updated))

        val result = dao.getById("dp-1")
        assertNotNull(result)
        assertEquals("Updated", result!!.name)
    }

    @Test
    fun `getAllByUserId solo retorna planes del usuario especificado`() = runTest {
        dao.upsertAll(listOf(
            fakeDietPlanEntity(id = "dp-1", userId = "user-A"),
            fakeDietPlanEntity(id = "dp-2", userId = "user-B"),
        ))

        val result = dao.getAllByUserId("user-A")

        assertEquals(1, result.size)
        assertEquals("dp-1", result[0].id)
    }

    @Test
    fun `deleteAllByUserId elimina todos los planes del usuario`() = runTest {
        dao.upsertAll(listOf(
            fakeDietPlanEntity(id = "dp-1", userId = "user-A"),
            fakeDietPlanEntity(id = "dp-2", userId = "user-A"),
            fakeDietPlanEntity(id = "dp-3", userId = "user-B"),
        ))

        dao.deleteAllByUserId("user-A")

        val resultA = dao.getAllByUserId("user-A")
        val resultB = dao.getAllByUserId("user-B")
        assertTrue(resultA.isEmpty())
        assertEquals(1, resultB.size)
    }
}

