package com.example.carga_activa_termo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews

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

            configurarClicks(context, views, appWidgetId)
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

            val serviceIntent = Intent(context, WidgetUpdateService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun configurarClicks(context: Context, views: RemoteViews, widgetId: Int) {
        val intentActualizar = Intent(context, CargaActivaWidgetSimple::class.java).apply {
            action = "ACTUALIZAR_WIDGET_SIMPLE"
        }
        val pendingIntentActualizar = PendingIntent.getBroadcast(
            context, widgetId, intentActualizar,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_simple_root, pendingIntentActualizar)
    }
}