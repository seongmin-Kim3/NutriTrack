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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.nutritrack.data.network.HybridFoodSearchClient
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanScreen(
    onFound: (String, String, Int, Int, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFetching by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("바코드 스캔") },
                navigationIcon = { TextButton(onClick = onBack) { Text("뒤로") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize()) {
                    BarcodeScannerView { scannedCode ->
                        if (!isFetching) {
                            isFetching = true
                            Toast.makeText(context, "바코드 인식 완료! 정보를 불러옵니다...", Toast.LENGTH_SHORT).show()

                            scope.launch {
                                // 🌟 수정된 식품안전나라 API 호출!
                                val result = HybridFoodSearchClient.fetchByBarcode(scannedCode)

                                if (result != null) {
                                    onFound(
                                        scannedCode,
                                        result.name ?: "알 수 없는 제품",
                                        result.caloriesKcal ?: 0,
                                        result.carbsG ?: 0,
                                        result.proteinG ?: 0,
                                        result.fatG ?: 0
                                    )
                                } else {
                                    Toast.makeText(context, "국내 DB에 없는 상품입니다. 직접 입력해주세요!", Toast.LENGTH_LONG).show()
                                    onBack()
                                }
                                isFetching = false
                            }
                        }
                    }
                    if (isFetching) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("카메라 권한이 필요합니다.", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("권한 허용") }
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