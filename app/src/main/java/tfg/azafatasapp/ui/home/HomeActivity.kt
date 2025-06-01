package tfg.azafatasapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import tfg.azafatasapp.ui.profile.PerfilActivity
import tfg.azafatasapp.ui.users.BillingUserActivity
import tfg.azafatasapp.ui.users.MessageUserActivity
import tfg.azafatasapp.ui.users.WorksUserActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }

    @Composable
    fun HomeScreen() {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: return
        val context = LocalContext.current

        var eventosAsignados by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var grupoSeleccionado by remember { mutableStateOf<Map<String, Any>?>(null) }
        var mensajes by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var nuevoMensaje by remember { mutableStateOf("") }
        var userRoles by remember { mutableStateOf(mapOf<String, String>()) }

        // Cargar eventos asignados
        LaunchedEffect(Unit) {
            firestore.collection("eventos")
                .whereEqualTo("estado", "En curso")
                .whereArrayContains("usuariosAsignados", userId)
                .addSnapshotListener { snapshot, _ ->
                    eventosAsignados = snapshot?.documents?.mapNotNull { it.data?.plus("id" to it.id) } ?: emptyList()
                }

            val usuariosSnapshot = firestore.collection("users").get().await()
            userRoles = usuariosSnapshot.documents.associate {
                it.id to (it.getString("role") ?: "user")
            }
        }

        // Escuchar mensajes del grupo seleccionado
        DisposableEffect(grupoSeleccionado) {
            val listener = grupoSeleccionado?.get("id")?.let { grupoId ->
                firestore.collection("message")
                    .document(grupoId.toString())
                    .addSnapshotListener { snapshot, _ ->
                        val data = snapshot?.get("mensajes") as? List<Map<String, Any>>
                        mensajes = data ?: emptyList()
                    }
            }
            onDispose { listener?.remove() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFBBDEFB), Color.White)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = 80.dp)
            ) {
                Text("Eventos asignados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0057D9))
                Spacer(Modifier.height(8.dp))
                eventosAsignados.forEach { evento ->
                    val nombre = evento["evento"] as? String ?: "Sin nombre"
                    val fecha = evento["fecha"] as? String ?: "Sin fecha"

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D47A1))
                            Text(fecha, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Grupos de mensajes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0057D9))
                Spacer(Modifier.height(8.dp))

                eventosAsignados.forEach { grupo ->
                    val nombre = grupo["evento"] as? String ?: "Sin nombre"
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { grupoSeleccionado = grupo },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(nombre, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6A1B9A))
                            Text("Toca para abrir el chat", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // Footer
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
                        startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                    })
                    Icon(Icons.Default.Face, "Trabajos", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@HomeActivity, WorksUserActivity::class.java))
                        Toast.makeText(this@HomeActivity, "Trabajos clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Search, "Facturas", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@HomeActivity, BillingUserActivity::class.java))
                        Toast.makeText(this@HomeActivity, "Facturas clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Notifications, "Mensajes", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@HomeActivity, MessageUserActivity::class.java))
                        Toast.makeText(this@HomeActivity, "Mensajes clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Person, "Perfil", Modifier.size(28.dp).clickable {
                        startActivity(Intent(this@HomeActivity, PerfilActivity::class.java))
                        Toast.makeText(this@HomeActivity, "Perfil clickeado", Toast.LENGTH_SHORT).show()
                    })
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

            // Diálogo de Chat
            grupoSeleccionado?.let { grupo ->
                Dialog(onDismissRequest = { grupoSeleccionado = null }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Chat: ${grupo["evento"]}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                mensajes.forEach { msg ->
                                    val sender = msg["senderName"] as? String ?: "Anon"
                                    val content = msg["message"] as? String ?: ""
                                    val time = (msg["timestamp"] as? Timestamp)?.toDate()?.let {
                                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                                    } ?: ""
                                    val role = userRoles[msg["senderId"]] ?: "user"
                                    val align = if (role == "user") Alignment.End else Alignment.Start
                                    val bg = if (role == "user") Color(0xFFD4F0C8) else Color(0xFFE6E6E6)

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = align
                                    ) {
                                        Text("$sender · $time", fontSize = 10.sp)
                                        Surface(color = bg, shape = MaterialTheme.shapes.medium) {
                                            Text(content, modifier = Modifier.padding(8.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nuevoMensaje,
                                onValueChange = { nuevoMensaje = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Escribe tu mensaje") }
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val grupoId = grupo["id"] as? String ?: return@Button
                                    val mensaje = mapOf(
                                        "senderId" to userId,
                                        "senderName" to (currentUser.displayName ?: "Usuario"),
                                        "message" to nuevoMensaje,
                                        "timestamp" to Timestamp.now()
                                    )
                                    val ref = firestore.collection("message").document(grupoId)
                                    if (nuevoMensaje.isNotBlank()) {
                                        ref.update("mensajes", FieldValue.arrayUnion(mensaje))
                                            .addOnSuccessListener { nuevoMensaje = "" }
                                            .addOnFailureListener {
                                                ref.set(mapOf("mensajes" to listOf(mensaje)))
                                                    .addOnSuccessListener { nuevoMensaje = "" }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Enviar")
                            }
                            TextButton(onClick = { grupoSeleccionado = null }) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
