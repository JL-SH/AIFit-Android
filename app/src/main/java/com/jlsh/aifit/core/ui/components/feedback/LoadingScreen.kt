package com.jlsh.aifit.core.ui.components.feedback

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.theme.AIFitTheme

/**
 * Full-screen centred loading indicator shown while asynchronous data is being fetched.
 *
 * Fills the available space with [MaterialTheme.colorScheme.background] and renders
 * a single [CircularProgressIndicator] in the centre. Tagged with `"loading_screen"`
 * for UI tests.
 *
 * @param modifier Modifier applied to the outer [Box].
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("loading_screen"),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun LoadingScreenPreview() {
    AIFitTheme(darkTheme = true) {
        LoadingScreen()
    }
}
