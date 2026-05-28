package com.jlsh.aifit.feature.education.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.AppException
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.feature.education.domain.usecase.GetExerciseExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetGlossaryTermUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetMealExplanationUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisExerciseUseCase
import com.jlsh.aifit.feature.education.domain.usecase.GetWhyThisMealUseCase
import com.jlsh.aifit.feature.education.ui.state.ExplanationState
import com.jlsh.aifit.feature.education.ui.state.GlossaryState
import com.jlsh.aifit.feature.education.ui.state.WhyThisState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel shared by contextual educational content screens.
 *
 * **Exposed UiState** ([explanationState] — [ExplanationState]):
 * - [ExplanationState.Idle]: No explanation loaded.
 * - [ExplanationState.Loading]: loading exercise or food explanation.
 * - [ExplanationState.Success]: Contextual explanation ready.
 * - [ExplanationState.Error]: error message.
 *
 * **UiState exposed** ([whyThisState] — [WhyThisState]):
 * - [WhyThisState.Idle]: No explanation "why this".
 * - [WhyThisState.Loading]: loading justification.
 * - [WhyThisState.Success]: explanation ready.
 * - [WhyThisState.Error]: error message.
 *
 * **UiState exposed** ([glossaryState] — [GlossaryState]):
 * - [GlossaryState.Idle]: Glossary without active search.
 * - [GlossaryState.Loading]: Querying term.
 * - [GlossaryState.Success]: definition of the term list.
 * - [GlossaryState.Error]: error message.
 *
 * Does not emit navigation events; The screens react directly to [StateFlow].
 *
 * @param getExerciseExplanationUseCase Explanation of an exercise in the plan.
 * @param getMealExplanationUseCase Explanation of a meal in the plan.
 * @param getWhyThisExerciseUseCase Justification for why this exercise is there.
 * @param getWhyThisMealUseCase Justification for why that food is there.
 * @param getGlossaryTermUseCase Definition of a glossary term.
 */
@HiltViewModel
class EducationViewModel @Inject constructor(
    private val getExerciseExplanationUseCase: GetExerciseExplanationUseCase,
    private val getMealExplanationUseCase: GetMealExplanationUseCase,
    private val getWhyThisExerciseUseCase: GetWhyThisExerciseUseCase,
    private val getWhyThisMealUseCase: GetWhyThisMealUseCase,
    private val getGlossaryTermUseCase: GetGlossaryTermUseCase,
) : ViewModel() {

    private val _explanationState = MutableStateFlow<ExplanationState>(ExplanationState.Idle)

    /** State of the contextual explanation of exercise or food.*/
    val explanationState: StateFlow<ExplanationState> = _explanationState.asStateFlow()

    private val _whyThisState = MutableStateFlow<WhyThisState>(WhyThisState.Idle)

    /** State of the "why this exercise/food" explanation.*/
    val whyThisState: StateFlow<WhyThisState> = _whyThisState.asStateFlow()

    private val _glossaryState = MutableStateFlow<GlossaryState>(GlossaryState.Idle)

    /** Search status in the glossary.*/
    val glossaryState: StateFlow<GlossaryState> = _glossaryState.asStateFlow()

    /**
     * Load the educational explanation of an exercise.
     *
     * @param exerciseId Identifier of the exercise in the plan.
     */
    fun loadExerciseExplanation(exerciseId: String) {
        viewModelScope.launch {
            safeLogDebug("education: llamando exerciseExplanation id=$exerciseId")
            _explanationState.value = ExplanationState.Loading
            when (val result = getExerciseExplanationUseCase(exerciseId)) {
                is Result.Success -> {
                    safeLogDebug("education: exerciseExplanation OK data=${result.data}")
                    _explanationState.value = ExplanationState.Success(result.data)
                }
                is Result.Error -> {
                    safeLogError("education: exerciseExplanation ERROR=${result.exception}")
                    _explanationState.value = ExplanationState.Error(result.exception.userMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Load the educational explanation of a food.
     *
     * @param mealId Identifier of the meal in the plan.
     */
    fun loadMealExplanation(mealId: String) {
        viewModelScope.launch {
            safeLogDebug("education: llamando mealExplanation id=$mealId")
            _explanationState.value = ExplanationState.Loading
            when (val result = getMealExplanationUseCase(mealId)) {
                is Result.Success -> {
                    safeLogDebug("education: mealExplanation OK data=${result.data}")
                    _explanationState.value = ExplanationState.Success(result.data)
                }
                is Result.Error -> {
                    safeLogError("education: mealExplanation ERROR=${result.exception}")
                    _explanationState.value = ExplanationState.Error(result.exception.userMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Upload the justification of why the plan includes that exercise.
     *
     * @param exerciseId Identifier of the exercise in the plan.
     */
    fun loadWhyThisExercise(exerciseId: String) {
        viewModelScope.launch {
            safeLogDebug("education: llamando whyThisExercise id=$exerciseId")
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisExerciseUseCase(exerciseId)) {
                is Result.Success -> {
                    safeLogDebug("education: whyThisExercise OK data=${result.data}")
                    _whyThisState.value = WhyThisState.Success(result.data)
                }
                is Result.Error -> {
                    safeLogError("education: whyThisExercise ERROR=${result.exception}")
                    _whyThisState.value = WhyThisState.Error(result.exception.userMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Upload the justification of why the plan includes that meal.
     *
     * @param mealId Identifier of the meal in the plan.
     */
    fun loadWhyThisMeal(mealId: String) {
        viewModelScope.launch {
            safeLogDebug("education: llamando whyThisMeal id=$mealId")
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisMealUseCase(mealId)) {
                is Result.Success -> {
                    safeLogDebug("education: whyThisMeal OK data=${result.data}")
                    _whyThisState.value = WhyThisState.Success(result.data)
                }
                is Result.Error -> {
                    safeLogError("education: whyThisMeal ERROR=${result.exception}")
                    _whyThisState.value = WhyThisState.Error(result.exception.userMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * See the definition of a term in the glossary.
     *
     * @param term Term to search for (already trimmed text is recommended).
     */
    fun loadGlossaryTerm(term: String) {
        viewModelScope.launch {
            safeLogDebug("education: llamando glossary term=$term")
            _glossaryState.value = GlossaryState.Loading
            when (val result = getGlossaryTermUseCase(term)) {
                is Result.Success -> {
                    safeLogDebug("education: glossary OK data=${result.data}")
                    _glossaryState.value = GlossaryState.Success(result.data)
                }
                is Result.Error -> {
                    safeLogError("education: glossary ERROR=${result.exception}")
                    _glossaryState.value = GlossaryState.Error(result.exception.userMessage())
                }
                else -> Unit
            }
        }
    }

    /** Restablece [explanationState] a [ExplanationState.Idle]. */
    fun resetExplanationState() {
        _explanationState.value = ExplanationState.Idle
    }

    /** Restablece [whyThisState] a [WhyThisState.Idle]. */
    fun resetWhyThisState() {
        _whyThisState.value = WhyThisState.Idle
    }

    /** Restablece [glossaryState] a [GlossaryState.Idle]. */
    fun resetGlossaryState() {
        _glossaryState.value = GlossaryState.Idle
    }

    private fun safeLogDebug(message: String) {
        runCatching { android.util.Log.d("AIFIT_DEBUG", message) }
    }

    private fun safeLogError(message: String) {
        runCatching { android.util.Log.e("AIFIT_DEBUG", message) }
    }

    private fun AppException.userMessage(): String = when (this) {
        is AppException.NetworkException -> "Sin conexión. Comprueba tu internet."
        is AppException.UnauthorizedException -> "Sesión expirada. Vuelve a iniciar sesión."
        is AppException.ForbiddenException -> "No tienes permisos para realizar esta acción."
        is AppException.NotFoundException -> "No se encontró $resource."
        is AppException.ValidationException -> errors.values.firstOrNull() ?: "Datos inválidos."
        is AppException.ConflictException -> "El recurso ya existe o hay un conflicto."
        is AppException.ServerException -> "Error del servidor. Inténtalo más tarde."
        is AppException.AiOverloadedException -> AppException.AI_OVERLOADED_MESSAGE
        is AppException.UnknownException -> message.ifBlank { "Error inesperado. Inténtalo de nuevo." }
        is AppException.InsufficientDataException -> "Necesitas más datos para realizar este análisis. Registra al menos 2 semanas de peso y entrenamientos."
    }
}

