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
            views.setTextViewText(R.id.tv_unidad1, "Un.1: -")
            views.setTextViewText(R.id.tv_unidad2, "Un.2: -")
            views.setTextViewText(R.id.tv_unidad3, "Un.3: -")
            views.setTextViewText(R.id.tv_hora, "--:--")
            views.setTextViewText(R.id.tv_estado, "Actualizar")
            views.setTextViewText(R.id.icono_actualizar, "⟳")
            views.setInt(R.id.icono_actualizar, "setVisibility", View.VISIBLE)
            views.setInt(R.id.spinner_icono, "setVisibility", View.GONE)
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
            val componentName =
                android.content.ComponentName(context, CargaActivaWidgetSimple::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)

            for (id in ids) {
                val views = RemoteViews(context.packageName, R.layout.widget_simple)
                views.setInt(R.id.icono_actualizar, "setVisibility", View.GONE)
                views.setInt(R.id.spinner_icono, "setVisibility", View.VISIBLE)
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
        // Click en la imagen del Che → abre la app
        val intentApp = Intent(context, MainActivity::class.java)
        val pendingIntentApp = PendingIntent.getActivity(
            context, widgetId, intentApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.iv_logo, pendingIntentApp)

        // Click en el resto del widget → actualiza
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