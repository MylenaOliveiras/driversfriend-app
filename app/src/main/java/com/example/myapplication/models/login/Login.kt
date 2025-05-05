package com.example.myapplication.models.login

data class LoginRequest(
    val email: String,
    val senha: String
)

data class LoginResponse(
    val token: String,
    val userId: Int
)
