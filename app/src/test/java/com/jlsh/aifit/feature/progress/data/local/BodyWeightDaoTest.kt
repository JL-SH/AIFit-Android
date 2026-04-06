package com.jlsh.aifit.feature.progress.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.testutil.fakeBodyWeightEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BodyWeightDaoTest {

    private lateinit var db: AiFitDatabase
    private lateinit var dao: BodyWeightDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AiFitDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.bodyWeightDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsert y getAll retornan la entidad insertada`() = runTest {
        val entity = fakeBodyWeightEntity()
        dao.upsert(entity)

        val result = dao.getAll()

        assertEquals(1, result.size)
        assertEquals(entity, result.first())
    }

    @Test
    fun `getAll retorna lista vacía cuando no hay datos`() = runTest {
        val result = dao.getAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getByDateRange retorna solo entidades dentro del rango`() = runTest {
        val e1 = fakeBodyWeightEntity(id = "bw-1", date = 100L)
        val e2 = fakeBodyWeightEntity(id = "bw-2", date = 200L)
        val e3 = fakeBodyWeightEntity(id = "bw-3", date = 300L)
        dao.upsertAll(listOf(e1, e2, e3))

        val result = dao.getByDateRange(100L, 200L)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "bw-1" })
        assertTrue(result.any { it.id == "bw-2" })
    }

    @Test
    fun `getByDateRange retorna lista vacía si no hay datos en el rango`() = runTest {
        val entity = fakeBodyWeightEntity(date = 50L)
        dao.upsert(entity)

        val result = dao.getByDateRange(100L, 200L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLatest retorna la entidad con fecha más reciente`() = runTest {
        val older = fakeBodyWeightEntity(id = "bw-old", date = 100L)
        val newer = fakeBodyWeightEntity(id = "bw-new", date = 200L)
        dao.upsertAll(listOf(older, newer))

        val result = dao.getLatest()

        assertNotNull(result)
        assertEquals("bw-new", result!!.id)
    }

    @Test
    fun `getLatest retorna null cuando la tabla está vacía`() = runTest {
        val result = dao.getLatest()

        assertNull(result)
    }

    @Test
    fun `upsert actualiza entidad existente con mismo id`() = runTest {
        val entity = fakeBodyWeightEntity()
        dao.upsert(entity)

        val updated = entity.copy(weight = 80.0)
        dao.upsert(updated)

        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals(80.0, result.first().weight, 0.01)
    }

    @Test
    fun `upsertAll inserta múltiples entidades`() = runTest {
        val entities = listOf(
            fakeBodyWeightEntity(id = "bw-1"),
            fakeBodyWeightEntity(id = "bw-2"),
            fakeBodyWeightEntity(id = "bw-3"),
        )
        dao.upsertAll(entities)

        val result = dao.getAll()
        assertEquals(3, result.size)
    }
}

