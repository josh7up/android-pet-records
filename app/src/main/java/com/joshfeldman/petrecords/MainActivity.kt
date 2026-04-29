package com.joshfeldman.petrecords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joshfeldman.petrecords.core.design.theme.PetRecordsTheme
import com.joshfeldman.petrecords.core.navigation.PetRecordsApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetRecordsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PetRecordsApp()
                }
            }
        }
    }
}
