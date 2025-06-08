package API

import com.example.myapplication.models.Brand
import com.example.myapplication.models.ConsumptionResponse
import com.example.myapplication.models.Fueling
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.LoginResponse
import com.example.myapplication.models.NewFuel
import com.example.myapplication.models.RegisterRequest
import com.example.myapplication.models.User
import com.example.myapplication.models.Vehicle
import com.example.myapplication.models.VehicleList
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/auth/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @POST("/auth/register")
    fun register(
        @Body user: RegisterRequest
    ): Call<String>

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

    @GET("/vehicles/brands")
    fun getAllBrands(): Call<List<Brand>>


    @GET("/vehicles/brands")
    fun getBrandsByVehicleType(
        @Query("vehicleType") vehicleType: String
    ): Call<List<Brand>>


    @GET("/consumption/{vehicleId}")
    fun getConsumption(
        @Path("vehicleId") vehicleId: Int
    ): Call<ConsumptionResponse>

    @DELETE("/fueling/{vehicleId}/{fuelingId}")
    fun deleteFueling(
        @Path("vehicleId") vehicleId: Int,
        @Path("fuelingId") fuelingId: Int
    ): Call<Void>

    @POST("/fueling/{vehicleId}")
    fun createFueling(@Body newFuel: NewFuel, @Path("vehicleId") vehicleId: Int): Call<Fueling>

    @GET("/fueling/{vehicleId}")
    fun listFuelingsByVehicle(@Path("vehicleId") vehicleId: Int): Call<List<Fueling>>

}
