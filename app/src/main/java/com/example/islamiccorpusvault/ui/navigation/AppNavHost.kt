package com.example.islamiccorpusvault.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.islamiccorpusvault.ui.screens.CategoryScreen
import com.example.islamiccorpusvault.ui.screens.GeneralNotesScreen
import com.example.islamiccorpusvault.ui.screens.HomeScreen
import com.example.islamiccorpusvault.ui.screens.LibraryScreen
import com.example.islamiccorpusvault.ui.screens.NoteDetailScreen
import com.example.islamiccorpusvault.ui.screens.NoteEditorScreen
import com.example.islamiccorpusvault.ui.screens.ScholarDetailScreen
import com.example.islamiccorpusvault.ui.screens.ScholarsScreen
import com.example.islamiccorpusvault.ui.screens.SettingsScreen
import com.example.islamiccorpusvault.ui.screens.SubcategoryScreen

private fun slugify(input: String): String {
    val lowered = input.lowercase()
    val sb = StringBuilder()
    var lastUnderscore = false
    for (ch in lowered) {
        val ok = (ch in 'a'..'z') || (ch in '0'..'9')
        if (ok) {
            sb.append(ch)
            lastUnderscore = false
        } else if (!lastUnderscore) {
            sb.append('_')
            lastUnderscore = true
        }
    }
    return sb.toString().trim('_').ifBlank { "scholar" }
}

private fun noteDetailRoute(noteId: String): String {
    return "note_detail/${Uri.encode(noteId)}"
}

private fun noteEditorRoute(noteId: String): String {
    return "note_editor/${Uri.encode(noteId)}"
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
                onOpenGeneralNotes = { navController.navigate(Routes.GENERAL_NOTES) },
                onOpenNoteDetail = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onOpenNoteEditor = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        composable(Routes.GENERAL_NOTES) {
            GeneralNotesScreen(
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onOpenGeneralNotes = { navController.navigate(Routes.GENERAL_NOTES) }
            )
        }
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
                        "category/${Uri.encode(id)}/${Uri.encode(name)}/${Uri.encode(categoryName)}"
                    )
                }
            )
        }

        composable(route = Routes.CATEGORY) { backStackEntry ->
            val scholarId = Uri.decode(backStackEntry.arguments?.getString("scholarId") ?: "")
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")

            CategoryScreen(
                scholarId = scholarId,
                scholarName = scholarName,
                categoryName = categoryName,
                onCreateSubcategory = { },
                onSubcategoryClick = { subcategoryId, subcategoryName ->
                    navController.navigate(
                        "subcategory/${Uri.encode(scholarId)}/${Uri.encode(scholarName)}/${Uri.encode(categoryName)}/${Uri.encode(subcategoryId)}/${Uri.encode(subcategoryName)}"
                    )
                },
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        // Backward-compatible old route format kept to prevent restore-state crashes.
        composable(route = "category/{scholarName}/{categoryName}") { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")
            val scholarId = slugify(scholarName)

            CategoryScreen(
                scholarId = scholarId,
                scholarName = scholarName,
                categoryName = categoryName,
                onCreateSubcategory = { },
                onSubcategoryClick = { subcategoryId, subcategoryName ->
                    navController.navigate(
                        "subcategory/${Uri.encode(scholarId)}/${Uri.encode(scholarName)}/${Uri.encode(categoryName)}/${Uri.encode(subcategoryId)}/${Uri.encode(subcategoryName)}"
                    )
                },
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }

        composable(route = Routes.SUBCATEGORY) { backStackEntry ->
            val scholarId = Uri.decode(backStackEntry.arguments?.getString("scholarId") ?: "")
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")
            val subcategoryName = Uri.decode(backStackEntry.arguments?.getString("subcategoryName") ?: "")

            SubcategoryScreen(
                scholarId = scholarId,
                scholarName = scholarName,
                categoryName = categoryName,
                subcategoryName = subcategoryName,
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        // Backward-compatible old route format kept to prevent restore-state crashes.
        composable(route = "subcategory/{scholarName}/{categoryName}/{subcategoryId}/{subcategoryName}") { backStackEntry ->
            val scholarName = Uri.decode(backStackEntry.arguments?.getString("scholarName") ?: "")
            val categoryName = Uri.decode(backStackEntry.arguments?.getString("categoryName") ?: "")
            val subcategoryName = Uri.decode(backStackEntry.arguments?.getString("subcategoryName") ?: "")
            val scholarId = slugify(scholarName)

            SubcategoryScreen(
                scholarId = scholarId,
                scholarName = scholarName,
                categoryName = categoryName,
                subcategoryName = subcategoryName,
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }

        composable(route = Routes.NOTE_DETAIL) { backStackEntry ->
            val noteId = Uri.decode(backStackEntry.arguments?.getString("noteId") ?: "")

            NoteDetailScreen(
                noteId = noteId,
                onEdit = { navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        // Backward-compatible old route format kept to prevent restore-state crashes.
        composable(route = "note_detail?noteId={noteId}&title={title}&body={body}&citation={citation}") { backStackEntry ->
            val noteId = Uri.decode(backStackEntry.arguments?.getString("noteId") ?: "")
            NoteDetailScreen(
                noteId = noteId,
                onEdit = { navController.navigate(noteEditorRoute(noteId)) }
            )
        }

        composable(route = Routes.NOTE_EDITOR) { backStackEntry ->
            val noteId = Uri.decode(backStackEntry.arguments?.getString("noteId") ?: "")

            NoteEditorScreen(
                noteId = noteId,
                onCancel = { navController.popBackStack() },
                onSave = {
                    navController.navigate(noteDetailRoute(noteId)) {
                        popUpTo(Routes.NOTE_EDITOR) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Backward-compatible old route format kept to prevent restore-state crashes.
        composable(route = "note_editor?noteId={noteId}&title={title}&body={body}&citation={citation}") { backStackEntry ->
            val noteId = Uri.decode(backStackEntry.arguments?.getString("noteId") ?: "")
            NoteEditorScreen(
                noteId = noteId,
                onCancel = { navController.popBackStack() },
                onSave = {
                    navController.navigate(noteDetailRoute(noteId)) {
                        popUpTo(Routes.NOTE_EDITOR) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
