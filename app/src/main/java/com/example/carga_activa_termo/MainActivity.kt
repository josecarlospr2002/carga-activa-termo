package com.example.carga_activa_termo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carga_activa_termo.R
import com.example.carga_activa_termo.ui.PantallaCargaActiva
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Carga_Activa_Termo)

        window.statusBarColor = android.graphics.Color.parseColor("#000000")
        window.navigationBarColor = android.graphics.Color.parseColor("#000000")

        setContent {
            var mostrarSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1500)
                mostrarSplash = false
            }

            if (mostrarSplash) {
                // Splash con texto de espera
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1565C0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White,
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Espere unos segundos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Por favor",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // App normal
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000000),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        color = Color(0xFF1565C0),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        PantallaCargaActiva()
                    }
                }
            }
        }
    }
}