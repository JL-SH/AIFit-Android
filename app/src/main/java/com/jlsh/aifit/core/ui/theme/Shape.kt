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

/** Chips, badges y tracks de barras — look técnico/deportivo (8 dp). */
val ChipShape: Shape = RoundedCornerShape(8.dp)

/** Botones primarios y CTAs — equilibrado, no circular (12 dp). */
val ButtonShape: Shape = RoundedCornerShape(12.dp)

/** Indicador de selección en bottom navigation — cápsula. */
val NavIndicatorShape: Shape = RoundedCornerShape(50)

/** @deprecated Use [NavIndicatorShape] for nav indicators or [ButtonShape] for CTAs. */
val FullShape: Shape = NavIndicatorShape

/**
 * Hexágono regular con el primer vértice en la parte superior.
 * Pensado para badges de gamificación y estado de plan.
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
