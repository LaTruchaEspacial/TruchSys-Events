// Ruta: tfg/azafatasapp/ui/home/HomeActivity.kt
package tfg.azafatasapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tfg.azafatasapp.R
import tfg.azafatasapp.models.User
import tfg.azafatasapp.ui.perfil.PerfilActivity // Asegúrate de importar PerfilActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var logoUser: ImageView // Declarar ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        welcomeTextView = findViewById(R.id.welcome_text_view)
        logoUser = findViewById(R.id.logo_user) // Inicializar ImageView

        // Configurar el click listener para el logo
        logoUser.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }

        // Obtener el nombre del usuario del Intent
        val updatedName = intent.getStringExtra("updatedName")
        if (updatedName != null) {
            displayWelcomeMessage(updatedName)
        } else {
            loadUserNameFromFirestore()
        }
    }

    private fun displayWelcomeMessage(userName: String) {
        welcomeTextView.text = "Bienvenido, $userName"
    }

    private fun loadUserNameFromFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            displayWelcomeMessage(user.name)
                        } else {
                            welcomeTextView.text = "Bienvenido"
                        }
                    } else {
                        welcomeTextView.text = "Bienvenido"
                    }
                }
                .addOnFailureListener { exception ->
                    welcomeTextView.text = "Error al cargar nombre: ${exception.message}"
                }
        } else {
            welcomeTextView.text = "No hay usuario autenticado"
        }
    }
}
