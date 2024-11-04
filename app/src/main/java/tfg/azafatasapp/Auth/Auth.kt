// Ruta: tfg/azafatasapp/Auth/Auth.kt
package tfg.azafatasapp.Auth

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tfg.azafatasapp.models.User
import tfg.azafatasapp.ui.home.HomeActivity

class Auth(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Método para cargar los datos del usuario desde Firestore
    fun loadUserData(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onFailure(Exception("No se encontraron datos del usuario"))
                        }
                    } else {
                        onFailure(Exception("Documento de usuario no encontrado"))
                    }
                }
                .addOnFailureListener { exception -> onFailure(exception) }
        } else {
            onFailure(Exception("No hay usuario autenticado"))
        }
    }

    // Ruta: tfg/azafatasapp/Auth/Auth.kt
    fun deleteAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid

            // Eliminar el documento del usuario de Firestore primero
            db.collection("users").document(userId).delete()
                .addOnSuccessListener {
                    // Si se elimina el documento, eliminar también el usuario de Firebase Auth
                    currentUser.delete().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess() // Llama al callback de éxito
                        } else {
                            onFailure(task.exception ?: Exception("Error desconocido al eliminar la cuenta."))
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception) // Llama al callback de error
                }
        } ?: run {
            onFailure(Exception("No hay usuario autenticado para eliminar."))
        }
    }

    // Método para actualizar los datos del usuario
    fun updateUserData(
        newName: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "name" to newName,
            "email" to newEmail
        )

        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // Método de inicio de sesión
    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    onSuccess()
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    // Método de registro de usuario
    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            Log.e("Auth", "Error al registrar en Firestore: ${e.message}")
                            onFailure(e)
                        }
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }

    // Método para recuperar contraseña
    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    Toast.makeText(context, "Se ha enviado un enlace para restablecer la contraseña", Toast.LENGTH_LONG).show()
                } else {
                    task.exception?.let { onFailure(it) }
                }
            }
    }
}
