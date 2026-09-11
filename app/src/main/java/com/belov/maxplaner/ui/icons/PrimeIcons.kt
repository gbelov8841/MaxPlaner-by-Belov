package com.belov.maxplaner.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Approved set A: consistent 24-unit outlines, tinted by the active theme. */
object PrimeIcons {
    private fun icon(name: String, draw: PathBuilder.() -> Unit): ImageVector = ImageVector.Builder(
        name = "Prime.$name", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero, pathBuilder = draw)
    }.build()
    val Home = icon("Home") {
        moveTo(2f, 10f); lineTo(12f, 2f); lineTo(22f, 10f)
        moveTo(5f, 8f); verticalLineTo(22f); horizontalLineTo(19f); verticalLineTo(8f)
        moveTo(9f, 22f); verticalLineTo(15f); horizontalLineTo(15f); verticalLineTo(22f)
    }
    val Calendar = icon("Calendar") {
        moveTo(7f, 2f); verticalLineTo(6f); moveTo(17f, 2f); verticalLineTo(6f)
        moveTo(3f, 9f); horizontalLineTo(21f)
        moveTo(5f, 4f); horizontalLineTo(19f); quadraticBezierTo(21f, 4f, 21f, 6f); verticalLineTo(20f)
        quadraticBezierTo(21f, 22f, 19f, 22f); horizontalLineTo(5f); quadraticBezierTo(3f, 22f, 3f, 20f)
        verticalLineTo(6f); quadraticBezierTo(3f, 4f, 5f, 4f)
        listOf(7f, 12f, 17f).forEach { x -> moveTo(x, 13f); horizontalLineTo(x + .2f); moveTo(x, 17f); horizontalLineTo(x + .2f) }
    }
    val Progress = icon("Progress") {
        moveTo(3f, 14f); horizontalLineTo(6f); verticalLineTo(22f); horizontalLineTo(3f); close()
        moveTo(10f, 11f); horizontalLineTo(13f); verticalLineTo(22f); horizontalLineTo(10f); close()
        moveTo(17f, 7f); horizontalLineTo(20f); verticalLineTo(22f); horizontalLineTo(17f); close()
        moveTo(3f, 9f); lineTo(9f, 3f); lineTo(13f, 6f); lineTo(21f, 1f)
        moveTo(17f, 1f); horizontalLineTo(21f); verticalLineTo(5f)
    }
    val More = icon("More") {
        listOf(5f, 12f, 19f).forEach { x ->
            moveTo(x + 1f, 12f); arcTo(1f, 1f, 0f, false, true, x - 1f, 12f); arcTo(1f, 1f, 0f, false, true, x + 1f, 12f); close()
        }
    }
    val Health = icon("Health") {
        moveTo(12f, 21f); cubicTo(8f, 18f, 2f, 13f, 2f, 7f)
        cubicTo(2f, 2f, 8f, 1f, 12f, 6f); cubicTo(16f, 1f, 22f, 2f, 22f, 7f)
        cubicTo(22f, 13f, 16f, 18f, 12f, 21f)
        moveTo(3f, 12f); horizontalLineTo(8f); lineTo(10f, 8f); lineTo(13f, 16f); lineTo(15f, 12f); horizontalLineTo(20f)
    }
    val Sport = icon("Sport") {
        moveTo(8f, 12f); horizontalLineTo(16f)
        moveTo(5f, 5f); horizontalLineTo(8f); verticalLineTo(19f); horizontalLineTo(5f); close()
        moveTo(16f, 5f); horizontalLineTo(19f); verticalLineTo(19f); horizontalLineTo(16f); close()
        moveTo(2f, 8f); horizontalLineTo(5f); verticalLineTo(16f); horizontalLineTo(2f); close()
        moveTo(19f, 8f); horizontalLineTo(22f); verticalLineTo(16f); horizontalLineTo(19f); close()
    }
    val Book = icon("Book") {
        moveTo(12f, 5f); cubicTo(8f, 2f, 5f, 2f, 2f, 4f); verticalLineTo(21f)
        cubicTo(5f, 19f, 8f, 19f, 12f, 22f); cubicTo(16f, 19f, 19f, 19f, 22f, 21f)
        verticalLineTo(4f); cubicTo(19f, 2f, 16f, 2f, 12f, 5f); verticalLineTo(22f)
    }
    val Shield = icon("Shield") {
        moveTo(12f, 2f); cubicTo(9f, 4f, 6f, 5f, 3f, 5f); verticalLineTo(11f)
        cubicTo(3f, 17f, 7f, 20f, 12f, 23f); cubicTo(17f, 20f, 21f, 17f, 21f, 11f)
        verticalLineTo(5f); cubicTo(18f, 5f, 15f, 4f, 12f, 2f)
        moveTo(8f, 12f); lineTo(11f, 15f); lineTo(17f, 9f)
    }
    val Wallet = icon("Wallet") {
        moveTo(20f, 7f); verticalLineTo(4f); horizontalLineTo(5f); quadraticBezierTo(2f, 4f, 2f, 7f)
        verticalLineTo(19f); quadraticBezierTo(2f, 22f, 5f, 22f); horizontalLineTo(21f); verticalLineTo(7f); horizontalLineTo(5f)
        moveTo(21f, 12f); horizontalLineTo(17f); quadraticBezierTo(14f, 12f, 14f, 15f)
        quadraticBezierTo(14f, 18f, 17f, 18f); horizontalLineTo(21f); moveTo(17f, 15f); horizontalLineTo(17.2f)
    }
    val Repeat = icon("Repeat") {
        moveTo(3f, 12f); verticalLineTo(9f); cubicTo(3f, 5f, 6f, 3f, 10f, 3f); horizontalLineTo(20f)
        moveTo(16f, 0.9f); lineTo(20f, 3f); lineTo(16f, 6f)
        moveTo(21f, 12f); verticalLineTo(15f); cubicTo(21f, 19f, 18f, 21f, 14f, 21f); horizontalLineTo(4f)
        moveTo(8f, 18f); lineTo(4f, 21f); lineTo(8f, 23.1f)
    }
}
