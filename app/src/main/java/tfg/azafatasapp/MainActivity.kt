// Ruta: tfg/azafatasapp/MainActivity.kt
package tfg.azafatasapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import tfg.azafatasapp.ui.login.LoginActivity  // Asegúrate de que este paquete sea correcto
import tfg.azafatasapp.ui.register.RegisterActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura el botón para navegar a RegisterActivity
        val registerButton: Button = findViewById(R.id.btn_registrarse)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Configura el botón para navegar a LoginActivity
        val loginButton: Button = findViewById(R.id.btn_iniciar_sesion)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
