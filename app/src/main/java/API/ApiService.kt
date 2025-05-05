package API

import com.example.myapplication.models.login.LoginRequest
import com.example.myapplication.models.login.LoginResponse
import com.example.myapplication.models.user.User
import com.example.myapplication.models.vehicle.Vehicle
import com.example.myapplication.models.vehicle.VehicleList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("/auth/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @POST("/auth/register")
    fun register(
        @Body user: User
    ): Call<User>

    @GET("/vehicles")
    fun getVehicles(): Call<List<VehicleList>>

    @POST("/vehicles")
    fun createVehicle(
        @Body vehicle: Vehicle
    ): Call<String>

    @PUT("/vehicles/{id}")
    fun updateVehicle(
        @Path("id") vehicleId: Int,
        @Body vehicle: Vehicle
    ): Call<String>

    @DELETE("/vehicles/{id}")
    fun deleteVehicle(
        @Path("id") vehicleId: Int
    ): Call<Void>

    @GET("/vehicles/{id}")
    fun getVehicleById(
        @Path("id") vehicleId: Int
    ): Call<Vehicle>
}
