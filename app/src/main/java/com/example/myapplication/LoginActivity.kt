package com.example.myapplication

import API.RetrofitClient
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.models.ErrorResponse
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var registerTextView: TextView

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
        registerTextView = findViewById(R.id.register)

        registerTextView.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

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
                    val user = loginResponse?.user

                    Log.d("LoginResponse", user.toString())


                    getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit().apply {
                        putString("token", token)
                        putString("userName", user?.nome)
                        putString("userEmail", user?.email)
                        putInt("userId", user?.id ?: 0)
                        apply()
                    }

                    Toast.makeText(this@LoginActivity, "Login bem-sucedido", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {

                    val errorBody = response.errorBody()
                    var mensagemDeErro = "Erro desconhecido"

                    if (errorBody != null) {
                        try {
                            val erroString = errorBody.string()
                            Log.d("LoginErrorRaw", "Corpo do erro: $erroString")

                            val gson = com.google.gson.Gson()
                            val error = gson.fromJson(erroString, ErrorResponse::class.java)

                            if (error?.message != null) {
                                mensagemDeErro = error.message
                            }

                        } catch (e: Exception) {
                            Log.e("LoginErrorParse", "Falha ao parsear erro: ${e.message}")
                        }

                    Toast.makeText(this@LoginActivity, mensagemDeErro, Toast.LENGTH_LONG).show()
                    Log.e("LoginErrorFinal", "Mensagem exibida: $mensagemDeErro")
                }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login", "Erro no login", t)
                Toast.makeText(this@LoginActivity, "Erro: ${t.message}",
                    Toast.LENGTH_LONG).show()
            }
        })
    }


}