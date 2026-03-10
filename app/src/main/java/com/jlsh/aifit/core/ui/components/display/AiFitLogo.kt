package com.jlsh.aifit.core.ui.components.display

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.jlsh.aifit.core.ui.theme.AIFitTheme

@Composable
fun AiFitLogoSplit() {
    val lime = MaterialTheme.colorScheme.primaryContainer
    val soft = MaterialTheme.colorScheme.onSurface

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = lime)) {
                append("AI")
            }
            withStyle(SpanStyle(color = soft)) {
                append("Fit")
            }
        },
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "AiFitLogo Split — Dark",
)
@Composable
private fun AiFitLogoSplitPreview() {
    AIFitTheme(darkTheme = true) {
        AiFitLogoSplit()
    }
}

