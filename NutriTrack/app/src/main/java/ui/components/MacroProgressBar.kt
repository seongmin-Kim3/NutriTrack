package com.example.nutritrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun MacroProgressBar(
    title: String,
    current: Int,
    target: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    val safeTarget = max(1, target)
    val ratio = min(1f, current.toFloat() / safeTarget.toFloat())

    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text("$current / $target $unit", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
    }
}
