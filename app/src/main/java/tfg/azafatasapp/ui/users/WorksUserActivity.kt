package tfg.azafatasapp.ui.users

import android.content.Intent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tfg.azafatasapp.ui.home.HomeActivity
import tfg.azafatasapp.ui.profile.PerfilActivity
import tfg.azafatasapp.ui.users.BillingUserActivity
import tfg.azafatasapp.ui.users.MessageUserActivity

class WorksUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorksUserScreen() }
    }

    @Composable
    fun WorksUserScreen() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val context = LocalContext.current
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFBBDEFB), Color.White)))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(bottom = 80.dp)) {
                Text("Mis trabajos asignados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0057D9))
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(assignedEvents) { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Evento: ${event["evento"]}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D47A1))
                                Text("Dirección: ${event["direccion"]}", color = Color.DarkGray)
                                Text("Fecha: ${event["fecha"]}", color = Color.DarkGray)
                                Text("Hora: ${event["horaInicio"]}", color = Color.DarkGray)
                                Text("Horas de trabajo: ${event["horas"]}", color = Color.DarkGray)
                                Text("Estado: ${event["estado"]}", fontWeight = FontWeight.SemiBold, color = Color(0xFF00695C))
                            }
                        }
                    }
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
                        context.startActivity(Intent(context, HomeActivity::class.java))
                    })
                    Icon(Icons.Default.Face, "Trabajos", Modifier.size(28.dp).clickable {
                        context.startActivity(Intent(context, WorksUserActivity::class.java))
                        Toast.makeText(context, "Trabajos clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Search, "Facturas", Modifier.size(28.dp).clickable {
                        context.startActivity(Intent(context, BillingUserActivity::class.java))
                        Toast.makeText(context, "Facturas clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Notifications, "Mensajes", Modifier.size(28.dp).clickable {
                        context.startActivity(Intent(context, MessageUserActivity::class.java))
                        Toast.makeText(context, "Mensajes clickeado", Toast.LENGTH_SHORT).show()
                    })
                    Icon(Icons.Default.Person, "Perfil", Modifier.size(28.dp).clickable {
                        context.startActivity(Intent(context, PerfilActivity::class.java))
                        Toast.makeText(context, "Perfil clickeado", Toast.LENGTH_SHORT).show()
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
        }
    }
}
