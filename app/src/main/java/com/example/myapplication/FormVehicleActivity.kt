package com.example.myapplication

import API.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.models.Brand
import com.example.myapplication.models.Vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormVehicleActivity : AppCompatActivity() {
    private var vehicleId: Int? = null
    private lateinit var editTextCor: EditText
    private lateinit var editTextModelo: EditText
    private lateinit var editTextKmInicial: EditText
    private lateinit var editTextAnoModelo: EditText
    private lateinit var editTextAnoFabricacao: EditText
    private lateinit var editTextPlacaVeiculo: EditText
    private lateinit var autoCompleteTipoCombustivel: AutoCompleteTextView
    private lateinit var autoCompleteTipoVeiculo: AutoCompleteTextView
    private lateinit var autoCompleteMarca: AutoCompleteTextView
    private lateinit var btnEnviar: Button

    private lateinit var marcaAdapter: ArrayAdapter<Brand>
    private var selectedMarcaId: Int? = null
    private var vehicleMarcaIdToSelect: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_vehicle)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollViewForm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vehicleId = intent?.getIntExtra("id", 0)?.takeIf { it != 0 }

        editTextCor = findViewById(R.id.editTextCor)
        editTextModelo = findViewById(R.id.editTextModelo)
        editTextKmInicial = findViewById(R.id.editTextKmInicial)
        editTextAnoModelo = findViewById(R.id.editTextAnoModelo)
        editTextAnoFabricacao = findViewById(R.id.editTextAnoFabricacao)
        editTextPlacaVeiculo = findViewById(R.id.editTextPlacaVeiculo)
        autoCompleteTipoCombustivel = findViewById(R.id.autoCompleteTipoCombustivel)
        autoCompleteTipoVeiculo = findViewById(R.id.autoCompleteTipoVeiculo)
        autoCompleteMarca = findViewById(R.id.autoCompleteMarca)
        btnEnviar = findViewById(R.id.buttonEnviar)

        autoCompleteMarca.isEnabled = false

        autoCompleteMarca.setOnClickListener {
            (it as AutoCompleteTextView).showDropDown()
        }
        autoCompleteTipoCombustivel.setOnClickListener {
            (it as AutoCompleteTextView).showDropDown()
        }
        autoCompleteTipoVeiculo.setOnClickListener {
            (it as AutoCompleteTextView).showDropDown()
        }

        marcaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        autoCompleteMarca.setAdapter(marcaAdapter)

        val tiposVeiculo = resources.getStringArray(R.array.tipos_veiculo)
        val tipoVeiculoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposVeiculo)
        autoCompleteTipoVeiculo.setAdapter(tipoVeiculoAdapter)

        val tiposCombustivel = resources.getStringArray(R.array.tipos_combustivel)
        val tipoCombustivelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposCombustivel)
        autoCompleteTipoCombustivel.setAdapter(tipoCombustivelAdapter)

        autoCompleteTipoVeiculo.setOnItemClickListener { parent, _, position, _ ->
            val tipoVeiculoSelecionado = parent.getItemAtPosition(position).toString()
            Log.d("Autocomplete", "Tipo de Veículo selecionado: $tipoVeiculoSelecionado")
            fetchMarcasForTipoVeiculo(tipoVeiculoSelecionado)
        }

        autoCompleteMarca.setOnItemClickListener { parent, _, position, _ ->
            val selectedMarca = parent.getItemAtPosition(position) as? Brand
            selectedMarcaId = selectedMarca?.id
        }

        if (vehicleId == null) {
            btnEnviar.text = "Criar"
            btnEnviar.setOnClickListener { onCreateVehicle() }
        } else {
            btnEnviar.text = "Editar"
            btnEnviar.setOnClickListener { vehicleId?.let { id -> onEditVehicle(id) } }
            loadVehicleDetails(vehicleId)
        }
    }

    private fun fetchMarcasForTipoVeiculo(tipoVeiculo: String) {
        updateMarcasSpinnerState(isLoading = true)

        RetrofitClient.apiService.getBrandsByVehicleType(tipoVeiculo).enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    val marcas = response.body()
                    if (marcas != null) {
                        updateMarcasSpinner(marcas)
                        vehicleMarcaIdToSelect?.let { idToSelect ->
                            selectMarcaInSpinner(idToSelect)
                            selectedMarcaId = idToSelect
                            vehicleMarcaIdToSelect = null
                        }
                    } else {
                        showError("Resposta da API vazia")
                        clearMarcasSpinner()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Erro ao carregar marcas: ${response.code()}")
                    Log.e("AutocompleteUpdate", "Erro: ${response.code()} - $errorBody")
                    clearMarcasSpinner()
                }
                updateMarcasSpinnerState(isLoading = false)
            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                showError("Falha ao carregar marcas: ${t.message}")
                Log.e("AutocompleteUpdate", "Erro: ${t.message}", t)
                clearMarcasSpinner()
                updateMarcasSpinnerState(isLoading = false)
            }
        })
    }

    private fun updateMarcasSpinner(marcas: List<Brand>) {
        marcaAdapter.clear()
        marcaAdapter.addAll(marcas)
        marcaAdapter.notifyDataSetChanged()
        if (vehicleId == null) {
            autoCompleteMarca.setText("", false)
        }
    }

    private fun clearMarcasSpinner() {
        marcaAdapter.clear()
        marcaAdapter.add(Brand(-1, "Erro ao carregar marcas"))
        autoCompleteMarca.setText("Erro ao carregar marcas", false)
        selectedMarcaId = null
        marcaAdapter.notifyDataSetChanged()
    }

    private fun updateMarcasSpinnerState(isLoading: Boolean) {
        autoCompleteMarca.isEnabled = !isLoading
        if (isLoading) {
            marcaAdapter.clear()
            marcaAdapter.add(Brand(-1, "Carregando..."))
            autoCompleteMarca.setText("Carregando...", false)
            selectedMarcaId = null
            marcaAdapter.notifyDataSetChanged()
        } else if (marcaAdapter.isEmpty) {
            marcaAdapter.add(Brand(-1, "Nenhuma marca disponível"))
            autoCompleteMarca.setText("Nenhuma marca disponível", false)
            selectedMarcaId = null
            marcaAdapter.notifyDataSetChanged()
        }
    }

    private fun selectMarcaInSpinner(marcaId: Int) {
        for (i in 0 until marcaAdapter.count) {
            val marca = marcaAdapter.getItem(i)
            if (marca?.id == marcaId) {
                autoCompleteMarca.setText(marca.nome, false)
                selectedMarcaId = marca.id
                break
            }
        }
    }

    private fun loadVehicleDetails(id: Int?) {
        id?.let {
            RetrofitClient.apiService.getVehicleById(it).enqueue(object : Callback<Vehicle> {
                override fun onResponse(call: Call<Vehicle>, response: Response<Vehicle>) {
                    if (response.isSuccessful) {
                        val vehicle = response.body()
                        vehicle?.let {
                            editTextCor.setText(it.cor ?: "")
                            editTextModelo.setText(it.modelo ?: "")
                            editTextKmInicial.setText(it.kmInicial?.toString() ?: "")
                            editTextAnoModelo.setText(it.anoModelo?.toString() ?: "")
                            editTextAnoFabricacao.setText(it.anoFabricacao?.toString() ?: "")
                            editTextPlacaVeiculo.setText(it.placaVeiculo ?: "")

                            autoCompleteTipoVeiculo.setText(it.tipoVeiculo, false)
                            autoCompleteTipoCombustivel.setText(it.combustivel, false)

                            vehicleMarcaIdToSelect = it.marca
                            selectedMarcaId = it.marca

                            autoCompleteTipoVeiculo.post {
                                fetchMarcasForTipoVeiculo(it.tipoVeiculo)
                            }
                        }
                    } else {
                        showError("Erro ao carregar detalhes do veículo")
                    }
                }

                override fun onFailure(call: Call<Vehicle>, t: Throwable) {
                    showError("Falha ao carregar detalhes do veículo: ${t.message}")
                }
            })
        }
    }

    private fun onCreateVehicle() {
        val marca = selectedMarcaId
        val cor = editTextCor.text.toString()
        val modelo = editTextModelo.text.toString()
        val kmInicial = editTextKmInicial.text.toString().toIntOrNull()
        val anoModelo = editTextAnoModelo.text.toString().toIntOrNull()
        val anoFabricacao = editTextAnoFabricacao.text.toString().toIntOrNull()
        val placa = editTextPlacaVeiculo.text.toString()
        val tipoCombustivel = autoCompleteTipoCombustivel.text.toString()
        val tipoVeiculo = autoCompleteTipoVeiculo.text.toString()

        if (marca == null || anoModelo == null || anoFabricacao == null || kmInicial == null || marca <= 0 || modelo.isBlank() || placa.isBlank() || tipoCombustivel.isBlank() || tipoVeiculo.isBlank()) {
            showError("Preencha todos os campos obrigatórios e selecione uma marca válida.")
            return
        }

        val vehicleRequest = Vehicle(marca, modelo, cor, anoModelo, anoFabricacao, placa, kmInicial, tipoCombustivel, tipoVeiculo)

        RetrofitClient.apiService.createVehicle(vehicleRequest).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FormVehicleActivity, "Veículo criado com sucesso", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    showError("Erro ao criar veículo: ${response.code()} - $errorMessage")
                    Log.e("FormVehicleActivity", "Erro criar veículo: ${response.code()} - $errorMessage")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showError("Erro: ${t.message}")
                Log.e("FormVehicleActivity", "Falha na conexão ao criar veículo", t)
            }
        })
    }

    private fun onEditVehicle(id: Int) {
        val marca = selectedMarcaId
        val cor = editTextCor.text.toString()
        val modelo = editTextModelo.text.toString()
        val kmInicial = editTextKmInicial.text.toString().toIntOrNull()
        val anoModelo = editTextAnoModelo.text.toString().toIntOrNull()
        val anoFabricacao = editTextAnoFabricacao.text.toString().toIntOrNull()
        val placa = editTextPlacaVeiculo.text.toString()
        val tipoCombustivel = autoCompleteTipoCombustivel.text.toString()
        val tipoVeiculo = autoCompleteTipoVeiculo.text.toString()

        if (marca == null || anoModelo == null || anoFabricacao == null || kmInicial == null || marca <= 0 || modelo.isBlank() || placa.isBlank() || tipoCombustivel.isBlank() || tipoVeiculo.isBlank()) {
            showError("Preencha todos os campos obrigatórios e selecione uma marca válida.")
            return
        }

        val updatedVehicle = Vehicle(marca, modelo, cor, anoModelo, anoFabricacao, placa, kmInicial, tipoCombustivel, tipoVeiculo)

        RetrofitClient.apiService.updateVehicle(id, updatedVehicle).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@FormVehicleActivity, "Veículo atualizado com sucesso", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                    showError("Erro ao atualizar veículo: ${response.code()} - $errorMessage")
                    Log.e("FormVehicleActivity", "Erro atualizar veículo: ${response.code()} - $errorMessage")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showError("Erro: ${t.message}")
                Log.e("FormVehicleActivity", "Falha na conexão ao atualizar veículo", t)
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}