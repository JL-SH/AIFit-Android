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
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.user.ui.UserProfileScreen
import com.jlsh.aifit.feature.user.ui.UserViewModel
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
class UserProfileScreenScreenshotTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun buildViewModel(profileFlow: Flow<Result<UserProfile>>): UserViewModel {
        val getUserProfile: GetUserProfileUseCase = mockk()
        every { getUserProfile() } returns profileFlow

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
            savedStateHandle = SavedStateHandle(mapOf("mode" to "edit")),
        )
    }

    @Test
    fun `UserProfileScreen snapshot estado Success dark mode`() {
        val vm = buildViewModel(flowOf(Result.Success(fakeUserProfile(name = "Test User"))))
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/UserProfileScreen_success_dark.png",
        )
    }

    @Test
    fun `UserProfileScreen snapshot estado Loading dark mode`() {
        val vm = buildViewModel(flowOf(Result.Loading))
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                UserProfileScreen(onNavigateBack = {}, viewModel = vm)
            }
        }
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/UserProfileScreen_loading_dark.png",
        )
    }
}
