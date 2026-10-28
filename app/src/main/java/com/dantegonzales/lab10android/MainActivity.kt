package com.dantegonzales.lab10android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dantegonzales.lab10android.ui.theme.LAB10ANDROID_DanteGonzalesTheme
import com.dantegonzales.lab10android.ui.CarteleraScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LAB10ANDROID_DanteGonzalesTheme {
                CarteleraScreen()
            }
        }
    }
}
