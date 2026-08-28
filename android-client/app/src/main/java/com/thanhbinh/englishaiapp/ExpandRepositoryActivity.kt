package com.thanhbinh.englishaiapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.thanhbinh.englishaiapp.data.local.AppDatabase
import com.thanhbinh.englishaiapp.data.local.entity.VocabularyEntity
import com.thanhbinh.englishaiapp.data.model.StagingVocabulary
import com.thanhbinh.englishaiapp.databinding.ActivityExpandRepositoryBinding
import com.thanhbinh.englishaiapp.databinding.DialogEditStagingWordBinding
import com.thanhbinh.englishaiapp.ui.adapter.StagingAdapter
import com.thanhbinh.englishaiapp.utils.ExcelCsvImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpandRepositoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpandRepositoryBinding
    private lateinit var stagingAdapter: StagingAdapter

    private var collectionId: Long = -1
    private var collectionName: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            importFile(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpandRepositoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        collectionId = intent.getLongExtra("collection_id", -1)
        collectionName = intent.getStringExtra("collection_name") ?: "Collection"

        if (collectionId == -1L) {
            Toast.makeText(this, "Không tìm thấy thông tin bộ từ vựng!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener { finish() }
        binding.txtTargetCollection.text = "THƯ MỤC: ${collectionName.uppercase()}"

        // Staging RecyclerView Setup
        stagingAdapter = StagingAdapter(
            onEditClick = { position, item -> showEditDialog(position, item) },
            onDeleteClick = { position, _ -> removeItemFromStaging(position) }
        )
        binding.rvStaging.layoutManager = LinearLayoutManager(this)
        binding.rvStaging.adapter = stagingAdapter

        // Manual Input: Append to List
        binding.btnAppendToList.setOnClickListener {
            handleManualAppend()
        }

        // External Import: Choose Excel / CSV
        binding.cardExternalImport.setOnClickListener {
            val mimeTypes = arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "text/tab-separated-values",
                "text/plain",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel",
                "application/octet-stream",
                "*/*"
            )
            filePickerLauncher.launch(mimeTypes)
        }

        // Commit All
        binding.btnCommitAll.setOnClickListener {
            commitAllToDatabase()
        }

        // Batch Actions Menu
        binding.btnBatchActions.setOnClickListener { view ->
            showBatchActionsMenu(view)
        }

        updateStagingUI()
    }

    private fun handleManualAppend() {
        val term = binding.edtTerm.text.toString().trim()
        val meaning = binding.edtMeaning.text.toString().trim()
        val example = binding.edtExample.text.toString().trim()

        if (term.isEmpty()) {
            binding.edtTerm.error = "Vui lòng nhập từ vựng"
            binding.edtTerm.requestFocus()
            return
        }

        if (meaning.isEmpty()) {
            binding.edtMeaning.error = "Vui lòng nhập nghĩa của từ"
            binding.edtMeaning.requestFocus()
            return
        }

        val item = StagingVocabulary(term = term, meaning = meaning, example = example)
        stagingAdapter.addItem(item)

        // Clear input fields
        binding.edtTerm.text?.clear()
        binding.edtMeaning.text?.clear()
        binding.edtExample.text?.clear()
        binding.edtTerm.requestFocus()

        updateStagingUI()
        Toast.makeText(this, "Đã thêm \"$term\" vào danh sách chờ", Toast.LENGTH_SHORT).show()
    }

    private fun importFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val importedList = withContext(Dispatchers.IO) {
                    ExcelCsvImporter.importFromUri(applicationContext, uri)
                }

                if (importedList.isNotEmpty()) {
                    stagingAdapter.addAll(importedList)
                    updateStagingUI()
                    Toast.makeText(
                        this@ExpandRepositoryActivity,
                        "Đã nhập thành công ${importedList.size} từ vựng vào hàng chờ!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@ExpandRepositoryActivity,
                        "Không tìm thấy từ vựng hợp lệ trong file. File cần có 3 cột: Từ, Nghĩa, Ví dụ.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ExpandRepositoryActivity,
                    "Lỗi khi đọc file: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showEditDialog(position: Int, item: StagingVocabulary) {
        val dialogBinding = DialogEditStagingWordBinding.inflate(LayoutInflater.from(this))
        dialogBinding.edtEditTerm.setText(item.term)
        dialogBinding.edtEditMeaning.setText(item.meaning)
        dialogBinding.edtEditExample.setText(item.example)

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Lưu") { _, _ ->
                val newTerm = dialogBinding.edtEditTerm.text.toString().trim()
                val newMeaning = dialogBinding.edtEditMeaning.text.toString().trim()
                val newExample = dialogBinding.edtEditExample.text.toString().trim()

                if (newTerm.isNotEmpty() && newMeaning.isNotEmpty()) {
                    val updated = StagingVocabulary(newTerm, newMeaning, newExample)
                    stagingAdapter.updateItem(position, updated)
                    Toast.makeText(this, "Đã cập nhật từ vựng", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Từ vựng và nghĩa không được để trống!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun removeItemFromStaging(position: Int) {
        stagingAdapter.removeItem(position)
        updateStagingUI()
    }

    private fun updateStagingUI() {
        val count = stagingAdapter.itemCount
        if (count == 0) {
            binding.layoutEmptyStaging.visibility = View.VISIBLE
            binding.rvStaging.visibility = View.GONE
            binding.txtStagingSubtitle.text = "Các từ vựng đang chờ lưu trữ."
            binding.btnCommitAll.isEnabled = false
            binding.btnCommitAll.alpha = 0.5f
            binding.btnCommitAll.text = "Lưu tất cả"
        } else {
            binding.layoutEmptyStaging.visibility = View.GONE
            binding.rvStaging.visibility = View.VISIBLE
            binding.txtStagingSubtitle.text = "$count từ đang chờ lưu vào thư mục"
            binding.btnCommitAll.isEnabled = true
            binding.btnCommitAll.alpha = 1.0f
            binding.btnCommitAll.text = "Lưu tất cả ($count)"
        }
    }

    private fun commitAllToDatabase() {
        val items = stagingAdapter.getItems()
        if (items.isEmpty()) {
            Toast.makeText(this, "Hàng chờ đang trống!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = System.currentTimeMillis()
        val entities = items.map { item ->
            VocabularyEntity(
                collectionId,
                item.term,
                item.meaning,
                item.example,
                false,
                currentTime
            )
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getInstance(applicationContext)
                    .vocabularyDao()
                    .insertAll(entities)
            }

            Toast.makeText(
                this@ExpandRepositoryActivity,
                "Đã lưu thành công ${entities.size} từ vựng vào \"$collectionName\"!",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showBatchActionsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Xóa tất cả hàng chờ")
        popup.menu.add(0, 2, 1, "Xem định dạng file Excel/CSV mẫu")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    if (stagingAdapter.itemCount > 0) {
                        AlertDialog.Builder(this)
                            .setTitle("Xóa danh sách chờ")
                            .setMessage("Bạn có chắc muốn xóa tất cả ${stagingAdapter.itemCount} từ vựng đang chờ lưu?")
                            .setPositiveButton("Xóa") { _, _ ->
                                stagingAdapter.clearAll()
                                updateStagingUI()
                                Toast.makeText(this, "Đã xóa toàn bộ hàng chờ", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Hủy", null)
                            .show()
                    } else {
                        Toast.makeText(this, "Danh sách chờ hiện đang trống!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                2 -> {
                    showSampleFormatDialog()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showSampleFormatDialog() {
        AlertDialog.Builder(this)
            .setTitle("Định dạng file Excel / CSV")
            .setMessage(
                "File cần có 3 cột theo thứ tự:\n\n" +
                "• Cột 1: Từ vựng (Từ vựng)\n" +
                "• Cột 2: Định nghĩa / Nghĩa (Định nghĩa / Nghĩa)\n" +
                "• Cột 3: Ví dụ minh họa (Ví dụ minh họa - Tùy chọn)\n\n" +
                "Ví dụ:\n" +
                "Ubiquitous, Phổ biến khắp nơi, Mobile phones are ubiquitous."
            )
            .setPositiveButton("Đã hiểu", null)
            .show()
    }
}
