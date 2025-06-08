package com.example.myapplication.models

data class ConsumptionData(
    val data: String?,
    val kmRodado: Int,
    val litros: Double?,
    val consumoMedio: Double
)

data class ConsumptionResponse(
    val mediaTotal: Double,
    val series: List<ConsumptionData>
)
