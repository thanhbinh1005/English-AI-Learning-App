package com.thanhbinh.englishaiapp.presentation.screens.Screens_Scan
import android.content.Context
import android.content.Intent
import com.thanhbinh.englishaiapp.data.local.entity.ScannedDocEntity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thanhbinh.englishaiapp.presentation.navigation.Screen
import com.thanhbinh.englishaiapp.ui.theme.*
import com.thanhbinh.englishaiapp.presentation.viewmodel.scan.ScanViewModel
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class giả lập để sau này bạn đổ dữ liệu từ Database/API vào đây
data class ScannedDoc(val name: String, val size: String, val date: String, val type: String)

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel(),
    navController: NavHostController, // Thêm cái này để thực hiện lệnh chuyển trang
    onNavigateToCamera: () -> Unit
) {
    // Thu thập dữ liệu từ Database dưới dạng State để giao diện tự cập nhật
    val scannedDocs by viewModel.allDocs.collectAsState(initial = emptyList())


    // Dùng LazyColumn để toàn bộ màn hình có thể cuộn được
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Banner Header: Số hóa thế giới (Digitize your world)
        item {
            HeaderBanner(onScanClick = {
                // Khi nhấn nút, nó sẽ tự thêm 1 dòng vào Database để bạn kiểm tra
//                viewModel.addDocument("", "", "Nội dung quét thử nghiệm")
                onNavigateToCamera()
            })
        }

        // 2. Section: Thống kê dung lượng Sync (All documents synced)
        item {
            SyncStatusCard(current = 6.5f, total = 10f)
        }

        // 3. Section: Tiêu đề danh sách
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Bản quét của tôi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { /* Toggle view mode */ }) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 4. Danh sách các file đã quét
        if (scannedDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chưa có tài liệu nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chụp tài liệu mới bằng camera hoặc mở văn bản mẫu để trải nghiệm tính năng Tóm tắt bằng AI (Llama 3.1).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val sampleText = """
                                Artificial intelligence (AI) is transforming language learning by providing personalized, interactive, and adaptive learning experiences.
                                With large language models like Llama 3.1, learners can practice natural conversations, ask grammar questions, and receive instant explanations.
                                AI can summarize lengthy documents, highlight key points, and extract important vocabulary for learners.
                                Moreover, automated speech recognition and text-to-speech tools enable students to practice listening and pronunciation anywhere, anytime.
                                While AI cannot completely replace human interaction, it serves as a powerful 24/7 tutor that accelerates fluency and confidence.
                                """.trimIndent()
                                val encodedSample = URLEncoder.encode(sampleText, "UTF-8")
                                navController.navigate(Screen.ScanResult.route + "/${encodedSample}?docId=0")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mở văn bản mẫu thử Tóm tắt AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(scannedDocs) { doc ->
                ScannedFileItem(
                    doc = doc,
                    onDelete = { viewModel.deleteDocument(doc) },
                    onViewClick = {
                        // 1. Mã hóa nội dung để URL không bị lỗi nếu văn bản có dấu cách/xuống dòng
                        val encodedContent = URLEncoder.encode(doc.content, "UTF-8")

                        // 2. QUAN TRỌNG: Truyền ID của tệp này qua tham số docId
                        // Cấu trúc: Route + /Nội_dung?docId=Số_ID
                        navController.navigate(Screen.ScanResult.route + "/${encodedContent}?docId=${doc.id}")
                    }
                )
            }
        }
    }
}

@Composable
fun HeaderBanner(onScanClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    // Card dùng Gradient để nhìn hiện đại như ảnh mẫu
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(listOf(primaryColor, primaryContainer))
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Số hóa thế giới.",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Quét tài liệu, trích xuất văn bản với AI và dịch tức thì.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Nút Quét mới - Đây là nơi bạn sẽ gọi Camera
            Button(
                onClick =  onScanClick ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary, // Nút trắng trên nền xanh
                    contentColor = primaryColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Icon(Icons.Default.DocumentScanner, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("QUÉT TÀI LIỆU MỚI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SyncStatusCard(current: Float, total: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.CloudDone,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Đã đồng bộ dữ liệu",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Lần cuối: Hôm nay lúc 14:45",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            // Thanh tiến trình (ProgressBar)
            LinearProgressIndicator(
                progress = { current / total },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "$current GB / $total GB",
                modifier = Modifier.align(Alignment.End),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
    var searchQuery by remember { mutableStateOf("") }

// Thêm thanh tìm kiếm trên cùng danh sách
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Tìm kiếm file...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(25.dp)
    )
}

@Composable
// Chấp nhận tham số là ScannedDocEntity (Dữ liệu từ Database)
fun ScannedFileItem(
    doc: ScannedDocEntity,
    onDelete: () -> Unit,
    onViewClick: () -> Unit
    ) {

    // --- DÒNG QUAN TRỌNG: Lấy context để dùng cho hàm shareText ---
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon loại file: doc.fileType lấy từ Database
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if(doc.fileType == "PDF") Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = if(doc.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Description,
                    contentDescription = null,
                    tint = if(doc.fileType == "PDF") Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.fileName,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${doc.fileSize}  •  ${formatTimestamp(doc.createdAt)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // SỬA: Chữ xám chuẩn M3
                )
            }

            // Nút Share
            IconButton(onClick = { shareText(context, doc.content) }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
            }
            // Nút Xem chi tiết
            Button(
                onClick = onViewClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Xem", fontWeight = FontWeight.Bold)
            }

            // Menu Xóa: Gọi hàm onDelete khi nhấn
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Đặt ở cuối file hoặc ngoài hàm Composable
fun shareText(context: Context, text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

@Preview(showSystemUi = true)
@Composable
fun ScanScreenPreview() {
    // 1. Tạo một cái navController "giả" chỉ dành riêng cho việc xem giao diện
    val navController = rememberNavController()

    EnglishAIAppTheme {
        // 2. Truyền cái giả đó vào đây
        ScanScreen(
            navController = navController,
            onNavigateToCamera = {}
        )
    }
}