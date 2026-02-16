package com.example.islamiccorpusvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.islamiccorpusvault.data.di.AppContainer
import com.example.islamiccorpusvault.ui.theme.IslamicCorpusVaultTheme
import com.example.islamiccorpusvault.ui.shell.MainShell
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        lifecycleScope.launch {
            AppContainer.seedIfEmpty()
        }
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
