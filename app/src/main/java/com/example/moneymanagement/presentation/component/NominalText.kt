package com.example.moneymanagement.presentation.component

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@Composable
fun NominalText(
    amountText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    var containerWidth by remember { mutableIntStateOf(0) }
    val textMeasurer = rememberTextMeasurer()

    val textWidth = remember(amountText, style) {
        textMeasurer.measure(
            text = amountText,
            style = style
        ).size.width
    }

    val isOverflowing = containerWidth > 0 && textWidth > containerWidth

    Box(
        modifier = modifier.onSizeChanged {
            containerWidth = it.width
        }
    ) {
        Text(
            text = amountText,
            modifier = if (isOverflowing) {
                Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    animationMode = MarqueeAnimationMode.Immediately,
                    spacing = MarqueeSpacing(32.dp),
                    velocity = 40.dp
                )
            } else {
                Modifier
            },
            style = style,
            color = color,
            maxLines = 1
        )
    }
}
