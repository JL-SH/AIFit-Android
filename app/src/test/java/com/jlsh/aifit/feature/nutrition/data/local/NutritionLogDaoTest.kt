package com.jlsh.aifit.feature.nutrition.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jlsh.aifit.core.local.AiFitDatabase
import com.jlsh.aifit.testutil.fakeNutritionLogEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NutritionLogDaoTest {

    private lateinit var db: AiFitDatabase
    private lateinit var dao: NutritionLogDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AiFitDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.nutritionLogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `upsert y getByDate retornan la misma entidad`() = runTest {
        val entity = fakeNutritionLogEntity()
        dao.upsert(entity)

        val result = dao.getByDate(entity.date)

        assertEquals(entity, result)
    }

    @Test
    fun `getByDate retorna null cuando no hay datos para la fecha`() = runTest {
        val result = dao.getByDate(99999L)

        assertNull(result)
    }

    @Test
    fun `upsert actualiza entidad existente con mismo id`() = runTest {
        val entity = fakeNutritionLogEntity()
        dao.upsert(entity)

        val updated = entity.copy(totalCalories = 2500)
        dao.upsert(updated)

        val result = dao.getByDate(entity.date)
        assertEquals(2500, result?.totalCalories)
    }

    @Test
    fun `deleteByDate elimina la entidad y getByDate retorna null`() = runTest {
        val entity = fakeNutritionLogEntity()
        dao.upsert(entity)
        dao.deleteByDate(entity.date)

        val result = dao.getByDate(entity.date)

        assertNull(result)
    }

    @Test
    fun `deleteByDate no afecta a entidades de otras fechas`() = runTest {
        val entity1 = fakeNutritionLogEntity(id = "log-1", date = 100L)
        val entity2 = fakeNutritionLogEntity(id = "log-2", date = 200L)
        dao.upsert(entity1)
        dao.upsert(entity2)

        dao.deleteByDate(100L)

        assertNull(dao.getByDate(100L))
        assertNotNull(dao.getByDate(200L))
    }
}

