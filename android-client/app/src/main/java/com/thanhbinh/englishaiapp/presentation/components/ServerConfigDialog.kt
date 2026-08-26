package com.thanhbinh.englishaiapp.presentation.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.thanhbinh.englishaiapp.utils.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var ipInput by remember { mutableStateOf(AppConfig.currentServerIp) }
    var isChecking by remember { mutableStateOf(false) }
    var checkResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Cấu hình Máy chủ AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Hỗ trợ cả cáp USB và mạng Wifi LAN",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Quick preset buttons
                Text(
                    text = "Chọn nhanh chế độ kết nối:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        title = "USB Debug",
                        subtitle = "127.0.0.1",
                        isSelected = ipInput == "127.0.0.1",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            ipInput = "127.0.0.1"
                            checkResult = null
                        }
                    )
                    PresetChip(
                        title = "Máy ảo",
                        subtitle = "10.0.2.2",
                        isSelected = ipInput == "10.0.2.2",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            ipInput = "10.0.2.2"
                            checkResult = null
                        }
                    )
                }

                // Input Field
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = {
                        ipInput = it
                        checkResult = null
                    },
                    label = { Text("IP Máy tính (Port mặc định: 5000)") },
                    placeholder = { Text("Ví dụ: 127.0.0.1 hoặc 10.20.247.63") },
                    leadingIcon = {
                        Icon(Icons.Default.Lan, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Check result message
                checkResult?.let { (success, message) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (success) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = message,
                                fontSize = 12.sp,
                                color = if (success) Color(0xFF2E7D32) else Color(0xFFC62828),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Instructions tip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 Lưu ý khi dùng cáp USB: Mở cmd trên máy tính và chạy:\n'adb reverse tcp:5000 tcp:5000'",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 15.sp
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isChecking = true
                            checkResult = null
                            AppConfig.pingServer(ipInput) { success, msg ->
                                isChecking = false
                                checkResult = Pair(success, msg)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isChecking && ipInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Kiểm tra")
                        }
                    }

                    Button(
                        onClick = {
                            AppConfig.saveServerIp(context, ipInput)
                            Toast.makeText(context, "Đã lưu IP: $ipInput:5000", Toast.LENGTH_SHORT).show()
                            onDismissRequest()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = ipInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu & Áp dụng")
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
