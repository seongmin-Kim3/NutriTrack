package com.example.nutritrack.data.repo

import com.example.nutritrack.data.db.FoodDao
import com.example.nutritrack.data.entity.FoodHistoryEntity
import com.example.nutritrack.data.entity.FoodTemplateEntity
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val dao: FoodDao
) {
    fun observeTemplates(): Flow<List<FoodTemplateEntity>> = dao.observeTemplates()
    fun searchTemplates(q: String): Flow<List<FoodTemplateEntity>> = dao.searchTemplates(q)

    suspend fun insertTemplate(entity: FoodTemplateEntity) = dao.insertTemplate(entity)
    suspend fun getTemplateById(id: Long): FoodTemplateEntity? = dao.getTemplateById(id)
    suspend fun updateTemplate(entity: FoodTemplateEntity) = dao.updateTemplate(entity)
    suspend fun deleteTemplate(id: Long) = dao.deleteTemplate(id)

    fun observeRecentHistory(limit: Int): Flow<List<FoodHistoryEntity>> = dao.observeRecentHistory(limit)
    suspend fun insertHistory(entity: FoodHistoryEntity) = dao.insertHistory(entity)
}
