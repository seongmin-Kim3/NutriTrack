package com.example.nutritrack.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutritrack.R
import com.example.nutritrack.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authVm: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val isLoading by authVm.isLoading.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(120.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            // 🌟 NutriTrack -> Nuon으로 변경!
            Text(
                "Nuon", 
                fontSize = 40.sp,
                fontWeight = FontWeight.Black, 
                color = Color(0xFF1565C0)
            )
            Text(
                "AI와 함께하는 영양관리",
                fontSize = 14.sp, 
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email, 
                        onValueChange = { email = it; showError = false }, 
                        label = { Text("이메일") }, 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), 
                        modifier = Modifier.fillMaxWidth(), 
                        singleLine = true, 
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password, 
                        onValueChange = { password = it; showError = false }, 
                        label = { Text("비밀번호") }, 
                        visualTransformation = PasswordVisualTransformation(), 
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), 
                        modifier = Modifier.fillMaxWidth(), 
                        singleLine = true, 
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFEF5350),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "이메일과 비밀번호를 입력해주세요."
                        showError = true
                        return@Button
                    }
                    authVm.login(
                        email = email,
                        password = password,
                        onSuccess = {
                            showError = false
                            onLoginSuccess()
                        },
                        onError = { errorMsg ->
                            errorMessage = errorMsg
                            showError = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("로그인", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("처음이신가요? ", color = Color.Gray)
                Text("회원가입 하기", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }
    }
}
