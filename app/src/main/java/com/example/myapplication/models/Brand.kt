package com.example.myapplication.models

data class Brand(
    val id: Int,
    val nome: String
) {
    override fun toString(): String = nome
}
