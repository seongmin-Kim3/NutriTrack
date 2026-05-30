package com.example.nutritrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list")
data class ShoppingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isChecked: Boolean = false
)
