package com.jlsh.aifit.feature.training.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.testutil.FAKE_USER_ID
import com.jlsh.aifit.testutil.fakeTrainingPlanEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrainingPlanDaoTest {

    private lateinit var db: AiFitDatabase
    private lateinit var dao: TrainingPlanDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AiFitDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.trainingPlanDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsertAll y getAllByUserId retornan las entidades`() = runTest {
        val entity = fakeTrainingPlanEntity(id = "p-1")
        dao.upsertAll(listOf(entity))

        val result = dao.getAllByUserId(FAKE_USER_ID)

        assertEquals(1, result.size)
        assertEquals("p-1", result[0].id)
    }

    @Test
    fun `getById retorna la entidad correcta`() = runTest {
        val entity = fakeTrainingPlanEntity(id = "p-1")
        dao.upsertAll(listOf(entity))

        val result = dao.getById("p-1")

        assertNotNull(result)
        assertEquals("p-1", result!!.id)
        assertEquals("Test Plan", result.name)
    }

    @Test
    fun `getById retorna null cuando no existe`() = runTest {
        val result = dao.getById("nonexistent")

        assertNull(result)
    }

    @Test
    fun `deleteById elimina la entidad`() = runTest {
        val entity = fakeTrainingPlanEntity(id = "p-1")
        dao.upsertAll(listOf(entity))

        dao.deleteById("p-1")

        val result = dao.getById("p-1")
        assertNull(result)
    }

    @Test
    fun `upsertAll actualiza entidad existente con mismo id`() = runTest {
        val original = fakeTrainingPlanEntity(id = "p-1", name = "Original")
        dao.upsertAll(listOf(original))

        val updated = fakeTrainingPlanEntity(id = "p-1", name = "Updated")
        dao.upsertAll(listOf(updated))

        val result = dao.getById("p-1")
        assertNotNull(result)
        assertEquals("Updated", result!!.name)
    }

    @Test
    fun `deleteAllNotInIds elimina planes que no están en la lista`() = runTest {
        dao.upsertAll(listOf(
            fakeTrainingPlanEntity(id = "p-1"),
            fakeTrainingPlanEntity(id = "p-2"),
            fakeTrainingPlanEntity(id = "p-3"),
        ))

        dao.deleteAllNotInIds(FAKE_USER_ID, listOf("p-1", "p-3"))

        val result = dao.getAllByUserId(FAKE_USER_ID)
        assertEquals(2, result.size)
        assertTrue(result.map { it.id }.containsAll(listOf("p-1", "p-3")))
    }

    @Test
    fun `deleteAll elimina todas las entidades`() = runTest {
        dao.upsertAll(listOf(
            fakeTrainingPlanEntity(id = "p-1"),
            fakeTrainingPlanEntity(id = "p-2"),
        ))

        dao.deleteAll()

        val result = dao.getAllByUserId(FAKE_USER_ID)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllByUserId solo retorna planes del usuario especificado`() = runTest {
        dao.upsertAll(listOf(
            fakeTrainingPlanEntity(id = "p-1", userId = "user-A"),
            fakeTrainingPlanEntity(id = "p-2", userId = "user-B"),
        ))

        val result = dao.getAllByUserId("user-A")

        assertEquals(1, result.size)
        assertEquals("p-1", result[0].id)
    }
}

