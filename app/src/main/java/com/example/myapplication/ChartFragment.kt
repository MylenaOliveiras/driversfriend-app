package com.example.myapplication

import API.RetrofitClient
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView // Importe TextView
import android.widget.Toast
import com.example.myapplication.models.ConsumptionResponse
import com.example.myapplication.models.VehicleList
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import androidx.core.content.ContextCompat
import android.graphics.drawable.Drawable

class ChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnAddRefuel: Button
    private lateinit var tvOverallAverage: TextView

    private val SELECTED_VEHICLE_KEY = "selected_vehicle_json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        btnAddRefuel = view.findViewById(R.id.btnAddRefuel)
        tvOverallAverage = view.findViewById(R.id.tvOverallAverage)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAddRefuel.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavItem(R.id.menu_fuel)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = requireActivity().getSharedPreferences("vehicle_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString(SELECTED_VEHICLE_KEY, null)
        var vehicleId: Int? = null

        tvOverallAverage.text = String.format(Locale.getDefault(), "Média Total Geral: -- Km/L")

        if (json != null) {
            try {
                val selectedVehicle = Gson().fromJson(json, VehicleList::class.java)
                vehicleId = selectedVehicle.id
                Log.d("ChartFragment", "ID do veículo selecionado do SharedPreferences: $vehicleId")
            } catch (e: Exception) {
                Log.e("ChartFragment", "Erro ao deserializar veículo selecionado: ${e.message}")
                Toast.makeText(context, "Erro ao carregar veículo selecionado. Por favor, selecione um veículo.", Toast.LENGTH_LONG).show()
                showEmptyState()
            }
        }

        if (vehicleId != null && vehicleId != 0) {
            loadConsumptionData(vehicleId)
        } else {
            Log.d("ChartFragment", "Nenhum veículo selecionado no SharedPreferences ou ID inválido.")
            showEmptyState()
        }
    }

    private fun loadConsumptionData(vehicleId: Int) {
        lineChart.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        lineChart.setNoDataText("Carregando dados...")
        tvOverallAverage.text = String.format(Locale.getDefault(), "Média Total Geral: -- Km/L") // Limpar ao carregar

        RetrofitClient.apiService.getConsumption(vehicleId).enqueue(object : Callback<ConsumptionResponse> {
            override fun onResponse(call: Call<ConsumptionResponse>, response: Response<ConsumptionResponse>) {
                if (response.isSuccessful) {
                    val consumptionResponse = response.body()
                    if (consumptionResponse != null && consumptionResponse.series.isNotEmpty()) {
                        setupChart(consumptionResponse)
                        // Exibir a média total no TextView
                        tvOverallAverage.text = String.format(Locale.getDefault(), "Média Total Geral: %.2f Km/L", consumptionResponse.mediaTotal)
                        tvOverallAverage.visibility = View.VISIBLE

                        lineChart.visibility = View.VISIBLE
                        emptyStateLayout.visibility = View.GONE
                    } else {
                        Log.d("ChartFragment", "Nenhum dado de consumo encontrado para o veículo $vehicleId.")
                        showEmptyState()
                        tvOverallAverage.visibility = View.GONE
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    Log.e("ChartFragment", "Erro na resposta da API: ${response.code()} - $errorMessage")
                    showErrorState("Erro ao carregar dados: ${response.code()} - $errorMessage")
                    tvOverallAverage.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<ConsumptionResponse>, t: Throwable) {
                Log.e("ChartFragment", "Falha ao carregar dados de consumo: ${t.message}", t)
                showErrorState("Falha de conexão: ${t.localizedMessage}")
                tvOverallAverage.visibility = View.GONE
            }
        })
    }

    private fun setupChart(consumptionResponse: ConsumptionResponse) {
        val sortedSeries = consumptionResponse.series.sortedBy {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(it.data)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        val entries = sortedSeries.mapIndexed { index, dataPoint ->
            Entry(index.toFloat(), dataPoint.consumoMedio.toFloat())
        }

        val consumptionDataSet = LineDataSet(entries, "Consumo Médio por Abastecimento")
        consumptionDataSet.color = ContextCompat.getColor(requireContext(), R.color.purpleMedium)
        consumptionDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.grayDark)
        consumptionDataSet.lineWidth = 2.8f
        consumptionDataSet.circleRadius = 4.5f
        consumptionDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.purpleDark))
        consumptionDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        consumptionDataSet.setDrawValues(true)
        consumptionDataSet.valueTextSize = 9f
        consumptionDataSet.setDrawCircleHole(false)

        consumptionDataSet.setDrawFilled(true)
        val fillDrawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.chart_fill)
        if (fillDrawable != null) {
            consumptionDataSet.fillDrawable = fillDrawable
        } else {
            consumptionDataSet.fillColor = ContextCompat.getColor(requireContext(), R.color.purpleAccent)
            consumptionDataSet.fillAlpha = 80
        }

        val lineData = LineData(consumptionDataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.grayDark)
        xAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.grayMedium)
        xAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.grayLighter)
        xAxis.setDrawGridLines(true)
        xAxis.valueFormatter = object : ValueFormatter() {
            private val displayDateFormat = SimpleDateFormat("MM/yy", Locale.getDefault())
            private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < sortedSeries.size) {
                    val dateString = sortedSeries[index].data
                    dateString?.let {
                        try {
                            val date = apiDateFormat.parse(it)
                            date?.let { displayDateFormat.format(it) } ?: ""
                        } catch (e: Exception) {
                            Log.e("ChartFormatter", "Erro ao parsear data: $it", e)
                            ""
                        }
                    } ?: ""
                } else {
                    ""
                }
            }
        }

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.grayDark)
        yAxisLeft.axisLineColor = ContextCompat.getColor(requireContext(), R.color.grayMedium)
        yAxisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.grayLighter)
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.setLabelCount(5, true)
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format(Locale.getDefault(), "%.2f Km/L", value)
            }
        }
        yAxisLeft.setAxisMinimum(0f)

        lineChart.axisRight.isEnabled = false

        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.isDragEnabled = true
        lineChart.setDrawGridBackground(false)
        lineChart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.grayDark)
        lineChart.legend.setDrawInside(false)
        lineChart.legend.yOffset = 10f

        lineChart.animateX(1000)
        lineChart.invalidate()
    }

    private fun showEmptyState() {
        lineChart.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        tvOverallAverage.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        lineChart.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.e("ChartFragment", message)
        tvOverallAverage.visibility = View.GONE
    }
}