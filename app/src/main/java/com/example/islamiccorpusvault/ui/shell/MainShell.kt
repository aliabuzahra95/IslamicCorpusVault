package com.example.islamiccorpusvault.ui.shell

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islamiccorpusvault.ui.navigation.AppNavHost
import com.example.islamiccorpusvault.ui.navigation.Routes
import com.example.islamiccorpusvault.ui.navigation.bottomNavItems

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}