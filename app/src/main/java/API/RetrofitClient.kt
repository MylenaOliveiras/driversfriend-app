package API

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private lateinit var retrofit: Retrofit

    fun init(context: Context) {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl("http://10.130.170.214:3000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
