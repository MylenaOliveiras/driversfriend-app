package com.example.myapplication

import API.RetrofitClient
import android.util.Log
import com.example.myapplication.models.Vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object VehicleRepository {
     fun getVehicleDetails(vehicleId: Int, callback: (Vehicle?, Throwable?) -> Unit) {
        RetrofitClient.apiService.getVehicleById(vehicleId).enqueue(object : Callback<Vehicle> {
            override fun onResponse(call: Call<Vehicle>, response: Response<Vehicle>) {
                if (response.isSuccessful) {
                    Log.d("VehicleRepository", "Vehicle details fetched successfully: ${response.body()}")
                    callback(response.body(), null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("VehicleRepository", "Error fetching vehicle details: ${response.code()} - $errorBody")
                    callback(null, Exception("Error fetching vehicle details: ${response.code()} - $errorBody"))
                }
            }

            override fun onFailure(call: Call<Vehicle>, t: Throwable) {
                Log.e("VehicleRepository", "Failed to fetch vehicle details", t)
                callback(null, t)
            }
        })
    }

}
