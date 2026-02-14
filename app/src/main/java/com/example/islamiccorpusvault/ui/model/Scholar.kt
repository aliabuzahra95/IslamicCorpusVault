package com.example.islamiccorpusvault.ui.model

data class Scholar(
    val id: String,
    val name: String,
    val era: String? = null,
    val madhhab: String? = null
)