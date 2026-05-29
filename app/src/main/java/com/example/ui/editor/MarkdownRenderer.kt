package com.example.ui.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color

object MarkdownRenderer {

    fun parse(text: String, darkTheme: Boolean = true): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split("\n")
            lines.forEachIndexed { index, line ->
                val isHeader = line.startsWith("# ")
                val isBullet = line.startsWith("- ") || line.startsWith("* ")
                
                val cleanLine = when {
                    isHeader -> "⌗  " + line.substring(2)
                    isBullet -> "•  " + line.substring(2)
                    else -> line
                }

                val startIdx = length
                append(cleanLine)
                val endIdx = length

                // Style block level items
                if (isHeader) {
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (darkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2)
                        ),
                        startIdx,
                        endIdx
                    )
                } else if (isBullet) {
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            color = if (darkTheme) Color(0xFF81C784) else Color(0xFF388E3C)
                        ),
                        startIdx,
                        startIdx + 3
                    )
                }

                // Style inline decorations (syntax markers + text)
                parseInlineStyles(cleanLine, startIdx, darkTheme)

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.parseInlineStyles(lineText: String, lineStartOffset: Int, darkTheme: Boolean) {
        val syntaxColor = if (darkTheme) Color(0x7FFFFFFF) else Color(0x7F000000)

        // Bold match
        Regex("(\\*\\*)(.*?)\\1").findAll(lineText).forEach { match ->
            val matchedRange = match.range
            addStyle(
                SpanStyle(fontWeight = FontWeight.Bold),
                lineStartOffset + matchedRange.first,
                lineStartOffset + matchedRange.last + 1
            )
            // Color syntax tags slightly lighter
            match.groups[1]?.let { grp ->
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.first,
                    lineStartOffset + matchedRange.first + grp.value.length
                )
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.last + 1 - grp.value.length,
                    lineStartOffset + matchedRange.last + 1
                )
            }
        }

        // Italic match
        Regex("(\\*|_)(.*?)\\1").findAll(lineText).forEach { match ->
            // Ensure we don't duplicate on double bold stars
            if (!match.value.startsWith("**")) {
                val matchedRange = match.range
                addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    lineStartOffset + matchedRange.first,
                    lineStartOffset + matchedRange.last + 1
                )
                match.groups[1]?.let { grp ->
                    addStyle(
                        SpanStyle(color = syntaxColor),
                        lineStartOffset + matchedRange.first,
                        lineStartOffset + matchedRange.first + grp.value.length
                    )
                    addStyle(
                        SpanStyle(color = syntaxColor),
                        lineStartOffset + matchedRange.last + 1 - grp.value.length,
                        lineStartOffset + matchedRange.last + 1
                    )
                }
            }
        }

        // Strikethrough match
        Regex("(~~)(.*?)\\1").findAll(lineText).forEach { match ->
            val matchedRange = match.range
            addStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough),
                lineStartOffset + matchedRange.first,
                lineStartOffset + matchedRange.last + 1
            )
            match.groups[1]?.let { grp ->
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.first,
                    lineStartOffset + matchedRange.first + grp.value.length
                )
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.last + 1 - grp.value.length,
                    lineStartOffset + matchedRange.last + 1
                )
            }
        }

        // Underline match (<u>text</u>)
        Regex("(<u>)(.*?)(</u>)").findAll(lineText).forEach { match ->
            val matchedRange = match.range
            addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline),
                lineStartOffset + matchedRange.first,
                lineStartOffset + matchedRange.last + 1
            )
            match.groups[1]?.let { grp ->
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.first,
                    lineStartOffset + matchedRange.first + grp.value.length
                )
            }
            match.groups[3]?.let { grp ->
                addStyle(
                    SpanStyle(color = syntaxColor),
                    lineStartOffset + matchedRange.last + 1 - grp.value.length,
                    lineStartOffset + matchedRange.last + 1
                )
            }
        }
    }
}
