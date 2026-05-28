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
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement

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
                    ) {
                        PantallaCargaActiva()
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaCargaActiva() {
    var lecturas by remember { mutableStateOf<List<Lectura>>(emptyList()) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var reconectando by remember { mutableStateOf(false) }
    var modalAbierto by remember { mutableStateOf(false) }
    var unidadSeleccionada by remember { mutableStateOf(0) }
    var modalGraficoAbierto by remember { mutableStateOf(false) }
    var datos24h by remember { mutableStateOf<List<Lectura>>(emptyList()) }
    var cargandoGrafico by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val esModoMock = ApiClient.isMockMode()
    var snackbarMensaje by remember { mutableStateOf<String?>(null) }
    var snackbarVisible by remember { mutableStateOf(false) }
    var mostrarUnidad1 by remember { mutableStateOf(true) }
    var mostrarUnidad2 by remember { mutableStateOf(true) }
    var mostrarUnidad3 by remember { mutableStateOf(true) }
    var graficoUnidadEspecifica by remember { mutableStateOf(false) }

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

                Column(
                    modifier = Modifier.offset(x = 32.dp)
                ) {
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                mostrarUnidad1 = true
                                mostrarUnidad2 = true
                                mostrarUnidad3 = true

                                graficoUnidadEspecifica = false

                                cargandoGrafico = true
                                modalGraficoAbierto = true
                                coroutineScope.launch {
                                    try {
                                        datos24h = if (esModoMock) {
                                            ApiClient.getMockData24h()
                                        } else {
                                            ApiClient.apiService.getCargaActiva24h()
                                        }
                                    } catch (e: Exception) {
                                        datos24h = emptyList()
                                    } finally {
                                        cargandoGrafico = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF42A5F5)
                            )
                        ) {
                            Text(
                                text = "Gráfico Últimas 24 Horas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
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

        // Modal de unidad
        if (modalAbierto) {
            val ultimaLectura = lecturas.lastOrNull()
            val valorUnidad = when (unidadSeleccionada) {
                1 -> ultimaLectura?.lec1
                2 -> ultimaLectura?.lec2
                3 -> ultimaLectura?.lec3
                else -> null
            }

            Dialog(
                onDismissRequest = { modalAbierto = false },
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
                                modalAbierto = false

                                // Filtros para mostrar solo la unidad seleccionada
                                mostrarUnidad1 = true
                                mostrarUnidad2 = true
                                mostrarUnidad3 = true

                                // Marcar que viene de una unidad específica
                                graficoUnidadEspecifica = true

                                // Cargar datos y abrir gráfico
                                cargandoGrafico = true
                                modalGraficoAbierto = true
                                coroutineScope.launch {
                                    try {
                                        datos24h = if (esModoMock) {
                                            ApiClient.getMockData24h()
                                        } else {
                                            ApiClient.apiService.getCargaActiva24h()
                                        }
                                    } catch (e: Exception) {
                                        datos24h = emptyList()
                                    } finally {
                                        cargandoGrafico = false
                                    }
                                }
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

        // Modal Gráfico 24 Horas
        if (modalGraficoAbierto) {
            Dialog(
                onDismissRequest = { modalGraficoAbierto = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.66f)
                        .padding(horizontal = 8.dp)
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = if (graficoUnidadEspecifica)
                                "Comportamiento de la Unidad $unidadSeleccionada en las Últimas 24 Horas"
                            else
                                "Comportamiento de las Unidades en las Últimas 24 Horas",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(3.dp)
                                .background(Color(0xFF1976D2))
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (cargandoGrafico) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        color = Color(0xFF0D47A1),
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Cargando datos...",
                                        fontSize = 14.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        } else if (datos24h.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Sin datos disponibles",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        } else {
                            val anchoGrafico = maxOf(600.dp, (datos24h.size * 30).dp)

                            Row(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Eje Y fijo a la izquierda
                                Column(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight()
                                        .padding(bottom = 30.dp, start = 0.dp),
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    for (i in 10 downTo 0) {
                                        Text(
                                            text = "${i * 10}",
                                            fontSize = 9.sp,
                                            color = Color(0xFF656565),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 1.dp)
                                        )
                                    }
                                }

                                // Scroll horizontal
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .width(anchoGrafico)
                                            .height(320.dp)
                                            .padding(
                                                start = 4.dp,
                                                end = 8.dp,
                                                top = 4.dp,
                                                bottom = 4.dp
                                            )
                                            .pointerInput(datos24h) {
                                                detectTapGestures { tapOffset ->
                                                    val anchoPx = size.width.toFloat()
                                                    val altoPx = size.height.toFloat()

                                                    val indice =
                                                        ((tapOffset.x / anchoPx) * (datos24h.size - 1))
                                                            .roundToInt()
                                                            .coerceIn(0, datos24h.size - 1)

                                                    val valoresUnidad1 =
                                                        datos24h.map {
                                                            if (it.lec1 == null || it.lec1 <= 0) 0.0 else it.lec1
                                                        }
                                                    val valoresUnidad2 =
                                                        datos24h.map {
                                                            if (it.lec2 == null || it.lec2 <= 0) 0.0 else it.lec2
                                                        }
                                                    val valoresUnidad3 =
                                                        datos24h.map {
                                                            if (it.lec3 == null || it.lec3 <= 0) 0.0 else it.lec3
                                                        }

                                                    var mejorUnidad = "1"
                                                    var mejorDist = Float.MAX_VALUE

                                                    // Unidad 1
                                                    val v1 = valoresUnidad1[indice]
                                                    val prop1 = (v1 / 100.0).toFloat()
                                                    val y1 = altoPx - (prop1 * altoPx)
                                                    val dist1 = kotlin.math.abs(tapOffset.y - y1)
                                                    if (dist1 < mejorDist) {
                                                        mejorDist = dist1
                                                        mejorUnidad = "1"
                                                    }

                                                    // Unidad 2
                                                    val v2 = valoresUnidad2[indice]
                                                    val prop2 = (v2 / 100.0).toFloat()
                                                    val y2 = altoPx - (prop2 * altoPx)
                                                    val dist2 = kotlin.math.abs(tapOffset.y - y2)
                                                    if (dist2 < mejorDist) {
                                                        mejorDist = dist2
                                                        mejorUnidad = "2"
                                                    }

                                                    // Unidad 3
                                                    val v3 = valoresUnidad3[indice]
                                                    val prop3 = (v3 / 100.0).toFloat()
                                                    val y3 = altoPx - (prop3 * altoPx)
                                                    val dist3 = kotlin.math.abs(tapOffset.y - y3)
                                                    if (dist3 < mejorDist) {
                                                        mejorDist = dist3
                                                        mejorUnidad = "3"
                                                    }

                                                    if (mejorDist < 50f) {
                                                        val hora = datos24h.getOrNull(indice)?.hora
                                                            ?: "--:--"

                                                        if (!graficoUnidadEspecifica) {

                                                            val v1 =
                                                                if (valoresUnidad1[indice] <= 0) 0 else valoresUnidad1[indice].roundToInt()
                                                            val v2 =
                                                                if (valoresUnidad2[indice] <= 0) 0 else valoresUnidad2[indice].roundToInt()
                                                            val v3 =
                                                                if (valoresUnidad3[indice] <= 0) 0 else valoresUnidad3[indice].roundToInt()

                                                            // Verificar que el toque está cerca de alguna unidad visible
                                                            val hayUnidadVisibleCerca = when {
                                                                mostrarUnidad1 && dist1 < 50f -> true
                                                                mostrarUnidad2 && dist2 < 50f -> true
                                                                mostrarUnidad3 && dist3 < 50f -> true
                                                                else -> false
                                                            }

                                                            if (hayUnidadVisibleCerca) {
                                                                // Encontrar la unidad visible
                                                                var mejorUnidadVisible = "1"
                                                                var mejorDistVisible =
                                                                    Float.MAX_VALUE

                                                                if (mostrarUnidad1 && dist1 < mejorDistVisible) {
                                                                    mejorDistVisible = dist1
                                                                    mejorUnidadVisible = "1"
                                                                }
                                                                if (mostrarUnidad2 && dist2 < mejorDistVisible) {
                                                                    mejorDistVisible = dist2
                                                                    mejorUnidadVisible = "2"
                                                                }
                                                                if (mostrarUnidad3 && dist3 < mejorDistVisible) {
                                                                    mejorDistVisible = dist3
                                                                    mejorUnidadVisible = "3"
                                                                }

                                                                // Agrupar las unidades visibles por valor
                                                                val grupos =
                                                                    mutableMapOf<Int, MutableList<String>>()
                                                                if (mostrarUnidad1) grupos.getOrPut(
                                                                    v1
                                                                ) { mutableListOf() }
                                                                    .add("Unidad 1")
                                                                if (mostrarUnidad2) grupos.getOrPut(
                                                                    v2
                                                                ) { mutableListOf() }
                                                                    .add("Unidad 2")
                                                                if (mostrarUnidad3) grupos.getOrPut(
                                                                    v3
                                                                ) { mutableListOf() }
                                                                    .add("Unidad 3")

                                                                val valorTocado =
                                                                    when (mejorUnidadVisible) {
                                                                        "1" -> v1
                                                                        "2" -> v2
                                                                        else -> v3
                                                                    }

                                                                val unidadesDelGrupo =
                                                                    grupos[valorTocado]
                                                                        ?: listOf("Unidad $mejorUnidadVisible")

                                                                snackbarMensaje =
                                                                    if (unidadesDelGrupo.size > 1) {
                                                                        "${
                                                                            unidadesDelGrupo.joinToString(
                                                                                ", "
                                                                            ) { "$it = $valorTocado" }
                                                                        } a las $hora"
                                                                    } else {
                                                                        "${unidadesDelGrupo.first()} = $valorTocado a las $hora"
                                                                    }

                                                                snackbarVisible = true
                                                            }
                                                        } else {

                                                            val valorSeleccionado =
                                                                when (unidadSeleccionada) {
                                                                    1 -> if (valoresUnidad1[indice] <= 0) 0 else valoresUnidad1[indice].roundToInt()
                                                                    2 -> if (valoresUnidad2[indice] <= 0) 0 else valoresUnidad2[indice].roundToInt()
                                                                    else -> if (valoresUnidad3[indice] <= 0) 0 else valoresUnidad3[indice].roundToInt()
                                                                }

                                                            val ySeleccionada =
                                                                when (unidadSeleccionada) {
                                                                    1 -> y1
                                                                    2 -> y2
                                                                    else -> y3
                                                                }

                                                            if (kotlin.math.abs(tapOffset.y - ySeleccionada) < 50f) {
                                                                snackbarMensaje =
                                                                    "Unidad $unidadSeleccionada = $valorSeleccionado a las $hora"
                                                                snackbarVisible = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        val ancho = size.width
                                        val alto = size.height

                                        val valores1 = datos24h.map {
                                            if (it.lec1 == null || it.lec1 <= 0) 0.0 else it.lec1
                                        }
                                        val valores2 = datos24h.map {
                                            if (it.lec2 == null || it.lec2 <= 0) 0.0 else it.lec2
                                        }
                                        val valores3 = datos24h.map {
                                            if (it.lec3 == null || it.lec3 <= 0) 0.0 else it.lec3
                                        }

                                        val maxValor = 100.0
                                        val minValor = 0.0
                                        val rango = maxValor - minValor

                                        val cantidadPuntos = datos24h.size

                                        fun dibujarLinea(
                                            valores: List<Double>,
                                            color: Color,
                                            grosor: Float
                                        ) {
                                            if (valores.isEmpty() || cantidadPuntos < 2) return

                                            val path = Path()

                                            for (i in 0 until cantidadPuntos - 1) {
                                                val x1 =
                                                    (i.toFloat() / (cantidadPuntos - 1)) * ancho
                                                val proporcion1 =
                                                    ((valores[i] - minValor) / rango).toFloat()
                                                val y1 = alto - (proporcion1 * alto)

                                                val x2 =
                                                    ((i + 1).toFloat() / (cantidadPuntos - 1)) * ancho
                                                val proporcion2 =
                                                    ((valores[i + 1] - minValor) / rango).toFloat()
                                                val y2 = alto - (proporcion2 * alto)

                                                if (i == 0) {
                                                    path.moveTo(x1, y1)
                                                }

                                                val cx1 = x1 + (x2 - x1) / 3
                                                val cy1 = y1
                                                val cx2 = x2 - (x2 - x1) / 3
                                                val cy2 = y2

                                                path.cubicTo(cx1, cy1, cx2, cy2, x2, y2)
                                            }

                                            drawPath(
                                                path = path,
                                                color = color,
                                                style = Stroke(
                                                    width = grosor,
                                                    cap = StrokeCap.Round,
                                                    join = StrokeJoin.Round
                                                )
                                            )
                                        }

                                        fun dibujarPuntos(valores: List<Double>, color: Color) {
                                            valores.forEachIndexed { index, valor ->
                                                val x =
                                                    (index.toFloat() / (cantidadPuntos - 1)) * ancho
                                                val proporcion =
                                                    ((valor - minValor) / rango).toFloat()
                                                val y = alto - (proporcion * alto)

                                                drawCircle(
                                                    color = Color.White,
                                                    radius = 14f,
                                                    center = Offset(x, y)
                                                )
                                                drawCircle(
                                                    color = color,
                                                    radius = 11f,
                                                    center = Offset(x, y)
                                                )
                                            }
                                        }

                                        for (i in 0..10) {
                                            val valor = i * 10.0           // 0, 10, 20, 30... 100
                                            val proporcion = (valor / 100.0).toFloat()
                                            val y = alto - (proporcion * alto)

                                            val colorLinea = when (i) {
                                                0 -> Color(0xFF000000)
                                                10 -> Color(0xFF000000)
                                                else -> Color(0xFFBDBDBD)
                                            }
                                            val grosorLinea = when (i) {
                                                0, 10 -> 2f
                                                else -> 1f
                                            }

                                            drawLine(
                                                color = colorLinea,
                                                start = Offset(0f, y),
                                                end = Offset(ancho, y),
                                                strokeWidth = grosorLinea
                                            )
                                        }

                                        for (i in 0 until cantidadPuntos) {
                                            val x = (i.toFloat() / (cantidadPuntos - 1)) * ancho
                                            val colorLinea =
                                                if (i == 0) Color(0xFF000000) else Color(0xFF737373)
                                            val grosorLinea = if (i == 0) 2f else 0.5f
                                            drawLine(
                                                color = colorLinea,
                                                start = Offset(x, 0f),
                                                end = Offset(x, alto),
                                                strokeWidth = grosorLinea
                                            )
                                        }

                                        // Dibujar líneas de datos
                                        // Unidad 3
                                        if (mostrarUnidad3 && (!graficoUnidadEspecifica || unidadSeleccionada == 3)) {
                                            dibujarLinea(valores3, Color(0xFFFF5722), 3f)
                                        }
                                        // Unidad 2
                                        if (mostrarUnidad2 && (!graficoUnidadEspecifica || unidadSeleccionada == 2)) {
                                            dibujarLinea(valores2, Color(0xFF388E3C), 3f)
                                        }
                                        // Unidad 1
                                        if (mostrarUnidad1 && (!graficoUnidadEspecifica || unidadSeleccionada == 1)) {
                                            dibujarLinea(valores1, Color(0xFF7B1FA2), 3f)
                                        }

                                        // Puntos
                                        if (mostrarUnidad3 && (!graficoUnidadEspecifica || unidadSeleccionada == 3)) {
                                            dibujarPuntos(valores3, Color(0xFFFFA000))
                                        }
                                        if (mostrarUnidad2 && (!graficoUnidadEspecifica || unidadSeleccionada == 2)) {
                                            dibujarPuntos(valores2, Color(0xFF388E3C))
                                        }
                                        if (mostrarUnidad1 && (!graficoUnidadEspecifica || unidadSeleccionada == 1)) {
                                            dibujarPuntos(valores1, Color(0xFF7B1FA2))
                                        }
                                    }

                                    // Etiquetas del eje X (horas)
                                    Row(
                                        modifier = Modifier
                                            .width(anchoGrafico)
                                            .offset(y = (0).dp),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        for (i in datos24h.indices) {
                                            if (i % 2 == 0) {

                                                val horaCompleta = datos24h[i].hora ?: ""
                                                val partes = horaCompleta.split(" ")
                                                val horaMostrar = if (partes.size == 2) {
                                                    "${partes[0]}\n${partes[1]}"
                                                } else {
                                                    horaCompleta
                                                }

                                                Text(
                                                    text = horaMostrar,
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF656565),
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 10.sp,
                                                    modifier = Modifier
                                                        .width(anchoGrafico / datos24h.size)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.width(anchoGrafico / datos24h.size))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Leyenda
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Unidad 1
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .then(
                                                if (!graficoUnidadEspecifica) {
                                                    Modifier.clickable {
                                                        mostrarUnidad1 = !mostrarUnidad1
                                                    }
                                                } else {
                                                    Modifier  // Sin click en modo unidad específica
                                                }
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = when {
                                                        graficoUnidadEspecifica && unidadSeleccionada != 1 -> Color(
                                                            0xFFBDBDBD
                                                        )

                                                        !mostrarUnidad1 -> Color(0xFFBDBDBD)
                                                        else -> Color(0xFF7B1FA2)
                                                    },
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Unidad 1",
                                            fontSize = 12.sp,
                                            color = when {
                                                graficoUnidadEspecifica && unidadSeleccionada != 1 -> Color(
                                                    0xFFBDBDBD
                                                )

                                                !mostrarUnidad1 -> Color(0xFFBDBDBD)
                                                else -> Color(0xFF757575)
                                            },
                                            fontWeight = when {
                                                graficoUnidadEspecifica && unidadSeleccionada == 1 -> FontWeight.Bold
                                                mostrarUnidad1 && !graficoUnidadEspecifica -> FontWeight.Medium
                                                else -> FontWeight.Normal
                                            }
                                        )
                                    }

                                    // Unidad 2
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .then(
                                                if (!graficoUnidadEspecifica) {
                                                    Modifier.clickable {
                                                        mostrarUnidad2 = !mostrarUnidad2
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = when {
                                                        graficoUnidadEspecifica && unidadSeleccionada != 2 -> Color(
                                                            0xFFBDBDBD
                                                        )

                                                        !mostrarUnidad2 -> Color(0xFFBDBDBD)
                                                        else -> Color(0xFF388E3C)
                                                    },
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Unidad 2",
                                            fontSize = 12.sp,
                                            color = when {
                                                graficoUnidadEspecifica && unidadSeleccionada != 2 -> Color(
                                                    0xFFBDBDBD
                                                )

                                                !mostrarUnidad2 -> Color(0xFFBDBDBD)
                                                else -> Color(0xFF757575)
                                            },
                                            fontWeight = when {
                                                graficoUnidadEspecifica && unidadSeleccionada == 2 -> FontWeight.Bold
                                                mostrarUnidad2 && !graficoUnidadEspecifica -> FontWeight.Medium
                                                else -> FontWeight.Normal
                                            }
                                        )
                                    }

                                    // Unidad 3
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .then(
                                                if (!graficoUnidadEspecifica) {
                                                    Modifier.clickable {
                                                        mostrarUnidad3 = !mostrarUnidad3
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = when {
                                                        graficoUnidadEspecifica && unidadSeleccionada != 3 -> Color(
                                                            0xFFBDBDBD
                                                        )

                                                        !mostrarUnidad3 -> Color(0xFFBDBDBD)
                                                        else -> Color(0xFFFF5722)
                                                    },
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Unidad 3",
                                            fontSize = 12.sp,
                                            color = when {
                                                graficoUnidadEspecifica && unidadSeleccionada != 3 -> Color(
                                                    0xFFBDBDBD
                                                )

                                                !mostrarUnidad3 -> Color(0xFFBDBDBD)
                                                else -> Color(0xFF757575)
                                            },
                                            fontWeight = when {
                                                graficoUnidadEspecifica && unidadSeleccionada == 3 -> FontWeight.Bold
                                                mostrarUnidad3 && !graficoUnidadEspecifica -> FontWeight.Medium
                                                else -> FontWeight.Normal
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { modalGraficoAbierto = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(45.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0D47A1)
                                    )
                                ) {
                                    Text(
                                        text = "Cerrar",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Snackbar flotante
                            if (snackbarVisible) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF323232)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = snackbarMensaje ?: "",
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(
                                            onClick = { snackbarVisible = false }
                                        ) {
                                            Text(
                                                text = "OK",
                                                fontSize = 14.sp,
                                                color = Color(0xFF4FC3F7),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Ocultar automáticamente después de 3 segundos
                                LaunchedEffect(snackbarVisible) {
                                    if (snackbarVisible) {
                                        delay(3000)
                                        snackbarVisible = false
                                    }
                                }
                            }
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
