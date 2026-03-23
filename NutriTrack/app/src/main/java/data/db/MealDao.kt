package com.example.nutritrack.data.db

import androidx.room.*
import com.example.nutritrack.data.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Query("SELECT * FROM meals WHERE createdAtMillis BETWEEN :from AND :to ORDER BY createdAtMillis DESC")
    fun observeMealsBetween(from: Long, to: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MealEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MealEntity): Long

    @Update
    suspend fun update(entity: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
