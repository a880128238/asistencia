package com.eduardo.asistencia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eduardo.asistencia.ui.theme.AsistenciaTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = Firebase.firestore
        guardarAsistencia(db)
        setContent {
            AsistenciaTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "registro") {

                    composable("registro") {
                        PantallaAsistencia(
                            onVerLista = { navController.navigate("lista") }
                        )
                    }

                    composable("lista") {
                        ListaAsistencias()
                    }
                }
            }
        }

    }
}

fun guardarAsistencia(db: FirebaseFirestore) {
    val data = hashMapOf(
        "nombre" to "Juan Pérez",
        "hora" to System.currentTimeMillis()
    )

    db.collection("asistencias")
        .add(data)
        .addOnSuccessListener {
            Log.d("Firestore", "Registro guardado con ID: ${it.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al guardar", e)
        }
}

@Composable
fun ListaAsistencias() {
    val db = Firebase.firestore
    var lista by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("asistencias")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val datos = snapshot.documents.map { doc ->
                    mapOf(
                        "id" to doc.id,
                        "nombre" to (doc["nombre"] ?: ""),
                        "hora" to (doc["hora"] ?: "")
                    )
                }

                lista = datos
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Lista de asistencias",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        lista.forEach { doc ->
            val id = doc["id"].toString()
            val nombre = doc["nombre"].toString()
            val hora = doc["hora"].toString()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text("Nombre: $nombre")
                    Text("Hora: $hora")
                }

                Row {
                    Button(
                        onClick = { editarAsistencia(id, db) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = { eliminarAsistencia(id, db) }
                    ) {
                        Text("Borrar")
                    }
                }
            }

            Divider()
        }
    }
}

@Composable
fun PantallaAsistencia(onVerLista: () -> Unit) {

    val db = Firebase.firestore

    var nombre by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (nombre.isNotEmpty()) {
                    val data = hashMapOf(
                        "nombre" to nombre,
                        "hora" to System.currentTimeMillis()
                    )

                    db.collection("asistencias")
                        .add(data)
                        .addOnSuccessListener {
                            mensaje = "Asistencia registrada"
                            nombre = ""
                        }
                        .addOnFailureListener {
                            mensaje = "Error: ${it.message}"
                        }
                } else {
                    mensaje = "Escribe un nombre"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar asistencia")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onVerLista() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver lista de asistencias")
        }

        if (mensaje.isNotEmpty()) {
            Text(mensaje)
        }
    }
}

fun eliminarAsistencia(id: String, db: FirebaseFirestore) {
    db.collection("asistencias").document(id)
        .delete()
        .addOnSuccessListener {
            Log.d("Firestore", "Documento eliminado")
        }
        .addOnFailureListener {
            Log.e("Firestore", "Error al eliminar", it)
        }
}

fun editarAsistencia(id: String, db: FirebaseFirestore) {
    val nuevosDatos = mapOf(
        "nombre" to "Actualizado",
        "hora" to System.currentTimeMillis()
    )

    db.collection("asistencias").document(id)
        .update(nuevosDatos)
        .addOnSuccessListener {
            Log.d("Firestore", "Documento actualizado")
        }
        .addOnFailureListener {
            Log.e("Firestore", "Error al actualizar", it)
        }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AsistenciaTheme {
        Greeting("Android")
    }
}