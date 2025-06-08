package com.example.myapplication

import API.RetrofitClient
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.myapplication.models.Vehicle
import com.example.myapplication.models.VehicleList
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var vehicleCard: CardView
    private lateinit var noVehicleRegisteredLayout: View
    private lateinit var vehicleDashboardLayout: ConstraintLayout
    private lateinit var addVehicleButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        vehicleCard = view.findViewById(R.id.vehicleCard)
        noVehicleRegisteredLayout = view.findViewById(R.id.noVehicleRegisteredLayout)
        vehicleDashboardLayout = view.findViewById(R.id.vehicleDashboardLayout)
        addVehicleButton = view.findViewById(R.id.addVehicleButton)

        noVehicleRegisteredLayout.visibility = View.GONE
        vehicleDashboardLayout.visibility = View.GONE

        val cardManutencao = view.findViewById<MaterialCardView>(R.id.cardManutencao)
        val cardGastos = view.findViewById<MaterialCardView>(R.id.cardGastos)
        val cardVeiculos = view.findViewById<MaterialCardView>(R.id.cardVeiculos)
        val cardConsumo = view.findViewById<MaterialCardView>(R.id.cardConsumo)

        cardConsumo.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavItem(R.id.menu_consumption)
        }

        addVehicleButton.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavItem(R.id.menu_car_list)
        }

        cardVeiculos.setOnClickListener {
            (requireActivity() as MainActivity).selectBottomNavItem(R.id.menu_car_list)
        }

        val comingSoonClickListener = View.OnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Coming Soon")
                .setMessage("Essa funcionalidade ainda está em desenvolvimento")
                .show()
        }

        cardManutencao.setOnClickListener(comingSoonClickListener)
        cardGastos.setOnClickListener(comingSoonClickListener)

        return view
    }

    override fun onResume() {
        super.onResume()
        loadSelectedVehicleFromPrefsAndDisplay()
    }

    private fun loadSelectedVehicleFromPrefsAndDisplay() {
        val prefs = requireContext().getSharedPreferences("vehicle_prefs", MODE_PRIVATE)
        val SELECTED_VEHICLE_KEY = "selected_vehicle_json"
        val json = prefs.getString(SELECTED_VEHICLE_KEY, null)

        if (json != null) {
            try {
                val selectedVehicleList = Gson().fromJson(json, VehicleList::class.java)
                val vehicleId = selectedVehicleList.id
                // Se um veículo está salvo, tente carregá-lo
                callVehicle(vehicleId)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Erro ao deserializar veículo selecionado do prefs", e)
                // Se houver erro de deserialização, limpe e tente buscar a lista
                clearVehicleDataFromPrefs()
                callVehiclesAndHandleSelection()
            }
        } else {
            // Se não há veículo salvo no prefs, tente buscar a lista de veículos
            callVehiclesAndHandleSelection()
        }
    }

    // Esta função será responsável por buscar a lista e lidar com a seleção inicial
    private fun callVehiclesAndHandleSelection() {
        RetrofitClient.apiService.getVehicles().enqueue(object : Callback<List<VehicleList>> {
            override fun onResponse(
                call: Call<List<VehicleList>>,
                response: Response<List<VehicleList>>
            ) {
                if (response.isSuccessful) {
                    val vehicles = response.body() ?: emptyList()
                    if (vehicles.isNotEmpty()) {
                        // Se há veículos, seleciona o primeiro e salva no SharedPreferences
                        val firstVehicle = vehicles.first()
                        saveSelectedVehicle(firstVehicle) // Salva o primeiro veículo
                        callVehicle(firstVehicle.id) // Exibe o primeiro veículo
                    } else {
                        // Se não há veículos, mostra o estado de "nenhum veículo"
                        showNoVehicleRegisteredState()
                        clearVehicleDataFromPrefs()
                    }
                } else {
                    Log.e("HomeFragment", "Erro ao buscar veículos da API: ${response.code()}")
                    showNoVehicleRegisteredState()
                    clearVehicleDataFromPrefs()
                }
            }

            override fun onFailure(call: Call<List<VehicleList>>, t: Throwable) {
                Log.e("HomeFragment", "Falha na conexão ao buscar veículos", t)
                showNoVehicleRegisteredState()
                clearVehicleDataFromPrefs()
            }
        })
    }

    private fun callVehicle(vehicleId: Int) {
        VehicleRepository.getVehicleDetails(vehicleId) { vehicle, error ->
            if (error != null) {
                Log.e("HomeFragment", "Erro ao conectar com API para detalhes do veículo", error)
                showNoVehicleRegisteredState()
                clearVehicleDataFromPrefs()
                return@getVehicleDetails
            }

            if (vehicle != null) {
                showVehicleDashboard(vehicle)
            } else {
                Log.e("HomeFragment", "Detalhes do veículo retornado nulo para ID: $vehicleId")
                showNoVehicleRegisteredState()
                clearVehicleDataFromPrefs()
            }
        }
    }

    private fun showVehicleDashboard(vehicle: Vehicle) {
        view?.findViewById<TextView>(R.id.vehicleName)?.text = "Veículo: ${vehicle.modelo}"
        view?.findViewById<TextView>(R.id.vehiclePlate)?.text = "Placa: ${vehicle.placaVeiculo}"
        view?.findViewById<TextView>(R.id.vehicleYear)?.text = "Ano: ${vehicle.anoModelo}"
        view?.findViewById<TextView>(R.id.vehicleColor)?.text = "Cor: ${vehicle.cor}"

        vehicleDashboardLayout.visibility = View.VISIBLE
        noVehicleRegisteredLayout.visibility = View.GONE
    }

    private fun showNoVehicleRegisteredState() {
        noVehicleRegisteredLayout.visibility = View.VISIBLE
        vehicleDashboardLayout.visibility = View.GONE
    }

    private fun clearVehicleDataFromPrefs() {
        val prefs = requireContext().getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        prefs.edit().apply {
            remove("idVeiculo")
            remove("placaVeiculo")
            remove("anoModeloVeiculo")
            remove("corVeiculo")
            remove("nomeVeiculo")
            apply()
        }
        val vehiclePrefs = requireContext().getSharedPreferences("vehicle_prefs", MODE_PRIVATE)
        vehiclePrefs.edit().apply {
            remove("selected_vehicle_json")
            apply()
        }
    }

    private fun saveSelectedVehicle(vehicle: VehicleList) {
        val prefs = requireContext().getSharedPreferences("vehicle_prefs", MODE_PRIVATE)
        val editor = prefs.edit()
        val SELECTED_VEHICLE_KEY = "selected_vehicle_json"
        val gson = Gson()
        val json = gson.toJson(vehicle)
        editor.putString(SELECTED_VEHICLE_KEY, json)
        editor.apply()
    }
}