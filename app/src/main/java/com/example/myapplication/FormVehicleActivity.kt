package com.example.myapplication

import API.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.models.vehicle.Vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormVehicleActivity : AppCompatActivity() {
    private var vehicleId: Int? = null
    private lateinit var editTextMarca: EditText
    private lateinit var editTextCor: EditText
    private lateinit var editTextModelo: EditText
    private lateinit var editTextKmInicial: EditText
    private lateinit var editTextAnoModelo: EditText
    private lateinit var editTextAnoFabricacao: EditText
    private lateinit var editTextPlacaVeiculo: EditText
    private lateinit var spinnerTipoCombustivel: Spinner
    private lateinit var spinnerTipoVeiculo: Spinner
    private lateinit var btnEnviar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_vehicle)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollViewForm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent: Intent? = intent
        vehicleId = intent?.getIntExtra("id", 0)

        editTextMarca = findViewById(R.id.editTextMarca)
        editTextCor = findViewById(R.id.editTextCor)
        editTextModelo = findViewById(R.id.editTextModelo)
        editTextKmInicial = findViewById(R.id.editTextKmInicial)
        editTextAnoModelo = findViewById(R.id.editTextAnoModelo)
        editTextAnoFabricacao = findViewById(R.id.editTextAnoFabricacao)
        editTextPlacaVeiculo = findViewById(R.id.editTextPlacaVeiculo)
        spinnerTipoCombustivel = findViewById(R.id.spinnerTipoCombustivel)
        spinnerTipoVeiculo = findViewById(R.id.spinnerTipoVeiculo)
        btnEnviar = findViewById(R.id.buttonEnviar)

        if (vehicleId == 0) {
            btnEnviar.text = "Criar"
            btnEnviar.setOnClickListener {
                onCreateVehicle()
            }
        } else {
            btnEnviar.text = "Editar"
            btnEnviar.setOnClickListener {
                vehicleId?.let { id -> onEditVehicle(id) }
            }
            loadVehicleDetails(vehicleId)
        }
    }

    private fun loadVehicleDetails(id: Int?) {
        id?.let { vehicleId ->
            RetrofitClient.apiService.getVehicleById(vehicleId).enqueue(
                object : Callback<Vehicle> {
                    override fun onResponse(
                        call: Call<Vehicle>,
                        response: Response<Vehicle>,
                    ) {
                        if (response.isSuccessful) {
                            val vehicle: Vehicle? = response.body()
                            vehicle?.let {
                                Log.d("API Success", "Veículo recebido para edição: ${it.modelo}")
                                editTextMarca.setText(it.marca?.toString() ?: "")
                                editTextCor.setText(it.cor ?: "")
                                editTextModelo.setText(it.modelo ?: "")
                                editTextKmInicial.setText(it.kmInicial?.toString() ?: "")
                                editTextAnoModelo.setText(it.anoModelo?.toString() ?: "")
                                editTextAnoFabricacao.setText(it.anoFabricacao?.toString() ?: "")
                                editTextPlacaVeiculo.setText(it.placaVeiculo ?: "")

                                val combustivelAdapter = spinnerTipoCombustivel.adapter
                                for (i in 0 until combustivelAdapter.count) {
                                    if (combustivelAdapter.getItem(i)
                                            .toString() == it.combustivel
                                    ) {
                                        spinnerTipoCombustivel.setSelection(i)
                                        break
                                    }
                                }

                                val veiculoAdapter = spinnerTipoVeiculo.adapter
                                for (i in 0 until veiculoAdapter.count) {
                                    if (veiculoAdapter.getItem(i).toString() == it.tipoVeiculo) {
                                        spinnerTipoVeiculo.setSelection(i)
                                        break
                                    }
                                }
                            }
                        } else {
                            val errorCode = response.code()
                            val errorBody = response.errorBody()?.string()
                            Log.e(
                                "API Error",
                                "Erro ao buscar veículo para edição. Código: $errorCode, Mensagem: $errorBody"
                            )
                            Toast.makeText(
                                this@FormVehicleActivity,
                                "Erro ao carregar detalhes do veículo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Vehicle>, t: Throwable) {
                        Log.e(
                            "API Failure",
                            "Falha ao conectar com a API para editar: ${t.message}"
                        )
                        Toast.makeText(
                            this@FormVehicleActivity,
                            "Falha ao carregar detalhes do veículo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun onCreateVehicle() {
        val marca = editTextMarca.text.toString().toIntOrNull()
        val cor = editTextCor.text.toString()
        val modelo = editTextModelo.text.toString()
        val kmInicial = editTextKmInicial.text.toString().toIntOrNull()
        val anoModelo = editTextAnoModelo.text.toString().toIntOrNull()
        val anoFabricacao = editTextAnoFabricacao.text.toString().toIntOrNull()
        val placa = editTextPlacaVeiculo.text.toString()
        val tipoCombustivel = spinnerTipoCombustivel.selectedItem.toString()
        val tipoVeiculo = spinnerTipoVeiculo.selectedItem.toString()

        if (marca == null || anoModelo == null || anoFabricacao == null || kmInicial == null) {
            Toast.makeText(
                this@FormVehicleActivity,
                "Por favor, preencha todos os campos obrigatórios.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val vehicleRequest = Vehicle(
            marca,
            modelo,
            cor,
            anoModelo,
            anoFabricacao,
            placa,
            kmInicial,
            tipoCombustivel,
            tipoVeiculo
        )

        val call = RetrofitClient.apiService.createVehicle(vehicleRequest)

        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        this@FormVehicleActivity,
                        "Veículo criado com sucesso",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "FormVehicleActivity",
                        "Erro ao criar veículo: ${response.code()} - $errorBody"
                    )
                    Toast.makeText(
                        this@FormVehicleActivity,
                        "Erro ao criar veículo: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("FormVehicleActivity", "Falha ao conectar com a API para criar: ${t.message}")
                Toast.makeText(
                    this@FormVehicleActivity, "Erro: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun onEditVehicle(id: Int) {
        val marca = editTextMarca.text.toString().toIntOrNull()
        val cor = editTextCor.text.toString()
        val modelo = editTextModelo.text.toString()
        val kmInicial = editTextKmInicial.text.toString().toIntOrNull()
        val anoModelo = editTextAnoModelo.text.toString().toIntOrNull()
        val anoFabricacao = editTextAnoFabricacao.text.toString().toIntOrNull()
        val placa = editTextPlacaVeiculo.text.toString()
        val tipoCombustivel = spinnerTipoCombustivel.selectedItem.toString()
        val tipoVeiculo = spinnerTipoVeiculo.selectedItem.toString()

        if (marca == null || anoModelo == null || anoFabricacao == null || kmInicial == null) {
            Toast.makeText(
                this@FormVehicleActivity,
                "Por favor, preencha todos os campos obrigatórios.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val vehicleUpdateRequest = Vehicle(
            marca,
            modelo,
            cor,
            anoModelo,
            anoFabricacao,
            placa,
            kmInicial,
            tipoCombustivel,
            tipoVeiculo
        )

        RetrofitClient.apiService.updateVehicle(id, vehicleUpdateRequest).enqueue(
            object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            this@FormVehicleActivity,
                            "Veículo atualizado com sucesso",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(
                            "FormVehicleActivity",
                            "Erro ao atualizar veículo: ${response.code()} - $errorBody"
                        )
                        Toast.makeText(
                            this@FormVehicleActivity,
                            "Erro ao atualizar veículo: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e(
                        "FormVehicleActivity",
                        "Falha ao conectar com a API para atualizar: ${t.message}"
                    )
                    Toast.makeText(
                        this@FormVehicleActivity, "Erro: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}