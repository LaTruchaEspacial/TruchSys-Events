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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorksAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WorksScreen() }
    }

    @Composable
    fun WorksScreen() {
        val firestore = FirebaseFirestore.getInstance()
        var searchQuery by remember { mutableStateOf("") }
        var events by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var selectedEvent by remember { mutableStateOf<Map<String, Any>?>(null) }

        LaunchedEffect(Unit) {
            val snapshot = firestore.collection("eventos")
                .whereEqualTo("estado", "En curso")
                .get()
                .await()

            events = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                data + mapOf("id" to doc.id)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre de evento") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    selectedEvent = mapOf(
                        "evento" to "",
                        "direccion" to "",
                        "fecha" to "",
                        "horaInicio" to "",
                        "horas" to "",
                        "usuariosAsignados" to emptyList<String>(),
                        "id" to ""
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Crear nuevo evento")
            }

            val filteredEvents = events.filter {
                it["evento"].toString().contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredEvents) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedEvent = event },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Evento: ${event["evento"]}")
                            Text("Fecha: ${event["fecha"]}")
                            Text("Hora: ${event["horaInicio"]}")
                            Text("Horas trabajadas: ${event["horas"] ?: "No asignadas"}")
                            Text("Dirección: ${event["direccion"]}")
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().background(Color.LightGray).padding(vertical = 8.dp)
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

        selectedEvent?.let { event ->
            val firestore = FirebaseFirestore.getInstance()
            var editedName by remember { mutableStateOf(event["evento"]?.toString() ?: "") }
            var editedAddress by remember { mutableStateOf(event["direccion"]?.toString() ?: "") }
            var editedFecha by remember { mutableStateOf(event["fecha"]?.toString() ?: "") }
            var editedHora by remember { mutableStateOf(event["horaInicio"]?.toString() ?: "") }
            var editedHorasTrabajadas by remember { mutableStateOf(event["horas"]?.toString() ?: "") }

            val currentAssigned = remember { mutableStateListOf<String>().apply { addAll(event["usuariosAsignados"] as? List<String> ?: emptyList()) } }
            var userList by remember { mutableStateOf(listOf<Map<String, String>>()) }
            var assignedNames by remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(event) {
                val snapshot = firestore.collection("users").get().await()
                userList = snapshot.documents.mapNotNull {
                    val uid = it.id
                    val name = it.getString("name") ?: return@mapNotNull null
                    val estado = it.getString("estado") ?: "Activo"
                    val role = it.getString("role") ?: ""
                    if ((estado == "Activo" || currentAssigned.contains(uid)) && role == "user") {
                        mapOf("uid" to uid, "name" to name, "estado" to estado)
                    } else null
                }
                assignedNames = userList.filter { currentAssigned.contains(it["uid"]) }.mapNotNull { it["name"] }
            }

            AlertDialog(
                onDismissRequest = { selectedEvent = null },
                title = { Text("Editar evento") },
                text = {
                    Column {
                        OutlinedTextField(editedName, { editedName = it }, label = { Text("Nombre del evento") })
                        OutlinedTextField(editedAddress, { editedAddress = it }, label = { Text("Dirección") })
                        OutlinedTextField(editedFecha, { editedFecha = it }, label = { Text("Fecha") })
                        OutlinedTextField(editedHora, { editedHora = it }, label = { Text("Hora inicio") })
                        OutlinedTextField(editedHorasTrabajadas, { editedHorasTrabajadas = it }, label = { Text("Horas trabajadas") })

                        Spacer(Modifier.height(8.dp))
                        Text("Participantes seleccionados:")
                        assignedNames.forEach { name -> Text("\u2022 $name") }

                        Spacer(Modifier.height(8.dp))
                        Text("Agregar/Quitar trabajadores:")

                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(userList) { user ->
                                val uid = user["uid"]!!
                                val name = user["name"]!!
                                val estado = user["estado"]!!
                                val isSelected = currentAssigned.contains(uid)

                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        if (isSelected) currentAssigned.remove(uid)
                                        else if (estado == "Activo") currentAssigned.add(uid)
                                    }.padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = isSelected, onCheckedChange = null)
                                    Text("$name ($estado)")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val id = event["id"] as? String
                        val oldAssigned = event["usuariosAsignados"] as? List<String> ?: emptyList()
                        val newAssigned = currentAssigned.toList()
                        val added = newAssigned - oldAssigned
                        val removed = oldAssigned - newAssigned

                        added.forEach { uid ->
                            firestore.collection("users").document(uid).update("estado", "Inactivo")
                        }
                        removed.forEach { uid ->
                            firestore.collection("users").document(uid).update("estado", "Activo")
                        }

                        val update = mapOf(
                            "evento" to editedName,
                            "direccion" to editedAddress,
                            "fecha" to editedFecha,
                            "horaInicio" to editedHora,
                            "horas" to editedHorasTrabajadas,
                            "usuariosAsignados" to newAssigned,
                            "estado" to "En curso"
                        )

                        if (!id.isNullOrEmpty()) {
                            firestore.collection("eventos").document(id).update(update)
                                .addOnSuccessListener {
                                    Toast.makeText(this@WorksAdminActivity, "Evento actualizado", Toast.LENGTH_SHORT).show()
                                    selectedEvent = null
                                }
                        } else {
                            firestore.collection("eventos").add(update)
                                .addOnSuccessListener {
                                    Toast.makeText(this@WorksAdminActivity, "Evento creado", Toast.LENGTH_SHORT).show()
                                    selectedEvent = null
                                }
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        val id = event["id"] as String
                        val userUIDs = currentAssigned.toList()

                        firestore.collection("eventos").document(id).update("estado", "Finalizado")
                        userUIDs.forEach { uid ->
                            firestore.collection("users").document(uid).update("estado", "Activo")
                        }

                        firestore.collection("mensajes")
                            .whereEqualTo("eventoId", id)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                val batch = firestore.batch()
                                snapshot.documents.forEach { doc ->
                                    batch.delete(doc.reference)
                                }
                                batch.commit()
                            }

                        // Crear facturas solo para usuarios con rol "user"
                        firestore.collection("users")
                            .whereIn(FieldPath.documentId(), userUIDs)
                            .get()
                            .addOnSuccessListener { usersSnapshot ->
                                usersSnapshot.documents.forEach { userDoc ->
                                    val uid = userDoc.id
                                    val role = userDoc.getString("role") ?: "user"
                                    if (role == "user") {
                                        val facturaData = mapOf(
                                            "nombreEvento" to editedName,
                                            "participanteId" to uid,
                                            "horasTrabajadas" to "",
                                            "notas" to ""
                                        )
                                        firestore.collection("facturas").add(facturaData)
                                    }
                                }
                            }

                        Toast.makeText(this@WorksAdminActivity, "Evento finalizado, mensajes eliminados y facturas creadas", Toast.LENGTH_SHORT).show()
                        selectedEvent = null
                    }) {
                        Text("Finalizar evento", color = Color.Red)
                    }
                }
            )
        }
    }
}
