package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDeniedScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("카메라 권한이 필요합니다.", style = MaterialTheme.typography.titleMedium)
                Text("설정에서 카메라 권한을 허용한 뒤 다시 시도해 주세요.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("돌아가기")
                }
            }
        }
    }
}
