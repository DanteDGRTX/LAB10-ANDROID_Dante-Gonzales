package com.dantegonzales.lab10android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.dantegonzales.lab10android.ui.theme.LAB10ANDROID_DanteGonzalesTheme
import com.dantegonzales.lab10android.books.BookshelfScreen
import com.dantegonzales.lab10android.ui.CarteleraScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LAB10ANDROID_DanteGonzalesTheme {
                var showBookshelf by rememberSaveable { mutableStateOf(false) }
                if (showBookshelf) {
                    BookshelfScreen(onBack = { showBookshelf = false })
                } else {
                    CarteleraScreen(onOpenBookshelf = { showBookshelf = true })
                }
            }
        }
    }
}
