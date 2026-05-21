package com.jlsh.aifit.feature.progress.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.progress.data.dto.LogBodyWeightRequestDto
import com.jlsh.aifit.feature.progress.domain.usecase.GetBodyWeightHistoryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetProgressDashboardUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.GetWeeklyProgressSummaryUseCase
import com.jlsh.aifit.feature.progress.domain.usecase.LogBodyWeightUseCase
import com.jlsh.aifit.feature.progress.ui.state.BodyWeightUiState
import com.jlsh.aifit.feature.progress.ui.state.DashboardUiState
import com.jlsh.aifit.feature.progress.ui.state.ProgressUiEvent
import com.jlsh.aifit.feature.progress.ui.state.WeeklySummaryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel del módulo de progreso: dashboard, peso corporal y resumen semanal.
 *
 * **UiState expuesto**:
 * - [dashboardState] — [DashboardUiState]: panel principal (carga, error o [ProgressDashboard]).
 * - [bodyWeightState] — [BodyWeightUiState]: historial y formulario de registro de peso.
 * - [weeklySummaryState] — [WeeklySummaryUiState]: resumen semanal.
 * - [selectedPeriod]: etiqueta del filtro temporal del dashboard (7 / 30 / 90 días).
 *
 * **Eventos emitidos** ([events] — [ProgressUiEvent]):
 * - [ProgressUiEvent.NavigateToBodyWeight]: abrir pantalla de peso.
 * - [ProgressUiEvent.NavigateToWeeklySummary]: abrir resumen semanal.
 * - [ProgressUiEvent.NavigateToMetabolic]: abrir análisis metabólico.
 * - [ProgressUiEvent.NavigateBack]: volver atrás.
 * - [ProgressUiEvent.ShowSnackbar]: mensaje transitorio (éxito o error).
 *
 * @param getProgressDashboardUseCase Carga del dashboard agregado.
 * @param getWeeklyProgressSummaryUseCase Resumen semanal.
 * @param logBodyWeightUseCase Registro de un nuevo peso.
 * @param getBodyWeightHistoryUseCase Historial reactivo de peso.
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressDashboardUseCase: GetProgressDashboardUseCase,
    private val getWeeklyProgressSummaryUseCase: GetWeeklyProgressSummaryUseCase,
    private val logBodyWeightUseCase: LogBodyWeightUseCase,
    private val getBodyWeightHistoryUseCase: GetBodyWeightHistoryUseCase,
) : ViewModel() {

    // 1. UI STATE
    private val _dashboardState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    /** Estado del dashboard de progreso. */
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val _bodyWeightState = MutableStateFlow(BodyWeightUiState())
    /** Estado de historial y formulario de peso corporal. */
    val bodyWeightState: StateFlow<BodyWeightUiState> = _bodyWeightState.asStateFlow()

    private val _weeklySummaryState = MutableStateFlow<WeeklySummaryUiState>(WeeklySummaryUiState.Loading)
    /** Estado del resumen semanal. */
    val weeklySummaryState: StateFlow<WeeklySummaryUiState> = _weeklySummaryState.asStateFlow()

    // 2. EVENTS CHANNEL
    private val _events = Channel<ProgressUiEvent>(Channel.BUFFERED)
    /** Flujo único de eventos de navegación y snackbar. */
    val events = _events.receiveAsFlow()

    // 3. LOCAL UI STATE
    private val _selectedPeriod = MutableStateFlow("30 días")
    /** Periodo seleccionado en el dashboard («7 días», «30 días», «90 días»). */
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private var bodyWeightHistoryJob: Job? = null

    // 4. INIT BLOCK
    init {
        loadDashboard()
    }

    // 5. PUBLIC FUNCTIONS

    /** Actualiza el filtro temporal y recarga el dashboard. */
    fun onPeriodSelected(period: String) {
        _selectedPeriod.value = period
        loadDashboard()
    }

    /** Fuerza la recarga del dashboard con el periodo actual. */
    fun onRefreshDashboard() {
        loadDashboard()
    }

    /** Solicita navegación a la pantalla de peso corporal. */
    fun onNavigateToBodyWeight() {
        emitEvent(ProgressUiEvent.NavigateToBodyWeight)
    }

    /** Solicita navegación al resumen semanal. */
    fun onNavigateToWeeklySummary() {
        emitEvent(ProgressUiEvent.NavigateToWeeklySummary)
    }

    /** Solicita navegación al análisis metabólico. */
    fun onNavigateToMetabolic() {
        emitEvent(ProgressUiEvent.NavigateToMetabolic)
    }

    // ===== BODY WEIGHT =====

    /** Inicia la observación del historial de peso de los últimos seis meses. */
    fun loadBodyWeightHistory() {
        bodyWeightHistoryJob?.cancel()
        bodyWeightHistoryJob = viewModelScope.launch {
            _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = true)
            val from = LocalDate.now().minusMonths(6).format(dateFormatter)
            val to = LocalDate.now().format(dateFormatter)
            getBodyWeightHistoryUseCase(from, to).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(
                            weightHistory = result.data,
                            isLoading = false,
                        )
                    }
                    is Result.Error -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = false)
                        emitEvent(ProgressUiEvent.ShowSnackbar(result.exception.toMessage()))
                    }
                    is Result.Loading -> {
                        _bodyWeightState.value = _bodyWeightState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    /** Actualiza el valor del campo de peso en el formulario. */
    fun onWeightChanged(value: String) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formWeight = value)
    }

    /** Actualiza la fecha del registro de peso. */
    fun onWeightDateChanged(date: LocalDate) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formDate = date)
    }

    /** Actualiza las notas opcionales del registro. */
    fun onWeightNotesChanged(notes: String) {
        _bodyWeightState.value = _bodyWeightState.value.copy(formNotes = notes)
    }

    /** Valida el formulario, envía el peso y refresca el historial si tiene éxito. */
    fun onLogWeight() {
        val state = _bodyWeightState.value
        val weight = state.formWeight.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _bodyWeightState.value = state.copy(isSaving = true)
            val request = LogBodyWeightRequestDto(
                weight = weight,
                date = state.formDate.format(dateFormatter),
                notes = state.formNotes.ifBlank { null },
            )
            when (val result = logBodyWeightUseCase(request)) {
                is Result.Success -> {
                    _bodyWeightState.value = _bodyWeightState.value.copy(
                        formWeight = "",
                        formNotes = "",
                        formDate = LocalDate.now(),
                        isSaving = false,
                    )
                    emitEvent(ProgressUiEvent.ShowSnackbar("Peso registrado"))
                    loadBodyWeightHistory()
                }
                is Result.Error -> {
                    _bodyWeightState.value = _bodyWeightState.value.copy(isSaving = false)
                    emitEvent(ProgressUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }

    // ===== WEEKLY SUMMARY =====

    /** Carga el resumen de progreso de la semana actual. */
    fun loadWeeklySummary() {
        viewModelScope.launch {
            _weeklySummaryState.value = WeeklySummaryUiState.Loading
            when (val result = getWeeklyProgressSummaryUseCase()) {
                is Result.Success -> {
                    _weeklySummaryState.value = WeeklySummaryUiState.Success(summary = result.data)
                }
                is Result.Error -> {
                    _weeklySummaryState.value = WeeklySummaryUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    // 6. PRIVATE HELPERS

    private fun loadDashboard() {
        viewModelScope.launch {
            _dashboardState.value = DashboardUiState.Loading
            val days = when (_selectedPeriod.value) {
                "7 días" -> 7L
                "90 días" -> 90L
                else -> 30L
            }
            val to = LocalDate.now().format(dateFormatter)
            val from = LocalDate.now().minusDays(days).format(dateFormatter)

            when (val result = getProgressDashboardUseCase(from, to)) {
                is Result.Success -> {
                    _dashboardState.value = DashboardUiState.Success(
                        dashboard = result.data,
                        selectedPeriod = _selectedPeriod.value,
                    )
                }
                is Result.Error -> {
                    _dashboardState.value = DashboardUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    private fun emitEvent(event: ProgressUiEvent) {
        viewModelScope.launch { _events.send(event) }
    }
}

