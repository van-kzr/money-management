package com.example.moneymanagement.presentation.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        if (digits.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        val number = digits.toLongOrNull() ?: 0L
        val locale = Locale.forLanguageTag("id-ID")
        val formatted = NumberFormat.getNumberInstance(locale).format(number)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, digits.length)
                val sub = digits.take(clampedOffset)
                val formattedSub = if (sub.isNotEmpty()) {
                    NumberFormat.getNumberInstance(locale).format(sub.toLongOrNull() ?: 0L)
                } else {
                    ""
                }
                return formattedSub.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clampedOffset = offset.coerceIn(0, formatted.length)
                val unformatted = formatted.take(clampedOffset).filter { it.isDigit() }
                return unformatted.length
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
