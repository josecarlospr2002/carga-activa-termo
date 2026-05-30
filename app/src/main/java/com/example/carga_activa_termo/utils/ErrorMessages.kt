package com.example.carga_activa_termo.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun obtenerMensajeError(exception: Exception): String {
    return when (exception) {
        is UnknownHostException ->
            "No se puede conectar al servidor.\nVerifica que estés conectado a la red de la empresa."

        is SocketTimeoutException ->
            "El servidor está tardando mucho en responder.\nIntenta de nuevo en unos momentos."

        is ConnectException ->
            "No hay conexión a Internet.\nRevisa tu WiFi o datos móviles."

        else -> {
            val mensaje = exception.message ?: "Error desconocido"
            if (mensaje.contains("Unable to resolve host")) {
                "No se puede conectar al servidor.\nVerifica que estés conectado a la red de la empresa."
            } else if (mensaje.contains("500")) {
                "Error interno del servidor.\nContacta al equipo técnico."
            } else {
                "Ocurrió un error inesperado.\nPor favor, intenta de nuevo."
            }
        }
    }
}