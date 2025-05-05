package tfg.azafatasapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tfg.azafatasapp.Auth.Auth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import tfg.azafatasapp.Admin.MainActivityAdmin
import tfg.azafatasapp.ui.home.HomeActivity
import tfg.azafatasapp.ui.login.LoginActivity

class PerfilActivity : ComponentActivity() {

    private lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Auth(this)

        setContent {
            PerfilScreen()
        }
    }

    @Composable
    fun PerfilScreen() {
        val user = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()
        var fullName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("Male") }
        var role by remember { mutableStateOf("") } // Variable para almacenar el rol
        var loading by remember { mutableStateOf(true) } // Estado de carga

        // Verificar si el usuario está autenticado
        if (user == null) {
            Toast.makeText(this@PerfilActivity, "No estás autenticado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cargar datos del usuario desde Firestore
        LaunchedEffect(user) {
            try {
                val documentSnapshot = firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .await() // Usamos await para esperar la respuesta asincrónica

                if (documentSnapshot.exists()) {
                    fullName = documentSnapshot.getString("name") ?: "Desconocido"
                    phone = documentSnapshot.getString("phone") ?: "No disponible"
                    gender = documentSnapshot.getString("gender") ?: "Male"

                    // Obtener el rol
                    role = documentSnapshot.getString("role") ?: "user" // Definir "user" como valor por defecto
                    Log.d("PerfilActivity", "role: $role") // Depuración para verificar el valor del rol

                } else {
                    Toast.makeText(this@PerfilActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PerfilActivity, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                UserInfoCard(name = fullName, phone = phone, gender = gender)

                Spacer(modifier = Modifier.height(16.dp))

                // Imagen de perfil
                Image(
                    painter = rememberImagePainter(user?.photoUrl ?: "https://www.example.com/default_profile_picture.png"),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                        .clip(CircleShape)
                )

                // Mostrar los campos editables
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Radio buttons para seleccionar género
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("Género:", modifier = Modifier.align(Alignment.CenterVertically))

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.Start) {
                        RadioButton(
                            selected = gender == "Male",
                            onClick = { gender = "Male" }
                        )
                        Text("Masculino", modifier = Modifier.align(Alignment.CenterVertically))

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = gender == "Female",
                            onClick = { gender = "Female" }
                        )
                        Text("Femenino", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para guardar los cambios
                Button(
                    onClick = {
                        user?.let {
                            val updatedUser = hashMapOf(
                                "name" to fullName,
                                "phone" to phone,
                                "gender" to gender
                            )

                            firestore.collection("users")
                                .document(it.uid)
                                .update(updatedUser as Map<String, Any>)
                                .addOnSuccessListener {
                                    Toast.makeText(this@PerfilActivity, "Datos actualizados con éxito", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this@PerfilActivity, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar cambios")
                }


                // Botón para cerrar sesión
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@PerfilActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))




            // Footer de navegación en la parte inferior de la pantalla
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.LightGray)
                    .padding(vertical = 8.dp)
            ) {
                // Fila con los iconos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de Inicio
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Inicio",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                // Depuración: Verificar antes de navegar
                                Log.d("PerfilActivity", "Navigating to Home. Role: $role")

                                // Verificar el rol y redirigir
                                val intent = if (role == "admin") {
                                    Intent(this@PerfilActivity, MainActivityAdmin::class.java)
                                } else {
                                    Intent(this@PerfilActivity, HomeActivity::class.java)
                                }
                                startActivity(intent)
                            }
                    )
                    // Puedes agregar más iconos aquí si es necesario
                }
            }
        }
    }

    // Composable para la tarjeta de información del usuario
    @Composable
    fun UserInfoCard(name: String, phone: String, gender: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Información del usuario", fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Nombre: $name")
                Text(text = "Teléfono: $phone")
                Text(text = "Género: $gender")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        PerfilScreen()
    }
}
