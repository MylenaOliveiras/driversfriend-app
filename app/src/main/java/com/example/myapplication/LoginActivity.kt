package com.example.myapplication

import API.RetrofitClient
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.models.login.LoginRequest
import com.example.myapplication.models.login.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        RetrofitClient.init(this.applicationContext)

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.senha)
        val loginButton: Button = findViewById(R.id.login)
        loginButton.setOnClickListener {
            blockLogin()
        }
    }
    private fun blockLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val loginRequest = LoginRequest(email, password)

        val call = RetrofitClient.apiService.login(loginRequest)

        Log.d("Login", "Tentativa de login com email: $email e senha: $password")

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    Log.d("Login", "Login bem-sucedido")
                    val loginResponse = response.body()
                    val token = loginResponse?.token

                    val prefs = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
                    prefs.edit().putString("TOKEN", token).apply()

                    Toast.makeText(this@LoginActivity, "Login bem-sucedido", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "Erro no login", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Erro: ${t.message}",
                    Toast.LENGTH_LONG).show()
            }
        })
    }


}