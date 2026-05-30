package com.example.carga_activa_termo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import com.example.carga_activa_termo.data.ApiClient
import com.example.carga_activa_termo.data.Lectura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class WidgetUpdateService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        crearCanalNotificacion()
        val notification = crearNotificacion()
        startForeground(1001, notification)

        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val esModoMock = ApiClient.isMockMode()
                val lecturas: List<Lectura> = if (esModoMock) {
                    ApiClient.getMockData()
                } else {
                    ApiClient.apiService.getCargaActiva()
                }
                val ultima = lecturas.lastOrNull()
                withContext(Dispatchers.Main) {
                    actualizarWidgets(ultima)
                    detenerServicio()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    actualizarWidgetsConError(e.message ?: "Error desconocido")
                    detenerServicio()
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun actualizarWidgets(ultima: Lectura?) {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        val componentePrincipal = ComponentName(this, CargaActivaWidget::class.java)
        val idsPrincipal = appWidgetManager.getAppWidgetIds(componentePrincipal)
        for (id in idsPrincipal) {
            val views = RemoteViews(packageName, R.layout.widget_layout)
            if (ultima != null) {
                views.setTextViewText(R.id.tv_carga1, ultima.lec1?.roundToInt()?.toString() ?: "-")
                views.setTextViewText(R.id.tv_carga2, ultima.lec2?.roundToInt()?.toString() ?: "-")
                views.setTextViewText(R.id.tv_carga3, ultima.lec3?.roundToInt()?.toString() ?: "-")
                views.setTextViewText(R.id.tv_hora, ultima.hora ?: "--:--")
                views.setTextViewText(R.id.tv_estado, "Actualizar")
                views.setTextViewText(R.id.icono_actualizar, "⟳")
                views.setInt(R.id.spinner, "setVisibility", View.GONE)
            } else {
                views.setTextViewText(R.id.tv_estado, "Sin datos")
                views.setTextViewText(R.id.icono_actualizar, "⟳")
                views.setInt(R.id.spinner, "setVisibility", View.GONE)
            }
            configurarClicksPrincipal(views, id)
            appWidgetManager.updateAppWidget(id, views)
        }

        val componenteSimple = ComponentName(this, CargaActivaWidgetSimple::class.java)
        val idsSimple = appWidgetManager.getAppWidgetIds(componenteSimple)
        for (id in idsSimple) {
            val views = RemoteViews(packageName, R.layout.widget_simple)
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
            configurarClicksSimple(views, id)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun actualizarWidgetsConError(mensaje: String) {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        val componentePrincipal = ComponentName(this, CargaActivaWidget::class.java)
        val idsPrincipal = appWidgetManager.getAppWidgetIds(componentePrincipal)
        for (id in idsPrincipal) {
            val views = RemoteViews(packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.tv_estado, "Sin conexión")
            views.setTextViewText(R.id.icono_actualizar, "⟳")
            views.setInt(R.id.spinner, "setVisibility", View.GONE)
            configurarClicksPrincipal(views, id)
            appWidgetManager.updateAppWidget(id, views)
        }

        val componenteSimple = ComponentName(this, CargaActivaWidgetSimple::class.java)
        val idsSimple = appWidgetManager.getAppWidgetIds(componenteSimple)
        for (id in idsSimple) {
            val views = RemoteViews(packageName, R.layout.widget_simple)
            views.setTextViewText(R.id.tv_unidad1, "U.1 = -")
            views.setTextViewText(R.id.tv_unidad2, "U.2 = -")
            views.setTextViewText(R.id.tv_unidad3, "U.3 = -")
            views.setTextViewText(R.id.tv_hora, "--:--")
            views.setTextViewText(R.id.tv_estado, "Error")
            views.setImageViewResource(R.id.circulo_estado, R.drawable.circulo_negro)
            views.setInt(R.id.circulo_estado, "setVisibility", View.VISIBLE)
            views.setInt(R.id.spinner, "setVisibility", View.GONE)
            configurarClicksSimple(views, id)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun configurarClicksPrincipal(views: RemoteViews, widgetId: Int) {
        val intentActualizar = Intent(this, CargaActivaWidget::class.java).apply {
            action = "ACTUALIZAR_WIDGET"
        }
        val pendingIntentActualizar = android.app.PendingIntent.getBroadcast(
            this, widgetId, intentActualizar,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_actualizar, pendingIntentActualizar)

        val intentApp = Intent(this, MainActivity::class.java)
        val pendingIntentApp = android.app.PendingIntent.getActivity(
            this, widgetId, intentApp,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntentApp)
    }

    private fun configurarClicksSimple(views: RemoteViews, widgetId: Int) {
        val intentActualizar = Intent(this, CargaActivaWidgetSimple::class.java).apply {
            action = "ACTUALIZAR_WIDGET_SIMPLE"
        }
        val pendingIntentActualizar = android.app.PendingIntent.getBroadcast(
            this, widgetId, intentActualizar,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_simple_root, pendingIntentActualizar)
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "widget_update_channel",
                "Actualización de Widget",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para actualizar datos del widget"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun crearNotificacion(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "widget_update_channel")
                .setContentTitle("Actualizando datos")
                .setContentText("Obteniendo carga activa...")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Actualizando datos")
                .setContentText("Obteniendo carga activa...")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setOngoing(true)
                .build()
        }
    }

    private fun detenerServicio() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}