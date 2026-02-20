package com.example.islamiccorpusvault.ui.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.islamiccorpusvault.ui.screens.GeneralNotesScreen
import com.example.islamiccorpusvault.ui.screens.HomeScreen
import com.example.islamiccorpusvault.ui.screens.LibraryScreen
import com.example.islamiccorpusvault.ui.screens.NoteDetailScreen
import com.example.islamiccorpusvault.ui.screens.NoteEditorScreen
import com.example.islamiccorpusvault.ui.screens.NotesScreen
import com.example.islamiccorpusvault.ui.screens.SettingsScreen

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
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenGeneralNotes = { navController.navigate(Routes.NOTES) },
                onOpenNoteDetail = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onOpenNoteEditor = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        composable(Routes.NOTES) {
            NotesScreen(
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        composable(Routes.GENERAL_NOTES) {
            NotesScreen(
                onNoteClick = { noteId -> navController.navigate(noteDetailRoute(noteId)) },
                onCreateNote = { noteId -> navController.navigate(noteEditorRoute(noteId)) }
            )
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onOpenGeneralNotes = { navController.navigate(Routes.NOTES) },
                onOpenNote = { noteId -> navController.navigate(noteDetailRoute(noteId)) }
            )
        }
        composable(Routes.SETTINGS) { SettingsScreen() }

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
