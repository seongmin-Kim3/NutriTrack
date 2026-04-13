package com.example.nutritrack.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.nutritrack.data.network.OpenFoodFactsClient // 🌟 진짜 통신병 임포트!
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch // 🌟 네트워크 통신을 위한 코루틴 임포트!
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    onFound: (String, String, Int, Int, Int, Int) -> Unit, // 🌟 지방(fat)까지 6개의 데이터를 넘깁니다.
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // 🌟 서버 통신을 위한 스코프

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFetching by remember { mutableStateOf(false) } // 로딩 중인지 확인하는 변수

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("바코드 스캔") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BarcodeScannerView { scannedCode ->
                        if (!isFetching) { // 중복 스캔 방지
                            isFetching = true
                            Toast.makeText(context, "바코드 인식 완료! 정보를 불러옵니다...", Toast.LENGTH_SHORT).show()

                            // 🌟 가짜 데이터 대신, 진짜 OpenFoodFacts 서버에 바코드를 물어봅니다!
                            scope.launch {
                                val result = OpenFoodFactsClient.fetchByBarcode(scannedCode)

                                if (result != null && result.name != null) {
                                    // DB에서 음식을 찾았을 때!
                                    onFound(
                                        scannedCode,
                                        result.name,
                                        result.caloriesKcal ?: 0,
                                        result.carbsG ?: 0,
                                        result.proteinG ?: 0,
                                        result.fatG ?: 0
                                    )
                                } else {
                                    // DB에 없는 바코드일 때 (예: 한국 전용 로컬 상품 등)
                                    Toast.makeText(context, "DB에 없는 상품입니다. 직접 입력해주세요!", Toast.LENGTH_LONG).show()
                                    onBack() // 빈손으로 돌아갑니다
                                }
                            }
                        }
                    }

                    // 서버 통신 중일 때 화면 가운데 로딩 빙글빙글 띄우기
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                // 권한 요청 UI (기존 코드와 동일)
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏋️‍♂️\n정확한 음식 등록을 위해\n카메라 권한이 필요해요.", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
                    Text("스캔한 바코드 번호로 전 세계 음식 영양 정보를\n순식간에 불러올 수 있습니다.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("카메라 권한 허용하기") }
                }
            }
        }
    }
}

@Composable
fun BarcodeScannerView(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isBarcodeDetected by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy, barcodeScanner) { scannedCode ->
                                if (!isBarcodeDetected) {
                                    isBarcodeDetected = true
                                    onBarcodeDetected(scannedCode)
                                }
                            }
                        }
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                    } catch (e: Exception) { Log.e("BarcodeScanner", "카메라 바인딩 실패", e) }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        Box(modifier = Modifier.align(Alignment.Center).size(width = 280.dp, height = 180.dp).clip(RoundedCornerShape(12.dp)).background(Color.Transparent).padding(2.dp), contentAlignment = Alignment.Center) {
            Text("바코드를 사각형 안에 맞춰주세요.", color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun processImageProxy(imageProxy: ImageProxy, barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner, onBarcodeDetected: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(inputImage).addOnSuccessListener { barcodes ->
            barcodes.firstOrNull()?.rawValue?.let { scannedCode -> onBarcodeDetected(scannedCode) }
        }.addOnCompleteListener { imageProxy.close() }
    } else { imageProxy.close() }
}