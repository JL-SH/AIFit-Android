package com.jlsh.aifit.core.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

val AiFitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/** Cards principales — amplio, moderno (16 dp). */
val CardShape: Shape = RoundedCornerShape(16.dp)

/** Chips, badges and bar tracks — technical/sporty look (8 dp).*/
val ChipShape: Shape = RoundedCornerShape(8.dp)

/** Botones primarios y CTAs — equilibrado, no circular (12 dp). */
val ButtonShape: Shape = RoundedCornerShape(12.dp)

/** Selection indicator in bottom navigation — capsule.*/
val NavIndicatorShape: Shape = RoundedCornerShape(50)

/** @deprecated Use [NavIndicatorShape] for nav indicators or [ButtonShape] for CTAs. */
val FullShape: Shape = NavIndicatorShape

/**
 * Regular hexagon with the first vertex at the top.
 * Designed for gamification and plan status badges.
 */
val HexagonShape: Shape = GenericShape { size, _ ->
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = min(size.width, size.height) / 2f
    val startAngle = (-Math.PI / 2).toFloat()

    for (i in 0..5) {
        val angle = startAngle + (Math.PI / 3 * i).toFloat()
        val x = cx + radius * cos(angle)
        val y = cy + radius * sin(angle)
        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
}
