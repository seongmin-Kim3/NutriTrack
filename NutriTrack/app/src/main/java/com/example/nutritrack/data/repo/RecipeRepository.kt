package com.example.nutritrack.data.repo

import com.example.nutritrack.data.db.FavoriteRecipeDao
import com.example.nutritrack.data.entity.FavoriteRecipeEntity
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val dao: FavoriteRecipeDao) {
    fun getFavoriteRecipes(): Flow<List<FavoriteRecipeEntity>> = dao.getAll()
    suspend fun insertFavorite(entity: FavoriteRecipeEntity) = dao.insert(entity)
    suspend fun deleteFavorite(entity: FavoriteRecipeEntity) = dao.delete(entity)
}
