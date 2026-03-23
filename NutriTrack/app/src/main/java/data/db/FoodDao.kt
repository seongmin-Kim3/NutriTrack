package com.example.nutritrack.data.db

import androidx.room.*
import com.example.nutritrack.data.entity.FoodHistoryEntity
import com.example.nutritrack.data.entity.FoodTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // ---------- Templates(내 음식) ----------
    @Query("SELECT * FROM food_templates ORDER BY createdAtMillis DESC")
    fun observeTemplates(): Flow<List<FoodTemplateEntity>>

    @Query("SELECT * FROM food_templates WHERE name LIKE '%' || :q || '%' ORDER BY createdAtMillis DESC")
    fun searchTemplates(q: String): Flow<List<FoodTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(entity: FoodTemplateEntity)

    @Query("SELECT * FROM food_templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): FoodTemplateEntity?

    @Update
    suspend fun updateTemplate(entity: FoodTemplateEntity)

    @Query("DELETE FROM food_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    // ---------- History(검색/기록) ----------
    @Query("SELECT * FROM food_history ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecentHistory(limit: Int): Flow<List<FoodHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: FoodHistoryEntity)
}
