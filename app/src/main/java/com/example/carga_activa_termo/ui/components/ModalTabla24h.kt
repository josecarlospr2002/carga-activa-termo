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
    cargando: Boolean,
    unidadEspecifica: Int? = null,
    onAtras: (() -> Unit)? = null
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
                // Título
                Text(
                    text = if (unidadEspecifica != null)
                        "Marcaje de la Unidad $unidadEspecifica en las Últimas 24 Horas"
                    else
                        "Marcajes Últimas 24 Horas",
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
                    val datosMostrar = if (ordenInvertido) datos24h else datos24h.reversed()

                    // Función para obtener valores redondeados de una unidad
                    fun valoresRedondeados(unidad: Int): List<Int> {
                        return datos24h.mapNotNull {
                            val valor = when (unidad) {
                                1 -> it.lec1; 2 -> it.lec2; else -> it.lec3
                            }
                            if (valor == null) null else if (valor <= 0) 0 else valor.roundToInt()
                        }
                    }

                    // Calcular para unidad específica o todas
                    val vrU1 =
                        if (unidadEspecifica == null || unidadEspecifica == 1) valoresRedondeados(1) else emptyList()
                    val vrU2 =
                        if (unidadEspecifica == null || unidadEspecifica == 2) valoresRedondeados(2) else emptyList()
                    val vrU3 =
                        if (unidadEspecifica == null || unidadEspecifica == 3) valoresRedondeados(3) else emptyList()

                    val maxU1 = if (vrU1.isNotEmpty()) vrU1.max() else null
                    val minU1 = if (vrU1.isNotEmpty()) vrU1.min() else null
                    val maxU2 = if (vrU2.isNotEmpty()) vrU2.max() else null
                    val minU2 = if (vrU2.isNotEmpty()) vrU2.min() else null
                    val maxU3 = if (vrU3.isNotEmpty()) vrU3.max() else null
                    val minU3 = if (vrU3.isNotEmpty()) vrU3.min() else null

                    val esConstanteU1 = maxU1 != null && minU1 != null && maxU1 == minU1
                    val esConstanteU2 = maxU2 != null && minU2 != null && maxU2 == minU2
                    val esConstanteU3 = maxU3 != null && minU3 != null && maxU3 == minU3

                    val promU1 = if (vrU1.isNotEmpty()) vrU1.average().roundToInt() else null
                    val promU2 = if (vrU2.isNotEmpty()) vrU2.average().roundToInt() else null
                    val promU3 = if (vrU3.isNotEmpty()) vrU3.average().roundToInt() else null

                    // Fila de promedio
                    @Composable
                    fun FilaPromedio() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEDE7F6))
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Prom",
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2),
                                textAlign = TextAlign.Center
                            )
                            if (unidadEspecifica == null || unidadEspecifica == 1) {
                                Text(
                                    text = if (promU1 != null) promU1.toString() else "-",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2),
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (unidadEspecifica == null || unidadEspecifica == 2) {
                                Text(
                                    text = if (promU2 != null) promU2.toString() else "-",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2),
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (unidadEspecifica == null || unidadEspecifica == 3) {
                                Text(
                                    text = if (promU3 != null) promU3.toString() else "-",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        // Encabezado
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
                            if (unidadEspecifica == null || unidadEspecifica == 1)
                                Text(
                                    text = "U1",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            if (unidadEspecifica == null || unidadEspecifica == 2)
                                Text(
                                    text = "U2",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            if (unidadEspecifica == null || unidadEspecifica == 3)
                                Text(
                                    text = "U3",
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                        }

                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            if (ordenInvertido) item { FilaPromedio() }

                            items(datosMostrar) { lectura ->
                                val idx = datosMostrar.indexOf(lectura)
                                val colorFondo =
                                    if (idx % 2 == 0) Color(0xFFF5F5F5) else Color.White

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
                                    if (unidadEspecifica == null || unidadEspecifica == 1)
                                        Text(
                                            text = formatearValor(lectura.lec1),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = obtenerColorValorRedondeado(
                                                lectura.lec1,
                                                maxU1,
                                                minU1,
                                                esConstanteU1
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    if (unidadEspecifica == null || unidadEspecifica == 2)
                                        Text(
                                            text = formatearValor(lectura.lec2),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = obtenerColorValorRedondeado(
                                                lectura.lec2,
                                                maxU2,
                                                minU2,
                                                esConstanteU2
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    if (unidadEspecifica == null || unidadEspecifica == 3)
                                        Text(
                                            text = formatearValor(lectura.lec3),
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = obtenerColorValorRedondeado(
                                                lectura.lec3,
                                                maxU3,
                                                minU3,
                                                esConstanteU3
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                }
                            }

                            if (!ordenInvertido) item { FilaPromedio() }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = Color(0xFF1565C0),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "V. más Alto", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = Color(0xFFC62828),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "V. más Bajo", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = Color(0xFF7B1FA2),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Promedio", fontSize = 12.sp, color = Color(0xFF757575))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
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
            }
        }
    }
}

fun formatearValor(valor: Double?): String {
    return when {
        valor == null -> "-"; valor <= 0 -> "0"; else -> valor.roundToInt().toString()
    }
}

// Función para obtener el color según el valor redondeado
fun obtenerColorValorRedondeado(
    valor: Double?,
    maxRedondeado: Int?,
    minRedondeado: Int?,
    esConstante: Boolean
): Color {
    if (valor == null) return Color(0xFF424242)    // Si es null, color normal

    if (maxRedondeado == null || minRedondeado == null) return Color(0xFF424242)    // Si no hay máximos/mínimos, color normal

    if (esConstante) return Color(0xFF424242)    // Si es constante (todos iguales), color normal

    val valorRedondeado =
        if (valor <= 0) 0 else valor.roundToInt()    // Obtener el valor redondeado (si es negativo, se convierte a 0)

    if (valorRedondeado == maxRedondeado) return Color(0xFF1565C0)    // Si el valor redondeado es igual al máximo redondeado, azul

    if (valorRedondeado == minRedondeado) return Color(0xFFC62828)    // Si el valor redondeado es igual al mínimo redondeado, rojo

    return Color(0xFF424242)    // Valor intermedio, color normal
}