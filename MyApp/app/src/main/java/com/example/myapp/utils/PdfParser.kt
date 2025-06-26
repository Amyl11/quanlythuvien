package com.example.myapp.utils

import com.example.myapp.data.entity.Chapter // Đảm bảo bạn đã import đúng lớp Chapter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.IOException


class PdfParser {

    private fun extractTextFromFile(file: File): String {
        try {
            PDDocument.load(file).use { document -> // 'use' sẽ tự động đóng document
                if (document.isEncrypted) {
                    // Bạn có thể thử giải mã nếu biết mật khẩu,
                    // hoặc thông báo cho người dùng rằng file được mã hóa.
                    // Ví dụ: document.decrypt("") // nếu không có mật khẩu
                    // Hiện tại, chúng ta sẽ trả về chuỗi rỗng nếu file được mã hóa
                    // mà không xử lý giải mã.
                    println("Warning: PDF file is encrypted. Text extraction might fail or be incomplete.")
                    // Nếu bạn muốn cố gắng trích xuất dù đã mã hóa (có thể không thành công):
                    // stripper.isExtractMarkedContent = true // Thử nghiệm
                }
                val stripper = PDFTextStripper()
                return stripper.getText(document)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Xử lý lỗi đọc file hoặc lỗi liên quan đến PDFBox
            return "" // Trả về chuỗi rỗng nếu có lỗi
        } catch (e: Exception) {
            e.printStackTrace()
            // Xử lý các lỗi khác
            return ""
        }
    }

    fun getTitle(file: File): String {
        val fullText = extractTextFromFile(file)
        if (fullText.isBlank()) {
            // Nếu không trích xuất được text, thử lấy từ metadata
            try {
                PDDocument.load(file).use { document ->
                    val info = document.documentInformation
                    if (!info.title.isNullOrBlank()) {
                        return info.title
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "Untitled" // Mặc định nếu không có gì
        }
        // Logic đơn giản: lấy dòng đầu tiên không rỗng làm tiêu đề
        // Bạn có thể cần logic phức tạp hơn tùy thuộc vào cấu trúc PDF
        return fullText.lines().firstOrNull { it.trim().isNotEmpty() } ?: "Untitled"
    }

    fun getAuthor(file: File): String {
        try {
            PDDocument.load(file).use { document ->
                val info = document.documentInformation
                return info.author ?: "Unknown Author"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Unknown Author"
        }
    }

    fun getChapters(file: File): List<Chapter> {
        val fullText = extractTextFromFile(file)
        if (fullText.isBlank()) return emptyList()

        val chapters = mutableListOf<Chapter>()
        var currentChapterContent = StringBuilder()
        var chapterNumber = 1

        // Logic đơn giản: chia nội dung dựa trên các dòng bắt đầu bằng "Chapter" hoặc "CHAPTER"
        // Bạn có thể cần logic phức tạp hơn tùy thuộc vào cấu trúc PDF
        // và cách văn bản được trích xuất (ví dụ: ngắt dòng có thể khác với DOCX).
        val lines = fullText.lines()
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("Chapter", ignoreCase = true) ||
                trimmedLine.matches(Regex("^CHAPTER\\s+\\d+.*", RegexOption.IGNORE_CASE)) || // "CHAPTER 1", "Chapter 2: Title"
                trimmedLine.matches(Regex("^Chương\\s+\\d+.*", RegexOption.IGNORE_CASE)) // Hỗ trợ tiếng Việt "Chương 1"
            ) {
                if (currentChapterContent.isNotEmpty()) {
                    chapters.add(Chapter(storyId = 0, chapterNumber = chapterNumber, content = currentChapterContent.toString().trim()))
                    currentChapterContent = StringBuilder()
                    chapterNumber++
                }
                // Thêm dòng tiêu đề chapter vào nội dung chapter nếu muốn
                // currentChapterContent.append(line).append("\n")
            }
            currentChapterContent.append(line).append("\n")
        }

        // Thêm chapter cuối cùng nếu có nội dung
        if (currentChapterContent.toString().isNotBlank()) {
            chapters.add(Chapter(storyId = 0, chapterNumber = chapterNumber, content = currentChapterContent.toString().trim()))
        }

        return chapters
    }
}