package com.jlsh.aifit.feature.workout.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.workout.domain.model.JointPainEntry
import com.jlsh.aifit.feature.workout.domain.model.JointZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizeSessionSheet(
    onDismiss: () -> Unit,
    onConfirm: (systemicFatigue: Int, jointPainReport: List<JointPainEntry>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        FinalizeSessionSheetContent(
            onConfirm = onConfirm,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinalizeSessionSheetContent(
    onConfirm: (systemicFatigue: Int, jointPainReport: List<JointPainEntry>) -> Unit,
) {
    var fatigueValue by remember { mutableFloatStateOf(0f) }
    var hasInteractedWithSlider by remember { mutableStateOf(false) }
    val selectedZones = remember { mutableStateMapOf<JointZone, Boolean>() }
    val zoneNotes: SnapshotStateMap<JointZone, String> = remember { mutableStateMapOf() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AiFitSpacing.md)
            .navigationBarsPadding(),
    ) {
        // ===== TITLE =====
        Text(
            text = stringResource(R.string.finalize_session_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))

        // ===== FATIGUE SLIDER =====
        Text(
            text = stringResource(R.string.finalize_session_fatigue_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.sm))

        Text(
            text = fatigueValue.toInt().toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.xs))

        Slider(
            value = fatigueValue,
            onValueChange = {
                fatigueValue = it
                hasInteractedWithSlider = true
            },
            onValueChangeFinished = {
                hasInteractedWithSlider = true
            },
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primaryContainer,
                activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.finalize_session_fatigue_fresh),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.finalize_session_fatigue_exhausted),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))

        // ===== JOINT PAIN SELECTOR =====
        Text(
            text = stringResource(R.string.finalize_session_joint_pain_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.sm))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            JointZone.entries.forEach { zone ->
                val isSelected = selectedZones[zone] == true
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedZones.remove(zone)
                            zoneNotes.remove(zone)
                        } else {
                            selectedZones[zone] = true
                        }
                    },
                    label = {
                        Text(
                            text = jointZoneDisplayName(zone),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.md))

        // ===== SELECTED ZONES WITH NOTE INPUTS =====
        val activeZones = JointZone.entries.filter { selectedZones[it] == true }

        activeZones.forEach { zone ->
            AnimatedVisibility(visible = true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AiFitSpacing.xs),
                ) {
                    Text(
                        text = jointZoneDisplayName(zone),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(AiFitSpacing.xs))

                    AiFitTextField(
                        value = zoneNotes[zone] ?: "",
                        onValueChange = { zoneNotes[zone] = it },
                        label = stringResource(R.string.finalize_session_note_hint),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))

        // ===== CONFIRM BUTTON =====
        PrimaryButton(
            text = stringResource(R.string.finalize_session_confirm),
            onClick = {
                val fatigue = fatigueValue.toInt()
                val jointPainReport = activeZones.map { zone ->
                    JointPainEntry(
                        zone = zone,
                        note = zoneNotes[zone]?.ifBlank { null },
                    )
                }
                onConfirm(fatigue, jointPainReport)
            },
            enabled = hasInteractedWithSlider,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.md))
    }
}

@Composable
private fun jointZoneDisplayName(zone: JointZone): String {
    return stringResource(
        when (zone) {
            JointZone.SHOULDER_LEFT -> R.string.joint_zone_shoulder_left
            JointZone.SHOULDER_RIGHT -> R.string.joint_zone_shoulder_right
            JointZone.KNEE_LEFT -> R.string.joint_zone_knee_left
            JointZone.KNEE_RIGHT -> R.string.joint_zone_knee_right
            JointZone.HIP_LEFT -> R.string.joint_zone_hip_left
            JointZone.HIP_RIGHT -> R.string.joint_zone_hip_right
            JointZone.LOWER_BACK -> R.string.joint_zone_lower_back
            JointZone.ELBOW_LEFT -> R.string.joint_zone_elbow_left
            JointZone.ELBOW_RIGHT -> R.string.joint_zone_elbow_right
            JointZone.WRIST_LEFT -> R.string.joint_zone_wrist_left
            JointZone.WRIST_RIGHT -> R.string.joint_zone_wrist_right
            JointZone.NECK -> R.string.joint_zone_neck
        }
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "FinalizeSessionSheet Dark",
)
@Composable
private fun FinalizeSessionSheetPreview() {
    AIFitTheme(darkTheme = true) {
        FinalizeSessionSheetContent(
            onConfirm = { _, _ -> },
        )
    }
}

// U-12/U-13 compliance fix — light mode preview
@Preview(
    showBackground = true,
    name = "FinalizeSessionSheet Light",
)
@Composable
private fun FinalizeSessionSheetLightPreview() {
    AIFitTheme(darkTheme = false) {
        FinalizeSessionSheetContent(
            onConfirm = { _, _ -> },
        )
    }
}

