package com.joshfeldman.petrecords

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joshfeldman.petrecords.core.design.theme.PetRecordsTheme
import com.joshfeldman.petrecords.core.navigation.PetRecordsApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
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
