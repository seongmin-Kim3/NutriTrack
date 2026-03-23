package com.example.nutritrack.data.repo

import com.example.nutritrack.data.db.MealDao
import com.example.nutritrack.data.entity.MealEntity
import kotlinx.coroutines.flow.Flow

class MealRepository(
    private val dao: MealDao
) {
    fun observeMealsBetween(from: Long, to: Long): Flow<List<MealEntity>> =
        dao.observeMealsBetween(from, to)

    suspend fun insert(entity: MealEntity): Long = dao.insert(entity)

    suspend fun getById(id: Long): MealEntity? = dao.getById(id)

    suspend fun update(entity: MealEntity) = dao.update(entity)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
