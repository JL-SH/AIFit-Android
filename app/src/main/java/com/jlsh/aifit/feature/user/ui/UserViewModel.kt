package com.jlsh.aifit.feature.user.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.core.datastore.UserPreferencesDataStore
import com.jlsh.aifit.core.session.SessionManager
import com.jlsh.aifit.feature.gamification.domain.model.StreakType
import com.jlsh.aifit.feature.gamification.domain.usecase.GetPersonalRecordsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserAchievementsUseCase
import com.jlsh.aifit.feature.gamification.domain.usecase.GetUserStreaksUseCase
import com.jlsh.aifit.feature.user.domain.model.ActivityLevel
import com.jlsh.aifit.feature.user.domain.model.CreateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.DietPreference
import com.jlsh.aifit.feature.user.domain.model.FitnessLevel
import com.jlsh.aifit.feature.user.domain.model.Gender
import com.jlsh.aifit.feature.user.domain.model.GoalType
import com.jlsh.aifit.feature.user.domain.model.WorkoutLocation
import com.jlsh.aifit.feature.user.domain.model.UpdateUserProfileRequest
import com.jlsh.aifit.feature.user.domain.model.UserProfile
import com.jlsh.aifit.feature.user.domain.usecase.CreateUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.GetUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.UpdateUserProfileUseCase
import com.jlsh.aifit.feature.user.domain.usecase.UploadProfilePhotoUseCase
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.usecase.LogBodyWeightUseCase
import com.jlsh.aifit.feature.user.ui.state.UserUiEvent
import com.jlsh.aifit.feature.user.ui.state.UserUiState
import com.jlsh.aifit.core.ui.components.inputs.DateValidationResult
import com.jlsh.aifit.core.ui.components.inputs.DateValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase,
    private val getUserStreaksUseCase: GetUserStreaksUseCase,
    private val getUserAchievementsUseCase: GetUserAchievementsUseCase,
    private val getPersonalRecordsUseCase: GetPersonalRecordsUseCase,
    private val logBodyWeightUseCase: LogBodyWeightUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 1. UI STATE
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<UserUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // 2b. THEME
    val isDarkTheme = userPreferencesDataStore.isDarkTheme

    // 2c. GAMIFICATION STATS
    private val _streakCount = MutableStateFlow<String>("—")
    val streakCount: StateFlow<String> = _streakCount.asStateFlow()

    private val _achievementsCount = MutableStateFlow<String>("—")
    val achievementsCount: StateFlow<String> = _achievementsCount.asStateFlow()

    private val _recordsCount = MutableStateFlow<String>("—")
    val recordsCount: StateFlow<String> = _recordsCount.asStateFlow()

    // 2d. PROFILE PICTURE
    private val _profilePictureUrl = MutableStateFlow<String?>(null)
    val profilePictureUrl: StateFlow<String?> = _profilePictureUrl.asStateFlow()

    /** URI of a photo selected locally but not yet uploaded to the server. */
    private val _pendingPhotoUri = MutableStateFlow<Uri?>(null)
    val pendingPhotoUri: StateFlow<Uri?> = _pendingPhotoUri.asStateFlow()

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    // 3. FORM FIELDS
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()

    private val _gender = MutableStateFlow("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _targetWeight = MutableStateFlow("")
    val targetWeight: StateFlow<String> = _targetWeight.asStateFlow()

    private val _goalType = MutableStateFlow("")
    val goalType: StateFlow<String> = _goalType.asStateFlow()

    private val _activityLevel = MutableStateFlow("")
    val activityLevel: StateFlow<String> = _activityLevel.asStateFlow()

    private val _fitnessLevel = MutableStateFlow("")
    val fitnessLevel: StateFlow<String> = _fitnessLevel.asStateFlow()

    private val _preferredLocation = MutableStateFlow("")
    val preferredLocation: StateFlow<String> = _preferredLocation.asStateFlow()

    private val _dietPreference = MutableStateFlow("")
    val dietPreference: StateFlow<String> = _dietPreference.asStateFlow()

    private val _weeklyWorkoutDays = MutableStateFlow("")
    val weeklyWorkoutDays: StateFlow<String> = _weeklyWorkoutDays.asStateFlow()

    private val _availableMinutes = MutableStateFlow("")
    val availableMinutes: StateFlow<String> = _availableMinutes.asStateFlow()

    private val _injuries = MutableStateFlow("")
    val injuries: StateFlow<String> = _injuries.asStateFlow()

    private val _calorieTarget = MutableStateFlow("")
    val calorieTarget: StateFlow<String> = _calorieTarget.asStateFlow()

    private val _birthDateError = MutableStateFlow<String?>(null)
    val birthDateError: StateFlow<String?> = _birthDateError.asStateFlow()

    val isEditMode: Boolean = savedStateHandle.get<String>("mode") == "edit"

    // 4. INIT
    init {
        if (isEditMode) {
            loadProfile()
        } else {
            checkIfProfileAlreadyExists()
        }
    }

    // 5. PUBLIC FUNCTIONS
    fun onNameChanged(value: String) { _name.value = value }
    fun onBirthDateChanged(value: String) {
        _birthDate.value = value
        _birthDateError.value = null
    }
    fun onGenderChanged(value: String) { _gender.value = value }
    fun onHeightChanged(value: String) { _height.value = value }
    fun onWeightChanged(value: String) { _weight.value = value }
    fun onTargetWeightChanged(value: String) { _targetWeight.value = value }
    fun onGoalTypeChanged(value: String) { _goalType.value = value }
    fun onActivityLevelChanged(value: String) { _activityLevel.value = value }
    fun onFitnessLevelChanged(value: String) { _fitnessLevel.value = value }
    fun onPreferredLocationChanged(value: String) { _preferredLocation.value = value }
    fun onDietPreferenceChanged(value: String) { _dietPreference.value = value }
    fun onWeeklyWorkoutDaysChanged(value: String) { _weeklyWorkoutDays.value = value }
    fun onAvailableMinutesChanged(value: String) { _availableMinutes.value = value }
    fun onInjuriesChanged(value: String) { _injuries.value = value }
    fun onCalorieTargetChanged(value: String) { _calorieTarget.value = value }

    fun onSaveProfile() {
        if (!validateForm()) return
        if (isEditMode) updateProfile() else createProfile()
    }

    fun onRefresh() {
        loadProfile()
        loadGamificationStats()
    }

    fun onLogout() {
        sessionManager.logout()
    }

    fun onProfilePictureSelected(uri: Uri?) {
        if (uri == null) return
        _pendingPhotoUri.value = uri
        uploadPhoto(uri)
    }

    fun onToggleTheme() {
        viewModelScope.launch {
            val current = userPreferencesDataStore.isDarkTheme.first()
            userPreferencesDataStore.setDarkTheme(!current)
        }
    }

    fun onNavigateToEditProfile() {
        emitEvent(UserUiEvent.NavigateToEditProfile)
    }

    // 6. PRIVATE HELPERS
    private fun validateForm(): Boolean {
        val dateStr = _birthDate.value
        if (dateStr.isNotBlank()) {
            val parsed = DateValidator.parseIsoString(dateStr)
            if (parsed == null) {
                _birthDateError.value = "Formato inválido. Usa yyyy-MM-dd (ej: 1995-03-15)"
                return false
            }
            val result = DateValidator.validate(parsed)
            if (result !is DateValidationResult.Valid) {
                _birthDateError.value = result.toErrorMessage()
                return false
            }
        }
        return true
    }

    private fun loadGamificationStats() {
        viewModelScope.launch {
            // Streaks
            when (val r = getUserStreaksUseCase()) {
                is Result.Success -> {
                    val combined = r.data.find { it.type == StreakType.COMBINED }
                    _streakCount.value = (combined?.currentCount ?: r.data.maxOfOrNull { it.currentCount } ?: 0).toString()
                }
                else -> _streakCount.value = "—"
            }
        }
        viewModelScope.launch {
            // Achievements
            when (val r = getUserAchievementsUseCase()) {
                is Result.Success -> _achievementsCount.value = r.data.size.toString()
                else -> _achievementsCount.value = "—"
            }
        }
        viewModelScope.launch {
            // Personal records
            when (val r = getPersonalRecordsUseCase()) {
                is Result.Success -> _recordsCount.value = r.data.size.toString()
                else -> _recordsCount.value = "—"
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            try {
                getUserProfileUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val merged = mergeProfilePicture(result.data)
                            populateForm(merged)
                            _uiState.value = UserUiState.Success(merged)
                        }
                        is Result.Error -> {
                            _uiState.value = UserUiState.Error(result.exception.toMessage())
                        }
                        is Result.Loading -> {
                            _uiState.value = UserUiState.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al cargar el perfil")
            }
        }
    }

    private fun mergeProfilePicture(incoming: UserProfile): UserProfile {
        val existingUrl = _profilePictureUrl.value
            ?: (uiState.value as? UserUiState.Success)?.profile?.profilePictureUrl
        val mergedUrl = incoming.profilePictureUrl?.takeIf { it.isNotBlank() } ?: existingUrl
        return if (mergedUrl != incoming.profilePictureUrl) {
            incoming.copy(profilePictureUrl = mergedUrl)
        } else {
            incoming
        }
    }

    private fun populateForm(profile: UserProfile) {
        _name.value = profile.name
        _profilePictureUrl.value = profile.profilePictureUrl
        _birthDate.value = profile.birthDate?.toString() ?: ""
        _gender.value = profile.gender?.name ?: ""
        _height.value = profile.height?.toString() ?: ""
        _weight.value = profile.weight?.toString() ?: ""
        _targetWeight.value = profile.targetWeight?.toString() ?: ""
        _goalType.value = profile.goalType?.name ?: ""
        _activityLevel.value = profile.activityLevel?.name ?: ""
        _fitnessLevel.value = profile.fitnessLevel?.name ?: ""
        _preferredLocation.value = profile.workoutLocation?.name ?: ""
        _dietPreference.value = profile.dietPreference?.name ?: ""
        _weeklyWorkoutDays.value = profile.weeklyWorkoutDays?.toString() ?: ""
        _availableMinutes.value = profile.availableMinutesPerSession?.toString() ?: ""
        _injuries.value = profile.injuries ?: ""
        _calorieTarget.value = profile.calorieTarget?.toString() ?: ""
    }

    private fun buildCreateRequest(): CreateUserProfileRequest = CreateUserProfileRequest(
        birthDate = _birthDate.value.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        gender = _gender.value.takeIf { it.isNotBlank() }
            ?.let { Gender.fromString(it) },
        goalType = _goalType.value.takeIf { it.isNotBlank() }
            ?.let { GoalType.fromString(it) },
        activityLevel = _activityLevel.value.takeIf { it.isNotBlank() }
            ?.let { ActivityLevel.fromString(it) },
        fitnessLevel = _fitnessLevel.value.takeIf { it.isNotBlank() }
            ?.let { FitnessLevel.fromString(it) },
        workoutLocation = _preferredLocation.value.takeIf { it.isNotBlank() }
            ?.let { WorkoutLocation.fromString(it) },
        dietPreference = _dietPreference.value.takeIf { it.isNotBlank() }
            ?.let { DietPreference.fromString(it) },
        height = _height.value.toFloatOrNull(),
        weight = _weight.value.toFloatOrNull(),
        targetWeight = _targetWeight.value.toFloatOrNull(),
        weeklyWorkoutDays = _weeklyWorkoutDays.value.toIntOrNull(),
        availableMinutesPerSession = _availableMinutes.value.toIntOrNull(),
        injuries = _injuries.value.takeIf { it.isNotBlank() },
        calorieTarget = _calorieTarget.value.toIntOrNull(),
    )

    private fun buildUpdateRequest(): UpdateUserProfileRequest = UpdateUserProfileRequest(
        name = _name.value.takeIf { it.isNotBlank() },
        birthDate = _birthDate.value.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        gender = _gender.value.takeIf { it.isNotBlank() }
            ?.let { Gender.fromString(it) },
        goalType = _goalType.value.takeIf { it.isNotBlank() }
            ?.let { GoalType.fromString(it) },
        activityLevel = _activityLevel.value.takeIf { it.isNotBlank() }
            ?.let { ActivityLevel.fromString(it) },
        fitnessLevel = _fitnessLevel.value.takeIf { it.isNotBlank() }
            ?.let { FitnessLevel.fromString(it) },
        workoutLocation = _preferredLocation.value.takeIf { it.isNotBlank() }
            ?.let { WorkoutLocation.fromString(it) },
        dietPreference = _dietPreference.value.takeIf { it.isNotBlank() }
            ?.let { DietPreference.fromString(it) },
        height = _height.value.toFloatOrNull(),
        weight = _weight.value.toFloatOrNull(),
        targetWeight = _targetWeight.value.toFloatOrNull(),
        weeklyWorkoutDays = _weeklyWorkoutDays.value.toIntOrNull(),
        availableMinutesPerSession = _availableMinutes.value.toIntOrNull(),
        injuries = _injuries.value.takeIf { it.isNotBlank() },
        calorieTarget = _calorieTarget.value.toIntOrNull(),
    )

    private fun createProfile() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Saving
            try {
                val request = buildCreateRequest()
                when (val result = createUserProfileUseCase(request)) {
                    is Result.Success -> {
                        _uiState.value = UserUiState.Success(result.data)

                        // BUG-012: Log the onboarding weight as the first BodyWeightLog
                        val initialWeight = request.weight
                        if (initialWeight != null) {
                            val weightRequest = LogBodyWeightRequestDto(
                                weight = initialWeight.toDouble(),
                                date = LocalDate.now().toString(),
                                notes = "Peso inicial",
                            )
                            when (val weightResult = logBodyWeightUseCase(weightRequest)) {
                                is Result.Success -> Log.d("AIFIT_DEBUG", "[CreateProfile] Peso inicial registrado: ${initialWeight}kg")
                                is Result.Error -> Log.w("AIFIT_DEBUG", "[CreateProfile] Error al registrar peso inicial: ${weightResult.exception.message}")
                                else -> Unit
                            }
                        }

                        emitEvent(UserUiEvent.ProfileSaved)
                    }
                    is Result.Error -> {
                        _uiState.value = UserUiState.Idle
                        emitEvent(UserUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                    is Result.Loading -> {
                        _uiState.value = UserUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState.Idle
                emitEvent(UserUiEvent.ShowSnackbar(e.message ?: "Error al crear el perfil"))
            }
        }
    }

    private fun checkIfProfileAlreadyExists() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            when (val result = getUserProfileUseCase().first { it !is Result.Loading }) {
                is Result.Success -> {
                    if (result.data.birthDate != null) {
                        emitEvent(UserUiEvent.ProfileSaved)
                    } else {
                        _uiState.value = UserUiState.Idle
                    }
                }
                else -> {
                    _uiState.value = UserUiState.Idle
                }
            }
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Saving
            try {
                when (val result = updateUserProfileUseCase(buildUpdateRequest())) {
                    is Result.Success -> {
                        _uiState.value = UserUiState.Success(result.data)
                        _pendingPhotoUri.value = null
                        emitEvent(UserUiEvent.ShowSnackbar("Perfil actualizado"))
                        emitEvent(UserUiEvent.NavigateBack)
                    }
                    is Result.Error -> {
                        _uiState.value = UserUiState.Idle
                        emitEvent(UserUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                    is Result.Loading -> {
                        // Shouldn't happen for a suspend call — recover to Idle
                        _uiState.value = UserUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState.Idle
                emitEvent(UserUiEvent.ShowSnackbar(e.message ?: "Error al actualizar el perfil"))
            }
        }
    }

    private fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            try {
                when (val result = uploadProfilePhotoUseCase(uri)) {
                    is Result.Success -> {
                        val merged = mergeProfilePicture(result.data)
                        val newUrl = merged.profilePictureUrl
                        _profilePictureUrl.value = newUrl
                        // Only clear the pending local URI once we have a server URL to
                        // display; if the follow-up getProfile() lost a race with the
                        // Cloudinary commit, keep the local thumbnail visible.
                        if (newUrl != null) {
                            _pendingPhotoUri.value = null
                        }
                        // Keep _uiState in sync so any consumer of successState.profile
                        // (e.g. ProfileHubScreen) also sees the new URL.
                        val current = _uiState.value
                        if (current is UserUiState.Success) {
                            _uiState.value = UserUiState.Success(merged)
                        }
                    }
                    is Result.Error -> {
                        _pendingPhotoUri.value = null
                        emitEvent(UserUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                    is Result.Loading -> Unit
                }
            } catch (e: Exception) {
                _pendingPhotoUri.value = null
                emitEvent(UserUiEvent.ShowSnackbar(e.message ?: "Error al subir la foto"))
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    private fun emitEvent(event: UserUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}
