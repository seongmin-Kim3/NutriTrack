package com.example.nutritrack.ui.screens

import android.widget.Toast
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
import com.example.nutritrack.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authVm: AuthViewModel, // 🌟 파이어베이스 사령관님 입장!
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by authVm.isLoading.collectAsState() // 🌟 로딩 상태 관찰

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    // 서버에서 오는 에러 메시지 띄울 상태
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isPasswordMatch = password == passwordConfirm
    // 파이어베이스는 비밀번호 6자리 이상을 요구하므로 조건 추가!
    val isFormValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && isPasswordMatch && password.length >= 6

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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

            OutlinedTextField(value = name, onValueChange = { name = it; showError = false }, label = { Text("이름 (또는 닉네임)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it; showError = false }, label = { Text("이메일") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it; showError = false }, label = { Text("비밀번호 (6자 이상)") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = passwordConfirm, onValueChange = { passwordConfirm = it; showError = false }, label = { Text("비밀번호 확인") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), isError = passwordConfirm.isNotBlank() && !isPasswordMatch, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.large)

            // 비밀번호 불일치 에러 (기존 디자인 그대로!)
            if (passwordConfirm.isNotBlank() && !isPasswordMatch) {
                Text("비밀번호가 일치하지 않습니다.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
            }

            // 🌟 파이어베이스 에러 발생 시 출력 (중복된 닉네임 등)
            if (showError) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        // 🌟 진짜 파이어베이스 회원가입 통신 시작!
                        authVm.signUp(
                            email = email,
                            password = password,
                            nickname = name, // 닉네임 자리에 입력한 이름을 넣습니다.
                            onSuccess = {
                                showError = false
                                Toast.makeText(context, "회원가입 성공! 환영합니다.", Toast.LENGTH_SHORT).show()
                                onSignupSuccess() // 가입 성공 시 로그인 화면으로 슝~
                            },
                            onError = { errorMsg ->
                                errorMessage = errorMsg
                                showError = true
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isFormValid && !isLoading, // 로딩중엔 연타 못하게 막기
                shape = MaterialTheme.shapes.large
            ) {
                // 🌟 통신 중일 땐 빙글빙글 로딩바 띄워주기
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("가입 완료", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}