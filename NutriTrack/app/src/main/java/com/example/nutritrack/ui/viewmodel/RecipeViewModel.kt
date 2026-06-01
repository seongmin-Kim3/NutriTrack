package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.entity.FavoriteRecipeEntity
import com.example.nutritrack.data.repo.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeViewModel(private val repo: RecipeRepository) : ViewModel() {
    val favorites: StateFlow<List<FavoriteRecipeEntity>> = repo.getFavoriteRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveFavorite(mealType: String, menuName: String, kcal: Int, ingredients: String, description: String) {
        viewModelScope.launch {
            repo.insertFavorite(
                FavoriteRecipeEntity(
                    mealType = mealType,
                    menuName = menuName,
                    kcal = kcal,
                    ingredients = ingredients,
                    description = description
                )
            )
        }
    }

    fun deleteFavorite(entity: FavoriteRecipeEntity) {
        viewModelScope.launch {
            repo.deleteFavorite(entity)
        }
    }
}

class RecipeViewModelFactory(private val repo: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RecipeViewModel(repo) as T
    }
}
