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
 * Cabecera hero compartida por todas las pantallas de autenticación.
 *
 * Muestra el logo de la app centrado, precedido del "breathing room" superior
 * definido en el Design System (xxl = 48 dp) y seguido del título de la
 * pantalla con el nivel tipográfico [MaterialTheme.typography.displaySmall]
 * (lg = 24 dp de separación).
 *
 * @param title Texto del título que se renderiza bajo el logo.
 * @param modifier Modificador opcional para personalizar el contenedor externo.
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
