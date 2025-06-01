package tfg.azafatasapp.Admin

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import tfg.azafatasapp.ui.admins.BillingAdminActivity
import tfg.azafatasapp.ui.admins.MessageAdminActivity
import tfg.azafatasapp.ui.admins.WorksAdminActivity
import tfg.azafatasapp.ui.profile.PerfilActivity

class MainActivityAdmin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AdminScreen() }
    }

    @Composable
    fun AdminScreen() {
        val firestore = FirebaseFirestore.getInstance()
        var genderFilter by remember { mutableStateOf("Todos") }
        var estadoFilter by remember { mutableStateOf("Todos") }
        var searchQuery by remember { mutableStateOf("") }
        var users by remember { mutableStateOf(listOf<Map<String, String>>()) }
        var estadosDisponibles by remember { mutableStateOf(listOf("Todos")) }
        var selectedUser by remember { mutableStateOf<Map<String, String>?>(null) }

        // Cargar usuarios y estados
        LaunchedEffect(Unit) {
            try {
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "user")
                    .get()
                    .await()

                users = snapshot.documents.mapNotNull { doc ->
                    mapOf(
                        "uid" to doc.id,
                        "name" to (doc.getString("name") ?: ""),
                        "phone" to (doc.getString("phone") ?: ""),
                        "gender" to (doc.getString("gender") ?: "Unknown"),
                        "email" to (doc.getString("email") ?: ""),
                        "dni" to (doc.getString("dni") ?: ""),
                        "birthYear" to (doc.getString("birthYear") ?: ""),
                        "role" to (doc.getString("role") ?: ""),
                        "estado" to (doc.getString("estado") ?: "Activo")
                    )
                }

                val estadosUnicos = snapshot.documents.mapNotNull { it.getString("estado") }.toSet().toList().sorted()
                estadosDisponibles = listOf("Todos") + estadosUnicos

            } catch (e: Exception) {
                Toast.makeText(this@MainActivityAdmin, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                // Filtros y buscador
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Filtro género
                    var genderExpanded by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            genderFilter,
                            modifier = Modifier
                                .clickable { genderExpanded = true }
                                .background(Color.LightGray)
                                .padding(8.dp)
                        )
                        DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            listOf("Todos", "Male", "Female").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        genderFilter = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                   //hola
                    // Filtro estado dinámico
                    var estadoExpanded by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            estadoFilter,
                            modifier = Modifier
                                .clickable { estadoExpanded = true }
                                .background(Color.LightGray)
                                .padding(8.dp)
                        )
                        DropdownMenu(expanded = estadoExpanded, onDismissRequest = { estadoExpanded = false }) {
                            estadosDisponibles.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        estadoFilter = option
                                        estadoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Buscador
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar por nombre") },
                        modifier = Modifier.width(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Aplicar filtros
                val filteredUsers = users.filter {
                    (genderFilter == "Todos" || it["gender"] == genderFilter) &&
                            (estadoFilter == "Todos" || it["estado"] == estadoFilter) &&
                            it["name"]!!.contains(searchQuery, ignoreCase = true)
                }

                // Lista de usuarios
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedUser = user },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Nombre: ${user["name"]}")
                                Text("Teléfono: ${user["phone"]}")
                                Text("Email: ${user["email"]}")
                                Text("DNI: ${user["dni"]}")
                                Text("Nacimiento: ${user["birthYear"]}")
                                Text("Género: ${user["gender"]}")
                                Text("Rol: ${user["role"]}")
                                Text("Estado: ${user["estado"]}")
                            }
                        }
                    }
                }

                // Footer navegación
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
                        Icon(Icons.Default.Face, "Trabajos", modifier = Modifier.size(28.dp).clickable {
                            val intent = Intent(this@MainActivityAdmin, WorksAdminActivity::class.java)
                            startActivity(intent)
                        })
                        Icon(Icons.Default.Search, "Facturas",  modifier = Modifier.size(28.dp).clickable {
                            val intent = Intent(this@MainActivityAdmin, BillingAdminActivity::class.java)
                            startActivity(intent)
                        })

                        Icon(Icons.Default.Notifications, "Mensajes",modifier = Modifier.size(28.dp).clickable {
                            val intent = Intent(this@MainActivityAdmin, MessageAdminActivity::class.java)
                            startActivity(intent)
                        })
                        Icon(Icons.Default.Person, "Perfil",
                            modifier = Modifier.size(28.dp).clickable {
                                val intent = Intent(this@MainActivityAdmin, PerfilActivity::class.java)
                                startActivity(intent)
                            }
                        )
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

            // Diálogo para editar (sin eliminar)
            selectedUser?.let { user ->
                var editedName by remember { mutableStateOf(user["name"] ?: "") }
                var editedPhone by remember { mutableStateOf(user["phone"] ?: "") }
                var editedGender by remember { mutableStateOf(user["gender"] ?: "Male") }

                AlertDialog(
                    onDismissRequest = { selectedUser = null },
                    title = { Text("Editar Usuario") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Nombre") }
                            )
                            OutlinedTextField(
                                value = editedPhone,
                                onValueChange = { editedPhone = it },
                                label = { Text("Teléfono") }
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Género: ")
                                RadioButton(selected = editedGender == "Male", onClick = { editedGender = "Male" })
                                Text("Masculino")
                                Spacer(Modifier.width(8.dp))
                                RadioButton(selected = editedGender == "Female", onClick = { editedGender = "Female" })
                                Text("Femenino")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val uid = user["uid"] ?: return@TextButton
                            val updateMap = mapOf(
                                "name" to editedName,
                                "phone" to editedPhone,
                                "gender" to editedGender
                            )
                            firestore.collection("users")
                                .document(uid)
                                .update(updateMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this@MainActivityAdmin, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                                    selectedUser = null
                                    users = users.map {
                                        if (it["uid"] == uid) it + updateMap else it
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@MainActivityAdmin, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedUser = null }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
