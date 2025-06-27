package com.example.myapp.presentation.activity


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView


import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.data.database.AppDatabase
import com.example.myapp.data.database.DatabaseProvider
import com.example.myapp.data.entity.Chapter
import com.example.myapp.data.entity.Story
import com.example.myapp.data.dao.ChapterDao
import com.example.myapp.data.dao.ReadingProgressDao
import com.example.myapp.data.dao.StoryDao
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.domain.usecase.DeleteStoryUseCase
import com.example.myapp.domain.usecase.GetChapterUseCase
import com.example.myapp.domain.usecase.GetStoryUseCase
import com.example.myapp.domain.usecase.ImportStoryUseCase
import com.example.myapp.domain.usecase.UpdateReadingProgressUseCase
import com.example.myapp.presentation.adapter.StoryAdapter
import com.example.myapp.presentation.viewmodel.StoryViewModel
import com.example.myapp.presentation.viewmodel.StoryViewModelFactory
import com.example.myapp.utils.DocxParser
import com.example.myapp.utils.ITextPdfParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var database: AppDatabase
    private lateinit var storyDao: StoryDao
    private lateinit var chapterDao: ChapterDao
    private lateinit var readingProgressDao: ReadingProgressDao
    private lateinit var viewModelFactory: StoryViewModelFactory
    private val storyViewModel: StoryViewModel by viewModels { viewModelFactory }

    private lateinit var pdfParser: ITextPdfParser

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { fileUri ->
                val contentResolver = applicationContext.contentResolver
                var storyTitle: String? = null
                var storyAuthor: String? = null
                var storyGenre: String? = null
                var chapters: List<Chapter> = emptyList()
                var tempCopiedFile: File? = null
                var originalFileName = "Untitled"
                var coverImageFilePath: String? = null

                try {
                    val cursor = contentResolver.query(fileUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (displayNameIndex != -1) {
                                originalFileName = it.getString(displayNameIndex)
                            }
                        }
                    }

                    val baseFileNameForCover = originalFileName.substringBeforeLast('.', originalFileName)

                    tempCopiedFile = File(cacheDir, "imported_pdf_${System.currentTimeMillis()}.pdf")
                    contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        tempCopiedFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    if (!tempCopiedFile.exists()) {
                        throw IOException("Failed to create temporary PDF file for path storage.")
                    }

                    val copiedFileUri = Uri.fromFile(tempCopiedFile)

                    storyTitle = pdfParser.getTitle(contentResolver, fileUri, originalFileName)
                    storyAuthor = pdfParser.getAuthor(contentResolver, fileUri)
                    storyGenre = pdfParser.getGenre(contentResolver, fileUri)
                    chapters = pdfParser.getChapters(contentResolver, fileUri)

                    coverImageFilePath = extractAndSaveCoverImageFromPdf(tempCopiedFile, baseFileNameForCover)
                    if (coverImageFilePath == null) {
                        Log.w("ImportStory", "Could not generate cover image for $originalFileName, but proceeding with import.")
                    }

                    if (storyTitle != null && storyAuthor != null && chapters.isNotEmpty()) {
                        val story = Story(
                            title = storyTitle ?: originalFileName,
                            author = storyAuthor ?: "Unknown Author",
                            genre = storyGenre ?: "Unknown Genre",
                            filePath = tempCopiedFile?.absolutePath ?: fileUri.toString(),
                            totalChapters = chapters.size,
                            coverImagePath = coverImageFilePath
                        )
                        storyViewModel.importStory(story, chapters)
                        Toast.makeText(this, "Imported story: ${story.title}", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = when {
                            storyTitle == null -> "Could not extract title."
                            storyAuthor == null -> "Could not extract author."
                            chapters.isEmpty() -> "Could not extract chapters or content is empty."
                            else -> "Could not parse the selected file."
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error processing file: ${e.message}", Toast.LENGTH_LONG).show()
                    tempCopiedFile?.delete() // Xóa file tạm nếu có lỗi nghiêm trọng
                    Log.e("ImportStory", "Critical error processing file $originalFileName: ${e.message}", e)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdfParser = ITextPdfParser()

        database = DatabaseProvider.provideDatabase(this)
        storyDao = database.storyDao()
        chapterDao = database.chapterDao()
        readingProgressDao = database.readingProgressDao()
        viewModelFactory = StoryViewModelFactory(
            ImportStoryUseCase(storyDao, chapterDao),
            GetStoryUseCase(storyDao),
            GetChapterUseCase(chapterDao),
            UpdateReadingProgressUseCase(readingProgressDao, storyDao),
            DeleteStoryUseCase(storyDao)
        )

        setupRecyclerView()
        setupSearchView()
        setupImportButton()

        storyViewModel.displayedStories.observe(this) { stories ->
            storyAdapter.submitList(stories)
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter(
            onItemClick = { story ->
                val intent = Intent(this, StoryDetailActivity::class.java)
                intent.putExtra("storyId", story.id)
                startActivity(intent)
            },
            onItemLongClick = { story ->
                showDeleteConfirmationDialog(story)
            }
        )
        binding.storyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
        }
    }

    private fun showDeleteConfirmationDialog(story: Story) {
        AlertDialog.Builder(this)
            .setTitle("Xóa Truyện")
            .setMessage("Bạn có chắc chắn muốn xóa truyện \"${story.title}\" không? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { dialog, which ->
                // Gọi hàm xóa trong ViewModel
                storyViewModel.deleteStory(story)
                Toast.makeText(this, "Đã xóa: ${story.title}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null) // null hoặc { dialog, which -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_alert) // Tùy chọn icon
            .show()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Có thể để trống hoặc xử lý ẩn bàn phím
                query?.let {
                    storyViewModel.setSearchQuery(it)
                }
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                storyViewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupImportButton() {
        binding.importButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/pdf"
            ))
        }
    }

    private fun extractAndSaveCoverImageFromPdf(pdfFile: File, baseFileName: String): String? {
        if (!pdfFile.exists() || !pdfFile.canRead()) {
            Log.e("CoverExtraction", "PDF file does not exist or cannot be read: ${pdfFile.path}")
            return null
        }

        var renderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        var bitmap: Bitmap? = null
        var fos: FileOutputStream? = null
        val coverImageFile: File

        try {
            // Tạo tên file cho ảnh bìa (ví dụ: cover_originalfilename.png)
            // Lưu trong thư mục con "covers" của cacheDir để dễ quản lý
            val coversDir = File(cacheDir, "covers")
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }
            coverImageFile = File(coversDir, "cover_${baseFileName.replace("\\s+".toRegex(), "_")}.png")

            // Mở PdfRenderer
            val parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(parcelFileDescriptor)

            if (renderer.pageCount > 0) {
                // Mở trang đầu tiên (index 0)
                currentPage = renderer.openPage(0)

                // Tạo Bitmap với kích thước của trang
                val width = currentPage.width
                val height = currentPage.height
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // Render trang vào Bitmap
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                // Lưu Bitmap thành file PNG
                fos = FileOutputStream(coverImageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos) // 90 là chất lượng (0-100)

                Log.d("CoverExtraction", "Cover image saved to: ${coverImageFile.absolutePath}")
                return coverImageFile.absolutePath
            } else {
                Log.w("CoverExtraction", "PDF has no pages: ${pdfFile.path}")
                return null
            }
        } catch (e: IOException) {
            Log.e("CoverExtraction", "IOException during cover extraction: ${e.message}", e)
            // Xóa file ảnh bìa nếu tạo lỗi giữa chừng
            return null
        } catch (e: Exception) {
            Log.e("CoverExtraction", "Exception during cover extraction: ${e.message}", e)
            return null
        } finally {
            // Đóng tài nguyên
            try {
                fos?.flush()
                fos?.close()
            } catch (e: IOException) {
                Log.e("CoverExtraction", "Error closing FileOutputStream", e) }
            try {
                currentPage?.close()
            } catch (e: Exception) {
                Log.e("CoverExtraction", "Error closing PdfRenderer.Page", e) }
            try {
                renderer?.close()
            } catch (e: Exception) {
                Log.e("CoverExtraction", "Error closing PdfRenderer", e) }
            // Không xóa bitmap ở đây nếu đã return đường dẫn, nó sẽ được GC dọn dẹp
        }
    }
}