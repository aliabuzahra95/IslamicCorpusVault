package com.example.islamiccorpusvault.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.islamiccorpusvault.ui.screens.CategoryScreen
import com.example.islamiccorpusvault.ui.screens.HomeScreen
import com.example.islamiccorpusvault.ui.screens.LibraryScreen
import com.example.islamiccorpusvault.ui.screens.ScholarDetailScreen
import com.example.islamiccorpusvault.ui.screens.ScholarsScreen
import com.example.islamiccorpusvault.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.LIBRARY) { LibraryScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }
        composable(Routes.SCHOLARS) { ScholarsScreen(navController) }

        composable(route = "${Routes.CATEGORY}/{scholarName}/{categoryName}") { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")

            CategoryScreen(
                scholarName = scholarName,
                categoryName = categoryName,
                onBack = { navController.popBackStack() }
            )
        }

        // ✅ IMPORTANT: this must EXACTLY match what we navigate to
        composable(route = "${Routes.SCHOLAR_DETAIL}/{id}/{name}") { backStackEntry ->
            val id = Uri.decode(backStackEntry.arguments?.getString("id") ?: "")
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")

            ScholarDetailScreen(
                id = id,
                name = name,
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryName ->
                    navController.navigate(
                        "${Routes.CATEGORY}/${Uri.encode(name)}/${Uri.encode(categoryName)}"
                    )
                }
            )
        }
    }
}