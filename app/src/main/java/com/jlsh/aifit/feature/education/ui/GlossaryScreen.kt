package com.jlsh.aifit.feature.education.ui

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jlsh.aifit.core.ui.components.display.AiFitCard
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateKind
import com.jlsh.aifit.core.ui.components.feedback.EmptyStateView
import com.jlsh.aifit.core.ui.components.feedback.ErrorContent
import com.jlsh.aifit.core.ui.components.feedback.InlineLoadingIndicator
import com.jlsh.aifit.core.ui.components.inputs.AiFitTextField
import com.jlsh.aifit.core.ui.components.layout.AiFitTopBar
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import com.jlsh.aifit.feature.education.domain.model.GlossaryDefinition
import com.jlsh.aifit.feature.education.ui.components.EducationConfirmSheet
import com.jlsh.aifit.feature.education.ui.state.GlossaryState

/**
 * Fitness and nutrition educational glossary search screen.
 *
 * Shows search field, confirmation sheet before query, empty/load/error statuses
 * and card with definition and chips of navigable related terms.
 *
 * @param onNavigateBack Returns to the previous screen.
 * @param viewModel Education ViewModel injected by Hilt.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: EducationViewModel = hiltViewModel(),
) {
    val glossaryState by viewModel.glossaryState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }
    var showConfirmSheet by remember { mutableStateOf(false) }


    if (showConfirmSheet && searchQuery.isNotBlank()) {
        EducationConfirmSheet(
            title = stringResource(R.string.education_glossary_confirm_title),
            description = stringResource(R.string.education_glossary_confirm_description),
            confirmText = stringResource(R.string.education_glossary_confirm_button),
            onDismiss = { showConfirmSheet = false },
            onConfirm = {
                showConfirmSheet = false
                viewModel.loadGlossaryTerm(searchQuery.trim())
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AiFitTopBar(
                    title = stringResource(R.string.education_glossary_title),
                    onBack = onNavigateBack,
                    background = MaterialTheme.colorScheme.background,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AiFitSpacing.md),
            ) {
                AiFitTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = stringResource(R.string.education_glossary_search_label),
                    trailingIcon = PhosphorIcons.Regular.MagnifyingGlass,
                    onTrailingIconClick = {
                        if (searchQuery.isNotBlank()) {
                            showConfirmSheet = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(AiFitSpacing.md))

                when (val state = glossaryState) {
                    is GlossaryState.Idle -> {
                        EmptyStateView(
                            icon = PhosphorIcons.Regular.BookOpen,
                            kind = EmptyStateKind.Glossary,
                            title = stringResource(R.string.education_glossary_empty_title),
                            subtitle = stringResource(R.string.education_glossary_empty_subtitle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AiFitSpacing.xxl),
                        )
                    }

                    is GlossaryState.Loading -> {
                        InlineLoadingIndicator(
                            message = stringResource(R.string.education_glossary_loading),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AiFitSpacing.lg),
                        )
                    }

                    is GlossaryState.Success -> {
                        GlossaryCard(
                            definition = state.data,
                            onRelatedTermClick = { term ->
                                searchQuery = term
                                viewModel.loadGlossaryTerm(term)
                            },
                        )
                    }

                    is GlossaryState.Error -> {
                        ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.loadGlossaryTerm(searchQuery.trim()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AiFitSpacing.lg),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GlossaryCard(
    definition: GlossaryDefinition,
    onRelatedTermClick: (String) -> Unit,
) {
    AiFitCard {
        Column(
            modifier = Modifier.padding(AiFitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
        ) {
            Text(
                text = definition.term,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = definition.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (definition.relatedTerms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AiFitSpacing.xs))
                Text(
                    text = stringResource(R.string.education_glossary_related_header),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AiFitSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AiFitSpacing.xs),
                ) {
                    definition.relatedTerms.forEach { term ->
                        AssistChip(
                            onClick = { onRelatedTermClick(term) },
                            label = {
                                Text(
                                    text = term,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GlossaryScreen Dark",
)
@Composable
private fun GlossaryScreenPreview() {
    AIFitTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier.padding(AiFitSpacing.md),
            ) {
                GlossaryCard(
                    definition = GlossaryDefinition(
                        term = "Progressive Overload",
                        definition = "The gradual increase of stress placed upon the body during exercise training to continually make gains in muscle size, strength, and endurance.",
                        category = "TRAINING",
                        relatedTerms = listOf("Hypertrophy", "Volume", "Intensity"),
                    ),
                    onRelatedTermClick = {},
                )
            }
        }
    }
}



