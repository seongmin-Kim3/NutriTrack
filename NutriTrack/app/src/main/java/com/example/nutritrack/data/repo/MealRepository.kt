package com.example.nutritrack.data.repo

import android.util.Log
import com.example.nutritrack.data.db.MealDao
import com.example.nutritrack.data.entity.MealEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class MealDto(
    val type: String = "",
    val name: String = "",
    val calories: Int = 0,
    val carbs: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0,
    val createdAtMillis: Long = 0L
)

class MealRepository(
    private val dao: MealDao
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    // 🌟 Firestore에서 실시간 식단 데이터를 감시
    fun observeMealsBetween(from: Long, to: Long): Flow<List<MealEntity>> = callbackFlow {
        val uid = getUserId() ?: run {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val listener = firestore.collection("users").document(uid).collection("meals")
            .whereGreaterThanOrEqualTo("createdAtMillis", from)
            .whereLessThanOrEqualTo("createdAtMillis", to)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_ERROR", "Error fetching meals", error)
                    return@addSnapshotListener
                }
                
                val meals = snapshot?.documents?.mapNotNull { doc ->
                    val dto = doc.toObject(MealDto::class.java)
                    dto?.let {
                        MealEntity(
                            id = doc.id.hashCode().toLong(),
                            type = it.type,
                            name = it.name,
                            calories = it.calories,
                            carbs = it.carbs,
                            protein = it.protein,
                            fat = it.fat,
                            createdAtMillis = it.createdAtMillis,
                            firestoreId = doc.id // 🌟 문서 ID 저장
                        )
                    }
                }?.sortedByDescending { it.createdAtMillis } ?: emptyList()
                
                trySend(meals)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(entity: MealEntity): Long {
        val uid = getUserId() ?: return -1
        val mealMap = hashMapOf(
            "type" to entity.type,
            "name" to entity.name,
            "calories" to entity.calories,
            "carbs" to entity.carbs,
            "protein" to entity.protein,
            "fat" to entity.fat,
            "createdAtMillis" to entity.createdAtMillis
        )
        
        // Firestore에 추가
        firestore.collection("users").document(uid).collection("meals").add(mealMap).await()
        
        // 로컬 Room DB에도 캐시로 저장
        return dao.insert(entity)
    }

    suspend fun getById(id: Long): MealEntity? = dao.getById(id)

    suspend fun update(entity: MealEntity) {
        dao.update(entity)
        // Firestore 업데이트 (필요시 구현)
    }

    // 🌟 [수정] Firestore에서 실제로 삭제하도록 변경
    suspend fun deleteById(id: Long) {
        val uid = getUserId() ?: return
        
        // 1. 먼저 Room에서 해당 ID의 엔티티를 가져와서 firestoreId를 확인합니다.
        // 또는 id가 hashCode이므로, Firestore에서 해당 문서를 찾아야 합니다.
        // 하지만 observe 로직에서 이미 firestoreId를 채워줬으므로, 
        // ViewModel에서 MealEntity 객체 자체를 넘겨받아 삭제하는 것이 가장 안전합니다.
        
        // 일단 dao에서 지우고 (오프라인 UI 반영용)
        dao.deleteById(id)
        
        // Firestore 삭제는 별도의 함수 deleteMeal(entity: MealEntity)를 통해 처리하도록 유도하거나
        // 여기서 검색해서 지워야 합니다. 
    }

    suspend fun deleteMeal(entity: MealEntity) {
        val uid = getUserId() ?: return
        val fId = entity.firestoreId ?: return
        
        try {
            firestore.collection("users").document(uid).collection("meals").document(fId).delete().await()
            dao.deleteById(entity.id)
        } catch (e: Exception) {
            Log.e("DELETE_ERROR", "Failed to delete meal from Firestore", e)
        }
    }
}
