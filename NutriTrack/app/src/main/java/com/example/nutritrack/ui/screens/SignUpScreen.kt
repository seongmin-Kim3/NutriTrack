package com.example.nutritrack.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    // 💡 스마트폰 내부 저장소를 불러오는 마법의 코드
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    val isPasswordMatch = password == passwordConfirm
    val isFormValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && isPasswordMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Nuon 시작하기", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 32.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름 (또는 닉네임)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = passwordConfirm, onValueChange = { passwordConfirm = it }, label = { Text("비밀번호 확인") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), isError = passwordConfirm.isNotBlank() && !isPasswordMatch, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)

            if (passwordConfirm.isNotBlank() && !isPasswordMatch) {
                Text("비밀번호가 일치하지 않습니다.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        // 🌟 핵심: 가입 성공 시 내부 저장소에 이메일과 비밀번호를 진짜로 저장합니다!
                        authPrefs.edit()
                            .putString("email", email)
                            .putString("password", password)
                            .apply()

                        onSignUpSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp), enabled = isFormValid, shape = MaterialTheme.shapes.large
            ) { Text("가입 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}