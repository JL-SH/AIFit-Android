package com.jlsh.aifit.feature.user.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserEnumsTest {

    // ─── GoalType ─────────────────────────────────────────────────────────────

    @Test
    fun `GoalType fromString con valor conocido retorna el enum correcto`() {
        assertEquals(GoalType.LOSE_WEIGHT, GoalType.fromString("LOSE_WEIGHT"))
        assertEquals(GoalType.GAIN_MUSCLE, GoalType.fromString("GAIN_MUSCLE"))
        assertEquals(GoalType.MAINTAIN, GoalType.fromString("MAINTAIN"))
        assertEquals(GoalType.BODY_RECOMPOSITION, GoalType.fromString("BODY_RECOMPOSITION"))
    }

    @Test
    fun `GoalType fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(GoalType.UNKNOWN, GoalType.fromString("VALOR_INEXISTENTE_BACKEND"))
    }

    @Test
    fun `GoalType fromString con null retorna UNKNOWN`() {
        assertEquals(GoalType.UNKNOWN, GoalType.fromString(null))
    }

    // ─── FitnessLevel ─────────────────────────────────────────────────────────

    @Test
    fun `FitnessLevel fromString con valor conocido retorna el enum correcto`() {
        assertEquals(FitnessLevel.BEGINNER, FitnessLevel.fromString("BEGINNER"))
        assertEquals(FitnessLevel.INTERMEDIATE, FitnessLevel.fromString("INTERMEDIATE"))
        assertEquals(FitnessLevel.ADVANCED, FitnessLevel.fromString("ADVANCED"))
    }

    @Test
    fun `FitnessLevel fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(FitnessLevel.UNKNOWN, FitnessLevel.fromString("PRO_GAMER"))
    }

    @Test
    fun `FitnessLevel fromString con null retorna UNKNOWN`() {
        assertEquals(FitnessLevel.UNKNOWN, FitnessLevel.fromString(null))
    }

    // ─── ActivityLevel ────────────────────────────────────────────────────────

    @Test
    fun `ActivityLevel fromString con valor conocido retorna el enum correcto`() {
        assertEquals(ActivityLevel.SEDENTARY, ActivityLevel.fromString("SEDENTARY"))
        assertEquals(ActivityLevel.LIGHT, ActivityLevel.fromString("LIGHT"))
        assertEquals(ActivityLevel.MODERATE, ActivityLevel.fromString("MODERATE"))
        assertEquals(ActivityLevel.ACTIVE, ActivityLevel.fromString("ACTIVE"))
        assertEquals(ActivityLevel.VERY_ACTIVE, ActivityLevel.fromString("VERY_ACTIVE"))
    }

    @Test
    fun `ActivityLevel fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(ActivityLevel.UNKNOWN, ActivityLevel.fromString("SUPER_ACTIVE"))
    }

    @Test
    fun `ActivityLevel fromString con null retorna UNKNOWN`() {
        assertEquals(ActivityLevel.UNKNOWN, ActivityLevel.fromString(null))
    }

    // ─── Gender ───────────────────────────────────────────────────────────────

    @Test
    fun `Gender fromString con valor conocido retorna el enum correcto`() {
        assertEquals(Gender.MALE, Gender.fromString("MALE"))
        assertEquals(Gender.FEMALE, Gender.fromString("FEMALE"))
        assertEquals(Gender.OTHER, Gender.fromString("OTHER"))
    }

    @Test
    fun `Gender fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(Gender.UNKNOWN, Gender.fromString("NONBINARY"))
    }

    @Test
    fun `Gender fromString con null retorna UNKNOWN`() {
        assertEquals(Gender.UNKNOWN, Gender.fromString(null))
    }

    // ─── DietPreference ───────────────────────────────────────────────────────

    @Test
    fun `DietPreference fromString con valor conocido retorna el enum correcto`() {
        assertEquals(DietPreference.VEGETARIAN, DietPreference.fromString("VEGETARIAN"))
        assertEquals(DietPreference.VEGAN, DietPreference.fromString("VEGAN"))
        assertEquals(DietPreference.KETO, DietPreference.fromString("KETO"))
        assertEquals(DietPreference.NONE, DietPreference.fromString("NONE"))
    }

    @Test
    fun `DietPreference fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(DietPreference.UNKNOWN, DietPreference.fromString("CARNIVORE"))
    }

    @Test
    fun `DietPreference fromString con null retorna UNKNOWN`() {
        assertEquals(DietPreference.UNKNOWN, DietPreference.fromString(null))
    }

    // ─── WorkoutLocation ──────────────────────────────────────────────────────

    @Test
    fun `WorkoutLocation fromString con valor conocido retorna el enum correcto`() {
        assertEquals(WorkoutLocation.GYM, WorkoutLocation.fromString("GYM"))
        assertEquals(WorkoutLocation.HOME, WorkoutLocation.fromString("HOME"))
        assertEquals(WorkoutLocation.OUTDOOR, WorkoutLocation.fromString("OUTDOOR"))
        assertEquals(WorkoutLocation.HOME_GYM, WorkoutLocation.fromString("HOME_GYM"))
    }

    @Test
    fun `WorkoutLocation fromString con valor desconocido retorna UNKNOWN sin crash`() {
        assertEquals(WorkoutLocation.UNKNOWN, WorkoutLocation.fromString("OFFICE"))
    }

    @Test
    fun `WorkoutLocation fromString con null retorna UNKNOWN`() {
        assertEquals(WorkoutLocation.UNKNOWN, WorkoutLocation.fromString(null))
    }
}

