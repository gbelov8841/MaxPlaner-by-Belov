package com.belov.maxplaner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.belov.maxplaner.ui.components.PlannerSurface
import com.belov.maxplaner.ui.theme.LocalStyleTokens

@Composable
internal fun RowScope.QuickAddCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tokens = LocalStyleTokens.current
    PlannerSurface(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        shape = tokens.compactShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(tokens.cardPadding)) {
            Surface(
                shape = tokens.compactShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp).size(24.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
