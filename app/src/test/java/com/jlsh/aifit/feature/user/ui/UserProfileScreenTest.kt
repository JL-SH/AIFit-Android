package com.jlsh.aifit.feature.user.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UserProfileScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun buildViewModel(
        profileFlow: Flow<Result<UserProfile>>,
    ): UserViewModel {
        val getUserProfile: GetUserProfileUseCase = mockk()
        every { getUserProfile() } returns profileFlow

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
            createUserProfileUseCase = mockk(),
            updateUserProfileUseCase = mockk(),
            getUserStreaksUseCase = streaks,
            getUserAchievementsUseCase = achievements,
            getPersonalRecordsUseCase = records,
            userPreferencesDataStore = prefs,
            sessionManager = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(mapOf("mode" to "edit")),
        )
    }

    @Test
    fun `muestra LoadingScreen cuando el estado es Loading`() {
        val vm = buildViewModel(flowOf(Result.Loading))
        composeTestRule.setContent {
            AIFitTheme {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }

        composeTestRule.onNodeWithTag("loading_screen").assertIsDisplayed()
    }

    @Test
    fun `muestra campos del formulario cuando el estado es Success`() {
        val profile = fakeUserProfile(name = "Pedro Lopez")
        val vm = buildViewModel(flowOf(Result.Success(profile)))
        composeTestRule.setContent {
            AIFitTheme {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }

        composeTestRule.onNodeWithText("Pedro Lopez").assertIsDisplayed()
    }

    @Test
    fun `muestra boton Guardar en el formulario`() {
        val vm = buildViewModel(flowOf(Result.Success(fakeUserProfile())))
        composeTestRule.setContent {
            AIFitTheme {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }

        composeTestRule.onNodeWithText("Guardar").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `muestra campo de fecha de nacimiento`() {
        val vm = buildViewModel(flowOf(Result.Success(fakeUserProfile())))
        composeTestRule.setContent {
            AIFitTheme {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }

        composeTestRule.onNodeWithText("Fecha de nacimiento").assertIsDisplayed()
    }
}
