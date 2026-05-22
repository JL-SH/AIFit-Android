package com.jlsh.aifit.core.ui.components.layout

import android.content.res.Configuration
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.R
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFitTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    background: Color = MaterialTheme.colorScheme.surface,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = stringResource(R.string.common_back),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = background,
            scrolledContainerColor = background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - With Back")
@Composable
private fun AiFitTopBarWithBackPreview() {
    AIFitTheme { AiFitTopBar(title = "Training", onBack = {}) }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark - No Back")
@Composable
private fun AiFitTopBarNoBackPreview() {
    AIFitTheme { AiFitTopBar(title = "Home") }
}
