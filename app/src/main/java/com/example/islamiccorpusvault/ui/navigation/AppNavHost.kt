package com.example.islamiccorpusvault.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

        // Main tabs
        composable(Routes.HOME) { HomeScreen() }
        composable(Routes.LIBRARY) { LibraryScreen() }
        composable(Routes.SCHOLARS) { ScholarsScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen() }

        // Scholar detail
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

        // Category screen
        composable(route = "${Routes.CATEGORY}/{scholarName}/{categoryName}") { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")

            CategoryScreen(
                scholarName = scholarName,
                categoryName = categoryName,
                onCreateSubcategory = {
                    // TODO next: navigate to a "CreateSubcategoryScreen"
                    // For now, just do nothing
                },
                onCreateNote = {
                    // TODO next: navigate to a "CreateNoteScreen" (direct note)
                    // For now, just do nothing
                }
            )
        }

        // Temporary entries placeholder (so the app doesn't crash when you click a subcategory)
        composable(route = "${Routes.ENTRY}/{scholarName}/{categoryName}/{subcategoryName}") { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")
            val subcategoryName = Uri.decode(backStackEntry.arguments?.getString("subcategoryName") ?: "")

            Text(
                text = "Entries: $scholarName → $categoryName → $subcategoryName",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}