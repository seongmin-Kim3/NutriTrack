package com.example.nutritrack.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutritrack.data.entity.ShoppingEntity
import com.example.nutritrack.data.repo.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(private val repo: ShoppingRepository) : ViewModel() {
    val items: StateFlow<List<ShoppingEntity>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String) {
        viewModelScope.launch { repo.insert(ShoppingEntity(name = name)) }
    }

    fun toggleCheck(entity: ShoppingEntity) {
        viewModelScope.launch { repo.update(entity.copy(isChecked = !entity.isChecked)) }
    }

    fun deleteItem(entity: ShoppingEntity) {
        viewModelScope.launch { repo.delete(entity) }
    }

    fun clearAll() {
        viewModelScope.launch { repo.clearAll() }
    }
}

class ShoppingViewModelFactory(private val repo: ShoppingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShoppingViewModel(repo) as T
    }
}
