// Ruta: tfg/azafatasapp/ui/perfil/PerfilActivity.kt
package tfg.azafatasapp.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tfg.azafatasapp.Auth.Auth
import tfg.azafatasapp.R
import tfg.azafatasapp.models.User
import tfg.azafatasapp.ui.home.HomeActivity
import tfg.azafatasapp.MainActivity

class PerfilActivity : AppCompatActivity() {

    private lateinit var auth: Auth
    private lateinit var userNameView: TextView
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnExit: Button
    private lateinit var btnDeleteAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        auth = Auth(this)

        // Inicializar vistas
        userNameView = findViewById(R.id.user_name_view)
        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        btnSave = findViewById(R.id.btn_save)
        btnExit = findViewById(R.id.btn_exit)
        btnDeleteAccount = findViewById(R.id.btn_delete_account)

        // Cargar datos del usuario
        loadUserData()

        // Configurar botón de guardar
        btnSave.setOnClickListener {
            val newName = editName.text.toString()
            val newEmail = editEmail.text.toString()
            updateUserData(newName, newEmail)
        }

        // Configurar botón de salida
        btnExit.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Configurar botón de eliminar cuenta
        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }

    // Cargar datos del usuario desde Auth
    private fun loadUserData() {
        auth.loadUserData(
            onSuccess = { user -> displayUserData(user) },
            onFailure = { exception ->
                Toast.makeText(this, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Mostrar datos del usuario en las vistas
    private fun displayUserData(user: User) {
        userNameView.text = "Bienvenido, ${user.name}"
        editName.setText(user.name)
        editEmail.setText(user.email)
    }

    private fun updateUserData(newName: String, newEmail: String) {
        auth.updateUserData(
            newName = newName,
            newEmail = newEmail,
            onSuccess = {
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("updatedName", newName)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error al actualizar: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Eliminar cuenta del usuario
    // Ruta: tfg/azafatasapp/ui/perfil/PerfilActivity.kt
    private fun deleteAccount() {
        auth.deleteAccount(
            onSuccess = {
                Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error al eliminar cuenta: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

}
