package tfg.azafatasapp.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tfg.azafatasapp.ui.profile.PerfilActivity // Asegúrate de importar correctamente

class MainActivityAdmin : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AdminScreen()
        }
    }

    @Composable
    fun AdminScreen() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Aquí puedes colocar cualquier contenido adicional si lo deseas
                // Por ejemplo, un mensaje de bienvenida o algún otro contenido

                // Footer de navegación en la parte inferior de la pantalla
                Spacer(modifier = Modifier.weight(1f)) // Esto asegura que el pie de página se empuje al fondo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                    // Si estás en MainActivityAdmin, no necesitas hacer nada en este caso
                                    // O puedes redirigir a otra actividad si lo necesitas
                                    Toast.makeText(this@MainActivityAdmin, "Inicio clickeado", Toast.LENGTH_SHORT).show()
                                }
                        )
                        // Icono de Trabajos
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "Trabajos",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    // Aquí puedes agregar lo que haga este icono
                                    Toast.makeText(this@MainActivityAdmin, "Trabajos clickeado", Toast.LENGTH_SHORT).show()
                                }
                        )
                        // Icono de Ofertas
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Ofertas",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    // Aquí puedes agregar lo que haga este icono
                                    Toast.makeText(this@MainActivityAdmin, "Ofertas clickeado", Toast.LENGTH_SHORT).show()
                                }
                        )
                        // Icono de Mensajes
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Mensajes",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    // Aquí puedes agregar lo que haga este icono
                                    Toast.makeText(this@MainActivityAdmin, "Mensajes clickeado", Toast.LENGTH_SHORT).show()
                                }
                        )
                        // Icono de Perfil
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    // Redirigir al perfil
                                    Toast.makeText(this@MainActivityAdmin, "Clic en Perfil", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@MainActivityAdmin, PerfilActivity::class.java)
                                    startActivity(intent)
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
                        Text(
                            "Inicio",
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        // Texto de Trabajos
                        Text(
                            "Trabajos",
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        // Texto de Ofertas
                        Text(
                            "Ofertas",
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        // Texto de Mensajes
                        Text(
                            "Mensajes",
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        // Texto de Perfil
                        Text(
                            "Perfil",
                            fontSize = 8.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
