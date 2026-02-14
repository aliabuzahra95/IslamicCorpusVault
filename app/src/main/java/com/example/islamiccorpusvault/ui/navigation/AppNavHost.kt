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
import com.example.islamiccorpusvault.ui.screens.NoteDetailScreen
import com.example.islamiccorpusvault.ui.screens.ScholarDetailScreen
import com.example.islamiccorpusvault.ui.screens.ScholarsScreen
import com.example.islamiccorpusvault.ui.screens.SettingsScreen
import com.example.islamiccorpusvault.ui.screens.SubcategoryScreen

private fun noteDetailRoute(noteId: String, title: String, body: String, citation: String): String {
    return "note_detail?noteId=${Uri.encode(noteId)}&title=${Uri.encode(title)}&body=${Uri.encode(body)}&citation=${Uri.encode(citation)}"
}

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
        composable(Routes.HOME) {
            HomeScreen(
                onOpenScholars = { navController.navigate(Routes.SCHOLARS) },
                onOpenNoteDetail = { title, body, citation ->
                    navController.navigate(noteDetailRoute("home", title, body, citation))
                }
            )
        }
        composable(Routes.LIBRARY) { LibraryScreen() }
        composable(Routes.SCHOLARS) { ScholarsScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen() }

        composable(route = "${Routes.SCHOLAR_DETAIL}/{id}/{name}") { backStackEntry ->
            val id = Uri.decode(backStackEntry.arguments?.getString("id") ?: "")
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "")

            ScholarDetailScreen(
                id = id,
                name = name,
                onBack = { navController.popBackStack() },
                onCategoryClick = { categoryName ->
                    navController.navigate(
                        "category/${Uri.encode(name)}/${Uri.encode(categoryName)}"
                    )
                }
            )
        }

        composable(route = Routes.CATEGORY) { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")

            CategoryScreen(
                scholarName = scholarName,
                categoryName = categoryName,
                onCreateSubcategory = { },
                onCreateNote = { },
                onSubcategoryClick = { subcategoryId, subcategoryName ->
                    navController.navigate(
                        "subcategory/${Uri.encode(scholarName)}/${Uri.encode(categoryName)}/${Uri.encode(subcategoryId)}/${Uri.encode(subcategoryName)}"
                    )
                },
                onNoteClick = { noteId, title, body, citation ->
                    navController.navigate(noteDetailRoute(noteId, title, body, citation))
                }
            )
        }

        composable(route = Routes.SUBCATEGORY) { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")
            val subcategoryName = Uri.decode(backStackEntry.arguments?.getString("subcategoryName") ?: "")

            SubcategoryScreen(
                scholarName = scholarName,
                categoryName = categoryName,
                subcategoryName = subcategoryName,
                onNoteClick = { noteId, title, body, citation ->
                    navController.navigate(noteDetailRoute(noteId, title, body, citation))
                }
            )
        }

        composable(route = Routes.NOTE_DETAIL) { backStackEntry ->
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
            val body = Uri.decode(backStackEntry.arguments?.getString("body") ?: "")
            val citation = Uri.decode(backStackEntry.arguments?.getString("citation") ?: "")

            NoteDetailScreen(
                title = title,
                body = body,
                citation = citation
            )
        }
    }
}
