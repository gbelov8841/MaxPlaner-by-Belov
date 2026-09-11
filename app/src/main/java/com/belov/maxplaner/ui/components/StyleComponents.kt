package com.belov.maxplaner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
fun styleBorder(selected: Boolean = false): BorderStroke {
    val tokens = LocalStyleTokens.current
    val width = if (selected) tokens.selectedBorderWidth else tokens.borderWidth
    if (tokens.floatingGlass && !selected) {
        return BorderStroke(
            width,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = .22f),
                    MaterialTheme.colorScheme.primary.copy(alpha = .18f),
                    Color.White.copy(alpha = .06f)
                )
            )
        )
    }
    val color = if (selected || tokens.accentBorders) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant
    return BorderStroke(width, color.copy(alpha = if (selected) 1f else tokens.borderAlpha))
}

/**
 * Floating Glass uses translucent layered surfaces and light gradients.
 * This deliberately does not claim backdrop blur: Compose is only drawing transparent layers here.
 */
@Composable
fun PlannerBackdrop(modifier: Modifier = Modifier) {
    val tokens = LocalStyleTokens.current
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                if (tokens.floatingGlass) {
                    val radius = maxOf(size.width, size.height) * .72f
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = .11f), Color.Transparent),
                            center = Offset(size.width * .12f, size.height * .06f),
                            radius = radius
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = .07f), Color.Transparent),
                            center = Offset(size.width * .90f, size.height * .72f),
                            radius = radius * .82f
                        )
                    )
                }
            }
    )
}

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
    val resolvedColors = if (tokens.floatingGlass) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = if (hero) (tokens.surfaceOpacity + .08f).coerceAtMost(.92f) else tokens.surfaceOpacity
            )
        )
    } else colors

    Card(
        modifier = modifier,
        shape = shape,
        colors = resolvedColors,
        border = styleBorder(selected),
        elevation = CardDefaults.cardElevation(defaultElevation = if (hero) tokens.heroElevation else tokens.cardElevation)
    ) {
        val highlight = when {
            hero && tokens.heroHighlightAlpha > 0f -> tokens.heroHighlightAlpha
            tokens.floatingGlass -> .035f
            else -> 0f
        }
        if (highlight > 0f) {
            Column(
                Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (tokens.floatingGlass) .055f else 0f),
                            MaterialTheme.colorScheme.primary.copy(alpha = highlight),
                            Color.Transparent
                        )
                    )
                ),
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
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val tokens = LocalStyleTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val baseAlpha = if (tokens.floatingGlass) tokens.surfaceOpacity else 1f
    val stateAlpha = when { !enabled -> tokens.disabledAlpha; pressed -> (1f - tokens.pressedAlpha); else -> 1f }
    val resolved = color.copy(alpha = baseAlpha * stateAlpha)
    val surfaceModifier = if (onClick != null) modifier.clip(shape).background(Color.Transparent).then(
        Modifier
    ) else modifier
    Surface(
        modifier = surfaceModifier,
        onClick = onClick ?: {},
        enabled = enabled && onClick != null,
        interactionSource = interactionSource,
        shape = shape,
        color = resolved,
        contentColor = contentColorFor(color),
        border = styleBorder(selected),
        tonalElevation = if (tokens.floatingGlass) tokens.cardElevation else androidx.compose.ui.unit.Dp.Unspecified,
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
        trackColor = if (tokens.floatingGlass) Color.White.copy(alpha = .10f) else MaterialTheme.colorScheme.outlineVariant,
        gapSize = tokens.progressHeight / 2,
        drawStopIndicator = {}
    )
}
