package com.example.carga_activa_termo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

class CargaActivaWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setTextViewText(R.id.tv_carga1, "-")
            views.setTextViewText(R.id.tv_carga2, "-")
            views.setTextViewText(R.id.tv_carga3, "-")
            views.setTextViewText(R.id.tv_hora, "--:--")
            views.setTextViewText(R.id.tv_estado, "Actualizar")
            views.setTextViewText(R.id.icono_actualizar, "⟳")
            views.setInt(R.id.spinner, "setVisibility", View.GONE)

            configurarClicks(context, views, appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTUALIZAR_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, CargaActivaWidget::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)
                views.setTextViewText(R.id.tv_estado, "Actualizando...")
                views.setTextViewText(R.id.icono_actualizar, "")
                views.setInt(R.id.spinner, "setVisibility", View.VISIBLE)
                appWidgetManager.updateAppWidget(id, views)
            }

            val serviceIntent = Intent(context, WidgetUpdateService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun configurarClicks(context: Context, views: RemoteViews, widgetId: Int) {
        // Zona superior → Abre la app
        val intentApp = Intent(context, MainActivity::class.java)
        val pendingIntentApp = PendingIntent.getActivity(
            context, widgetId, intentApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.zona_abrir_app, pendingIntentApp)

        // Zona inferior completa → Actualizar widget
        val intentActualizar = Intent(context, CargaActivaWidget::class.java).apply {
            action = "ACTUALIZAR_WIDGET"
        }
        val pendingIntentActualizar = PendingIntent.getBroadcast(
            context, widgetId, intentActualizar,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_actualizar_zona, pendingIntentActualizar)
    }
}