package com.belov.maxplaner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.theme.LocalStyleTokens

/** Native checked semantics, a full touch target and feedback only on a user action. */
@Composable
fun CompletionButton(completed: Boolean, title: String, onToggle: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val tint by animateColorAsState(
        targetValue = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(LocalStyleTokens.current.motionDurationMillis),
        label = "Completion color"
    )
    IconToggleButton(
        checked = completed,
        onCheckedChange = {
            onToggle()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).semantics {
            contentDescription = title
            stateDescription = if (completed) "Выполнено" else "Не выполнено"
        }
    ) {
        Icon(
            if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = tint
        )
    }
}
