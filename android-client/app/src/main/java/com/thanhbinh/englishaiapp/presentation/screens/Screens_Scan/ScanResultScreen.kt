package com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
import com.thanhbinh.englishaiapp.ui.theme.EnglishAIAppTheme
import com.thanhbinh.englishaiapp.presentation.viewmodel.scan.ScanResultViewModel

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

    // Sử dụng LaunchedEffect để nạp dữ liệu ngay khi màn hình hiện lên
    LaunchedEffect(docId) {
        if (docId != 0) {
            // Nếu docId khác 0, đây là file ĐÃ LƯU. Cần lấy dữ liệu từ Database.
            viewModel.setCurrentDocId(docId)
            viewModel.getDocumentById(docId) { doc ->
                // Cập nhật State để giao diện hiển thị đúng tên và nội dung cũ
                fileName = doc.fileName
                textState = doc.content
            }
        } else {
            // Nếu docId = 0, đây là file MỚI QUÉT từ camera.
            // Chỉ tạo tên tự động nếu người dùng chưa đặt tên.
            if (fileName.isEmpty()) {
                fileName = scannedText.trim().split("\\s+".toRegex())
                    .take(3).joinToString("_")
                    .filter { it.isLetterOrDigit() || it == '_' }
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var summaryText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showServerConfigDialog) {
        com.thanhbinh.englishaiapp.presentation.components.ServerConfigDialog(
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
                        viewModel.saveNewDocument(fileName, textState, "Word",
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
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // 1. Nhập tên file
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("Tên tệp") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tabs (Giữ nguyên logic của bạn)
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(4.dp)) {
                TabButton("Nguyên bản", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                TabButton("Tóm tắt AI", selectedTab == 1, Modifier.weight(1f)) {
                    selectedTab = 1
                    if ((summaryText.isEmpty() || summaryText.startsWith("Lỗi kết nối")) && !isLoading) {
                        isLoading = true
                        viewModel.summarizeText(textState) { result ->
                            summaryText = result
                            isLoading = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. KHUNG HIỂN THỊ VÀ CHO PHÉP SỬA
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Đang phân tích & tóm tắt với Llama 3.1...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // SỬA: Dùng TextField thay vì Text để người dùng có thể sửa
                        TextField(
                            value = if (selectedTab == 0) textState else summaryText,
                            onValueChange = { if (selectedTab == 0) textState = it else summaryText = it },
                            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
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

            // 4. Thanh điều hướng
            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) {
                    Text("Thoát")
                }

                // Nút Cập nhật (Sửa ID logic)
                Button(
                    onClick = {
                        viewModel.updateCurrentDocument(fileName, textState, "Word",
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
                    // Quan trọng: Nút này sẽ bật lên nếu ID khác 0
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = null
    ) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScanResultScreenPreview() {
    EnglishAIAppTheme {
        // Tạo một nội dung mẫu dài để test tính năng cuộn
        val sampleText = """
            Quy tắc đạo đức trong công ty công nghệ bao gồm các quy tắc quan trọng. 
            Đầu tiên là tính minh bạch trong thuật toán. Thứ hai là bảo mật dữ liệu người dùng. 
            Thứ ba là trách nhiệm xã hội của các kỹ sư phần mềm. 
            Việc tuân thủ các quy tắc này giúp xây dựng niềm tin với khách hàng.
        """.trimIndent()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ScanResultScreen(
                scannedText = sampleText,
                onNavigateBack = { /* Không làm gì trong Preview */ },
                viewModel = viewModel() // Sử dụng viewModel trong Preview
                // Lưu ý: Trong thực tế, bạn nên dùng MockViewModel nếu app phức tạp,
                // nhưng với bài tập này, truyền tham số như trên là đủ để hiện giao diện.
            )
        }
    }
}
