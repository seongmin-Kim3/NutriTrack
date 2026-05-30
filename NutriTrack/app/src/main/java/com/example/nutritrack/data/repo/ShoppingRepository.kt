package com.example.nutritrack.data.repo

import com.example.nutritrack.data.db.ShoppingDao
import com.example.nutritrack.data.entity.ShoppingEntity
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(private val dao: ShoppingDao) {
    fun getAll(): Flow<List<ShoppingEntity>> = dao.getAll()
    suspend fun insert(entity: ShoppingEntity) = dao.insert(entity)
    suspend fun update(entity: ShoppingEntity) = dao.update(entity)
    suspend fun delete(entity: ShoppingEntity) = dao.delete(entity)
    suspend fun clearAll() = dao.deleteAll()
}
