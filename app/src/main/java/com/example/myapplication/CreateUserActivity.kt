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
import com.example.myapplication.models.RegisterRequest
import com.santalu.maskara.widget.MaskEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateUserActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var cpfEditText: MaskEditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        RetrofitClient.init(this.applicationContext)

        nameEditText = findViewById(R.id.nome)
        cpfEditText = findViewById(R.id.cpf)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.senha)
        text = findViewById(R.id.login)

        text.setOnClickListener{
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        val registerButton: Button = findViewById(R.id.cadastrarBtn)
        registerButton.setOnClickListener {
            blockRegister()
        }
    }
    private fun blockRegister() {
        val name = nameEditText.text.toString().trim()
        val cpf = cpfEditText.unMasked
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "Nome é obrigatório"
            return
        }

        if (cpf.isEmpty()) {
            cpfEditText.error = "CPF é obrigatório"
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "E-mail é obrigatório"
            return
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "E-mail inválido"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Senha é obrigatória"
            return
        } else if (password.length < 6) {
            passwordEditText.error = "Senha deve ter no mínimo 6 caracteres"
            return
        } else if (!password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"))) {
            passwordEditText.error = "Senha deve conter letras maiúsculas, minúsculas e números"
            return
        }

        val createRequest = RegisterRequest(name, cpf, email, password)

        val call = RetrofitClient.apiService.register(createRequest)
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    Log.d("Cadastrado", "Usuário cadastrado com sucesso")
                    val registerResponse = response.body()


                    Toast.makeText(this@CreateUserActivity, "Usuário cadastrado com sucesso", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@CreateUserActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    val errorBody = response.errorBody()
                    var mensagemDeErro = "Erro desconhecido"

                    if (errorBody != null) {
                        try {
                            val erroString = errorBody.string()
                            Log.d("eRRO", "Corpo do erro: $erroString")

                            val gson = com.google.gson.Gson()
                            val error = gson.fromJson(erroString, ErrorResponse::class.java)

                            if (error?.message != null) {
                                mensagemDeErro = error.message
                            }

                        } catch (e: Exception) {
                            Log.e("RegisterErrorParse", "Falha ao parsear erro: ${e.message}")
                        }

                        Toast.makeText(this@CreateUserActivity, mensagemDeErro, Toast.LENGTH_LONG).show()
                        Log.e("RegisterErrorFinal", "Mensagem exibida: $mensagemDeErro")
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("Login", "Erro no login", t)
                Toast.makeText(this@CreateUserActivity, "Erro: ${t.message}",
                    Toast.LENGTH_LONG).show()
            }
        })
    }


}

