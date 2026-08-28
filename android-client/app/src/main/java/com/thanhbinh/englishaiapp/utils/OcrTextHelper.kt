package com.thanhbinh.englishaiapp.utils

import com.google.mlkit.vision.text.Text

object OcrTextHelper {

    /**
     * Intelligently formats ML Kit OCR Text by:
     * 1. Preserving distinct paragraphs (different TextBlocks)
     * 2. Merging line-wrapped sentences within the same paragraph with a space instead of newline
     * 3. Recombining hyphenated words split across lines (e.g. "connec-" + "tion" -> "connection")
     * 4. Preserving list items (e.g., "1.", "2.", "•", "-")
     */
    @JvmStatic
    fun formatOcrText(visionText: Text): String {
        if (visionText.textBlocks.isEmpty()) {
            return cleanRawOcrText(visionText.text)
        }

        val result = StringBuilder()
        for (block in visionText.textBlocks) {
            val lines = block.lines
            if (lines.isEmpty()) continue

            val blockBuilder = StringBuilder()
            for (line in lines) {
                val currentLine = line.text.trim()
                if (currentLine.isEmpty()) continue

                if (blockBuilder.isEmpty()) {
                    blockBuilder.append(currentLine)
                } else {
                    // Check if previous line ended with hyphen for word-wrap
                    if (blockBuilder.endsWith("-")) {
                        val beforeHyphen = blockBuilder.dropLast(1)
                        if (beforeHyphen.isNotEmpty() && beforeHyphen.last().isLetter() && currentLine.first().isLetter()) {
                            blockBuilder.setLength(blockBuilder.length - 1)
                            blockBuilder.append(currentLine)
                        } else {
                            blockBuilder.append(" ").append(currentLine)
                        }
                    } else {
                        // Check if current line is a bullet or numbered list item
                        val isListItem = currentLine.matches(Regex("^([0-9]+[.)]|[-•*])\\s+.*"))
                        if (isListItem) {
                            blockBuilder.append("\n").append(currentLine)
                        } else {
                            // Merge unwrapped lines within paragraph with a space
                            blockBuilder.append(" ").append(currentLine)
                        }
                    }
                }
            }

            val blockText = blockBuilder.toString().trim()
            if (blockText.isNotEmpty()) {
                if (result.isNotEmpty()) {
                    result.append("\n\n")
                }
                result.append(blockText)
            }
        }

        return result.toString().trim()
    }

    /**
     * Clean raw OCR string if Vision Text object is not available
     */
    @JvmStatic
    fun cleanRawOcrText(rawText: String?): String {
        if (rawText.isNullOrBlank()) return ""

        val paragraphs = rawText.split(Regex("(\r?\n){2,}"))
        val formattedParagraphs = paragraphs.mapNotNull { paragraph ->
            val lines = paragraph.split(Regex("\r?\n")).map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return@mapNotNull null

            val sb = StringBuilder()
            for (line in lines) {
                if (sb.isEmpty()) {
                    sb.append(line)
                } else {
                    if (sb.endsWith("-")) {
                        val beforeHyphen = sb.dropLast(1)
                        if (beforeHyphen.isNotEmpty() && beforeHyphen.last().isLetter() && line.first().isLetter()) {
                            sb.setLength(sb.length - 1)
                            sb.append(line)
                        } else {
                            sb.append(" ").append(line)
                        }
                    } else if (line.matches(Regex("^([0-9]+[.)]|[-•*])\\s+.*"))) {
                        sb.append("\n").append(line)
                    } else {
                        sb.append(" ").append(line)
                    }
                }
            }
            sb.toString()
        }

        return formattedParagraphs.joinToString("\n\n").trim()
    }
}
