package com.example.nutritrack.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nutritrack.ui.viewmodel.DiagnosisUiState
import com.example.nutritrack.ui.viewmodel.HealthDiagnosisViewModel

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
        topBar = {
            TopAppBar(
                title = { Text("AI 건강 데이터 진단", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "뒤로") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "인바디 결과지나 건강검진 기록표를 업로드해주세요.\nAI 트레이너가 정밀하게 분석해 드립니다!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("📷 갤러리에서 결과지 선택하기", fontWeight = FontWeight.Bold)
            }

            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "선택된 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Button(
                    onClick = { selectedBitmap?.let { viewModel.analyzeHealthData(it) } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("✨ AI 진단 시작하기", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            when (val state = uiState) {
                is DiagnosisUiState.Initial -> { }
                is DiagnosisUiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI가 데이터를 꼼꼼히 읽고 있습니다...\n(최대 10~15초 소요)")
                    }
                }
                is DiagnosisUiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = state.result,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is DiagnosisUiState.Error -> {
                    Text(text = "에러: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}