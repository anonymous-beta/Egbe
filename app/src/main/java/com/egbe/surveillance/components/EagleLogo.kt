package com.egbe.surveillance.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.egbe.surveillance.ui.theme.EGBEGreen

@Composable
fun EagleLogo(size: Dp = 64.dp, color: Color = EGBEGreen) {
    Canvas(modifier = Modifier.size(size)) {
        val strokeWidth = size.value * 0.08f
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        val s = size.toPx()

        // Eagle body (diamond shape)
        drawLine(
            color = color,
            start = Offset(centerX, centerY - s * 0.35f),
            end = Offset(centerX - s * 0.25f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX, centerY - s * 0.35f),
            end = Offset(centerX + s * 0.25f, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX - s * 0.25f, centerY),
            end = Offset(centerX, centerY + s * 0.35f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX + s * 0.25f, centerY),
            end = Offset(centerX, centerY + s * 0.35f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Wings
        drawLine(
            color = color,
            start = Offset(centerX - s * 0.25f, centerY - s * 0.1f),
            end = Offset(centerX - s * 0.4f, centerY + s * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(centerX + s * 0.25f, centerY - s * 0.1f),
            end = Offset(centerX + s * 0.4f, centerY + s * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
