package com.jlsh.aifit.feature.education.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
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
 * ViewModel compartido por las pantallas de contenido educativo contextual.
 *
 * **UiState expuesto** ([explanationState] — [ExplanationState]):
 * - [ExplanationState.Idle]: sin explicación cargada.
 * - [ExplanationState.Loading]: cargando explicación de ejercicio o comida.
 * - [ExplanationState.Success]: explicación contextual lista.
 * - [ExplanationState.Error]: mensaje de error.
 *
 * **UiState expuesto** ([whyThisState] — [WhyThisState]):
 * - [WhyThisState.Idle]: sin explicación "por qué esto".
 * - [WhyThisState.Loading]: cargando justificación.
 * - [WhyThisState.Success]: explicación lista.
 * - [WhyThisState.Error]: mensaje de error.
 *
 * **UiState expuesto** ([glossaryState] — [GlossaryState]):
 * - [GlossaryState.Idle]: glosario sin búsqueda activa.
 * - [GlossaryState.Loading]: consultando término.
 * - [GlossaryState.Success]: definición del término lista.
 * - [GlossaryState.Error]: mensaje de error.
 *
 * No emite eventos de navegación; las pantallas reaccionan directamente al [StateFlow].
 *
 * @param getExerciseExplanationUseCase Explicación de un ejercicio del plan.
 * @param getMealExplanationUseCase Explicación de una comida del plan.
 * @param getWhyThisExerciseUseCase Justificación de por qué está ese ejercicio.
 * @param getWhyThisMealUseCase Justificación de por qué está esa comida.
 * @param getGlossaryTermUseCase Definición de un término del glosario.
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

    /** Estado de la explicación contextual de ejercicio o comida. */
    val explanationState: StateFlow<ExplanationState> = _explanationState.asStateFlow()

    private val _whyThisState = MutableStateFlow<WhyThisState>(WhyThisState.Idle)

    /** Estado de la explicación "por qué este ejercicio/comida". */
    val whyThisState: StateFlow<WhyThisState> = _whyThisState.asStateFlow()

    private val _glossaryState = MutableStateFlow<GlossaryState>(GlossaryState.Idle)

    /** Estado de la búsqueda en el glosario. */
    val glossaryState: StateFlow<GlossaryState> = _glossaryState.asStateFlow()

    /**
     * Carga la explicación educativa de un ejercicio.
     *
     * @param exerciseId Identificador del ejercicio en el plan.
     */
    fun loadExerciseExplanation(exerciseId: String) {
        viewModelScope.launch {
            Log.d("AIFIT_DEBUG", "education: llamando exerciseExplanation id=$exerciseId")
            _explanationState.value = ExplanationState.Loading
            when (val result = getExerciseExplanationUseCase(exerciseId)) {
                is Result.Success -> {
                    Log.d("AIFIT_DEBUG", "education: exerciseExplanation OK data=${result.data}")
                    _explanationState.value = ExplanationState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_DEBUG", "education: exerciseExplanation ERROR=${result.exception}", result.exception)
                    _explanationState.value = ExplanationState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Carga la explicación educativa de una comida.
     *
     * @param mealId Identificador de la comida en el plan.
     */
    fun loadMealExplanation(mealId: String) {
        viewModelScope.launch {
            Log.d("AIFIT_DEBUG", "education: llamando mealExplanation id=$mealId")
            _explanationState.value = ExplanationState.Loading
            when (val result = getMealExplanationUseCase(mealId)) {
                is Result.Success -> {
                    Log.d("AIFIT_DEBUG", "education: mealExplanation OK data=${result.data}")
                    _explanationState.value = ExplanationState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_DEBUG", "education: mealExplanation ERROR=${result.exception}", result.exception)
                    _explanationState.value = ExplanationState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Carga la justificación de por qué el plan incluye ese ejercicio.
     *
     * @param exerciseId Identificador del ejercicio en el plan.
     */
    fun loadWhyThisExercise(exerciseId: String) {
        viewModelScope.launch {
            Log.d("AIFIT_DEBUG", "education: llamando whyThisExercise id=$exerciseId")
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisExerciseUseCase(exerciseId)) {
                is Result.Success -> {
                    Log.d("AIFIT_DEBUG", "education: whyThisExercise OK data=${result.data}")
                    _whyThisState.value = WhyThisState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_DEBUG", "education: whyThisExercise ERROR=${result.exception}", result.exception)
                    _whyThisState.value = WhyThisState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Carga la justificación de por qué el plan incluye esa comida.
     *
     * @param mealId Identificador de la comida en el plan.
     */
    fun loadWhyThisMeal(mealId: String) {
        viewModelScope.launch {
            Log.d("AIFIT_DEBUG", "education: llamando whyThisMeal id=$mealId")
            _whyThisState.value = WhyThisState.Loading
            when (val result = getWhyThisMealUseCase(mealId)) {
                is Result.Success -> {
                    Log.d("AIFIT_DEBUG", "education: whyThisMeal OK data=${result.data}")
                    _whyThisState.value = WhyThisState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_DEBUG", "education: whyThisMeal ERROR=${result.exception}", result.exception)
                    _whyThisState.value = WhyThisState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    /**
     * Consulta la definición de un término en el glosario.
     *
     * @param term Término a buscar (se recomienda texto ya recortado).
     */
    fun loadGlossaryTerm(term: String) {
        viewModelScope.launch {
            Log.d("AIFIT_DEBUG", "education: llamando glossary term=$term")
            _glossaryState.value = GlossaryState.Loading
            when (val result = getGlossaryTermUseCase(term)) {
                is Result.Success -> {
                    Log.d("AIFIT_DEBUG", "education: glossary OK data=${result.data}")
                    _glossaryState.value = GlossaryState.Success(result.data)
                }
                is Result.Error -> {
                    Log.e("AIFIT_DEBUG", "education: glossary ERROR=${result.exception}", result.exception)
                    _glossaryState.value = GlossaryState.Error(result.exception.toMessage())
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
}

