package API

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            return chain.proceed(chain.request())
        }

        Log.d("AuthInterceptor", "Token: $token")

        val request = chain.request().newBuilder()
        token?.let {
            request.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(request.build())
    }
}
