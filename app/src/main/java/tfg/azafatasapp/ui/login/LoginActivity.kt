package tfg.azafatasapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tfg.azafatasapp.Auth.Auth
import tfg.azafatasapp.R
import tfg.azafatasapp.ui.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: Auth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Auth(this)

        // Inicializar vistas
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)

        // Configurar botón de inicio de sesión
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            login(email, password)
        }

        // Configurar enlace para recuperar contraseña
        tvForgotPassword.setOnClickListener {
            recoverPassword()
        }
    }

    // Método para iniciar sesión
    private fun login(email: String, password: String) {
        auth.login(email, password,
            onSuccess = {
                // Iniciar HomeActivity si el login es exitoso
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error al iniciar sesión: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Método para recuperar contraseña
    private fun recoverPassword() {
        val email = etEmail.text.toString()
        if (email.isNotEmpty()) {
            auth.resetPassword(email,
                onSuccess = {
                    Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Error al enviar el correo: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(this, "Por favor, introduce tu correo electrónico", Toast.LENGTH_SHORT).show()
        }
    }
}
