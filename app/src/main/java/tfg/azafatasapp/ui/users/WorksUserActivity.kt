package tfg.azafatasapp.ui.users

import android.os.Bundle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorksUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorksUserScreen() }
    }

    @Composable
    fun WorksUserScreen() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        var assignedEvents by remember { mutableStateOf(listOf<Map<String, Any>>()) }

        LaunchedEffect(Unit) {
            val snapshot = firestore.collection("eventos")
                .whereArrayContains("usuariosAsignados", userId)
                .get()
                .await()

            assignedEvents = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                data + mapOf("id" to doc.id)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mis trabajos asignados", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(assignedEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Evento: ${event["evento"]}")
                            Text("Dirección: ${event["direccion"]}")
                            Text("Fecha: ${event["fecha"]}")
                            Text("Hora: ${event["horaInicio"]}")
                            Text("Horas de trabajo: ${event["horas"]}")
                            Text("Estado: ${event["estado"]}")
                        }
                    }
                }
            }

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
    }
}
