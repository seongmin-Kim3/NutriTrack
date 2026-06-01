package com.example.nutritrack.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nutritrack.ui.viewmodel.DiagnosisUiState
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel

@Composable
fun AiResultCard(tag: String, icon: String, content: String, color: Color) {
    val cleanContent = content.replace("[$tag]", "").trim()
    if (cleanContent.isBlank()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = color.copy(alpha = 0.1f), shape = CircleShape) {
                    Text(text = icon, modifier = Modifier.padding(10.dp), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = tag, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = cleanContent, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp, color = Color.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDiagnosisScreen(
    viewModel: HealthDiagnosisViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI 건강 분석 리포트", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 업로드 섹션 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "인바디 / 결과지 분석", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "이미지를 올리면 AI가 수치를 정밀 분석합니다.", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("결과지 사진 불러오기", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (selectedImageUri != null) {
                Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "선택된 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }

                Button(
                    onClick = { selectedBitmap?.let { viewModel.analyzeHealthData(it) } },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 정밀 분석 시작하기 ✨", fontWeight = FontWeight.Black)
                }
            }

            when (val state = uiState) {
                is DiagnosisUiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                        CircularProgressIndicator(color = Color(0xFF673AB7), strokeWidth = 6.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI가 수치를 읽고 리포트를 작성 중...", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                    }
                }
                is DiagnosisUiState.Success -> {
                    val res = state.result
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        AiResultCard("종합 분석", "📈", res.substringAfter("[분석]").substringBefore("[강점]"), Color(0xFF1976D2))
                        AiResultCard("나의 강점", "💪", res.substringAfter("[강점]").substringBefore("[개선]"), Color(0xFF388E3C))
                        AiResultCard("개선 필요", "🚩", res.substringAfter("[개선]").substringBefore("[식단]"), Color(0xFFD32F2F))
                        AiResultCard("맞춤 식단", "🥦", res.substringAfter("[식단]").substringBefore("[운동]"), Color(0xFFE64A19))
                        AiResultCard("추천 운동", "🏋️", res.substringAfter("[운동]"), Color(0xFF673AB7))
                    }
                }
                is DiagnosisUiState.Error -> {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(16.dp)) {
                        Text(text = state.message, modifier = Modifier.padding(20.dp), color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
