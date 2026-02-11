package com.example.islamiccorpusvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.islamiccorpusvault.ui.theme.IslamicCorpusVaultTheme
import com.example.islamiccorpusvault.ui.shell.MainShell
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IslamicCorpusVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainShell()
                }
            }
        }
    }
}