package com.example.nutritrack.data.db

import androidx.room.*
import com.example.nutritrack.data.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {
    @Query("SELECT * FROM favorite_recipes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<FavoriteRecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteRecipeEntity)

    @Delete
    suspend fun delete(entity: FavoriteRecipeEntity)
}
