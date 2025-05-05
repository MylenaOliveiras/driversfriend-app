package com.example.myapplication

import API.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.CustomVehicleAdapter
import com.example.myapplication.models.vehicle.VehicleList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VehiclesFragment : Fragment() {

    private lateinit var recicleViewVeiculos: RecyclerView
    private lateinit var adapter: CustomVehicleAdapter
    private lateinit var tvEmptyState: TextView
    private lateinit var btnAdd: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_vehicles, container, false)

        btnAdd = view.findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val intent = Intent(requireContext(), FormVehicleActivity::class.java)
            startActivity(intent)
        }

        recicleViewVeiculos = view.findViewById(R.id.recicleViewVeiculos)
        recicleViewVeiculos.layoutManager = LinearLayoutManager(requireContext())

        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        adapter = CustomVehicleAdapter(emptyList(), requireContext()) {
            carregarListaDeVeiculos()
        }
        recicleViewVeiculos.adapter = adapter

        recicleViewVeiculos.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = "Carregando..."

        return view
    }

    override fun onResume() {
        super.onResume()
        carregarListaDeVeiculos()
    }

    private fun carregarListaDeVeiculos() {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        RetrofitClient.apiService.getVehicles().enqueue(
            object : Callback<List<VehicleList>> {
                override fun onResponse(
                    call: Call<List<VehicleList>>,
                    response: Response<List<VehicleList>>,
                ) {
                    if (response.isSuccessful) {
                        val vehicles = response.body() ?: emptyList()

                        if (vehicles.isNotEmpty()) {
                            recicleViewVeiculos.visibility = View.VISIBLE
                            tvEmptyState.visibility = View.GONE
                            adapter.updateList(vehicles)
                        } else {
                            recicleViewVeiculos.visibility = View.GONE
                            tvEmptyState.visibility = View.VISIBLE
                            tvEmptyState.text = "Nenhum veículo encontrado."
                        }
                    } else {
                        Log.e("API Error", "Response not successful. Code: ${response.code()}")
                        recicleViewVeiculos.visibility = View.GONE
                        tvEmptyState.visibility = View.VISIBLE
                        tvEmptyState.text = "Erro ao carregar veículos."
                    }
                }

                override fun onFailure(call: Call<List<VehicleList>>, t: Throwable) {
                    Log.e("API Failure", "Error fetching products", t)
                    recicleViewVeiculos.visibility = View.GONE
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = "Falha na comunicação com o servidor."
                }
            },
        )
    }
}
