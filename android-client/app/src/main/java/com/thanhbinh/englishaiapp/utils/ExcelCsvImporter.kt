package com.thanhbinh.englishaiapp.utils

import android.content.Context
import android.net.Uri
import com.thanhbinh.englishaiapp.data.model.StagingVocabulary
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ExcelCsvImporter {

    /**
     * Import vocabulary list from Uri (CSV, TXT, TSV or XLSX)
     */
    fun importFromUri(context: Context, uri: Uri): List<StagingVocabulary> {
        val contentResolver = context.contentResolver
        val fileName = getFileName(context, uri).lowercase()

        val inputStream = contentResolver.openInputStream(uri) ?: return emptyList()

        return if (fileName.endsWith(".xlsx")) {
            inputStream.use { importFromXlsx(it) }
        } else {
            // Default to CSV / delimited text parser
            inputStream.use { importFromCsv(it) }
        }
    }

    /**
     * Parse CSV, TSV or delimiter separated text stream
     */
    fun importFromCsv(inputStream: InputStream): List<StagingVocabulary> {
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        val rows = mutableListOf<List<String>>()

        var line: String? = reader.readLine()
        // Remove UTF-8 BOM if present on first line
        if (line != null && line.startsWith("\uFEFF")) {
            line = line.substring(1)
        }

        // Determine delimiter from first line (comma, semicolon, or tab)
        var delimiter = ','
        if (line != null) {
            val commaCount = line.count { it == ',' }
            val semiCount = line.count { it == ';' }
            val tabCount = line.count { it == '\t' }
            delimiter = when {
                semiCount > commaCount && semiCount >= tabCount -> ';'
                tabCount > commaCount && tabCount > semiCount -> '\t'
                else -> ','
            }
        }

        while (line != null) {
            val parsedRow = parseCsvLine(line, delimiter)
            if (parsedRow.isNotEmpty() && parsedRow.any { it.isNotBlank() }) {
                rows.add(parsedRow)
            }
            line = reader.readLine()
        }

        return convertRowsToVocabulary(rows)
    }

    /**
     * Parse a single CSV line with support for quotes and delimiters
     */
    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Parse modern Excel .xlsx file (OpenXML) directly via ZipInputStream and XmlPullParser
     */
    fun importFromXlsx(inputStream: InputStream): List<StagingVocabulary> {
        val zipBytes = inputStream.readBytes()
        val sharedStrings = readSharedStrings(zipBytes)
        val sheetData = readFirstSheet(zipBytes, sharedStrings)
        return convertRowsToVocabulary(sheetData)
    }

    private fun readSharedStrings(zipBytes: ByteArray): List<String> {
        val sharedStrings = mutableListOf<String>()
        val zipIn = ZipInputStream(ByteArrayInputStream(zipBytes))
        var entry: ZipEntry? = zipIn.nextEntry

        while (entry != null) {
            if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                try {
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = false
                    val parser = factory.newPullParser()
                    parser.setInput(zipIn, "UTF-8")

                    var eventType = parser.eventType
                    var inText = false
                    val currentText = StringBuilder()

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                if (parser.name.equals("t", ignoreCase = true)) {
                                    inText = true
                                    currentText.setLength(0)
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (inText) {
                                    currentText.append(parser.text)
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                if (parser.name.equals("t", ignoreCase = true)) {
                                    inText = false
                                } else if (parser.name.equals("si", ignoreCase = true)) {
                                    sharedStrings.add(currentText.toString())
                                    currentText.setLength(0)
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                break
            }
            entry = zipIn.nextEntry
        }
        return sharedStrings
    }

    private fun readFirstSheet(zipBytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val zipIn = ZipInputStream(ByteArrayInputStream(zipBytes))
        var entry: ZipEntry? = zipIn.nextEntry

        while (entry != null) {
            val name = entry.name.lowercase()
            if (name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml")) {
                try {
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = false
                    val parser = factory.newPullParser()
                    parser.setInput(zipIn, "UTF-8")

                    var eventType = parser.eventType
                    var currentRow = mutableMapOf<Int, String>()
                    var currentCellCol = 0
                    var cellType = ""
                    var inValue = false
                    var inInlineText = false
                    val cellContent = StringBuilder()

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                when (parser.name.lowercase()) {
                                    "row" -> {
                                        currentRow = mutableMapOf()
                                    }
                                    "c" -> {
                                        val cellRef = parser.getAttributeValue(null, "r") ?: ""
                                        currentCellCol = columnRefToIndex(cellRef)
                                        cellType = parser.getAttributeValue(null, "t") ?: ""
                                        cellContent.setLength(0)
                                    }
                                    "v" -> {
                                        inValue = true
                                    }
                                    "t" -> {
                                        inInlineText = true
                                    }
                                }
                            }
                            XmlPullParser.TEXT -> {
                                if (inValue || inInlineText) {
                                    cellContent.append(parser.text)
                                }
                            }
                            XmlPullParser.END_TAG -> {
                                when (parser.name.lowercase()) {
                                    "v" -> inValue = false
                                    "t" -> inInlineText = false
                                    "c" -> {
                                        val rawVal = cellContent.toString().trim()
                                        val finalVal = if (cellType == "s") {
                                            val index = rawVal.toIntOrNull()
                                            if (index != null && index in sharedStrings.indices) {
                                                sharedStrings[index]
                                            } else {
                                                rawVal
                                            }
                                        } else {
                                            rawVal
                                        }
                                        currentRow[currentCellCol] = finalVal
                                    }
                                    "row" -> {
                                        if (currentRow.isNotEmpty()) {
                                            val maxCol = (currentRow.keys.maxOrNull() ?: 0)
                                            val rowList = mutableListOf<String>()
                                            for (c in 0..maxOf(maxCol, 2)) {
                                                rowList.add(currentRow[c] ?: "")
                                            }
                                            rows.add(rowList)
                                        }
                                    }
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                break // Process first sheet only
            }
            entry = zipIn.nextEntry
        }
        return rows
    }

    private fun columnRefToIndex(ref: String): Int {
        var col = 0
        for (ch in ref.uppercase()) {
            if (ch in 'A'..'Z') {
                col = col * 26 + (ch - 'A' + 1)
            } else {
                break
            }
        }
        return if (col > 0) col - 1 else 0
    }

    /**
     * Map raw row tokens into StagingVocabulary objects, skipping headers and invalid lines
     */
    private fun convertRowsToVocabulary(rows: List<List<String>>): List<StagingVocabulary> {
        val result = mutableListOf<StagingVocabulary>()
        if (rows.isEmpty()) return result

        var startIndex = 0
        val firstRow = rows[0]
        if (isHeaderRow(firstRow)) {
            startIndex = 1
        }

        for (i in startIndex until rows.size) {
            val row = rows[i]
            val term = row.getOrNull(0)?.trim().orEmpty()
            val meaning = row.getOrNull(1)?.trim().orEmpty()
            val example = row.getOrNull(2)?.trim().orEmpty()

            if (term.isNotEmpty() && meaning.isNotEmpty()) {
                result.add(StagingVocabulary(term = term, meaning = meaning, example = example))
            }
        }

        return result
    }

    /**
     * Detect if the first row is a header title row
     */
    private fun isHeaderRow(row: List<String>): Boolean {
        if (row.isEmpty()) return false
        val first = row.getOrNull(0)?.lowercase()?.trim().orEmpty()
        val second = row.getOrNull(1)?.lowercase()?.trim().orEmpty()
        val third = row.getOrNull(2)?.lowercase()?.trim().orEmpty()

        val headerTerms = listOf("term", "word", "từ", "lexical", "từ vựng", "vocab", "vocabulary", "english")
        val headerMeanings = listOf("meaning", "nghĩa", "định nghĩa", "vietnamese", "tiếng việt", "definition", "dịch")
        val headerExamples = listOf("example", "ví dụ", "scenario", "câu", "sentence", "usage")

        return headerTerms.any { first.contains(it) } ||
                headerMeanings.any { second.contains(it) } ||
                headerExamples.any { third.contains(it) }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex) ?: ""
                }
            }
        }
        if (name.isEmpty()) {
            name = uri.lastPathSegment ?: "file.csv"
        }
        return name
    }
}
