package com.jlsh.aifit.feature.vision.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jlsh.aifit.core.common.Result
import com.jlsh.aifit.core.common.toMessage
import com.jlsh.aifit.feature.nutrition.domain.usecase.TrackMealUseCase
import com.jlsh.aifit.feature.nutrition.domain.util.toTrackMealRequestDto
import com.jlsh.aifit.feature.vision.domain.usecase.AnalyzeFoodPhotoUseCase
import com.jlsh.aifit.feature.vision.ui.state.VisionUiEvent
import com.jlsh.aifit.feature.vision.ui.state.VisionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class VisionViewModel @Inject constructor(
    private val analyzeFoodPhotoUseCase: AnalyzeFoodPhotoUseCase,
    private val trackMealUseCase: TrackMealUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<VisionUiState>(VisionUiState.Idle)
    val uiState: StateFlow<VisionUiState> = _uiState.asStateFlow()

    private val _events = Channel<VisionUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onCapturePhoto(bitmap: Bitmap) {
        val bytes = bitmapToBytes(bitmap)
        analyzeImage(bytes, "image/jpeg")
    }

    fun onSelectFromGallery(bytes: ByteArray, contentType: String = "image/jpeg") {
        analyzeImage(bytes, contentType)
    }

    fun onTryAgain() {
        _uiState.value = VisionUiState.Idle
    }

    fun onLogMeal() {
        val current = _uiState.value
        if (current !is VisionUiState.Result || current.isSaving) return

        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            when (val result = trackMealUseCase(current.result.toTrackMealRequestDto())) {
                is Result.Success -> {
                    _events.send(VisionUiEvent.MealLogged)
                    _events.send(VisionUiEvent.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.value = current.copy(isSaving = false)
                    _events.send(VisionUiEvent.ShowSnackbar(result.exception.toMessage()))
                }
                else -> {
                    _uiState.value = current.copy(isSaving = false)
                }
            }
        }
    }

    private fun analyzeImage(bytes: ByteArray, contentType: String) {
        viewModelScope.launch {
            _uiState.value = VisionUiState.Analyzing
            when (val result = analyzeFoodPhotoUseCase(bytes, contentType)) {
                is Result.Success -> {
                    _uiState.value = VisionUiState.Result(result.data)
                }
                is Result.Error -> {
                    _uiState.value = VisionUiState.Error(result.exception.toMessage())
                }
                else -> Unit
            }
        }
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return stream.toByteArray()
    }
}
