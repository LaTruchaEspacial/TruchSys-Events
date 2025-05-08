package tfg.azafatasapp.ui.users

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillingUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BillingScreen() }
    }

    @Composable
    fun BillingScreen() {
        val context = LocalContext.current
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: return

        var facturas by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var userMap by remember { mutableStateOf(mapOf<String, String>()) }
        var userRolesMap by remember { mutableStateOf(mapOf<String, String>()) }
        var selectedFactura by remember { mutableStateOf<Map<String, Any>?>(null) }
        var horasTrabajadas by remember { mutableStateOf("") }
        var notas by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            val usersSnapshot = firestore.collection("users").get().await()
            userMap = usersSnapshot.documents.associate { it.id to (it.getString("name") ?: "Desconocido") }
            userRolesMap = usersSnapshot.documents.associate { it.id to (it.getString("role") ?: "user") }

            val snapshot = firestore.collection("facturas")
                .whereEqualTo("participanteId", userId)
                .get()
                .await()

            facturas = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mis Facturas", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.weight(1f)) {
                items(facturas) { factura ->
                    val evento = factura["nombreEvento"] as? String ?: "Sin nombre"
                    val participanteId = factura["participanteId"] as? String
                    val nombreParticipante = if (userRolesMap[participanteId] == "user") userMap[participanteId] else null

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .clickable {
                                selectedFactura = factura
                                horasTrabajadas = factura["horasTrabajadas"]?.toString() ?: ""
                                notas = factura["notas"]?.toString() ?: ""
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F9FF))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Evento: $evento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Participante: ${nombreParticipante ?: "Desconocido"}", fontSize = 14.sp)
                            Text("Horas trabajadas: ${factura["horasTrabajadas"] ?: "No asignadas"}", fontSize = 13.sp)
                            Text("Notas: ${factura["notas"] ?: "Sin notas"}", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Menú navegación
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
                    Icon(Icons.Default.Home, "Inicio", Modifier.size(28.dp))
                    Icon(Icons.Default.Face, "Trabajos", Modifier.size(28.dp))
                    Icon(Icons.Default.Search, "Ofertas", Modifier.size(28.dp))
                    Icon(Icons.Default.Notifications, "Mensajes", Modifier.size(28.dp))
                    Icon(Icons.Default.Person, "Perfil", Modifier.size(28.dp))
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

        // Diálogo de edición y envío
        selectedFactura?.let { factura ->
            AlertDialog(
                onDismissRequest = { selectedFactura = null },
                title = { Text("Editar Factura") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = horasTrabajadas,
                            onValueChange = { horasTrabajadas = it },
                            label = { Text("Horas trabajadas") }
                        )
                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas") }
                        )
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(onClick = {
                            val id = factura["id"] as? String ?: return@TextButton
                            val update = mapOf(
                                "horasTrabajadas" to horasTrabajadas.toIntOrNull(),
                                "notas" to notas
                            )
                            firestore.collection("facturas").document(id).update(update)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Factura actualizada", Toast.LENGTH_SHORT).show()
                                    selectedFactura = null
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Text("Guardar")
                        }

                        Spacer(Modifier.width(16.dp))

                        TextButton(onClick = {
                            val nombreEvento = factura["nombreEvento"] as? String ?: "Sin evento"
                            val datosFinal = mapOf(
                                "participanteId" to userId,
                                "nombreEvento" to nombreEvento,
                                "horasTrabajadas" to horasTrabajadas.toIntOrNull(),
                                "notas" to notas
                            )
                            firestore.collection("facturas_finalizadas").add(datosFinal)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Factura enviada", Toast.LENGTH_SHORT).show()
                                    selectedFactura = null
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al enviar", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Text("Enviar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedFactura = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
