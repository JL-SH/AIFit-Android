package com.jlsh.aifit.feature.vision.ui

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.buttons.SecondaryButton
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.components.layout.LocalBottomBarVisibility
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.nutrition.domain.model.FoodItemLog
import com.jlsh.aifit.feature.vision.domain.model.FoodPhotoAnalysisResult
import com.jlsh.aifit.feature.vision.ui.state.VisionUiEvent
import com.jlsh.aifit.feature.vision.ui.state.VisionUiState

@Composable
fun FoodVisionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrackMeal: (prefilled: String) -> Unit,
    viewModel: VisionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Permission state
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* recomposition triggers from permission change */ }

    val hasPermission = remember(context) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                viewModel.onSelectFromGallery(bytes)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VisionUiEvent.NavigateToTrackMeal -> onNavigateToTrackMeal(event.prefilled)
                is VisionUiEvent.NavigateBack -> onNavigateBack()
                is VisionUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CompositionLocalProvider(LocalBottomBarVisibility provides false) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                AiFitTopBar(
                    title = "Food Vision",
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            // Re-check permission on each recomposition
            val currentHasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!currentHasPermission) {
                PermissionDeniedContent(
                    paddingValues = paddingValues,
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                )
            } else {
                when (val state = uiState) {
                    is VisionUiState.Idle -> CameraContent(
                        paddingValues = paddingValues,
                        onCapture = viewModel::onCapturePhoto,
                        onGallery = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    )
                    is VisionUiState.Analyzing -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center,
                        ) {
                            InlineLoadingIndicator(message = "Analizando imagen…")
                        }
                    }
                    is VisionUiState.Result -> ResultOverlay(
                        paddingValues = paddingValues,
                        result = state.result,
                        onLogMeal = viewModel::onLogMeal,
                        onTryAgain = viewModel::onTryAgain,
                    )
                    is VisionUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.height(AiFitSpacing.md))
                                SecondaryButton(
                                    text = "Reintentar",
                                    onClick = viewModel::onTryAgain,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    paddingValues: PaddingValues,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(top = AiFitSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyStateView(
            icon = Icons.Rounded.CameraAlt,
            title = "Acceso a cámara necesario",
            subtitle = "Habilita la cámara en ajustes para escanear comida",
        )
        Spacer(modifier = Modifier.height(AiFitSpacing.md))
        SecondaryButton(
            text = "Abrir Ajustes",
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun CameraContent(
    paddingValues: PaddingValues,
    onCapture: (Bitmap) -> Unit,
    onGallery: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        // CameraX Viewfinder
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
                        } catch (_: Exception) { }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Bottom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(AiFitSpacing.xl),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onGallery) {
                Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = "Galería",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp),
                )
            }

            FloatingActionButton(
                onClick = {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmap()
                                onCapture(bitmap)
                                image.close()
                            }
                            override fun onError(exception: ImageCaptureException) { }
                        },
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Capturar",
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(modifier = Modifier.size(48.dp)) // balance
        }
    }
}

@Composable
private fun ResultOverlay(
    paddingValues: PaddingValues,
    result: FoodPhotoAnalysisResult,
    onLogMeal: () -> Unit,
    onTryAgain: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = AiFitSpacing.md,
            end = AiFitSpacing.md,
            top = AiFitSpacing.sm,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(AiFitSpacing.md),
        modifier = Modifier.padding(paddingValues),
    ) {
        item(key = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs)) {
                Text(
                    text = result.identifiedFoodName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Confianza: ${"%.0f".format(result.confidence * 100)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (result.warnings.isNotEmpty()) {
            item(key = "warnings") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = result.warnings.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        items(result.items, key = { it.id }) { item ->
            FoodItemCard(item = item)
        }

        item(key = "actions") {
            Column(verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm)) {
                PrimaryButton(
                    text = "REGISTRAR COMIDA",
                    onClick = onLogMeal,
                    modifier = Modifier.fillMaxWidth(),
                )
                SecondaryButton(
                    text = "INTENTAR DE NUEVO",
                    onClick = onTryAgain,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FoodItemCard(item: FoodItemLog) {
    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${"%.0f".format(item.quantity)} ${item.unit}  ·  ${item.calories} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MacroLabel("P", item.proteinGrams)
                MacroLabel("C", item.carbsGrams)
                MacroLabel("G", item.fatGrams)
            }
        }
    }
}

@Composable
private fun MacroLabel(label: String, grams: Double) {
    Text(
        text = "$label: ${"%.1f".format(grams)}g",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}



