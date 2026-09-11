package com.belov.maxplaner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.theme.*

@Composable
fun styleBorder(selected: Boolean = false): BorderStroke {
    val p = LocalThemePack.current
    return BorderStroke(if (selected) 1.dp else .5.dp,
        if (selected) p.accent.copy(alpha = .7f) else p.surfaces.border.copy(alpha = p.surfaces.borderAlpha))
}

/** One decoded background per theme. No expensive per-card blur or bitmap layers. */
@Composable
fun PlannerBackdrop(modifier: Modifier = Modifier) {
    val p = LocalThemePack.current
    Box(modifier.fillMaxSize().background(p.background)) {
        Image(painterResource(p.artwork.resource), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            alignment = BiasAlignment(p.artwork.focalX * 2 - 1, p.artwork.focalY * 2 - 1))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to p.background.copy(alpha = p.artwork.topScrim),
            .48f to p.background.copy(alpha = p.artwork.middleScrim),
            1f to p.background.copy(alpha = p.artwork.bottomScrim))))
    }
}

@Composable
fun PlannerCard(
    modifier: Modifier = Modifier, hero: Boolean = false, selected: Boolean = false,
    onClick: (() -> Unit)? = null, enabled: Boolean = true,
    shape: Shape = LocalStyleTokens.current.cardShape,
    colors: CardColors = CardDefaults.cardColors(), content: @Composable ColumnScope.() -> Unit
) {
    val p = LocalThemePack.current
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val alpha = (p.surfaces.opacity + if (pressed) p.states.pressedAlpha else 0f).coerceAtMost(1f)
    val cardColors = CardDefaults.cardColors(
        containerColor = p.surfaces.color.copy(alpha = alpha), contentColor = p.text,
        disabledContainerColor = p.surfaces.color.copy(alpha = alpha),
        disabledContentColor = p.text.copy(alpha = p.states.disabledAlpha))
    val elevation = CardDefaults.cardElevation(defaultElevation = p.surfaces.elevation)
    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = cardColors, border = styleBorder(selected), elevation = elevation, content = content)
    } else {
        Card(onClick = onClick, enabled = enabled, modifier = modifier, shape = shape,
            colors = cardColors, border = styleBorder(selected), elevation = elevation,
            interactionSource = interactions, content = content)
    }
}

@Composable
fun PlannerSurface(
    modifier: Modifier = Modifier, shape: Shape = LocalStyleTokens.current.compactShape,
    color: Color = MaterialTheme.colorScheme.surface, selected: Boolean = false,
    onClick: (() -> Unit)? = null, enabled: Boolean = true, content: @Composable () -> Unit
) {
    val p = LocalThemePack.current
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val base = if (color == MaterialTheme.colorScheme.surface) p.surfaces.color.copy(alpha = p.surfaces.opacity) else color
    val background = if (selected) p.accent.copy(alpha = p.states.selectionAlpha) else base
    val resolved = if (pressed) background.copy(alpha = (background.alpha + p.states.pressedAlpha).coerceAtMost(1f)) else background
    val foreground = if (color == p.accent && !selected) p.onAccent else p.text
    val glowModifier = if (selected && p.surfaces.glowAlpha > 0) modifier.drawBehind {
        drawRect(Brush.radialGradient(listOf(p.accent.copy(alpha = p.surfaces.glowAlpha), Color.Transparent),
            Offset(size.width / 2, size.height / 2), maxOf(size.width, size.height) / 2 + p.surfaces.glowRadius.toPx()))
    } else modifier
    if (onClick == null) {
        Surface(modifier = glowModifier, shape = shape, color = resolved, contentColor = foreground,
            border = styleBorder(selected), tonalElevation = 0.dp, content = content)
    } else {
        Surface(onClick = onClick, enabled = enabled, modifier = glowModifier, shape = shape, color = resolved,
            contentColor = if (enabled) foreground else foreground.copy(alpha = p.states.disabledAlpha),
            border = styleBorder(selected), tonalElevation = 0.dp, interactionSource = interactions, content = content)
    }
}

@Composable
fun PlannerProgressIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    val p = LocalThemePack.current
    val animated by animateFloatAsState(progress().coerceIn(0f, 1f), tween(160), label = "Planner progress")
    LinearProgressIndicator(progress = { animated }, modifier = modifier.height(p.progressHeight).clip(LocalStyleTokens.current.progressShape),
        color = p.accent, trackColor = p.secondaryText.copy(alpha = .18f), gapSize = 0.dp, drawStopIndicator = {})
}
