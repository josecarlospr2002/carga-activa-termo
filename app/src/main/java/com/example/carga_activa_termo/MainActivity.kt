package com.example.carga_activa_termo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carga_activa_termo.ui.PantallaCargaActiva

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
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