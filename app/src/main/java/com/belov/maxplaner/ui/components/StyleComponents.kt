package com.belov.maxplaner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
fun styleBorder(selected: Boolean = false): BorderStroke {
    val tokens = LocalStyleTokens.current
    val color = if (selected || tokens.accentBorders) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant
    return BorderStroke(
        if (selected) tokens.selectedBorderWidth else tokens.borderWidth,
        color.copy(alpha = if (selected) 1f else tokens.borderAlpha)
    )
}

/** Shared surfaces keep every screen responsive to the selected style. No blur passes. */
@Composable
fun PlannerCard(
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    selected: Boolean = false,
    shape: Shape = if (hero) LocalStyleTokens.current.heroShape else LocalStyleTokens.current.cardShape,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalStyleTokens.current
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        border = styleBorder(selected),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hero) tokens.heroElevation else tokens.cardElevation)
    ) {
        if (hero && tokens.heroHighlightAlpha > 0f) {
            Column(
                Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = tokens.heroHighlightAlpha),
                    Color.Transparent
                ))),
                content = content
            )
        } else content()
    }
}

@Composable
fun PlannerSurface(
    modifier: Modifier = Modifier,
    shape: Shape = LocalStyleTokens.current.compactShape,
    color: Color = MaterialTheme.colorScheme.surface,
    selected: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColorFor(color),
        border = styleBorder(selected),
        content = content
    )
}

@Composable
fun PlannerProgressIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    val tokens = LocalStyleTokens.current
    val animated by animateFloatAsState(
        targetValue = progress().coerceIn(0f, 1f),
        animationSpec = tween(tokens.motionDurationMillis),
        label = "Planner progress"
    )
    LinearProgressIndicator(
        progress = { animated },
        modifier = modifier.height(tokens.progressHeight).clip(tokens.progressShape),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
        gapSize = tokens.progressHeight / 2,
        drawStopIndicator = {}
    )
}
