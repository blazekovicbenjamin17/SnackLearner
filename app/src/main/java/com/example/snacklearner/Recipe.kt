package com.example.snacklearner

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val username: String = "",
    val ingredients: List<String> = emptyList(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val favorites: Int = 0,
    val timestamp: Long = 0L
)
