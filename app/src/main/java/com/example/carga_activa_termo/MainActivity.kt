package com.example.carga_activa_termo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TablaCargaActiva()
                }
            }
        }
    }
}

@Composable
fun TablaCargaActiva() {
    var lecturas by remember { mutableStateOf<List<Lectura>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Función para cargar datos
    fun cargarDatos() {
        coroutineScope.launch {
            try {
                val respuesta = ApiClient.apiService.getCargaActiva()
                lecturas = respuesta
                error = null
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            }
        }
    }

    // Actualizar cada 60 segundos
    LaunchedEffect(Unit) {
        while (true) {
            cargarDatos()
            delay(60_000) // 60 segundos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Carga Activa",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Mensaje de error si existe
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Tabla
        if (lecturas.isNotEmpty()) {
            // Encabezados de la tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2196F3))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lec1", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("Lec2", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("Lec3", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                Text("Hora", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            }

            // Datos de la tabla
            LazyColumn {
                items(lecturas) { lectura ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (lecturas.indexOf(lectura) % 2 == 0) Color(0xFFF5F5F5) else Color.White)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.2f", lectura.lec1 ?: 0.0),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format("%.2f", lectura.lec2 ?: 0.0),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (lectura.lec3 != null) String.format("%.2f", lectura.lec3) else "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = lectura.hora ?: "N/A",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            // Mensaje cuando no hay datos
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cargando datos...",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }
    }
}