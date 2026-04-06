package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.SavedStateHandle
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.user.ui.CreateProfileScreen
import com.jlsh.aifit.feature.user.ui.UserViewModel
import com.jlsh.aifit.testutil.MainDispatcherRule
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
class CreateProfileScreenScreenshotTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun buildViewModel(): UserViewModel {
        val getUserProfile: GetUserProfileUseCase = mockk()
        every { getUserProfile() } returns flowOf(Result.Error(AppException.NetworkException))

        val streaks: GetUserStreaksUseCase = mockk()
        coEvery { streaks() } returns Result.Error(AppException.NetworkException)

        val achievements: GetUserAchievementsUseCase = mockk()
        coEvery { achievements() } returns Result.Error(AppException.NetworkException)

        val records: GetPersonalRecordsUseCase = mockk()
        coEvery { records() } returns Result.Error(AppException.NetworkException)

        val prefs: UserPreferencesDataStore = mockk()
        every { prefs.isDarkTheme } returns flowOf(true)
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
            savedStateHandle = SavedStateHandle(mapOf("mode" to "create")),
        )
    }

    @Test
    fun `CreateProfileScreen snapshot estado Idle dark mode paso 1`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                CreateProfileScreen(
                    onNavigateToOnboarding = {},
                    viewModel = buildViewModel(),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/CreateProfileScreen_idle_dark.png",
        )
    }

    @Test
    fun `CreateProfileScreen snapshot estado Idle light mode paso 1`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = false) {
                CreateProfileScreen(
                    onNavigateToOnboarding = {},
                    viewModel = buildViewModel(),
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/CreateProfileScreen_idle_light.png",
        )
    }
}
