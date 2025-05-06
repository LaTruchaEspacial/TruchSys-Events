package tfg.azafatasapp.ui.admins

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MessageAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MessageAdminScreen() }
    }

    @Composable
    fun MessageAdminScreen() {
        val context = LocalContext.current
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        var events by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var selectedEvent by remember { mutableStateOf<Map<String, Any>?>(null) }
        var messages by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var messageText by remember { mutableStateOf("") }
        var usersRoles by remember { mutableStateOf(mapOf<String, String>()) }
        var listener: ListenerRegistration? by remember { mutableStateOf(null) }

        // Load events and roles
        LaunchedEffect(Unit) {
            firestore.collection("eventos")
                .whereEqualTo("estado", "En curso")
                .addSnapshotListener { snapshot, _ ->
                    events = snapshot?.documents?.mapNotNull { it.data?.plus("id" to it.id) } ?: emptyList()
                }

            val usersSnapshot = firestore.collection("users").get().await()
            usersRoles = usersSnapshot.documents.associate { it.id to (it.getString("role") ?: "user") }
        }

        // Listen for messages
        DisposableEffect(selectedEvent) {
            listener?.remove()
            selectedEvent?.get("id")?.let { eventId ->
                listener = firestore.collection("message")
                    .document(eventId.toString())
                    .addSnapshotListener { snapshot, _ ->
                        val data = snapshot?.get("mensajes") as? List<Map<String, Any>>
                        messages = data ?: emptyList()
                    }
            }
            onDispose { listener?.remove() }
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Eventos activos", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                events.forEach { event ->
                    val nombre = event["evento"] as? String ?: "Sin nombre"
                    val fecha = event["fecha"] as? String ?: "Sin fecha"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selectedEvent = event },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEEFF))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Evento: $nombre", fontWeight = FontWeight.Bold)
                            Text("Fecha: $fecha")
                        }
                    }
                }
            }

            selectedEvent?.let { event ->
                AlertDialog(
                    onDismissRequest = { selectedEvent = null },
                    title = { Text("Chat: ${event["evento"]}") },
                    text = {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            messages.forEach { msg ->
                                val senderId = msg["senderId"] as? String ?: ""
                                val senderName = msg["senderName"] as? String ?: "Anon"
                                val content = msg["message"] as? String ?: ""
                                val timestamp = (msg["timestamp"] as? Timestamp)?.toDate()
                                val timeText = timestamp?.let {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                                } ?: ""
                                val role = usersRoles[senderId] ?: "user"
                                val alignment = if (role == "user") Alignment.End else Alignment.Start
                                val background = if (role == "user") Color(0xFFDCF8C6) else Color(0xFFE6E6E6)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = alignment
                                ) {
                                    Text("$senderName · $timeText", fontSize = 10.sp)
                                    Surface(
                                        color = background,
                                        shape = MaterialTheme.shapes.medium,
                                        tonalElevation = 1.dp,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Text(content, modifier = Modifier.padding(8.dp))
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Column {
                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                label = { Text("Escribe tu mensaje") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    val eventoId = event["id"] as? String ?: return@Button
                                    if (messageText.isNotBlank()) {
                                        val msg = mapOf(
                                            "senderId" to userId,
                                            "senderName" to (currentUser.displayName ?: "Admin"),
                                            "message" to messageText,
                                            "timestamp" to Timestamp.now()
                                        )
                                        val docRef = firestore.collection("message").document(eventoId)
                                        docRef.update("mensajes", FieldValue.arrayUnion(msg))
                                            .addOnSuccessListener { messageText = "" }
                                            .addOnFailureListener {
                                                docRef.set(mapOf("mensajes" to listOf(msg)))
                                                    .addOnSuccessListener { messageText = "" }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enviar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedEvent = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            // Footer nav
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio", modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Face, contentDescription = "Trabajos", modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Search, contentDescription = "Ofertas", modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Notifications, contentDescription = "Mensajes", modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.size(28.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Inicio", "Trabajos", "Ofertas", "Mensajes", "Perfil").forEach {
                        Text(it, fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
