package com.example.myapplication.models



data class NewFuel(
    val dataAbastecimento: String,
    val valorUnitario: Double,
    val kmAtual: Int,
    val litrosAbastecidos: Double?,
    val observacao: String?
)

data class Fueling(
    val id: Int,
    val dataAbastecimento: String,
    val valorTotal: Double,
    val valorUnitario: Double,
    val litrosAbastecidos: Double,
    val kmAtual: Int,
    val observacao: String?
)