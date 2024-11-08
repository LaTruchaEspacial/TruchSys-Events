package tfg.azafatasapp.Auth

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tfg.azafatasapp.models.User

class Auth(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Método para verificar si el usuario está autenticado
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    // Método para obtener el usuario autenticado actual
    fun getCurrentUser(onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Usamos el email del usuario autenticado para buscarlo en Firestore
            getUserByEmail(firebaseUser.email ?: "", { user ->
                onSuccess(user)
            }, { exception ->
                onFailure(exception)
            })
        } else {
            // Si no hay usuario autenticado
            onSuccess(null)
        }
    }

    // Método para obtener un usuario por su email
    fun getUserByEmail(
        email: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val user = result.documents[0].toObject(User::class.java)
                    onSuccess(user)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Método para registrar un nuevo usuario
    fun register(
        email: String,
        password: String,
        name: String,
        phone: String,
        birthYear: String,
        gender: String,
        dni: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = User(
                            uid = it.uid,
                            name = name,
                            email = email,
                            phone = phone,
                            birthYear = birthYear,
                            gender = gender,
                            dni = dni
                        )
                        firestore.collection("users")
                            .document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }
                } else {
                    onFailure(task.exception ?: Exception("Error desconocido"))
                }
            }
    }

    // Método para iniciar sesión
    fun login(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                firebaseUser?.let {
                    getUserByEmail(firebaseUser.email ?: "", { user ->
                        user?.let {
                            onSuccess(user) // Pasa el `user` de Firestore al callback `onSuccess`
                        } ?: onFailure(Exception("Usuario no encontrado en Firestore"))
                    }, onFailure)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Método para cerrar sesión
    fun logout() {
        auth.signOut()
    }
}
