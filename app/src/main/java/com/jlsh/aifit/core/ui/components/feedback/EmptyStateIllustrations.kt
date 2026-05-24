package com.jlsh.aifit.core.ui.components.feedback

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class EmptyStateKind {
    TrainingPlans,
    TrainingDays,
    WorkoutHistory,
    NutritionTarget,
    NutritionMeals,
    NutritionDietPlans,
    ChatSessions,
    BodyWeight,
    FoodVision,
    Glossary,
    Metabolic,
    Achievements,
    StreakEmpty,
    ShoppingList,
}

@Composable
fun EmptyStateIllustration(
    kind: EmptyStateKind,
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = 72.dp,
) {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    val accentColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)

    Canvas(modifier = modifier.size(sizeDp)) {
        when (kind) {
            EmptyStateKind.TrainingPlans,
            EmptyStateKind.TrainingDays,
            -> drawBarbell(primaryColor, outlineColor)
            EmptyStateKind.WorkoutHistory -> drawTimeline(outlineColor, primaryColor)
            EmptyStateKind.NutritionTarget,
            EmptyStateKind.NutritionMeals,
            EmptyStateKind.NutritionDietPlans,
            -> drawPlate(outlineColor, primaryColor, accentColor)
            EmptyStateKind.ChatSessions -> drawChatBubbles(primaryColor, outlineColor)
            EmptyStateKind.BodyWeight -> drawScale(outlineColor, primaryColor)
            EmptyStateKind.FoodVision -> drawCamera(outlineColor, primaryColor)
            EmptyStateKind.Glossary -> drawBook(outlineColor, primaryColor)
            EmptyStateKind.Metabolic -> drawChart(outlineColor, primaryColor, accentColor)
            EmptyStateKind.Achievements,
            EmptyStateKind.StreakEmpty,
            -> drawTrophy(outlineColor, primaryColor)
            EmptyStateKind.ShoppingList -> drawCart(outlineColor, primaryColor)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBarbell(
    primary: Color,
    outline: Color,
) {
    val barY = size.height * 0.5f
    val stroke = 3.dp.toPx()
    drawLine(outline, Offset(size.width * 0.12f, barY), Offset(size.width * 0.88f, barY), stroke, StrokeCap.Round)
    drawCircle(primary, size.width * 0.14f, center = Offset(size.width * 0.14f, barY))
    drawCircle(primary, size.width * 0.14f, center = Offset(size.width * 0.86f, barY))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTimeline(
    outline: Color,
    primary: Color,
) {
    val stroke = 2.5.dp.toPx()
    val baseY = size.height * 0.72f
    drawLine(outline, Offset(size.width * 0.15f, baseY), Offset(size.width * 0.85f, baseY), stroke, StrokeCap.Round)
    listOf(0.22f, 0.42f, 0.62f, 0.82f).forEachIndexed { i, x ->
        val h = size.height * (0.35f + i * 0.08f)
        drawLine(
            if (i == 3) primary else outline,
            Offset(size.width * x, baseY),
            Offset(size.width * x, baseY - h),
            stroke,
            StrokeCap.Round,
        )
        drawCircle(primary.copy(alpha = 0.7f), 4.dp.toPx(), Offset(size.width * x, baseY - h))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlate(
    outline: Color,
    primary: Color,
    accent: Color,
) {
    val cx = size.width / 2f
    val cy = size.height * 0.55f
    val r = size.width * 0.32f
    drawCircle(outline, r, center = Offset(cx, cy), style = Stroke(2.5.dp.toPx()))
    drawArc(primary, 200f, 80f, false, Offset(cx - r * 0.6f, cy - r * 0.5f), Size(r * 1.2f, r), style = Stroke(3.dp.toPx()))
    drawCircle(accent, size.width * 0.07f, center = Offset(cx + r * 0.35f, cy - r * 0.2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChatBubbles(
    primary: Color,
    outline: Color,
) {
    val stroke = 2.dp.toPx()
    drawRoundRect(
        outline,
        topLeft = Offset(size.width * 0.1f, size.height * 0.2f),
        size = Size(size.width * 0.55f, size.height * 0.35f),
        cornerRadius = CornerRadius(12.dp.toPx()),
        style = Stroke(stroke),
    )
    drawRoundRect(
        primary.copy(alpha = 0.35f),
        topLeft = Offset(size.width * 0.35f, size.height * 0.42f),
        size = Size(size.width * 0.5f, size.height * 0.32f),
        cornerRadius = CornerRadius(12.dp.toPx()),
    )
    drawLine(primary, Offset(size.width * 0.22f, size.height * 0.38f), Offset(size.width * 0.5f, size.height * 0.38f), 2.dp.toPx(), StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScale(
    outline: Color,
    primary: Color,
) {
    val cx = size.width / 2f
    drawLine(outline, Offset(cx, size.height * 0.18f), Offset(cx, size.height * 0.82f), 2.5.dp.toPx(), StrokeCap.Round)
    drawLine(outline, Offset(size.width * 0.2f, size.height * 0.35f), Offset(size.width * 0.8f, size.height * 0.35f), 2.5.dp.toPx(), StrokeCap.Round)
    drawOval(primary.copy(alpha = 0.3f), topLeft = Offset(size.width * 0.28f, size.height * 0.48f), size = Size(size.width * 0.18f, size.height * 0.12f))
    drawOval(outline, topLeft = Offset(size.width * 0.54f, size.height * 0.55f), size = Size(size.width * 0.18f, size.height * 0.12f), style = Stroke(2.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCamera(
    outline: Color,
    primary: Color,
) {
    drawRoundRect(
        outline,
        topLeft = Offset(size.width * 0.15f, size.height * 0.28f),
        size = Size(size.width * 0.7f, size.height * 0.5f),
        cornerRadius = CornerRadius(10.dp.toPx()),
        style = Stroke(2.5.dp.toPx()),
    )
    drawCircle(primary.copy(alpha = 0.4f), size.width * 0.16f, center = Offset(size.width / 2f, size.height * 0.53f))
    drawRoundRect(primary.copy(alpha = 0.5f), topLeft = Offset(size.width * 0.32f, size.height * 0.18f), size = Size(size.width * 0.2f, size.height * 0.1f), cornerRadius = CornerRadius(4.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBook(
    outline: Color,
    primary: Color,
) {
    val path = Path().apply {
        moveTo(size.width * 0.25f, size.height * 0.2f)
        lineTo(size.width * 0.75f, size.height * 0.2f)
        lineTo(size.width * 0.75f, size.height * 0.8f)
        lineTo(size.width * 0.25f, size.height * 0.8f)
        close()
    }
    drawPath(path, outline, style = Stroke(2.5.dp.toPx()))
    drawLine(primary, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), 2.dp.toPx())
    repeat(3) { i ->
        val y = size.height * (0.38f + i * 0.12f)
        drawLine(outline.copy(alpha = 0.6f), Offset(size.width * 0.32f, y), Offset(size.width * 0.46f, y), 1.5.dp.toPx(), StrokeCap.Round)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChart(
    outline: Color,
    primary: Color,
    accent: Color,
) {
    drawLine(outline, Offset(size.width * 0.15f, size.height * 0.78f), Offset(size.width * 0.85f, size.height * 0.78f), 2.dp.toPx(), StrokeCap.Round)
    val path = Path().apply {
        moveTo(size.width * 0.2f, size.height * 0.65f)
        lineTo(size.width * 0.4f, size.height * 0.45f)
        lineTo(size.width * 0.58f, size.height * 0.55f)
        lineTo(size.width * 0.8f, size.height * 0.28f)
    }
    drawPath(path, primary, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    drawCircle(accent, 5.dp.toPx(), Offset(size.width * 0.8f, size.height * 0.28f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrophy(
    outline: Color,
    primary: Color,
) {
    drawPath(
        Path().apply {
            moveTo(size.width * 0.35f, size.height * 0.25f)
            lineTo(size.width * 0.65f, size.height * 0.25f)
            lineTo(size.width * 0.6f, size.height * 0.5f)
            lineTo(size.width * 0.4f, size.height * 0.5f)
            close()
        },
        outline,
        style = Stroke(2.5.dp.toPx()),
    )
    drawRoundRect(primary.copy(alpha = 0.35f), topLeft = Offset(size.width * 0.38f, size.height * 0.55f), size = Size(size.width * 0.24f, size.height * 0.12f), cornerRadius = CornerRadius(4.dp.toPx()))
    drawLine(outline, Offset(size.width * 0.3f, size.height * 0.35f), Offset(size.width * 0.22f, size.height * 0.5f), 2.dp.toPx(), StrokeCap.Round)
    drawLine(outline, Offset(size.width * 0.7f, size.height * 0.35f), Offset(size.width * 0.78f, size.height * 0.5f), 2.dp.toPx(), StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCart(
    outline: Color,
    primary: Color,
) {
    drawRoundRect(outline, topLeft = Offset(size.width * 0.2f, size.height * 0.35f), size = Size(size.width * 0.5f, size.height * 0.35f), cornerRadius = CornerRadius(6.dp.toPx()), style = Stroke(2.5.dp.toPx()))
    drawLine(outline, Offset(size.width * 0.15f, size.height * 0.35f), Offset(size.width * 0.28f, size.height * 0.35f), 2.5.dp.toPx(), StrokeCap.Round)
    drawCircle(primary, 4.dp.toPx(), Offset(size.width * 0.32f, size.height * 0.78f))
    drawCircle(primary, 4.dp.toPx(), Offset(size.width * 0.58f, size.height * 0.78f))
}
