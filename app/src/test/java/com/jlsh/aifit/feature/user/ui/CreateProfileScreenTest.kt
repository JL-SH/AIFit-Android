package com.jlsh.aifit.feature.user.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.CreateUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CreateProfileScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun buildViewModel(
        createResult: Result<UserProfile> = Result.Error(AppException.NetworkException),
    ): UserViewModel {
        val getUserProfile: GetUserProfileUseCase = mockk()
        every { getUserProfile() } returns flowOf(Result.Error(AppException.NetworkException))

        val createProfile: CreateUserProfileUseCase = mockk()
        coEvery { createProfile(any()) } returns createResult

        val streaks: GetUserStreaksUseCase = mockk()
        coEvery { streaks() } returns Result.Error(AppException.NetworkException)

        val achievements: GetUserAchievementsUseCase = mockk()
        coEvery { achievements() } returns Result.Error(AppException.NetworkException)

        val records: GetPersonalRecordsUseCase = mockk()
        coEvery { records() } returns Result.Error(AppException.NetworkException)

        val prefs: UserPreferencesDataStore = mockk()
        every { prefs.isDarkTheme } returns flowOf(false)
        coEvery { prefs.setDarkTheme(any()) } returns Unit

        return UserViewModel(
            getUserProfileUseCase = getUserProfile,
            createUserProfileUseCase = createProfile,
            updateUserProfileUseCase = mockk(),
            uploadProfilePhotoUseCase = mockk(relaxed = true),
            getUserStreaksUseCase = streaks,
            getUserAchievementsUseCase = achievements,
            getPersonalRecordsUseCase = records,
            logBodyWeightUseCase = mockk(relaxed = true),
            userPreferencesDataStore = prefs,
            sessionManager = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(mapOf("mode" to "create")),
        )
    }

    @Test
    fun `pantalla muestra el primer paso del wizard al arrancar`() {
        composeTestRule.setContent {
            AIFitTheme {
                CreateProfileScreen(
                    onNavigateToOnboarding = {},
                    viewModel = buildViewModel(),
                )
            }
        }

        // Step 1 shows goal options (GoalType)
        composeTestRule.onNodeWithText("Perder grasa").assertIsDisplayed()
    }

    @Test
    fun `pantalla muestra indicador de progreso`() {
        composeTestRule.setContent {
            AIFitTheme {
                CreateProfileScreen(
                    onNavigateToOnboarding = {},
                    viewModel = buildViewModel(),
                )
            }
        }

        composeTestRule.onNodeWithText("1 / 8").assertIsDisplayed()
    }

    @Test
    fun `seleccionar opcion y pulsar CONTINUAR avanza al siguiente paso`() {
        composeTestRule.setContent {
            AIFitTheme {
                CreateProfileScreen(
                    onNavigateToOnboarding = {},
                    viewModel = buildViewModel(),
                )
            }
        }

        composeTestRule.onNodeWithText("Perder grasa").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()

        composeTestRule.onNodeWithText("2 / 8").assertIsDisplayed()
    }

    @Test
    fun `guardar perfil con exito llama a onNavigateToOnboarding`() {
        var navigated = false
        composeTestRule.setContent {
            AIFitTheme {
                CreateProfileScreen(
                    onNavigateToOnboarding = { navigated = true },
                    viewModel = buildViewModel(createResult = Result.Success(fakeUserProfile())),
                )
            }
        }

        // Step 0: Objective
        composeTestRule.onNodeWithText("Perder grasa").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 1: Experiencia
        composeTestRule.onNodeWithText("Nunca he entrenado").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 2: Location
        composeTestRule.onNodeWithText("En casa").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 3: Training days
        composeTestRule.onNodeWithText("3 días").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 4: Time per session
        composeTestRule.onNodeWithText("30 min").performClick()
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 5: Lesiones (siempre habilitado)
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 6: Physical data (optional)
        composeTestRule.onNodeWithText("CONTINUAR").performClick()
        // Step 7: Dietary preference
        composeTestRule.onNodeWithText("Sin restricciones").performClick()
        composeTestRule.onNodeWithText("EMPEZAR").performClick()

        assert(navigated)
    }
}
