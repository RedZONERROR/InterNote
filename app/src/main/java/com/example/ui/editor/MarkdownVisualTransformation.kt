package com.example.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MarkdownVisualTransformation(private val isDark: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val parsed = MarkdownRenderer.parse(text.text, isDark)
        return TransformedText(parsed, OffsetMapping.Identity)
    }
}
