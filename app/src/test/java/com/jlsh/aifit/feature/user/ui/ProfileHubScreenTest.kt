package com.jlsh.aifit.feature.user.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.lifecycle.SavedStateHandle
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProfileHubScreenTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun createViewModel(
        profileFlow: Flow<Result<UserProfile>> = flowOf(Result.Error(AppException.NetworkException)),
        session: SessionManager = mockk(relaxed = true),
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
            uploadProfilePhotoUseCase = mockk(relaxed = true),
            getUserStreaksUseCase = streaks,
            getUserAchievementsUseCase = achievements,
            getPersonalRecordsUseCase = records,
            logBodyWeightUseCase = mockk(relaxed = true),
            userPreferencesDataStore = prefs,
            sessionManager = session,
            savedStateHandle = SavedStateHandle(mapOf("mode" to "edit")),
        )
    }

    @Test
    fun `muestra LoadingScreen cuando el estado es Loading`() {
        val vm = createViewModel(profileFlow = flowOf(Result.Loading))
        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithTag("loading_screen").assertIsDisplayed()
    }

    @Test
    fun `muestra ErrorScreen cuando el estado es Error`() {
        val vm = createViewModel(profileFlow = flowOf(Result.Error(AppException.NetworkException)))
        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithTag("error_screen").assertIsDisplayed()
    }

    @Test
    fun `muestra nombre del usuario en estado Success`() {
        val profile = fakeUserProfile(name = "Ana Garcia", goalType = GoalType.LOSE_WEIGHT)
        val vm = createViewModel(profileFlow = flowOf(Result.Success(profile)))
        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithText("Ana Garcia").assertIsDisplayed()
    }

    @Test
    fun `muestra seccion MI CUENTA en estado Success`() {
        val vm = createViewModel(profileFlow = flowOf(Result.Success(fakeUserProfile())))
        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithText("MI CUENTA").assertIsDisplayed()
    }

    @Test
    fun `pulsar Cerrar sesion abre ConfirmationDialog`() {
        val vm = createViewModel(profileFlow = flowOf(Result.Success(fakeUserProfile())))
        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_hub_list")
            .performScrollToNode(hasText("Cerrar sesión"))
        composeTestRule.onNodeWithText("Cerrar sesión").performClick()

        composeTestRule.onNodeWithText("¿Seguro que quieres cerrar sesión?").assertIsDisplayed()
    }

    @Test
    fun `confirmar logout en dialog llama a sessionManager logout`() {
        val session: SessionManager = mockk(relaxed = true)
        val vm = createViewModel(
            profileFlow = flowOf(Result.Success(fakeUserProfile())),
            session = session,
        )

        composeTestRule.setContent {
            AIFitTheme {
                ProfileHubScreen(
                    onNavigateToEditProfile = {},
                    onNavigateToDashboard = {},
                    onNavigateToBodyWeight = {},
                    onNavigateToMetabolic = {},
                    onNavigateToExport = {},
                    onNavigateToGamification = {},
                    onNavigateToGlossary = {},
                    viewModel = vm,
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_hub_list")
            .performScrollToNode(hasText("Cerrar sesión"))
        composeTestRule.onNodeWithText("Cerrar sesión").performClick()
        composeTestRule.onNodeWithText("CONFIRMAR").performClick()

        verify(exactly = 1) { session.logout() }
    }
}
