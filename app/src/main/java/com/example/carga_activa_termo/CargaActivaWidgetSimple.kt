package com.example.carga_activa_termo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

class CargaActivaWidgetSimple : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_simple)

            views.setTextViewText(R.id.tv_unidad1, "U.1 = -")
            views.setTextViewText(R.id.tv_unidad2, "U.2 = -")
            views.setTextViewText(R.id.tv_unidad3, "U.3 = -")
            views.setTextViewText(R.id.tv_hora, "--:--")
            views.setTextViewText(R.id.tv_estado, "Actualizar")
            views.setInt(R.id.spinner, "setVisibility", View.GONE)
            views.setInt(R.id.circulo_estado, "setVisibility", View.INVISIBLE)

            val intentActualizar = Intent(context, CargaActivaWidgetSimple::class.java).apply {
                action = "ACTUALIZAR_WIDGET_SIMPLE"
            }
            val pendingIntentActualizar = PendingIntent.getBroadcast(
                context, appWidgetId, intentActualizar,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_simple_root, pendingIntentActualizar)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTUALIZAR_WIDGET_SIMPLE") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, CargaActivaWidgetSimple::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_simple)
                views.setTextViewText(R.id.tv_estado, "Cargando...")
                views.setInt(R.id.spinner, "setVisibility", View.VISIBLE)
                appWidgetManager.updateAppWidget(id, views)
            }

            // Usar un scope con SupervisorJob para evitar cancelaciones en Android 15
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scope.launch {
                try {
                    val esModoMock = ApiClient.isMockMode()
                    val lecturas: List<Lectura> = if (esModoMock) {
                        ApiClient.getMockData()
                    } else {
                        withTimeout(20000) {
                            ApiClient.apiService.getCargaActiva()
                        }
                    }

                    val ultima = lecturas.lastOrNull()

                    for (id in ids) {
                        val views = RemoteViews(context.packageName, R.layout.widget_simple)

                        if (ultima != null) {
                            val v1 = if (ultima.lec1 == null || ultima.lec1 <= 0) 0 else ultima.lec1.roundToInt()
                            val v2 = if (ultima.lec2 == null || ultima.lec2 <= 0) 0 else ultima.lec2.roundToInt()
                            val v3 = if (ultima.lec3 == null || ultima.lec3 <= 0) 0 else ultima.lec3.roundToInt()

                            views.setTextViewText(R.id.tv_unidad1, "U.1 = $v1")
                            views.setTextViewText(R.id.tv_unidad2, "U.2 = $v2")
                            views.setTextViewText(R.id.tv_unidad3, "U.3 = $v3")
                            views.setTextViewText(R.id.tv_hora, ultima.hora ?: "--:--")
                            views.setTextViewText(R.id.tv_estado, "Actualizar")
                            views.setImageViewResource(R.id.circulo_estado, R.drawable.circulo_azul)
                            views.setInt(R.id.circulo_estado, "setVisibility", View.VISIBLE)
                        } else {
                            views.setTextViewText(R.id.tv_unidad1, "U.1 = -")
                            views.setTextViewText(R.id.tv_unidad2, "U.2 = -")
                            views.setTextViewText(R.id.tv_unidad3, "U.3 = -")
                            views.setTextViewText(R.id.tv_hora, "--:--")
                            views.setTextViewText(R.id.tv_estado, "Sin datos")
                            views.setImageViewResource(R.id.circulo_estado, R.drawable.circulo_rojo)
                            views.setInt(R.id.circulo_estado, "setVisibility", View.VISIBLE)
                        }

                        views.setInt(R.id.spinner, "setVisibility", View.GONE)

                        val intentActualizar = Intent(context, CargaActivaWidgetSimple::class.java).apply {
                            action = "ACTUALIZAR_WIDGET_SIMPLE"
                        }
                        val pendingIntentActualizar = PendingIntent.getBroadcast(
                            context, id, intentActualizar,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_simple_root, pendingIntentActualizar)

                        appWidgetManager.updateAppWidget(id, views)
                    }
                } catch (e: Exception) {
                    val (mensajeError, colorCirculo) = when (e) {
                        is java.net.UnknownHostException -> Pair("Sin conexión", R.drawable.circulo_rojo)
                        is java.net.SocketTimeoutException -> Pair("Tiempo agotado", R.drawable.circulo_amarillo)
                        is java.net.ConnectException -> Pair("Fallo en la llamada a la API", R.drawable.circulo_verde)
                        else -> {
                            val msg = e.message ?: ""
                            when {
                                msg.contains("Unable to resolve host") -> Pair("Sin conexión", R.drawable.circulo_rojo)
                                msg.contains("timeout", ignoreCase = true) -> Pair("Tiempo agotado", R.drawable.circulo_amarillo)
                                msg.contains("500") -> Pair("Error del servidor", R.drawable.circulo_verde)
                                msg.contains("404") -> Pair("API no encontrada", R.drawable.circulo_verde)
                                else -> Pair("Otro error", R.drawable.circulo_negro)
                            }
                        }
                    }

                    for (id in ids) {
                        val views = RemoteViews(context.packageName, R.layout.widget_simple)
                        views.setTextViewText(R.id.tv_unidad1, "U.1 = -")
                        views.setTextViewText(R.id.tv_unidad2, "U.2 = -")
                        views.setTextViewText(R.id.tv_unidad3, "U.3 = -")
                        views.setTextViewText(R.id.tv_hora, "--:--")
                        views.setTextViewText(R.id.tv_estado, mensajeError)
                        views.setInt(R.id.spinner, "setVisibility", View.GONE)
                        views.setImageViewResource(R.id.circulo_estado, colorCirculo)
                        views.setInt(R.id.circulo_estado, "setVisibility", View.VISIBLE)

                        val intentActualizar = Intent(context, CargaActivaWidgetSimple::class.java).apply {
                            action = "ACTUALIZAR_WIDGET_SIMPLE"
                        }
                        val pendingIntentActualizar = PendingIntent.getBroadcast(
                            context, id, intentActualizar,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_simple_root, pendingIntentActualizar)

                        appWidgetManager.updateAppWidget(id, views)
                    }
                }
            }
        }
    }
}