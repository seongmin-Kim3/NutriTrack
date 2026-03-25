package com.example.nutritrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WeeklyReportUi() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("주간 리포트(안정화 모드)", style = MaterialTheme.typography.titleMedium)
            Text("차트/TopFoods는 5-2에서 다시 연결합니다.")
        }
    }
}
