package com.jlsh.aifit.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.feature.auth.domain.usecase.GoogleLoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.LoginUseCase
import com.jlsh.aifit.feature.auth.domain.usecase.RegisterUseCase
import com.jlsh.aifit.feature.auth.ui.AuthViewModel
import com.jlsh.aifit.feature.auth.ui.LoginScreen
import com.jlsh.aifit.testutil.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginScreenScreenshotTest {

    @get:Rule(order = 0)
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private fun createViewModel() = AuthViewModel(mockk(), mockk(), mockk())

    @Test
    fun `LoginScreen snapshot estado Idle dark mode`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = true) {
                LoginScreen(
                    onNavigateToRegister = {},
                    onNavigateToMain = {},
                    onNavigateToCreateProfile = {},
                    viewModel = createViewModel(),
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/LoginScreen_idle_dark.png",
        )
    }

    @Test
    fun `LoginScreen snapshot estado Idle light mode`() {
        composeTestRule.setContent {
            AIFitTheme(darkTheme = false) {
                LoginScreen(
                    onNavigateToRegister = {},
                    onNavigateToMain = {},
                    onNavigateToCreateProfile = {},
                    viewModel = createViewModel(),
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/LoginScreen_idle_light.png",
        )
    }
}

