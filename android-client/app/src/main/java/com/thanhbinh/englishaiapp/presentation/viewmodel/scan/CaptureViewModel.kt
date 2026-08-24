package com.thanhbinh.englishaiapp.presentation.viewmodel.scan

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CaptureViewModel(application: Application) : AndroidViewModel(application) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // Trạng thái Loading khi đang quét chữ để UI hiển thị vòng xoay
    var isProcessing by mutableStateOf(false)
        private set

    fun processImage(context: Context, uri: Uri, onSuccess: (String) -> Unit) {
        isProcessing = true
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    isProcessing = false
                    if (visionText.text.isNotBlank()) {
                        onSuccess(visionText.text)
                    } else {
                        Log.d("OCR", "Không tìm thấy văn bản")
                    }
                }
                .addOnFailureListener { e ->
                    isProcessing = false
                    Log.e("OCR", "Lỗi quét chữ: ${e.message}")
                }
        } catch (e: Exception) {
            isProcessing = false
            Log.e("OCR", "Lỗi đọc file: ${e.message}")
        }
    }
}