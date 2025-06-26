package com.example.myapp.utils



import com.example.myapp.data.entity.Chapter
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream

class DocxParser {
    fun getTitle(file: File): String {
        val document = XWPFDocument(FileInputStream(file))
        val paragraphs = document.paragraphs
        return paragraphs.firstOrNull()?.text ?: "Untitled"
    }

    fun getAuthor(file: File): String {
        val document = XWPFDocument(FileInputStream(file))
        val coreProperties = document.properties.coreProperties
        return coreProperties.creator ?: "Unknown Author"
    }

    fun getChapters(file: File): List<Chapter> {
        val document = XWPFDocument(FileInputStream(file))
        val paragraphs = document.paragraphs
        val chapters = mutableListOf<Chapter>()
        var currentChapterContent = StringBuilder()
        var chapterNumber = 1
        for (paragraph in paragraphs) {
            if (paragraph.text.startsWith("Chapter") || paragraph.text.startsWith("CHAPTER")) {
                if (currentChapterContent.isNotEmpty()) {
                    chapters.add(Chapter(storyId = 0, chapterNumber = chapterNumber, content = currentChapterContent.toString()))
                    currentChapterContent = StringBuilder()
                    chapterNumber++
                }
            }
            currentChapterContent.append(paragraph.text).append("\n")
        }
        if (currentChapterContent.isNotEmpty()) {
            chapters.add(Chapter(storyId = 0, chapterNumber = chapterNumber, content = currentChapterContent.toString()))
        }
        return chapters
    }
}