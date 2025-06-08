package com.example.myapplication.models

data class RegisterRequest(
    val nome: String,
    val cpf: String,
    val email: String,
    val senha: String
)
