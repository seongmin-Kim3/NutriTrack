package com.example.nutritrack.data.db

import androidx.room.*
import com.example.nutritrack.data.entity.ShoppingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_list ORDER BY id DESC")
    fun getAll(): Flow<List<ShoppingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ShoppingEntity)

    @Update
    suspend fun update(entity: ShoppingEntity)

    @Delete
    suspend fun delete(entity: ShoppingEntity)

    @Query("DELETE FROM shopping_list")
    suspend fun deleteAll()
}
