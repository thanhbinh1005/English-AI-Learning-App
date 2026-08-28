package com.thanhbinh.englishaiapp.presentation.viewmodel.scan

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.thanhbinh.englishaiapp.data.local.AppDatabase
import com.thanhbinh.englishaiapp.data.local.entity.ScannedDocEntity
import com.thanhbinh.englishaiapp.utils.AppConfig
import com.thanhbinh.englishaiapp.utils.TranslationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ScanResultViewModel(application: Application) : AndroidViewModel(application) {
    private val scanDao = AppDatabase.Companion.getDatabase(application).scanDao()
    private val languageIdentifier = LanguageIdentification.getClient()
    private var activeTranslator: Translator? = null

    companion object {
        private const val MAX_CHUNK_SIZE = 2000
    }

    // Biến để giữ ID của file hiện tại
    // Lưu ID dưới dạng Int (khớp với Entity)
    // Quản lý ID file hiện tại (0 là file chưa lưu)
    private val _currentDocId = MutableStateFlow(0)
    val currentDocId = _currentDocId.asStateFlow()

    // HÀM 1: LƯU MỚI HOÀN TOÀN (Ép buộc tạo dòng mới)
    fun saveNewDocument(name: String, content: String, type: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                // Kiểm tra trùng tên
                val existingDoc = scanDao.getDocByTitle(name)
                if (existingDoc != null) {
                    onFailure("Tên tệp đã tồn tại. Vui lòng chọn tên khác.")
                    return@launch
                }

                val newDoc = ScannedDocEntity(
                    id = 0, // ID 0 để Room tự sinh ID mới
                    fileName = name,
                    content = content,
                    fileType = type,
                    createdAt = System.currentTimeMillis(),
                    fileSize = "${content.length / 1024} KB",
                    filePath = ""
                )
                val id = scanDao.insertDoc(newDoc)
                _currentDocId.value = id.toInt() // Ghi nhớ ID vừa tạo
                onSuccess()
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Lỗi lưu mới: ${e.message}")
            }
        }
    }

    // HÀM 2: CẬP NHẬT (Chỉ ghi đè lên file đang mở)
    fun updateCurrentDocument(name: String, content: String, type: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = _currentDocId.value
        if (id == 0) return // Không có ID thì không cập nhật

        viewModelScope.launch {
            try {
                // Kiểm tra xem tên mới có bị trùng với file KHÁC không
                val docWithSameName = scanDao.getDocByTitle(name)
                if (docWithSameName != null && docWithSameName.id != id) {
                    onFailure("Tên tệp đã tồn tại. Vui lòng chọn tên khác.")
                    return@launch
                }

                val existingDoc = scanDao.getDocById(id)
                if (existingDoc != null) {
                    val updatedDoc = existingDoc.copy(
                        fileName = name,
                        content = content,
                        fileType = type,
                        createdAt = System.currentTimeMillis()
                    )
                    scanDao.updateDoc(updatedDoc)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Lỗi cập nhật: ${e.message}")
            }
        }
    }

    // Lấy document theo ID
    fun getDocumentById(id: Int, onResult: (ScannedDocEntity) -> Unit) {
        viewModelScope.launch {
            val doc = scanDao.getDocById(id)
            if (doc != null) {
                onResult(doc)
            }
        }
    }

    // Đặt ID khi mở file cũ
    fun setCurrentDocId(id: Int) {
        _currentDocId.value = id
    }

    // Tóm tắt văn bản bằng Llama 3.1
    fun summarizeText(text: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return onResult("Văn bản rỗng")

        val jsonBody = JSONObject().apply {
            put("text", text)
        }

        AppConfig.sendPostRequestWithFallback(
            endpointPath = "/summarize",
            jsonPayload = jsonBody,
            onSuccess = { responseBody ->
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val result = jsonResponse.optString("summary", jsonResponse.optString("response", ""))
                    if (result.isNotBlank()) {
                        onResult(result.trim())
                    } else {
                        onResult("Không nhận được nội dung tóm tắt từ server.")
                    }
                } catch (e: Exception) {
                    onResult("Lỗi phân tích dữ liệu từ Python: ${e.message}")
                }
            },
            onFailure = { errorMsg ->
                onResult(errorMsg)
            }
        )
    }

    // Dịch văn bản bằng Google ML Kit Translation On-Device
    fun translateText(
        input: String,
        sourceLangName: String,
        targetLangName: String,
        onResult: (String) -> Unit
    ) {
        val trimmedInput = input.trim()
        if (trimmedInput.isBlank()) {
            onResult("Văn bản rỗng")
            return
        }

        val targetCode = getLanguageCode(targetLangName) ?: TranslateLanguage.VIETNAMESE

        if (sourceLangName == "Nhận diện ngôn ngữ" || sourceLangName == "Tự động") {
            languageIdentifier.identifyLanguage(trimmedInput)
                .addOnSuccessListener { detectedCode ->
                    val sourceCode = if (detectedCode != "und" && getLanguageName(detectedCode) != detectedCode) {
                        detectedCode
                    } else {
                        TranslateLanguage.ENGLISH
                    }
                    executeTranslation(trimmedInput, sourceCode, targetCode, onResult)
                }
                .addOnFailureListener {
                    executeTranslation(trimmedInput, TranslateLanguage.ENGLISH, targetCode, onResult)
                }
        } else {
            val sourceCode = getLanguageCode(sourceLangName) ?: TranslateLanguage.ENGLISH
            executeTranslation(trimmedInput, sourceCode, targetCode, onResult)
        }
    }

    private fun executeTranslation(
        input: String,
        sourceCode: String,
        targetCode: String,
        onResult: (String) -> Unit
    ) {
        val prepResult = TranslationHelper.preprocessInput(input, sourceCode, targetCode)
        val textToTranslate = prepResult.processedText

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()

        activeTranslator?.close()
        val translator = Translation.getClient(options)
        activeTranslator = translator

        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
            .addOnSuccessListener {
                if (textToTranslate.length > MAX_CHUNK_SIZE) {
                    translateLongText(translator, input, prepResult, sourceCode, targetCode, onResult)
                } else {
                    translator.translate(textToTranslate)
                        .addOnSuccessListener { res ->
                            val finalResult = TranslationHelper.postprocessOutput(res, prepResult, sourceCode, targetCode)
                            onResult(finalResult)
                        }
                        .addOnFailureListener { e ->
                            onResult("Lỗi dịch: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult("Lỗi tải gói ngôn ngữ: ${e.message}")
            }
    }

    private fun translateLongText(
        translator: Translator,
        fullText: String,
        prepResult: TranslationHelper.PreprocessResult,
        sourceCode: String,
        targetCode: String,
        onResult: (String) -> Unit
    ) {
        val textToTranslate = prepResult.processedText
        val chunks = splitIntoSentences(textToTranslate)
        val tasks = mutableListOf<Task<String>>()
        for (chunk in chunks) {
            tasks.add(translator.translate(chunk))
        }

        Tasks.whenAllSuccess<String>(tasks)
            .addOnSuccessListener { results ->
                val sb = StringBuilder()
                for (r in results) {
                    sb.append(r).append(" ")
                }
                val rawResult = sb.toString().trim()
                val finalResult = TranslationHelper.postprocessOutput(rawResult, prepResult, sourceCode, targetCode)
                onResult(finalResult)
            }
            .addOnFailureListener { e ->
                onResult("Lỗi dịch văn bản dài: ${e.message}")
            }
    }

    private fun splitIntoSentences(text: String): List<String> {
        val sentences = mutableListOf<String>()
        val split = text.split(Regex("(?<=[.!?])\\s+"))
        val current = StringBuilder()
        for (s in split) {
            if (current.length + s.length > MAX_CHUNK_SIZE) {
                sentences.add(current.toString())
                current.clear()
                current.append(s)
            } else {
                if (current.isNotEmpty()) current.append(" ")
                current.append(s)
            }
        }
        if (current.isNotEmpty()) sentences.add(current.toString())
        return sentences
    }

    fun getLanguageCode(name: String): String? {
        return when (name) {
            "Tiếng Việt" -> TranslateLanguage.VIETNAMESE
            "Tiếng Pháp" -> TranslateLanguage.FRENCH
            "Tiếng Nhật" -> TranslateLanguage.JAPANESE
            "Tiếng Đức" -> TranslateLanguage.GERMAN
            "Tiếng Tây Ban Nha" -> TranslateLanguage.SPANISH
            "Tiếng Anh" -> TranslateLanguage.ENGLISH
            else -> null
        }
    }

    fun getLanguageName(code: String): String {
        return when (code) {
            TranslateLanguage.VIETNAMESE -> "Tiếng Việt"
            TranslateLanguage.FRENCH -> "Tiếng Pháp"
            TranslateLanguage.JAPANESE -> "Tiếng Nhật"
            TranslateLanguage.GERMAN -> "Tiếng Đức"
            TranslateLanguage.SPANISH -> "Tiếng Tây Ban Nha"
            TranslateLanguage.ENGLISH -> "Tiếng Anh"
            else -> code
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeTranslator?.close()
        languageIdentifier.close()
    }
}