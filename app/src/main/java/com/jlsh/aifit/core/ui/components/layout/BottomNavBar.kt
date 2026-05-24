package com.jlsh.aifit.core.ui.components.layout

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.*
import com.adamglin.phosphoricons.regular.*
import com.jlsh.aifit.R
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitMotion

val LocalBottomBarVisibility = compositionLocalOf { true }

data class BottomNavItem(
    @StringRes val labelResId: Int,
    val iconOutline: ImageVector,
    val iconFilled: ImageVector,
    val route: String,
)

val bottomNavItems = listOf(
    BottomNavItem(
        labelResId = R.string.nav_home,
        iconOutline = PhosphorIcons.Regular.House,
        iconFilled = PhosphorIcons.Fill.House,
        route = "home",
    ),
    BottomNavItem(
        labelResId = R.string.nav_training,
        iconOutline = PhosphorIcons.Regular.Barbell,
        iconFilled = PhosphorIcons.Fill.Barbell,
        route = "training",
    ),
    BottomNavItem(
        labelResId = R.string.nav_nutrition,
        iconOutline = PhosphorIcons.Regular.ForkKnife,
        iconFilled = PhosphorIcons.Fill.ForkKnife,
        route = "nutrition",
    ),
    BottomNavItem(
        labelResId = R.string.nav_coach,
        iconOutline = PhosphorIcons.Regular.ChatTeardropDots,
        iconFilled = PhosphorIcons.Fill.ChatTeardropDots,
        route = "coach",
    ),
    BottomNavItem(
        labelResId = R.string.nav_profile,
        iconOutline = PhosphorIcons.Regular.UserCircle,
        iconFilled = PhosphorIcons.Fill.UserCircle,
        route = "profile",
    ),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            thickness = 1.dp,
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp,
        ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val label = stringResource(item.labelResId)
            val iconTint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            }
            val iconScale by animateFloatAsState(
                targetValue = if (selected) 1.12f else 1f,
                animationSpec = AiFitMotion.standardTween<Float>(),
                label = "navIconScale",
            )
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.iconFilled else item.iconOutline,
                        contentDescription = label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale),
                        tint = iconTint,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Unspecified,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
            )
        }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Composable
private fun BottomNavBarPreview() {
    AIFitTheme(darkTheme = true) {
        BottomNavBar(
            currentRoute = "home",
            onItemSelected = {},
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light"
)
@Composable
private fun BottomNavBarLightPreview() {
    AIFitTheme(darkTheme = false) {
        BottomNavBar(
            currentRoute = "nutrition",
            onItemSelected = {},
        )
    }
}
