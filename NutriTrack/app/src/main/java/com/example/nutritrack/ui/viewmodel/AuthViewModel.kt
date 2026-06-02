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
                    onFailure = { 
                        // 🌟 [수정] 이미 존재하는 계정 에러 문구를 한국어로 변경
                        val rawMsg = it.message ?: ""
                        val koreanMsg = when {
                            rawMsg.contains("email address is already in use") -> "이미 존재하는 이메일 입니다."
                            rawMsg.contains("password") && rawMsg.contains("at least 6 characters") -> "비밀번호는 최소 6자 이상이어야 합니다."
                            rawMsg.contains("badly formatted") -> "이메일 형식이 올바르지 않습니다."
                            else -> it.message ?: "회원가입에 실패했습니다."
                        }
                        onError(koreanMsg)
                    }
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
                        // 🌟 [추가] 로그인 에러 문구도 한국어로 대응
                        val rawMsg = it.message ?: ""
                        val koreanMsg = when {
                            rawMsg.contains("no user record") || rawMsg.contains("invalid-credential") || rawMsg.contains("wrong-password") -> 
                                "이메일 또는 비밀번호가 일치하지 않습니다."
                            rawMsg.contains("blocked all requests from this device") -> 
                                "너무 많은 시도로 인해 로그인이 일시적으로 제한되었습니다."
                            else -> "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요."
                        }
                        onError(koreanMsg)
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