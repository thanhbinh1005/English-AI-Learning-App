package com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thanhbinh.englishaiapp.presentation.components.ServerConfigDialog
import com.thanhbinh.englishaiapp.presentation.viewmodel.scan.ScanResultViewModel
import com.thanhbinh.englishaiapp.ui.theme.EnglishAIAppTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    scannedText: String,
    docId: Int = 0, // Nhận ID từ màn hình danh sách truyền sang
    onNavigateBack: () -> Unit,
    viewModel: ScanResultViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentId by viewModel.currentDocId.collectAsState()

    var fileName by remember { mutableStateOf("") }
    var textState by remember { mutableStateOf(scannedText) }

    // TTS Setup
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Sử dụng LaunchedEffect để nạp dữ liệu ngay khi màn hình hiện lên
    LaunchedEffect(docId) {
        if (docId != 0) {
            // Nếu docId khác 0, đây là file ĐÃ LƯU. Cần lấy dữ liệu từ Database.
            viewModel.setCurrentDocId(docId)
            viewModel.getDocumentById(docId) { doc ->
                fileName = doc.fileName
                textState = doc.content
            }
        } else {
            // Nếu docId = 0, đây là file MỚI QUÉT từ camera.
            if (fileName.isEmpty()) {
                fileName = scannedText.trim().split("\\s+".toRegex())
                    .take(3).joinToString("_")
                    .filter { it.isLetterOrDigit() || it == '_' }
            }
        }
    }

    // Tab states: 0 = Nguyên bản, 1 = Dịch thuật, 2 = Tóm tắt AI
    var selectedTab by remember { mutableIntStateOf(0) }

    // Translation states
    var sourceLanguage by remember { mutableStateOf("Nhận diện ngôn ngữ") }
    var targetLanguage by remember { mutableStateOf("Tiếng Việt") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    // Summary states
    var summaryText by remember { mutableStateOf("") }
    var isSummarizing by remember { mutableStateOf(false) }

    var showServerConfigDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showServerConfigDialog) {
        ServerConfigDialog(
            onDismissRequest = { showServerConfigDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết văn bản", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showServerConfigDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình Server",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        val contentToSave = when (selectedTab) {
                            1 -> if (translatedText.isNotEmpty()) translatedText else textState
                            2 -> if (summaryText.isNotEmpty()) summaryText else textState
                            else -> textState
                        }
                        viewModel.saveNewDocument(fileName, contentToSave, "Word",
                            onSuccess = {
                                Toast.makeText(context, "Đã lưu tệp mới!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            onFailure = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }) {
                        Text("LƯU MỚI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Nhập tên file
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("Tên tệp") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tabs: 3 Tabs (Nguyên bản | Dịch thuật | Tóm tắt AI)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(4.dp)
            ) {
                TabButton("Nguyên bản", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                TabButton("Dịch thuật", selectedTab == 1, Modifier.weight(1f)) {
                    selectedTab = 1
                    if (translatedText.isEmpty() && !isTranslating && textState.isNotBlank()) {
                        isTranslating = true
                        viewModel.translateText(textState, sourceLanguage, targetLanguage) { result ->
                            translatedText = result
                            isTranslating = false
                        }
                    }
                }
                TabButton("Tóm tắt AI", selectedTab == 2, Modifier.weight(1f)) {
                    selectedTab = 2
                    if ((summaryText.isEmpty() || summaryText.startsWith("Lỗi kết nối")) && !isSummarizing && textState.isNotBlank()) {
                        isSummarizing = true
                        viewModel.summarizeText(textState) { result ->
                            summaryText = result
                            isSummarizing = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab 1: Language selection bar when in Translate Tab
            if (selectedTab == 1) {
                LanguageControlBar(
                    sourceLang = sourceLanguage,
                    targetLang = targetLanguage,
                    onSourceLangChange = {
                        sourceLanguage = it
                        if (textState.isNotBlank()) {
                            isTranslating = true
                            viewModel.translateText(textState, sourceLanguage, targetLanguage) { res ->
                                translatedText = res
                                isTranslating = false
                            }
                        }
                    },
                    onTargetLangChange = {
                        targetLanguage = it
                        if (textState.isNotBlank()) {
                            isTranslating = true
                            viewModel.translateText(textState, sourceLanguage, targetLanguage) { res ->
                                translatedText = res
                                isTranslating = false
                            }
                        }
                    },
                    onSwap = {
                        if (sourceLanguage != "Nhận diện ngôn ngữ") {
                            val temp = sourceLanguage
                            sourceLanguage = targetLanguage
                            targetLanguage = temp
                            if (textState.isNotBlank()) {
                                isTranslating = true
                                viewModel.translateText(textState, sourceLanguage, targetLanguage) { res ->
                                    translatedText = res
                                    isTranslating = false
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 3. KHUNG HIỂN THỊ VÀ CHO PHÉP SỬA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTab == 1) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (selectedTab) {
                        0 -> {
                            // Tab Nguyên bản
                            TextField(
                                value = textState,
                                onValueChange = { textState = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                            )
                        }
                        1 -> {
                            // Tab Dịch thuật
                            if (isTranslating) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Đang dịch văn bản bằng ML Kit...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    TextField(
                                        value = translatedText,
                                        onValueChange = { translatedText = it },
                                        placeholder = { Text("Bản dịch sẽ xuất hiện tại đây...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .verticalScroll(scrollState),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                                    )

                                    // Action bar inside translation card
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Nút nghe phát âm
                                        IconButton(
                                            onClick = {
                                                if (translatedText.isNotEmpty()) {
                                                    val locale = when (targetLanguage) {
                                                        "Tiếng Việt" -> Locale("vi", "VN")
                                                        "Tiếng Pháp" -> Locale.FRENCH
                                                        "Tiếng Nhật" -> Locale.JAPANESE
                                                        "Tiếng Đức" -> Locale.GERMAN
                                                        "Tiếng Tây Ban Nha" -> Locale("es", "ES")
                                                        else -> Locale.US
                                                    }
                                                    tts?.language = locale
                                                    tts?.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Nghe",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Nút sao chép
                                        IconButton(
                                            onClick = {
                                                if (translatedText.isNotEmpty()) {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Translated Text", translatedText))
                                                    Toast.makeText(context, "Đã sao chép bản dịch!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Sao chép",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Nút chia sẻ
                                        IconButton(
                                            onClick = {
                                                if (translatedText.isNotEmpty()) {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, translatedText)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ bản dịch"))
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Chia sẻ",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Nút Dịch lại
                                        FilledTonalButton(
                                            onClick = {
                                                isTranslating = true
                                                viewModel.translateText(textState, sourceLanguage, targetLanguage) { res ->
                                                    translatedText = res
                                                    isTranslating = false
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Dịch lại", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Tab Tóm tắt AI
                            if (isSummarizing) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Đang phân tích & tóm tắt với Llama 3.1...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                TextField(
                                    value = summaryText,
                                    onValueChange = { summaryText = it },
                                    placeholder = { Text("Nội dung tóm tắt AI sẽ xuất hiện tại đây...") },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Thanh điều hướng dưới cùng
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Thoát")
                }

                // Nút Cập nhật
                Button(
                    onClick = {
                        val contentToUpdate = when (selectedTab) {
                            1 -> if (translatedText.isNotEmpty()) translatedText else textState
                            2 -> if (summaryText.isNotEmpty()) summaryText else textState
                            else -> textState
                        }
                        viewModel.updateCurrentDocument(fileName, contentToUpdate, "Word",
                            onSuccess = {
                                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            onFailure = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentId != 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentId != 0) Color(0xFF4CAF50) else Color.Gray
                    )
                ) {
                    Text("Cập nhật file")
                }
            }
        }
    }
}

// Composable cho thanh điều khiển ngôn ngữ
@Composable
fun LanguageControlBar(
    sourceLang: String,
    targetLang: String,
    onSourceLangChange: (String) -> Unit,
    onTargetLangChange: (String) -> Unit,
    onSwap: () -> Unit
) {
    val languages = listOf(
        "Nhận diện ngôn ngữ",
        "Tiếng Anh",
        "Tiếng Việt",
        "Tiếng Pháp",
        "Tiếng Nhật",
        "Tiếng Đức",
        "Tiếng Tây Ban Nha"
    )

    var showSourceMenu by remember { mutableStateOf(false) }
    var showTargetMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Source Language Selector
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { showSourceMenu = true }
                ) {
                    Text(
                        text = sourceLang,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSourceMenu,
                    onDismissRequest = { showSourceMenu = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, fontSize = 14.sp) },
                            onClick = {
                                onSourceLangChange(lang)
                                showSourceMenu = false
                            }
                        )
                    }
                }
            }

            // Swap Button
            IconButton(
                onClick = onSwap,
                modifier = Modifier.size(32.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Hoán đổi",
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Target Language Selector
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { showTargetMenu = true }
                ) {
                    Text(
                        text = targetLang,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = showTargetMenu,
                    onDismissRequest = { showTargetMenu = false }
                ) {
                    languages.filter { it != "Nhận diện ngôn ngữ" }.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, fontSize = 14.sp) },
                            onClick = {
                                onTargetLangChange(lang)
                                showTargetMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// Composable phụ cho nút Tab
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScanResultScreenPreview() {
    EnglishAIAppTheme {
        val sampleText = """
            Artificial intelligence is transforming education worldwide.
            Students can practice speaking, reading, and vocabulary anytime.
        """.trimIndent()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ScanResultScreen(
                scannedText = sampleText,
                onNavigateBack = {},
                viewModel = viewModel()
            )
        }
    }
}
