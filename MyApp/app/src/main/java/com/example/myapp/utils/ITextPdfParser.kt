package com.example.myapp.utils

import android.content.ContentResolver
import android.net.Uri
import com.example.myapp.data.entity.Chapter
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.IOException
import java.io.InputStream

class ITextPdfParser {

    private fun extractTextFromUriSafely(contentResolver: ContentResolver, uri: Uri): String {
        val stringBuilder = StringBuilder()
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = PdfReader(inputStream)
                val n = reader.numberOfPages
                for (i in 0 until n) {
                    stringBuilder.append(PdfTextExtractor.getTextFromPage(reader, i + 1))
                    stringBuilder.append("\n")
                }
                reader.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun getTitle(contentResolver: ContentResolver, uri: Uri, fallbackTitle: String): String {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = PdfReader(inputStream)
                val info = reader.info
                val titleFromMetadata = info["Title"]
                reader.close()
                if (!titleFromMetadata.isNullOrBlank()) {
                    return titleFromMetadata
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fallbackTitle
    }

    fun getGenre(contentResolver: ContentResolver, uri: Uri): String {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = PdfReader(inputStream)
                val info = reader.info
                val keyword = info["Keywords"]
                reader.close()
                return keyword ?: "Unknown Genre"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Unknown Genre"
    }

    fun getAuthor(contentResolver: ContentResolver, uri: Uri): String {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = PdfReader(inputStream)
                val info = reader.info
                val author = info["Author"]
                reader.close()
                return author ?: "Unknown Author"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Unknown Author"
    }

    fun getChapters(contentResolver: ContentResolver, uri: Uri): List<Chapter> {
        val fullText = extractTextFromUriSafely(contentResolver, uri)
        if (fullText.isBlank()) return emptyList()

        val chapters = mutableListOf<Chapter>()
        var currentChapterContent = StringBuilder()
        var chapterNumber = 1
        var currentChapterTitle: String? = null

        val lines = fullText.lines()
        for (line in lines) {
            val trimmedLine = line.trim()

            val isChapterStartLine = trimmedLine.matches(Regex("^CHAPTER\\s+[\\DIVXLC]+\\s*[:.\\-–—]?", RegexOption.IGNORE_CASE)) ||
                    trimmedLine.matches(Regex("^CHƯƠNG\\s+\\d+\\s*[:.\\-–—]?", RegexOption.IGNORE_CASE)) ||
                    trimmedLine.matches(Regex("^Phần\\s+\\d+\\s*[:.\\-–—]?", RegexOption.IGNORE_CASE)) ||
                    (trimmedLine.length < 80 && trimmedLine.isNotEmpty() && trimmedLine.all { it.isUpperCase() || it.isDigit() || it.isWhitespace() || it in listOf(':', '.', '-', '–', '—') })

            if (isChapterStartLine && currentChapterContent.length > 200) {
                if (currentChapterContent.isNotEmpty()) {
                    chapters.add(Chapter(
                        storyId = 0,
                        chapterNumber = chapterNumber,
                        content = currentChapterContent.toString().trim()
                    ))
                    currentChapterContent = StringBuilder()
                    chapterNumber++
                    currentChapterTitle = trimmedLine
                } else {
                    currentChapterTitle = trimmedLine
                }
            }
            currentChapterContent.append(line).append("\n")
        }

        if (currentChapterContent.toString().trim().isNotEmpty()) {
            chapters.add(Chapter(
                storyId = 0,
                chapterNumber = chapterNumber,
                content = currentChapterContent.toString().trim()
            ))
        }
        return chapters
    }
}