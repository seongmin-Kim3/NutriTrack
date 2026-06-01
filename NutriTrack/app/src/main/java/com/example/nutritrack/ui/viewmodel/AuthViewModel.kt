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

    // 🌟 회원가입 기능 (닉네임 즉시 저장 포함)
    fun signUp(
        email: String,
        password: String,
        nickname: String,
        goalPrefs: com.example.nutritrack.data.settings.GoalPrefs, // 🌟 GoalPrefs 추가
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val isAvailable = repository.isNicknameAvailable(nickname)
                if (!isAvailable) {
                    onError("이미 사용 중인 닉네임입니다. 다른 이름을 입력해주세요!")
                    return@launch
                }

                val result = repository.signUp(email, password, nickname)
                result.fold(
                    onSuccess = { 
                        // 🌟 회원가입 성공 시 로컬에도 즉시 닉네임 저장!
                        goalPrefs.saveUserNickname(nickname)
                        onSuccess() 
                    },
                    onFailure = { onError(it.message ?: "회원가입에 실패했습니다.") }
                )
            } catch (e: Exception) {
                onError("서버 에러: ${e.localizedMessage}")
            } finally {
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