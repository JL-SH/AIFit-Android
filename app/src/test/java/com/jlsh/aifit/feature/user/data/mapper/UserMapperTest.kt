package com.jlsh.aifit.feature.user.data.mapper

import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toDomain
import com.jlsh.aifit.feature.user.data.mapper.UserMapper.toEntity
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.testutil.fakeUserProfile
import com.jlsh.aifit.testutil.fakeUserProfileEntity
import com.jlsh.aifit.testutil.fakeUserProfileResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserMapperTest {

    // ─── UserProfileResponseDto.toDomain() ────────────────────────────────────

    @Test
    fun `toDomain desde DTO mapea campos básicos correctamente`() {
        val dto = fakeUserProfileResponseDto(
            id = "abc",
            name = "Ana",
            email = "ana@aifit.com",
            authProvider = "LOCAL",
        )

        val domain = dto.toDomain()

        assertEquals("abc", domain.id)
        assertEquals("Ana", domain.name)
        assertEquals("ana@aifit.com", domain.email)
        assertEquals("LOCAL", domain.authProvider)
    }

    @Test
    fun `toDomain desde DTO convierte goalType string a enum correctamente`() {
        val dto = fakeUserProfileResponseDto(goalType = "LOSE_WEIGHT")

        val domain = dto.toDomain()

        assertEquals(GoalType.LOSE_WEIGHT, domain.goalType)
    }

    @Test
    fun `toDomain desde DTO convierte fitnessLevel string a enum correctamente`() {
        val dto = fakeUserProfileResponseDto(fitnessLevel = "ADVANCED")

        val domain = dto.toDomain()

        assertEquals(FitnessLevel.ADVANCED, domain.fitnessLevel)
    }

    @Test
    fun `toDomain desde DTO convierte activityLevel string a enum correctamente`() {
        val dto = fakeUserProfileResponseDto(activityLevel = "VERY_ACTIVE")

        val domain = dto.toDomain()

        assertEquals(ActivityLevel.VERY_ACTIVE, domain.activityLevel)
    }

    @Test
    fun `toDomain desde DTO convierte gender string a enum correctamente`() {
        val dto = fakeUserProfileResponseDto(gender = "FEMALE")

        val domain = dto.toDomain()

        assertEquals(Gender.FEMALE, domain.gender)
    }

    @Test
    fun `toDomain desde DTO con goalType desconocido retorna UNKNOWN sin crash`() {
        val dto = fakeUserProfileResponseDto(goalType = "NUEVO_OBJETIVO_BACKEND")

        val domain = dto.toDomain()

        assertEquals(GoalType.UNKNOWN, domain.goalType)
    }

    @Test
    fun `toDomain desde DTO con fitnessLevel desconocido retorna UNKNOWN sin crash`() {
        val dto = fakeUserProfileResponseDto(fitnessLevel = "ELITE")

        val domain = dto.toDomain()

        assertEquals(FitnessLevel.UNKNOWN, domain.fitnessLevel)
    }

    @Test
    fun `toDomain desde DTO con activityLevel desconocido retorna UNKNOWN sin crash`() {
        val dto = fakeUserProfileResponseDto(activityLevel = "EXTREME")

        val domain = dto.toDomain()

        assertEquals(ActivityLevel.UNKNOWN, domain.activityLevel)
    }

    @Test
    fun `toDomain desde DTO con goalType null devuelve UNKNOWN (fromString retorna UNKNOWN para null)`() {
        val dto = fakeUserProfileResponseDto(goalType = null)

        val domain = dto.toDomain()

        // GoalType.fromString(null) = UNKNOWN según el companion object
        assertEquals(GoalType.UNKNOWN, domain.goalType)
    }

    @Test
    fun `toDomain desde DTO con gender null devuelve UNKNOWN`() {
        val dto = fakeUserProfileResponseDto(gender = null)

        val domain = dto.toDomain()

        assertEquals(Gender.UNKNOWN, domain.gender)
    }

    @Test
    fun `toDomain desde DTO con birthDate null devuelve null`() {
        val dto = fakeUserProfileResponseDto(birthDate = null)

        val domain = dto.toDomain()

        // birthDate usa ?.let { LocalDate.parse(it) }, por tanto null se preserva
        assertNull(domain.birthDate)
    }

    @Test
    fun `toDomain desde DTO convierte birthDate string a LocalDate correctamente`() {
        val dto = fakeUserProfileResponseDto(birthDate = "1995-06-15")

        val domain = dto.toDomain()

        assertNotNull(domain.birthDate)
        assertEquals(1995, domain.birthDate?.year)
        assertEquals(6, domain.birthDate?.monthValue)
        assertEquals(15, domain.birthDate?.dayOfMonth)
    }

    @Test
    fun `toDomain desde DTO con birthDate inválido devuelve null sin crash`() {
        val dto = fakeUserProfileResponseDto(birthDate = "no-es-fecha")

        val domain = dto.toDomain()

        assertNull(domain.birthDate)
    }

    // ─── UserProfile.toEntity() ───────────────────────────────────────────────

    @Test
    fun `toEntity mapea id, name, email correctamente`() {
        val profile = fakeUserProfile(id = "user-5", name = "Carlos", email = "carlos@aifit.com")

        val entity = profile.toEntity()

        assertEquals("user-5", entity.id)
        assertEquals("Carlos", entity.name)
        assertEquals("carlos@aifit.com", entity.email)
    }

    @Test
    fun `toEntity mapea goalType como String`() {
        val profile = fakeUserProfile(goalType = GoalType.GAIN_MUSCLE)

        val entity = profile.toEntity()

        assertEquals("GAIN_MUSCLE", entity.goalType)
    }

    @Test
    fun `toEntity mapea fitnessLevel como String`() {
        val profile = fakeUserProfile(fitnessLevel = FitnessLevel.INTERMEDIATE)

        val entity = profile.toEntity()

        assertEquals("INTERMEDIATE", entity.fitnessLevel)
    }

    @Test
    fun `toEntity con goalType null guarda null en entity`() {
        val profile = fakeUserProfile(goalType = null)

        val entity = profile.toEntity()

        assertNull(entity.goalType)
    }

    // ─── UserProfileEntity.toDomain() ────────────────────────────────────────

    @Test
    fun `toDomain desde Entity mapea campos básicos correctamente`() {
        val entity = fakeUserProfileEntity(id = "ent-1", name = "Maria", email = "maria@aifit.com")

        val domain = entity.toDomain()

        assertEquals("ent-1", domain.id)
        assertEquals("Maria", domain.name)
        assertEquals("maria@aifit.com", domain.email)
    }

    @Test
    fun `toDomain desde Entity convierte goalType string a enum`() {
        val entity = fakeUserProfileEntity(goalType = "MAINTAIN")

        val domain = entity.toDomain()

        assertEquals(GoalType.MAINTAIN, domain.goalType)
    }

    @Test
    fun `toDomain desde Entity con goalType desconocido retorna UNKNOWN sin crash`() {
        val entity = fakeUserProfileEntity(goalType = "NUEVO_OBJETIVO")

        val domain = entity.toDomain()

        assertEquals(GoalType.UNKNOWN, domain.goalType)
    }

    @Test
    fun `toDomain desde Entity con fitnessLevel null devuelve null (usa nullable let)`() {
        val entity = fakeUserProfileEntity(fitnessLevel = null)

        val domain = entity.toDomain()

        // fitnessLevel?.let { ... } = null cuando fitnessLevel es null
        assertNull(domain.fitnessLevel)
    }
}



