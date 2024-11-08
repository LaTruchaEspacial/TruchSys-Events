package tfg.azafatasapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import tfg.azafatasapp.ui.profile.PerfilActivity

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen()
        }
    }

    @Composable
    fun HomeScreen() {
        // Aquí va el contenido principal de la pantalla (por ejemplo, tu lista, contenido, etc.)

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Tu contenido principal de la actividad
                // Ejemplo: un texto o un listado
                Text(text = "Contenido principal de Home", fontSize = 20.sp)
            }

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
                                val intent = Intent(this@HomeActivity, HomeActivity::class.java)
                                startActivity(intent)
                            }
                    )
                    // Icono de Trabajos
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "Trabajos",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                Toast.makeText(this@HomeActivity, "Trabajos clickeado", Toast.LENGTH_SHORT).show()
                            }
                    )
                    // Icono de Ofertas
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Ofertas",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                Toast.makeText(this@HomeActivity, "Ofertas clickeado", Toast.LENGTH_SHORT).show()
                            }
                    )
                    // Icono de Mensajes
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Mensajes",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                Toast.makeText(this@HomeActivity, "Mensajes clickeado", Toast.LENGTH_SHORT).show()
                            }
                    )
                    // Icono de Perfil
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                val intent = Intent(this@HomeActivity, PerfilActivity::class.java)
                                startActivity(intent)
                                Toast.makeText(this@HomeActivity, "Perfil clickeado", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                // Fila con los textos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Texto de Inicio
                    Text("Inicio", fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    // Texto de Trabajos
                    Text("Trabajos", fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    // Texto de Ofertas
                    Text("Ofertas", fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    // Texto de Mensajes
                    Text("Mensajes", fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    // Texto de Perfil
                    Text("Perfil", fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}
