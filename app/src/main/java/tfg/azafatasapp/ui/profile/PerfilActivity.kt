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
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import tfg.azafatasapp.Admin.MainActivityAdmin
import tfg.azafatasapp.ui.home.HomeActivity
import tfg.azafatasapp.ui.login.LoginActivity
import tfg.azafatasapp.ui.users.BillingUserActivity
import tfg.azafatasapp.ui.users.MessageUserActivity
import tfg.azafatasapp.ui.users.WorksUserActivity

class PerfilActivity : ComponentActivity() {
    private lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Auth(this)
        setContent { PerfilScreen() }
    }

    @Composable
    fun PerfilScreen() {
        val user = FirebaseAuth.getInstance().currentUser
        val firestore = FirebaseFirestore.getInstance()
        var fullName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("Male") }
        var role by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(true) }

        if (user == null) {
            Toast.makeText(this@PerfilActivity, "No estás autenticado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        LaunchedEffect(user) {
            try {
                val documentSnapshot = firestore.collection("users").document(user.uid).get().await()
                if (documentSnapshot.exists()) {
                    fullName = documentSnapshot.getString("name") ?: "Desconocido"
                    phone = documentSnapshot.getString("phone") ?: "No disponible"
                    gender = documentSnapshot.getString("gender") ?: "Male"
                    role = documentSnapshot.getString("role") ?: "user"
                    Log.d("PerfilActivity", "role: $role")
                } else {
                    Toast.makeText(this@PerfilActivity, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PerfilActivity, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color.White)))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                UserInfoCard(name = fullName, phone = phone, gender = gender)
                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberImagePainter(user?.photoUrl ?: "https://www.example.com/default_profile_picture.png"),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                        .clip(CircleShape)
                )

                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Número de teléfono") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text("Género:", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(horizontalArrangement = Arrangement.Start) {
                        RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                        Text("Masculino", modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                        Text("Femenino", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    user?.let {
                        val updatedUser = hashMapOf("name" to fullName, "phone" to phone, "gender" to gender)
                        firestore.collection("users").document(it.uid).update(updatedUser as Map<String, Any>)
                            .addOnSuccessListener {
                                Toast.makeText(this@PerfilActivity, "Datos actualizados con éxito", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this@PerfilActivity, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar cambios")
                }

                Button(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@PerfilActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar sesión", color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.LightGray)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Icon(Icons.Default.Home, "Inicio", Modifier.size(28.dp).clickable {
                        val intent = if (role == "admin") Intent(this@PerfilActivity, MainActivityAdmin::class.java)
                        else Intent(this@PerfilActivity, HomeActivity::class.java)
                        startActivity(intent)
                    })
                    Icon(Icons.Default.Face, "Trabajos", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@PerfilActivity, WorksUserActivity::class.java))
                        Toast.makeText(this@PerfilActivity, "Trabajos clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Search, "Facturas", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@PerfilActivity, BillingUserActivity::class.java))
                        Toast.makeText(this@PerfilActivity, "Facturas clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Notifications, "Mensajes", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@PerfilActivity, MessageUserActivity::class.java))
                        Toast.makeText(this@PerfilActivity, "Mensajes clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Person, "Perfil", Modifier.size(28.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Inicio", "Trabajos", "Facturas", "Mensajes", "Perfil").forEach {
                        Text(it, fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    @Composable
    fun UserInfoCard(name: String, phone: String, gender: String) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Información del usuario", fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nombre: $name")
                Text("Teléfono: $phone")
                Text("Género: $gender")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        PerfilScreen()
    }
}
