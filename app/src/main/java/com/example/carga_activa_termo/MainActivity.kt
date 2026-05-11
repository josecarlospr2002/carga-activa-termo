package com.example.carga_activa_termo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color(0xFF000000),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        color = Color(0xFF1565C0),
                        shape = RoundedCornerShape(30.dp)
                    ){
                    PantallaCargaActiva()
                }
            }
        }
    } }
}

@Composable
fun PantallaCargaActiva() {
    var lecturas by remember { mutableStateOf<List<Lectura>>(emptyList()) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var reconectando by remember { mutableStateOf(false) }
    var modalAbierto by remember { mutableStateOf(false) }
    var unidadSeleccionada by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val esModoMock = ApiClient.isMockMode()

    fun obtenerMensajeError(exception: Exception): String {
        return when (exception) {
            is UnknownHostException ->
                "No se puede conectar al servidor.\nVerifica que estés conectado a la red de la empresa."
            is SocketTimeoutException ->
                "El servidor está tardando mucho en responder.\nIntenta de nuevo en unos momentos."
            is ConnectException ->
                "No hay conexión a Internet.\nRevisa tu WiFi o datos móviles."
            else -> {
                val mensaje = exception.message ?: "Error desconocido"
                if (mensaje.contains("Unable to resolve host")) {
                    "No se puede conectar al servidor.\nVerifica que estés conectado a la red de la empresa."
                } else if (mensaje.contains("500")) {
                    "Error interno del servidor.\nContacta al equipo técnico."
                } else {
                    "Ocurrió un error inesperado.\nPor favor, intenta de nuevo."
                }
            }
        }
    }

    fun cargarDatos() {
        coroutineScope.launch {
            try {
                reconectando = true
                if (esModoMock) {
                    lecturas = ApiClient.getMockData()
                    mensajeError = null
                } else {
                    val respuesta = ApiClient.apiService.getCargaActiva()
                    lecturas = respuesta
                    mensajeError = null
                }
            } catch (e: Exception) {
                mensajeError = obtenerMensajeError(e)
                lecturas = emptyList()
            } finally {
                reconectando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    LaunchedEffect(mensajeError) {
        if (mensajeError == null) {
            while (true) {
                delay(60_000)
                cargarDatos()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .then(if (modalAbierto) Modifier.blur(24.dp) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, start = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(86.dp)
                        .offset(x = 28.dp, y = (-8).dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.che_guevara),
                        contentDescription = "Che Guevara",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column (
                    modifier = Modifier.offset(x = 32.dp)
                ){
                    Text(
                        text = "CTE Ernesto",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "    Guevara",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (mensajeError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 40.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = mensajeError!!,
                            fontSize = 16.sp,
                            color = Color(0xFF424242),
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { cargarDatos() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color.White
                    ),
                    enabled = !reconectando
                ) {
                    if (reconectando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF0D47A1),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Reconectando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                    } else {
                        Text(
                            text = "⟳ Reintentar conexión",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }
            } else {
                val ultimaLectura = lecturas.lastOrNull()

                if (ultimaLectura != null) {
                    TarjetaUnidad(
                        numeroUnidad = 1,
                        valor = ultimaLectura.lec1,
                        onClick = {
                            unidadSeleccionada = 1
                            modalAbierto = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TarjetaUnidad(
                        numeroUnidad = 2,
                        valor = ultimaLectura.lec2,
                        onClick = {
                            unidadSeleccionada = 2
                            modalAbierto = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TarjetaUnidad(
                        numeroUnidad = 3,
                        valor = ultimaLectura.lec3,
                        onClick = {
                            unidadSeleccionada = 3
                            modalAbierto = true
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Última Actualización: ${ultimaLectura.hora ?: "--:--"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando datos...",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Modal
        if (modalAbierto) {
            val ultimaLectura = lecturas.lastOrNull()
            val valorUnidad = when (unidadSeleccionada) {
                1 -> ultimaLectura?.lec1
                2 -> ultimaLectura?.lec2
                3 -> ultimaLectura?.lec3
                else -> null
            }

            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unidad No. $unidadSeleccionada",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(3.dp)
                                .background(Color(0xFF1976D2))
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "CARGA ACTIVA",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF757575),
                                    letterSpacing = 2.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (valorUnidad != null) {
                                        valorUnidad.roundToInt().toString()
                                    } else {
                                        "-"
                                    },
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ÚLTIMA ACTUALIZACIÓN",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF757575),
                                    letterSpacing = 2.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = ultimaLectura?.hora ?: "--:--",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Button(
                            onClick = {
                                // Funcionalidad a futuro
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF42A5F5)
                            )
                        ) {
                            Text(
                                text = "Gráfico 24 Horas Unidad $unidadSeleccionada",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { modalAbierto = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D47A1)
                            )
                        ) {
                            Text(
                                text = "Cerrar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaUnidad(
    numeroUnidad: Int,
    valor: Double?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 40.dp, top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Unidad No. $numeroUnidad",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pulse para ver detalles",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = if (valor != null) {
                    valor.roundToInt().toString()
                } else {
                    "-"
                },
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}