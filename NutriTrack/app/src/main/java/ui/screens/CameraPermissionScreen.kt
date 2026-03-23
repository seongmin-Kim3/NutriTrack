package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraPermissionScreen(
    onRequest: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("카메라 권한이 필요합니다", style = MaterialTheme.typography.titleMedium)
                Text(
                    "바코드 스캔 기능을 사용하려면 카메라 접근을 허용해주셔야 합니다.",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(onClick = onRequest, modifier = Modifier.fillMaxWidth()) {
                    Text("권한 요청하기")
                }

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("돌아가기")
                }
            }
        }
    }
}
