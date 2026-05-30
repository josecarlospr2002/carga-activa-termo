package com.example.carga_activa_termo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carga_activa_termo.data.Lectura
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun GraficoLineas(
    datos24h: List<Lectura>,
    anchoGrafico: Dp,
    mostrarUnidad1: Boolean,
    mostrarUnidad2: Boolean,
    mostrarUnidad3: Boolean,
    graficoUnidadEspecifica: Boolean,
    unidadSeleccionada: Int,
    onSnackbarMensajeChange: (String?) -> Unit,
    onSnackbarVisibleChange: (Boolean) -> Unit
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
            .pointerInput(
                datos24h,
                mostrarUnidad1,
                mostrarUnidad2,
                mostrarUnidad3,
                graficoUnidadEspecifica,
                unidadSeleccionada
            ) {
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

                    // Calcular distancias para unidades visibles
                    var mejorUnidad = ""
                    var mejorDist = Float.MAX_VALUE

                    var dist1 = Float.MAX_VALUE
                    var dist2 = Float.MAX_VALUE
                    var dist3 = Float.MAX_VALUE
                    var y1 = 0f
                    var y2 = 0f
                    var y3 = 0f

                    // Solo calcular Unidad 1 si está visible
                    if (mostrarUnidad1 || (graficoUnidadEspecifica && unidadSeleccionada == 1)) {
                        val v1 = valoresUnidad1[indice]
                        val prop1 = (v1 / 100.0).toFloat()
                        y1 = altoPx - (prop1 * altoPx)
                        dist1 = abs(tapOffset.y - y1)
                        if (dist1 < mejorDist) {
                            mejorDist = dist1
                            mejorUnidad = "1"
                        }
                    }

                    // Solo calcular Unidad 2 si está visible
                    if (mostrarUnidad2 || (graficoUnidadEspecifica && unidadSeleccionada == 2)) {
                        val v2 = valoresUnidad2[indice]
                        val prop2 = (v2 / 100.0).toFloat()
                        y2 = altoPx - (prop2 * altoPx)
                        dist2 = abs(tapOffset.y - y2)
                        if (dist2 < mejorDist) {
                            mejorDist = dist2
                            mejorUnidad = "2"
                        }
                    }

                    // Solo calcular Unidad 3 si está visible
                    if (mostrarUnidad3 || (graficoUnidadEspecifica && unidadSeleccionada == 3)) {
                        val v3 = valoresUnidad3[indice]
                        val prop3 = (v3 / 100.0).toFloat()
                        y3 = altoPx - (prop3 * altoPx)
                        dist3 = abs(tapOffset.y - y3)
                        if (dist3 < mejorDist) {
                            mejorDist = dist3
                            mejorUnidad = "3"
                        }
                    }

                    // Área de toque de los puntos en el gráfico
                    if (mejorDist < 65f && mejorUnidad.isNotEmpty()) {
                        val hora = datos24h.getOrNull(indice)?.hora ?: "--:--"

                        if (!graficoUnidadEspecifica) {
                            val v1 =
                                if (valoresUnidad1[indice] <= 0) 0 else valoresUnidad1[indice].roundToInt()
                            val v2 =
                                if (valoresUnidad2[indice] <= 0) 0 else valoresUnidad2[indice].roundToInt()
                            val v3 =
                                if (valoresUnidad3[indice] <= 0) 0 else valoresUnidad3[indice].roundToInt()

                            // Solo agrupar unidades visibles
                            val grupos = mutableMapOf<Int, MutableList<String>>()

                            if (mostrarUnidad1) {
                                grupos.getOrPut(v1) { mutableListOf() }.add("Unidad 1")
                            }
                            if (mostrarUnidad2) {
                                grupos.getOrPut(v2) { mutableListOf() }.add("Unidad 2")
                            }
                            if (mostrarUnidad3) {
                                grupos.getOrPut(v3) { mutableListOf() }.add("Unidad 3")
                            }

                            // Determinar cuál es la unidad visible más cercana al toque
                            val unidadMasCercana = mejorUnidad

                            // Encontrar el valor de la unidad más cercana
                            val valorTocado = when (unidadMasCercana) {
                                "1" -> v1
                                "2" -> v2
                                "3" -> v3
                                else -> 0
                            }

                            // Verificar si hay otras unidades visibles con el mismo valor
                            val unidadesConMismoValor =
                                grupos[valorTocado] ?: listOf("Unidad $unidadMasCercana")

                            val mensaje = if (unidadesConMismoValor.size > 1) {
                                "${unidadesConMismoValor.joinToString(", ") { "$it = $valorTocado" }} a las $hora"
                            } else {
                                "${unidadesConMismoValor.first()} = $valorTocado a las $hora"
                            }

                            onSnackbarMensajeChange(mensaje)
                            onSnackbarVisibleChange(true)

                        } else {
                            // Modo unidad específica
                            val valorSeleccionado = when (unidadSeleccionada) {
                                1 -> if (valoresUnidad1[indice] <= 0) 0 else valoresUnidad1[indice].roundToInt()
                                2 -> if (valoresUnidad2[indice] <= 0) 0 else valoresUnidad2[indice].roundToInt()
                                else -> if (valoresUnidad3[indice] <= 0) 0 else valoresUnidad3[indice].roundToInt()
                            }

                            val ySeleccionada = when (unidadSeleccionada) {
                                1 -> y1
                                2 -> y2
                                else -> y3
                            }

                            if (abs(tapOffset.y - ySeleccionada) < 65f) {
                                val mensaje =
                                    "Unidad $unidadSeleccionada = $valorSeleccionado a las $hora"
                                onSnackbarMensajeChange(mensaje)
                                onSnackbarVisibleChange(true)
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
                val x1 = (i.toFloat() / (cantidadPuntos - 1)) * ancho
                val proporcion1 = ((valores[i] - minValor) / rango).toFloat()
                val y1 = alto - (proporcion1 * alto)

                val x2 = ((i + 1).toFloat() / (cantidadPuntos - 1)) * ancho
                val proporcion2 = ((valores[i + 1] - minValor) / rango).toFloat()
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
                val x = (index.toFloat() / (cantidadPuntos - 1)) * ancho
                val proporcion = ((valor - minValor) / rango).toFloat()
                val y = alto - (proporcion * alto)

                drawCircle(
                    color = Color.White,
                    radius = 16f,  // antes era 14f
                    center = Offset(x, y)
                )
                drawCircle(
                    color = color,
                    radius = 12f,  // antes era 11f
                    center = Offset(x, y)
                )
            }
        }

        for (i in 0..10) {
            val valor = i * 10.0
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
            val colorLinea = if (i == 0) Color(0xFF000000) else Color(0xFF737373)
            val grosorLinea = if (i == 0) 2f else 0.5f
            drawLine(
                color = colorLinea,
                start = Offset(x, 0f),
                end = Offset(x, alto),
                strokeWidth = grosorLinea
            )
        }

        // Dibujar líneas de datos
        if (mostrarUnidad3 && (!graficoUnidadEspecifica || unidadSeleccionada == 3)) {
            dibujarLinea(valores3, Color(0xFFFF5722), 3f)
        }
        if (mostrarUnidad2 && (!graficoUnidadEspecifica || unidadSeleccionada == 2)) {
            dibujarLinea(valores2, Color(0xFF388E3C), 3f)
        }
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