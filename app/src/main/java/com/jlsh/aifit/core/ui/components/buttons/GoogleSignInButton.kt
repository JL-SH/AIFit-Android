package com.jlsh.aifit.core.ui.components.buttons

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.FullShape

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Continuar con Google",
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        enabled = enabled && !isLoading,
        shape = FullShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GoogleSignInButton — Dark",
)
@Composable
private fun GoogleSignInButtonPreview() {
    AIFitTheme(darkTheme = true) {
        GoogleSignInButton(onClick = {})
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "GoogleSignInButton Loading — Dark",
)
@Composable
private fun GoogleSignInButtonLoadingPreview() {
    AIFitTheme(darkTheme = true) {
        GoogleSignInButton(onClick = {}, isLoading = true)
    }
}