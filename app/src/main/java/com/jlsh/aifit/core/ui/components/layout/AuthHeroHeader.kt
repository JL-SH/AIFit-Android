package com.jlsh.aifit.core.ui.components.layout

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing

/**
 * Hero header shared by all authentication screens.
 *
 * Shows the app logo centered, preceded by the upper breathing room
 * defined in the Design System (xxl = 48 dp) and followed by the title of the
 * display with typographic level [MaterialTheme.typography.displaySmall]
 * (lg = 24 dp separation).
 *
 * @param title Title text that is rendered under the logo.
 * @param modifier Optional modifier to customize the outer container.
 */
@Composable
fun AuthHeroHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(AiFitSpacing.xxl))

        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(R.string.app_logo_desc),
            modifier = Modifier.size(96.dp),
        )

        Spacer(modifier = Modifier.height(AiFitSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "AuthHeroHeader — Login Dark",
)
@Composable
private fun AuthHeroHeaderLoginPreview() {
    AIFitTheme(darkTheme = true) {
        AuthHeroHeader(title = "Tu entrenador de IA personal")
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "AuthHeroHeader — Register Dark",
)
@Composable
private fun AuthHeroHeaderRegisterPreview() {
    AIFitTheme(darkTheme = true) {
        AuthHeroHeader(title = "Crea tu cuenta")
    }
}
