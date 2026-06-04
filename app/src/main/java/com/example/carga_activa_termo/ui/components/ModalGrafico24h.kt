package com.example.carga_activa_termo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.carga_activa_termo.data.Lectura
import kotlinx.coroutines.delay

@Composable
fun ModalGrafico24h(
    onCerrar: () -> Unit,
    datos24h: List<Lectura>,
    cargandoGrafico: Boolean,
    graficoUnidadEspecifica: Boolean,
    unidadSeleccionada: Int,
    mostrarUnidad1: Boolean,
    mostrarUnidad2: Boolean,
    mostrarUnidad3: Boolean,
    snackbarMensaje: String?,
    snackbarVisible: Boolean,
    onMostrarUnidad1Change: (Boolean) -> Unit,
    onMostrarUnidad2Change: (Boolean) -> Unit,
    onMostrarUnidad3Change: (Boolean) -> Unit,
    onSnackbarMensajeChange: (String?) -> Unit,
    onSnackbarVisibleChange: (Boolean) -> Unit,
    onAtras: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onCerrar() },
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
            // Box principal que contiene todo
            Box(
                modifier = Modifier.fillMaxSize()
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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
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
                                            .padding(vertical = 9.dp)
                                    )
                                }
                            }

                            // Scroll horizontal con el gráfico
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                GraficoLineas(
                                    datos24h = datos24h,
                                    anchoGrafico = anchoGrafico,
                                    mostrarUnidad1 = mostrarUnidad1,
                                    mostrarUnidad2 = mostrarUnidad2,
                                    mostrarUnidad3 = mostrarUnidad3,
                                    graficoUnidadEspecifica = graficoUnidadEspecifica,
                                    unidadSeleccionada = unidadSeleccionada,
                                    onSnackbarMensajeChange = onSnackbarMensajeChange,
                                    onSnackbarVisibleChange = onSnackbarVisibleChange
                                )
                            }
                        }
                    }

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
                                            onMostrarUnidad1Change(!mostrarUnidad1)
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
                                            onMostrarUnidad2Change(!mostrarUnidad2)
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
                                            onMostrarUnidad3Change(!mostrarUnidad3)
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

                    // Fila de botones Atrás y Cerrar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onAtras != null) {
                            Button(
                                onClick = { onAtras() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(45.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF1565C0
                                    )
                                )
                            ) {
                                Text(
                                    text = "Atrás",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Button(
                            onClick = { onCerrar() },
                            modifier = Modifier
                                .weight(1f)
                                .height(45.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                        ) {
                            Text(
                                text = "Cerrar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Snackbar flotante en la parte inferior
                if (snackbarVisible) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                onClick = { onSnackbarVisibleChange(false) }
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
                            onSnackbarVisibleChange(false)
                        }
                    }
                }
            }
        }
    }
}