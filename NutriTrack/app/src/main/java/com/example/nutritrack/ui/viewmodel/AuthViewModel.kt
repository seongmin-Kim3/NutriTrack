package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 🌟 1. 회원가입 기능 (안전한 방어막 추가!)
    fun signUp(
        email: String,
        password: String,
        nickname: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true // 로딩 켜기

                // 닉네임 중복 검사 (여기서 에러가 터져서 무한로딩이 걸렸던 겁니다!)
                val isAvailable = repository.isNicknameAvailable(nickname)
                if (!isAvailable) {
                    onError("이미 사용 중인 닉네임입니다. 다른 이름을 입력해주세요!")
                    return@launch
                }

                // 진짜 회원가입 처리
                val result = repository.signUp(email, password, nickname)
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { onError(it.message ?: "회원가입에 실패했습니다.") }
                )
            } catch (e: Exception) {
                // 에러가 터지면 앱이 뻗지 않고 빨간 글씨로 진짜 원인을 알려줍니다!
                onError("서버 에러: ${e.localizedMessage}")
            } finally {
                // 🌟 성공하든, 에러가 나든 무조건 로딩 바를 끕니다! (무한로딩 완벽 해결)
                _isLoading.value = false
            }
        }
    }

    // 🌟 2. 로그인 기능 (안전한 방어막 추가!)
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.login(email, password)

                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = {
                        onError(it.message ?: "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.")
                    }
                )
            } catch (e: Exception) {
                onError("서버 에러: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}