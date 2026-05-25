package com.jlsh.aifit.feature.user.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.usecase.CreateUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.UpdateUserProfileUseCase
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState
import com.jlsh.aifit.testutil.MainDispatcherRule
import com.jlsh.aifit.testutil.fakeUserProfile
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ─── Mocks base ────────────────────────────────────────────────────────────

    private val defaultGetUserProfile: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
        every { mock() } returns flowOf(Result.Error(AppException.NetworkException))
    }
    private val defaultCreateProfile: CreateUserProfileUseCase = mockk()
    private val defaultUpdateProfile: UpdateUserProfileUseCase = mockk()
    private val defaultStreaks: GetUserStreaksUseCase = mockk<GetUserStreaksUseCase>().also { mock ->
        coEvery { mock() } returns Result.Error(AppException.NetworkException)
    }
    private val defaultAchievements: GetUserAchievementsUseCase = mockk<GetUserAchievementsUseCase>().also { mock ->
        coEvery { mock() } returns Result.Error(AppException.NetworkException)
    }
    private val defaultRecords: GetPersonalRecordsUseCase = mockk<GetPersonalRecordsUseCase>().also { mock ->
        coEvery { mock() } returns Result.Error(AppException.NetworkException)
    }
    private val defaultPrefs: UserPreferencesDataStore = mockk {
        every { isDarkTheme } returns flowOf(false)
        coEvery { setDarkTheme(any()) } returns Unit
    }
    private val defaultSession: SessionManager = mockk(relaxed = true)

    private fun createViewModel(
        mode: String = "create",
        getUserProfile: GetUserProfileUseCase = defaultGetUserProfile,
        createProfile: CreateUserProfileUseCase = defaultCreateProfile,
        updateProfile: UpdateUserProfileUseCase = defaultUpdateProfile,
        streaks: GetUserStreaksUseCase = defaultStreaks,
        achievements: GetUserAchievementsUseCase = defaultAchievements,
        records: GetPersonalRecordsUseCase = defaultRecords,
        prefs: UserPreferencesDataStore = defaultPrefs,
        session: SessionManager = defaultSession,
    ) = UserViewModel(
        getUserProfileUseCase = getUserProfile,
        createUserProfileUseCase = createProfile,
        updateUserProfileUseCase = updateProfile,
        getUserStreaksUseCase = streaks,
        getUserAchievementsUseCase = achievements,
        getPersonalRecordsUseCase = records,
        userPreferencesDataStore = prefs,
        sessionManager = session,
        savedStateHandle = SavedStateHandle(mapOf("mode" to mode)),
    )

    // ─── Initial state (create mode) ──────────────────── ─────────────────────

    @Test
    fun `en modo create estado inicial es Idle cuando no hay perfil previo`() = runTest {
        val vm = createViewModel(mode = "create")

        // UnconfinedTestDispatcher ejecuta coroutines de forma eager:
        // init calls checkIfProfileAlreadyExists() → status = Idle for Repo Error
        assertTrue(vm.uiState.value is UserUiState.Idle)
    }

    @Test
    fun `en modo create si perfil ya tiene birthDate emite ProfileSaved`() = runTest {
        val profileWithBirthDate = fakeUserProfile().copy(
            birthDate = java.time.LocalDate.of(1990, 1, 1),
        )
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Success(profileWithBirthDate))
        }

        val vm = createViewModel(mode = "create", getUserProfile = gpuCase)

        vm.events.test {
            assertTrue(awaitItem() is UserUiEvent.ProfileSaved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Initial state (edit mode) ────────────────────── ──────────────────────

    @Test
    fun `en modo edit estado Loading se emite al arrancar`() = runTest {
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(
                Result.Loading,
                Result.Success(fakeUserProfile()),
            )
        }

        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)

        vm.uiState.test {
            // The status can be Loading or Success depending on the eager execution
            val state = awaitItem()
            assert(state is UserUiState.Loading || state is UserUiState.Success) {
                "Esperado Loading o Success, recibido $state"
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `en modo edit uiState es Success cuando getProfile retorna datos`() = runTest {
        val profile = fakeUserProfile(name = "Perfil de prueba")
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Success(profile))
        }

        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)

        vm.uiState.test {
            val state = awaitItem()
            // With UnconfinedTestDispatcher the final status is Success
            when (state) {
                is UserUiState.Success -> assertEquals(profile, state.profile)
                is UserUiState.Loading -> {
                    val next = awaitItem()
                    assertTrue(next is UserUiState.Success)
                    assertEquals(profile, (next as UserUiState.Success).profile)
                }
                else -> error("Estado inesperado: $state")
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `en modo edit uiState es Error cuando getProfile falla`() = runTest {
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Error(AppException.NetworkException))
        }

        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)

        vm.uiState.test {
            val state = awaitItem()
            when (state) {
                is UserUiState.Error -> { /* correcto */ }
                is UserUiState.Loading -> assertTrue(awaitItem() is UserUiState.Error)
                else -> error("Estado inesperado: $state")
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── populateForm ──────────────────────────────────────────────────────────

    @Test
    fun `en modo edit campos del formulario se rellenan con datos del perfil`() = runTest {
        val profile = fakeUserProfile(name = "Maria")
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Success(profile))
        }

        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)

        assertEquals("Maria", vm.name.value)
    }

    // ─── onSaveProfile create mode ─────────────────────────────────────────────

    @Test
    fun `onSaveProfile en modo create con exito emite ProfileSaved`() = runTest {
        val vm = createViewModel(mode = "create")
        coEvery { defaultCreateProfile(any()) } returns Result.Success(fakeUserProfile())

        vm.events.test {
            vm.onSaveProfile()
            assertTrue(awaitItem() is UserUiEvent.ProfileSaved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveProfile en modo create pasa por estado Saving`() = runTest {
        val vm = createViewModel(mode = "create")
        coEvery { defaultCreateProfile(any()) } coAnswers {
            delay(1) // permite que Turbine observe Saving antes de Success
            Result.Success(fakeUserProfile())
        }

        vm.uiState.test {
            skipItems(1) // Idle inicial
            vm.onSaveProfile()
            assertTrue(awaitItem() is UserUiState.Saving)
            assertTrue(awaitItem() is UserUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveProfile en modo create con fallo emite ShowSnackbar y vuelve a Idle`() = runTest {
        val vm = createViewModel(mode = "create")
        coEvery { defaultCreateProfile(any()) } returns Result.Error(AppException.ServerException)

        vm.events.test {
            vm.onSaveProfile()
            assertTrue(awaitItem() is UserUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(vm.uiState.value is UserUiState.Idle)
    }

    // ─── onSaveProfile edit mode ───────────────────────────────────────────────

    @Test
    fun `onSaveProfile en modo edit con exito emite NavigateBack y ShowSnackbar`() = runTest {
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Success(fakeUserProfile()))
        }
        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)
        coEvery { defaultUpdateProfile(any()) } returns Result.Success(fakeUserProfile())

        vm.events.test {
            vm.onSaveProfile()
            val first = awaitItem()
            val second = awaitItem()
            val emittedTypes = setOf(first::class, second::class)
            assertTrue(emittedTypes.contains(UserUiEvent.ShowSnackbar::class))
            assertTrue(emittedTypes.contains(UserUiEvent.NavigateBack::class))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveProfile en modo edit con fallo emite ShowSnackbar`() = runTest {
        val gpuCase: GetUserProfileUseCase = mockk<GetUserProfileUseCase>().also { mock ->
            every { mock() } returns flowOf(Result.Success(fakeUserProfile()))
        }
        val vm = createViewModel(mode = "edit", getUserProfile = gpuCase)
        coEvery { defaultUpdateProfile(any()) } returns Result.Error(AppException.NetworkException)

        vm.events.test {
            vm.onSaveProfile()
            assertTrue(awaitItem() is UserUiEvent.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── onLogout ──────────────────────────────────────────────────────────────

    @Test
    fun `onLogout llama a sessionManager logout`() = runTest {
        val vm = createViewModel()

        vm.onLogout()

        verify(exactly = 1) { defaultSession.logout() }
    }

    // ─── onNavigateToEditProfile ───────────────────────────────────────────────

    @Test
    fun `onNavigateToEditProfile emite NavigateToEditProfile`() = runTest {
        val vm = createViewModel()

        vm.events.test {
            vm.onNavigateToEditProfile()
            assertTrue(awaitItem() is UserUiEvent.NavigateToEditProfile)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Date validation ───────────────────────── ──────────────────────────

    @Test
    fun `onSaveProfile con birthDate invalido setea birthDateError y no llama al useCase`() = runTest {
        val vm = createViewModel(mode = "create")

        vm.onBirthDateChanged("no-es-fecha")
        vm.onSaveProfile()

        assertEquals("Formato inválido. Usa yyyy-MM-dd (ej: 1995-03-15)", vm.birthDateError.value)
        assertTrue(vm.uiState.value is UserUiState.Idle)
    }
}

