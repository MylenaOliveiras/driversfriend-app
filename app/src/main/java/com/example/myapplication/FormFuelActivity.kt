package com.example.myapplication

import API.RetrofitClient
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.models.Fueling
import com.example.myapplication.models.NewFuel
import com.example.myapplication.models.VehicleList
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FormFuelActivity : AppCompatActivity() {

    private lateinit var editTextValorUnitario: TextInputEditText
    private lateinit var editTextKmAtual: TextInputEditText
    private lateinit var editTextLitrosAbastecidos: TextInputEditText
    private lateinit var editTextObservacao: TextInputEditText
    private lateinit var buttonRegistrarAbastecimento: Button

    private var selectedVehicleId: Int? = null
    private val SELECTED_VEHICLE_KEY = "selected_vehicle_json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_fuel)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_abastecimento)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextValorUnitario = findViewById(R.id.editTextValorUnitario)
        editTextKmAtual = findViewById(R.id.editTextKmAtual)
        editTextLitrosAbastecidos = findViewById(R.id.editTextLitrosAbastecidos)
        editTextObservacao = findViewById(R.id.editTextObservacao)
        buttonRegistrarAbastecimento = findViewById(R.id.buttonRegistrarAbastecimento)

        buttonRegistrarAbastecimento.setOnClickListener {
            registerFueling()
        }
    }

    override fun onResume() {
        super.onResume()
        selectedVehicleId = loadSelectedVehicleId()
        if (selectedVehicleId == null) {
            Toast.makeText(this, "Nenhum veículo selecionado. Por favor, selecione um veículo na tela inicial.", Toast.LENGTH_LONG).show()
            buttonRegistrarAbastecimento.isEnabled = false
        } else {
            buttonRegistrarAbastecimento.isEnabled = true
        }
    }

    private fun loadSelectedVehicleId(): Int? {
        val sharedPref = getSharedPreferences("vehicle_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString(SELECTED_VEHICLE_KEY, null)
        var vehicleId: Int? = null

        if (json != null) {
            try {
                val selectedVehicle = Gson().fromJson(json, VehicleList::class.java)
                vehicleId = selectedVehicle?.id
                Log.d("FormFuelActivity", "ID do veículo selecionado do SharedPreferences: $vehicleId")
            } catch (e: Exception) {
                Log.e("FormFuelActivity", "Erro ao deserializar veículo selecionado: ${e.message}", e)
                Toast.makeText(
                    this,
                    "Erro ao carregar veículo selecionado. Por favor, selecione um veículo.",
                    Toast.LENGTH_LONG
                ).show()
                return null
            }
        }
        return vehicleId
    }

    private fun registerFueling() {
        val currentDateTime = Date()

        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dataAbastecimento = isoDateFormat.format(currentDateTime)

        Log.d("FormFuelActivity", "Data Abastecimento (ISO): $dataAbastecimento")

        val valorUnitarioStr = editTextValorUnitario.text.toString().trim()
        val kmAtualStr = editTextKmAtual.text.toString().trim()
        val litrosAbastecidosStr = editTextLitrosAbastecidos.text.toString().trim()
        val observacao = editTextObservacao.text.toString().trim()

        if (valorUnitarioStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o Valor Unitário.", Toast.LENGTH_SHORT).show()
            return
        }
        if (kmAtualStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira o Km Atual.", Toast.LENGTH_SHORT).show()
            return
        }

        val valorUnitario = valorUnitarioStr.toDoubleOrNull()
        val kmAtual = kmAtualStr.toIntOrNull()
        val litrosAbastecidos = litrosAbastecidosStr.takeIf { it.isNotEmpty() }?.toDoubleOrNull()

        if (valorUnitario == null || kmAtual == null) {
            Toast.makeText(this, "Por favor, insira valores numéricos válidos para Valor Unitário e Km Atual.", Toast.LENGTH_LONG).show()
            return
        }

        val newFuelRequest = NewFuel(
            dataAbastecimento = dataAbastecimento,
            valorUnitario = valorUnitario,
            kmAtual = kmAtual,
            litrosAbastecidos = litrosAbastecidos,
            observacao = observacao.takeIf { it.isNotEmpty() }
        )

        selectedVehicleId?.let { vehicleId ->
            RetrofitClient.apiService.createFueling(newFuelRequest, vehicleId).enqueue(object : Callback<Fueling> {
                override fun onResponse(call: Call<Fueling>, response: Response<Fueling>) {
                    if (response.isSuccessful) {
                        val createdFueling = response.body()
                        if (createdFueling != null) {
                            Toast.makeText(this@FormFuelActivity, "Abastecimento registrado com sucesso!", Toast.LENGTH_LONG).show()
                            Log.d("FormFuelActivity", "Abastecimento criado: $createdFueling")
                        } else {
                            Toast.makeText(this@FormFuelActivity, "Abastecimento registrado, mas sem dados de retorno.", Toast.LENGTH_LONG).show()
                        }
                        finish()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                        showError("Erro ao registrar abastecimento: ${response.code()} - $errorMessage")
                        Log.e("FormFuelActivity", "Erro registrar abastecimento: ${response.code()} - $errorMessage")
                    }
                }

                override fun onFailure(call: Call<Fueling>, t: Throwable) {
                    showError("Falha na conexão: ${t.message}")
                    Log.e("FormFuelActivity", "Falha na conexão ao registrar abastecimento", t)
                }
            })
        } ?: run {
            Toast.makeText(this, "Nenhum veículo selecionado. Não é possível registrar o abastecimento.", Toast.LENGTH_LONG).show()
            Log.e("FormFuelActivity", "selectedVehicleId é nulo ao tentar registrar abastecimento.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}