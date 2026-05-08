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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class CargaActivaWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Valores iniciales
            views.setTextViewText(R.id.tv_carga1, "-")
            views.setTextViewText(R.id.tv_carga2, "-")
            views.setTextViewText(R.id.tv_carga3, "-")
            views.setTextViewText(R.id.tv_hora, "--:--")
            views.setTextViewText(R.id.tv_estado, "Actualizar")
            views.setTextViewText(R.id.icono_actualizar, "⟳")
            views.setInt(R.id.spinner, "setVisibility", View.GONE)

            // Click en el widget para abrir la App principal
            val intentApp = Intent(context, MainActivity::class.java)
            val pendingIntentApp = PendingIntent.getActivity(
                context, 0, intentApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntentApp)

            // Click en botón actualizar para actualizar los valores de este
            val intentActualizar = Intent(context, CargaActivaWidget::class.java).apply {
                action = "ACTUALIZAR_WIDGET"
            }
            val pendingIntentActualizar = PendingIntent.getBroadcast(
                context, 1, intentActualizar,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_actualizar, pendingIntentActualizar)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTUALIZAR_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, CargaActivaWidget::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)

            // Mostrar "Actualizando" y spinner, ocultar ícono
            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.tv_estado, "Actualizando")
                views.setTextViewText(R.id.icono_actualizar, "")
                views.setInt(R.id.spinner, "setVisibility", View.VISIBLE)
                appWidgetManager.updateAppWidget(id, views)
            }

            // Cargar datos de la API
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val esModoMock = ApiClient.isMockMode()
                    val lecturas: List<Lectura> = if (esModoMock) {
                        ApiClient.getMockData()
                    } else {
                        ApiClient.apiService.getCargaActiva()
                    }

                    val ultima = lecturas.lastOrNull()

                    for (id in ids) {
                        val views = RemoteViews(context.packageName, R.layout.widget_layout)

                        if (ultima != null) {
                            views.setTextViewText(
                                R.id.tv_carga1,
                                ultima.lec1?.roundToInt()?.toString() ?: "-"
                            )
                            views.setTextViewText(
                                R.id.tv_carga2,
                                ultima.lec2?.roundToInt()?.toString() ?: "-"
                            )
                            views.setTextViewText(
                                R.id.tv_carga3,
                                ultima.lec3?.roundToInt()?.toString() ?: "-"
                            )
                            views.setTextViewText(
                                R.id.tv_hora,
                                ultima.hora ?: "--:--"
                            )
                            views.setTextViewText(R.id.tv_estado, "Actualizar")
                            views.setInt(R.id.spinner, "setVisibility", View.GONE)
                            views.setTextViewText(R.id.icono_actualizar, "⟳")
                        } else {
                            views.setTextViewText(R.id.tv_estado, "Sin Conexión")
                            views.setInt(R.id.spinner, "setVisibility", View.GONE)
                            views.setTextViewText(R.id.icono_actualizar, "⟳")
                        }

                        // Reconfigurar clics
                        val intentActualizar = Intent(context, CargaActivaWidget::class.java).apply {
                            action = "ACTUALIZAR_WIDGET"
                        }
                        val pendingIntentActualizar = PendingIntent.getBroadcast(
                            context, 1, intentActualizar,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.btn_actualizar, pendingIntentActualizar)

                        val intentApp = Intent(context, MainActivity::class.java)
                        val pendingIntentApp = PendingIntent.getActivity(
                            context, 0, intentApp,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_root, pendingIntentApp)

                        appWidgetManager.updateAppWidget(id, views)
                    }
                } catch (e: Exception) {
                    for (id in ids) {
                        val views = RemoteViews(context.packageName, R.layout.widget_layout)
                        views.setTextViewText(R.id.tv_estado, "Sin Conexión")
                        views.setInt(R.id.spinner, "setVisibility", View.GONE)
                        views.setTextViewText(R.id.icono_actualizar, "⟳")

                        val intentActualizar = Intent(context, CargaActivaWidget::class.java).apply {
                            action = "ACTUALIZAR_WIDGET"
                        }
                        val pendingIntentActualizar = PendingIntent.getBroadcast(
                            context, 1, intentActualizar,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.btn_actualizar, pendingIntentActualizar)

                        appWidgetManager.updateAppWidget(id, views)
                    }
                }
            }
        }
    }
}