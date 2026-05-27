package com.jlsh.aifit.core.ui.components.plans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.display.PlanStatusBadge
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.training.domain.model.PlanStatus

@Composable
fun PlanSummaryCard(
    name: String,
    status: PlanStatus,
    subtitle: String,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    activateButtonText: String = stringResource(R.string.training_hub_activate_button),
    deleteContentDescription: String = stringResource(R.string.common_delete),
    modifier: Modifier = Modifier,
) {
    AiFitCard(onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (status != PlanStatus.ACTIVE) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Trash,
                            contentDescription = deleteContentDescription,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                PlanStatusBadge(status = status.name)
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (status != PlanStatus.ACTIVE && status != PlanStatus.COMPLETED) {
                TextButton(onClick = onActivate) {
                    Text(
                        text = activateButtonText.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
        }
    }
}
