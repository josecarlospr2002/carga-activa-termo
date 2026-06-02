package com.example.carga_activa_termo.ui

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
import com.example.carga_activa_termo.R
import com.example.carga_activa_termo.data.ApiClient
import com.example.carga_activa_termo.data.Lectura
import com.example.carga_activa_termo.utils.obtenerMensajeError
import com.example.carga_activa_termo.ui.components.TarjetaUnidad
import com.example.carga_activa_termo.ui.components.ModalUnidad
import com.example.carga_activa_termo.ui.components.ModalGrafico24h
import com.example.carga_activa_termo.ui.components.ModalTabla24h
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

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
    var modalTablaAbierto by remember { mutableStateOf(false) }
    var cargandoTabla by remember { mutableStateOf(false) }
    var tablaUnidadEspecifica by remember { mutableStateOf<Int?>(null) }

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

            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(18.dp))

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
                        Text(text = "⚠️", fontSize = 40.sp)
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
                        onClick = { unidadSeleccionada = 1; modalAbierto = true })
                    Spacer(modifier = Modifier.height(16.dp))
                    TarjetaUnidad(
                        numeroUnidad = 2,
                        valor = ultimaLectura.lec2,
                        onClick = { unidadSeleccionada = 2; modalAbierto = true })
                    Spacer(modifier = Modifier.height(16.dp))
                    TarjetaUnidad(
                        numeroUnidad = 3,
                        valor = ultimaLectura.lec3,
                        onClick = { unidadSeleccionada = 3; modalAbierto = true })
                    Spacer(modifier = Modifier.height(20.dp))

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
                                mostrarUnidad1 = true; mostrarUnidad2 = true; mostrarUnidad3 = true
                                graficoUnidadEspecifica = false; cargandoGrafico =
                                true; modalGraficoAbierto = true
                                coroutineScope.launch {
                                    try {
                                        datos24h =
                                            if (esModoMock) ApiClient.getMockData24h() else ApiClient.apiService.getCargaActiva24h()
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                        ) {
                            Text(
                                text = "Gráfico Últimas 24 Horas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                tablaUnidadEspecifica = null; cargandoTabla =
                                true; modalTablaAbierto = true
                                coroutineScope.launch {
                                    try {
                                        datos24h =
                                            if (esModoMock) ApiClient.getMockData24h() else ApiClient.apiService.getCargaActiva24h()
                                    } catch (e: Exception) {
                                        datos24h = emptyList()
                                    } finally {
                                        cargandoTabla = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text(
                                text = "Tabla Últimas 24 Horas",
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color.White,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Cargando datos...", fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Modal de unidad
        if (modalAbierto) {
            val ultimaLectura = lecturas.lastOrNull()
            ModalUnidad(
                unidadSeleccionada = unidadSeleccionada,
                ultimaLectura = ultimaLectura,
                onCerrar = { modalAbierto = false },
                onVerGrafico = {
                    mostrarUnidad1 = true; mostrarUnidad2 = true; mostrarUnidad3 = true
                    graficoUnidadEspecifica = true; cargandoGrafico = true; modalGraficoAbierto =
                    true
                    coroutineScope.launch {
                        try {
                            datos24h =
                                if (esModoMock) ApiClient.getMockData24h() else ApiClient.apiService.getCargaActiva24h()
                        } catch (e: Exception) {
                            datos24h = emptyList()
                        } finally {
                            cargandoGrafico = false
                        }
                    }
                },
                onVerTabla = {
                    tablaUnidadEspecifica = unidadSeleccionada; cargandoTabla =
                    true; modalTablaAbierto = true
                    coroutineScope.launch {
                        try {
                            datos24h =
                                if (esModoMock) ApiClient.getMockData24h() else ApiClient.apiService.getCargaActiva24h()
                        } catch (e: Exception) {
                            datos24h = emptyList()
                        } finally {
                            cargandoTabla = false
                        }
                    }
                }
            )
        }

        // Modal Gráfico 24 Horas
        if (modalGraficoAbierto) {
            ModalGrafico24h(
                onCerrar = { modalGraficoAbierto = false },
                datos24h = datos24h,
                cargandoGrafico = cargandoGrafico,
                graficoUnidadEspecifica = graficoUnidadEspecifica,
                unidadSeleccionada = unidadSeleccionada,
                mostrarUnidad1 = mostrarUnidad1,
                mostrarUnidad2 = mostrarUnidad2,
                mostrarUnidad3 = mostrarUnidad3,
                snackbarMensaje = snackbarMensaje,
                snackbarVisible = snackbarVisible,
                onMostrarUnidad1Change = { mostrarUnidad1 = it },
                onMostrarUnidad2Change = { mostrarUnidad2 = it },
                onMostrarUnidad3Change = { mostrarUnidad3 = it },
                onSnackbarMensajeChange = { snackbarMensaje = it },
                onSnackbarVisibleChange = { snackbarVisible = it },
                onAtras = if (graficoUnidadEspecifica) {
                    { modalGraficoAbierto = false; modalAbierto = true }
                } else null
            )
        }

        // Modal Tabla 24 Horas
        if (modalTablaAbierto) {
            ModalTabla24h(
                onCerrar = { modalTablaAbierto = false },
                datos24h = datos24h,
                cargando = cargandoTabla,
                unidadEspecifica = tablaUnidadEspecifica,
                onAtras = if (tablaUnidadEspecifica != null) {
                    { modalTablaAbierto = false; modalAbierto = true }
                } else null
            )
        }
    }
}