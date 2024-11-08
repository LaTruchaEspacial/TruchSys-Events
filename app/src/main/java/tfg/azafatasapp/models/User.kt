package tfg.azafatasapp.models

data class User(
    val uid: String = "", // ID único del usuario en Firebase Authentication
    val role: String = "user", // El rol por defecto es "user"
    val name: String = "", // Nombre completo del usuario
    val email: String = "", // Correo electrónico del usuario
    val phone: String = "", // Número de teléfono del usuario
    val dni: String = "", // DNI del usuario (único para cada persona)
    val birthYear: String = "", // Año de nacimiento
    val gender: String = "" // Género del usuario
)
