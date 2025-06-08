package com.example.myapplication

import API.RetrofitClient
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.FuelAdapter
import com.example.myapplication.models.Fueling
import com.example.myapplication.models.VehicleList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.Locale
import retrofit2.Callback
import retrofit2.Response

class FuelFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FuelAdapter
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tvEmptyStateMessage: TextView
    private lateinit var btnAdd: FloatingActionButton

    private var selectedVehicleId: Int? = null

    private val SELECTED_VEHICLE_KEY = "selected_vehicle_json"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_fuel, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewFuelings)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayoutFuelings)
        tvEmptyStateMessage = view.findViewById(R.id.tvEmptyState)



        btnAdd = view.findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val intent = Intent(requireContext(), FormFuelActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FuelAdapter(
            mutableListOf(),
            onDeleteClick = { fueling ->
                showDeleteConfirmationDialog(fueling)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.btnAddRefuelEmptyState)?.setOnClickListener {
            Toast.makeText(context, "Abrir tela de Adicionar Abastecimento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFuelingsData()
    }

    private fun loadSelectedVehicleId(): Int? {
        val sharedPref =
            requireActivity().getSharedPreferences("vehicle_prefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString(SELECTED_VEHICLE_KEY, null)
        var vehicleId: Int? = null

        if (json != null) {
            try {
                val selectedVehicle = Gson().fromJson(json, VehicleList::class.java)
                vehicleId = selectedVehicle?.id
                Log.d(
                    "FuelFragment",
                    "ID do veículo selecionado do SharedPreferences: $vehicleId"
                )
            } catch (e: Exception) {
                Log.e(
                    "FuelFragment",
                    "Erro ao deserializar veículo selecionado: ${e.message}"
                )
                Toast.makeText(
                    context,
                    "Erro ao carregar veículo selecionado. Por favor, selecione um veículo.",
                    Toast.LENGTH_LONG
                ).show()
                showEmptyState()
            }
        }
        return vehicleId
    }

    private fun loadFuelingsData() {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE

        selectedVehicleId = loadSelectedVehicleId()

        selectedVehicleId?.let { vehicleId ->
            RetrofitClient.apiService.listFuelingsByVehicle(vehicleId)
                .enqueue(object : Callback<List<Fueling>> {
                    override fun onResponse(
                        call: Call<List<Fueling>>,
                        response: Response<List<Fueling>>
                    ) {
                        if (response.isSuccessful) {
                            val fuelings = response.body()
                            if (fuelings != null && fuelings.isNotEmpty()) {
                                adapter.updateData(fuelings.sortedByDescending { it.dataAbastecimento })
                                recyclerView.visibility = View.VISIBLE
                                emptyStateLayout.visibility = View.GONE
                            } else {
                                showEmptyState("Não há abastecimentos registrados para este veículo.")
                            }
                        } else {
                            val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                            Log.e(
                                "FuelFragment",
                                "Erro ao carregar abastecimentos: ${response.code()} - $errorMessage"
                            )
                            showErrorState("Erro ao carregar abastecimentos: ${response.code()} - $errorMessage")
                        }
                    }

                    override fun onFailure(call: Call<List<Fueling>>, t: Throwable) {
                        Log.e(
                            "FuelFragment",
                            "Falha ao carregar abastecimentos: ${t.message}",
                            t
                        )
                        showErrorState("Falha de conexão ao carregar abastecimentos: ${t.localizedMessage}")
                    }
                })

        } ?: run {
            showEmptyState("Selecione um veículo para ver os abastecimentos.")
        }
    }

    private fun showEmptyState(message: String = "Não há abastecimentos registrados para este veículo.") {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        tvEmptyStateMessage.text = message
    }

    private fun showErrorState(message: String) {
        showEmptyState(message)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun showDeleteConfirmationDialog(fueling: Fueling) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = inputFormat.parse(fueling.dataAbastecimento)?.let { outputFormat.format(it) } ?: fueling.dataAbastecimento
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Abastecimento")
            .setMessage("Tem certeza que deseja excluir o abastecimento de $formattedDate (${fueling.kmAtual} km)?")
            .setPositiveButton("Sim") { dialog, _ ->
                selectedVehicleId?.let { vehicleId ->
                    deleteFueling(vehicleId, fueling.id)
                } ?: Toast.makeText(
                    context,
                    "Veículo não selecionado. Não foi possível excluir.",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteFueling(vehicleId: Int, fuelingId: Int) {
        RetrofitClient.apiService.deleteFueling(vehicleId, fuelingId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Abastecimento excluído com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadFuelingsData()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Erro desconhecido"
                        Toast.makeText(
                            context,
                            "Erro ao excluir: ${response.code()} - $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.e(
                            "FuelFragment",
                            "Erro ao excluir abastecimento: ${response.code()} - $errorMessage"
                        )
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Falha na exclusão: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(
                        "FuelFragment",
                        "Falha ao excluir abastecimento: ${t.message}",
                        t
                    )
                }
            })
    }
}