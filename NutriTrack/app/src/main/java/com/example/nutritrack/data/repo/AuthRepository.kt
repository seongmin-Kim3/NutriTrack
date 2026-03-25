package com.example.nutritrack.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun isNicknameAvailable(nickname: String): Boolean {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("nickname", nickname)
                .get()
                .await()

            snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUp(email: String, password: String, nickname: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("유저 고유번호(UID)를 가져올 수 없습니다.")

            val userData = hashMapOf(
                "email" to email,
                "nickname" to nickname,
                "createdAtMillis" to System.currentTimeMillis()
            )

            db.collection("users").document(uid).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}