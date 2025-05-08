package tfg.azafatasapp.ui.admins

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BillingAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BillingAdminScreen() }
    }

    @Composable
    fun BillingAdminScreen() {
        val context = LocalContext.current
        val firestore = FirebaseFirestore.getInstance()

        var facturas by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var userMap by remember { mutableStateOf(mapOf<String, String>()) }
        var selectedFactura by remember { mutableStateOf<Map<String, Any>?>(null) }

        LaunchedEffect(Unit) {
            val usersSnapshot = firestore.collection("users").get().await()
            userMap = usersSnapshot.documents.associate { it.id to (it.getString("name") ?: "Desconocido") }

            val snapshot = firestore.collection("facturas_finalizadas").get().await()
            facturas = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Facturas Finalizadas", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            LazyColumn(Modifier.weight(1f)) {
                items(facturas) { factura ->
                    val evento = factura["nombreEvento"] as? String ?: "Sin nombre"
                    val horas = factura["horasTrabajadas"]?.toString() ?: "-"
                    val notas = factura["notas"]?.toString() ?: "-"
                    val participanteId = factura["participanteId"]?.toString() ?: ""
                    val participante = userMap[participanteId] ?: "Desconocido"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .clickable { selectedFactura = factura },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Evento: $evento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Participante: $participante", fontSize = 14.sp)
                            Text("Horas trabajadas: $horas", fontSize = 13.sp)
                            Text("Notas: $notas", fontSize = 13.sp)
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

        selectedFactura?.let { factura ->
            val facturaId = factura["id"] as? String ?: return@let
            val nombreEvento = factura["nombreEvento"] as? String ?: return@let
            val participanteId = factura["participanteId"] as? String ?: return@let

            AlertDialog(
                onDismissRequest = { selectedFactura = null },
                title = { Text("Gestionar Factura") },
                text = { Text("¿Quieres aceptar o denegar esta factura?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Eliminar de facturas_finalizadas
                        firestore.collection("facturas_finalizadas").document(facturaId).delete()
                            .addOnSuccessListener {
                                // Buscar y eliminar factura original
                                firestore.collection("facturas")
                                    .whereEqualTo("nombreEvento", nombreEvento)
                                    .whereEqualTo("participanteId", participanteId)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        snapshot.documents.forEach { doc ->
                                            firestore.collection("facturas").document(doc.id).delete()
                                        }
                                    }
                                Toast.makeText(context, "Factura aceptada", Toast.LENGTH_SHORT).show()
                                selectedFactura = null
                            }
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        firestore.collection("facturas_finalizadas").document(facturaId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Factura denegada", Toast.LENGTH_SHORT).show()
                                selectedFactura = null
                            }
                    }) {
                        Text("Denegar", color = Color.Red)
                    }
                }
            )
        }
    }
}
