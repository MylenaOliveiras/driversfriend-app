package com.example.myapplication.models


data class Vehicle(
    val marca: Int,
    val modelo: String,
    val cor: String,
    val anoModelo: Int,
    val anoFabricacao: Int,
    val placaVeiculo: String,
    val kmInicial: Int,
    val combustivel: String,
    val tipoVeiculo: String,
)

data class VehicleBrand(
    val id: Int,
    val nome: String
)
