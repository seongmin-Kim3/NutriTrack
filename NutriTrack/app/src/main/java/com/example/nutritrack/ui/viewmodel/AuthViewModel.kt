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

    // 로딩 상태 (버튼 빙글빙글) 관리
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 🌟 1. 회원가입 기능
    fun signUp(
        email: String,
        password: String,
        nickname: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // 닉네임 중복 검사
            val isAvailable = repository.isNicknameAvailable(nickname)
            if (!isAvailable) {
                _isLoading.value = false
                onError("이미 사용 중인 닉네임입니다. 다른 이름을 입력해주세요!")
                return@launch
            }

            // 진짜 회원가입 처리
            val result = repository.signUp(email, password, nickname)
            _isLoading.value = false

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "회원가입에 실패했습니다.") }
            )
        }
    }

    // 🌟 2. 로그인 기능 (새로 추가됨!)
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(email, password) // 통신병에게 로그인 지시!
            _isLoading.value = false

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = {
                    onError(it.message ?: "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.")
                }
            )
        }
    }
}