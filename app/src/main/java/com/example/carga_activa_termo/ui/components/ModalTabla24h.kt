package com.example.carga_activa_termo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlin.math.roundToInt

@Composable
fun ModalTabla24h(
    onCerrar: () -> Unit,
    datos24h: List<Lectura>,
    cargando: Boolean
) {
    var ordenInvertido by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.85f)
                .padding(horizontal = 4.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Marcajes Últimas 24 Horas",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.dp)
                        .background(Color(0xFF1976D2))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (cargando) {
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
                    // Preparar datos según orden
                    val datosMostrar = if (ordenInvertido) {
                        datos24h
                    } else {
                        datos24h.reversed()
                    }

                    // Calcular valores redondeados para cada unidad
                    val valoresRedondeadosU1 = datos24h.mapNotNull {
                        if (it.lec1 == null || it.lec1 < 0) null else it.lec1.roundToInt()
                    }
                    val valoresRedondeadosU2 = datos24h.mapNotNull {
                        if (it.lec2 == null || it.lec2 < 0) null else it.lec2.roundToInt()
                    }
                    val valoresRedondeadosU3 = datos24h.mapNotNull {
                        if (it.lec3 == null || it.lec3 < 0) null else it.lec3.roundToInt()
                    }

                    // Calcular máximos y mínimos con valores redondeados
                    val maxU1 = if (valoresRedondeadosU1.isNotEmpty()) valoresRedondeadosU1.max() else null
                    val minU1 = if (valoresRedondeadosU1.isNotEmpty()) valoresRedondeadosU1.min() else null
                    val maxU2 = if (valoresRedondeadosU2.isNotEmpty()) valoresRedondeadosU2.max() else null
                    val minU2 = if (valoresRedondeadosU2.isNotEmpty()) valoresRedondeadosU2.min() else null
                    val maxU3 = if (valoresRedondeadosU3.isNotEmpty()) valoresRedondeadosU3.max() else null
                    val minU3 = if (valoresRedondeadosU3.isNotEmpty()) valoresRedondeadosU3.min() else null

                    // Verificar si son todos iguales (constante) con valores redondeados
                    val esConstanteU1 = maxU1 != null && minU1 != null && maxU1 == minU1
                    val esConstanteU2 = maxU2 != null && minU2 != null && maxU2 == minU2
                    val esConstanteU3 = maxU3 != null && minU3 != null && maxU3 == minU3

                    // Tabla
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Encabezado clickeable
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF0D47A1),
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
                                .clickable { ordenInvertido = !ordenInvertido }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Hora ${if (ordenInvertido) "▲" else "▼"}",
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "U1",
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "U2",
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "U3",
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Filas de datos
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(datosMostrar) { lectura ->
                                val colorFondo = if (datosMostrar.indexOf(lectura) % 2 == 0) {
                                    Color(0xFFF5F5F5)
                                } else {
                                    Color.White
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colorFondo)
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = lectura.hora ?: "--:--",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        color = Color(0xFF424242),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = formatearValor(lectura.lec1),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = obtenerColorValorRedondeado(
                                            valor = lectura.lec1,
                                            maxRedondeado = maxU1,
                                            minRedondeado = minU1,
                                            esConstante = esConstanteU1
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = formatearValor(lectura.lec2),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = obtenerColorValorRedondeado(
                                            valor = lectura.lec2,
                                            maxRedondeado = maxU2,
                                            minRedondeado = minU2,
                                            esConstante = esConstanteU2
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = formatearValor(lectura.lec3),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = obtenerColorValorRedondeado(
                                            valor = lectura.lec3,
                                            maxRedondeado = maxU3,
                                            minRedondeado = minU3,
                                            esConstante = esConstanteU3
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Leyenda
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Valor más Alto
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = Color(0xFF1565C0),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Valor más Alto",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }

                    // Valor más Bajo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = Color(0xFFC62828),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Valor más Bajo",
                            fontSize = 13.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Cerrar
                Button(
                    onClick = { onCerrar() },
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
            }
        }
    }
}

// Función auxiliar para formatear valores
fun formatearValor(valor: Double?): String {
    return when {
        valor == null -> "-"
        valor <= 0 -> "0"
        else -> valor.roundToInt().toString()
    }
}

// Función para obtener el color según el valor redondeado
fun obtenerColorValorRedondeado(
    valor: Double?,
    maxRedondeado: Int?,
    minRedondeado: Int?,
    esConstante: Boolean
): Color {
    // Si es null, color normal
    if (valor == null) return Color(0xFF424242)

    // Si no hay máximos/mínimos, color normal
    if (maxRedondeado == null || minRedondeado == null) return Color(0xFF424242)

    // Si es constante (todos iguales), color normal
    if (esConstante) return Color(0xFF424242)

    // Obtener el valor redondeado (si es negativo, se convierte a 0)
    val valorRedondeado = if (valor <= 0) 0 else valor.roundToInt()

    // Si el valor redondeado es igual al máximo redondeado, azul
    if (valorRedondeado == maxRedondeado) return Color(0xFF1565C0)

    // Si el valor redondeado es igual al mínimo redondeado, rojo
    if (valorRedondeado == minRedondeado) return Color(0xFFC62828)

    // Valor intermedio, color normal
    return Color(0xFF424242)
}