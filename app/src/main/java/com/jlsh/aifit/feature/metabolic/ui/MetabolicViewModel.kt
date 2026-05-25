package com.jlsh.aifit.feature.metabolic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.metabolic.data.dto.ApplyMetabolicAdjustmentRequestDto
import com.jlsh.aifit.feature.metabolic.domain.usecase.AnalyzeMetabolicProgressUseCase
import com.jlsh.aifit.feature.metabolic.domain.usecase.ApplyMetabolicAdjustmentUseCase
import com.jlsh.aifit.feature.metabolic.domain.usecase.GetMetabolicInsightsUseCase
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiEvent
import com.jlsh.aifit.feature.metabolic.ui.state.MetabolicUiState
import com.jlsh.aifit.feature.nutrition.domain.usecase.GetCurrentNutritionTargetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of metabolic analysis and application of nutritional adjustments.
 *
 * **Exposed UiState** ([uiState] — [MetabolicUiState]):
 * - [MetabolicUiState.Loading]: analysis and insights in parallel loading.
 * - [MetabolicUiState.Success]: analysis, insight history and [MetabolicUiState.Success.isApplying] flag.
 * - [MetabolicUiState.Error]: Generic error message.
 * - [MetabolicUiState.InsufficientData]: There is not enough data for analysis.
 *
 * **Emitted events** ([events] — [MetabolicUiEvent]):
 * - [MetabolicUiEvent.ShowSnackbar]: Confirmation or error applying adjustment.
 * - [MetabolicUiEvent.AdjustmentApplied]: saved adjustment; the UI can remain on screen.
 * - [MetabolicUiEvent.NavigateBack]: go back.
 *
 * @param analyzeMetabolicProgressUseCase Primary analysis.
 * @param getMetabolicInsightsUseCase History of applied settings.
 * @param applyMetabolicAdjustmentUseCase Persistence of the recommended setting.
 * @param getCurrentNutritionTargetUseCase Invalidate target cache after applying.
 */
@HiltViewModel
class MetabolicViewModel @Inject constructor(
    private val analyzeMetabolicProgressUseCase: AnalyzeMetabolicProgressUseCase,
    private val getMetabolicInsightsUseCase: GetMetabolicInsightsUseCase,
    private val applyMetabolicAdjustmentUseCase: ApplyMetabolicAdjustmentUseCase,
    private val getCurrentNutritionTargetUseCase: GetCurrentNutritionTargetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MetabolicUiState>(MetabolicUiState.Loading)
    /** Status of metabolic analysis and application of adjustments.*/
    val uiState: StateFlow<MetabolicUiState> = _uiState.asStateFlow()

    private val _events = Channel<MetabolicUiEvent>(Channel.BUFFERED)
    /** Snackbar flow and adjustment sign applied.*/
    val events = _events.receiveAsFlow()

    init {
        loadAll()
    }

    /** Reload analysis and insights history in parallel.*/
    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = MetabolicUiState.Loading

            val analysisDeferred = async { analyzeMetabolicProgressUseCase() }
            val insightsDeferred = async { getMetabolicInsightsUseCase() }

            val analysisResult = analysisDeferred.await()
            val insightsResult = insightsDeferred.await()

            when (analysisResult) {
                is Result.Success -> {
                    val insights = when (insightsResult) {
                        is Result.Success -> insightsResult.data
                        else -> emptyList()
                    }
                    _uiState.value = MetabolicUiState.Success(
                        analysis = analysisResult.data,
                        insights = insights,
                    )
                }
                is Result.Error -> {
                    _uiState.value = if (analysisResult.exception is AppException.InsufficientDataException) {
                        MetabolicUiState.InsufficientData
                    } else {
                        MetabolicUiState.Error(analysisResult.exception.toMessage())
                    }
                }
                else -> Unit
            }
        }
    }

    /** Sends the current recommendation to the backend and refreshes the analysis if it is successful.*/
    fun onApplyAdjustment() {
        val current = _uiState.value
        if (current !is MetabolicUiState.Success) return
        val recommendation = current.analysis.recommendation ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isApplying = true)

            val request = ApplyMetabolicAdjustmentRequestDto(
                newCalorieTarget = recommendation.suggestedCalorieTarget,
                newProteinTarget = recommendation.suggestedProteinTarget,
                newCarbsTarget = recommendation.suggestedCarbsTarget,
                newFatTarget = recommendation.suggestedFatTarget,
                adjustmentType = recommendation.type.name,
                magnitude = recommendation.magnitude.name,
                rationale = current.analysis.rationale,
            )

            when (val result = applyMetabolicAdjustmentUseCase(request)) {
                is Result.Success -> {
                    // Invalidate nutrition target cache by triggering re-fetch
                    getCurrentNutritionTargetUseCase().first()

                    _events.send(MetabolicUiEvent.ShowSnackbar("Ajuste aplicado correctamente"))
                    _events.send(MetabolicUiEvent.AdjustmentApplied)

                    // Refresh analysis
                    loadAll()
                }
                is Result.Error -> {
                    _uiState.value = current.copy(isApplying = false)
                    _events.send(MetabolicUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> Unit
            }
        }
    }
}

