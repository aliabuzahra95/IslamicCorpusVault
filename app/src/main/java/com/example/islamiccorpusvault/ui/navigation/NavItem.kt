package com.example.islamiccorpusvault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : NavItem(Routes.HOME, "Home", Icons.Outlined.Home)
    data object Notes : NavItem(Routes.NOTES, "Notes", Icons.Outlined.Description)
    data object Library : NavItem(Routes.LIBRARY, "Library", Icons.AutoMirrored.Outlined.LibraryBooks)
    data object Quran : NavItem(Routes.QURAN, "Quran", Icons.Outlined.MenuBook)
}

val bottomNavItems = listOf(
    NavItem.Home,
    NavItem.Notes,
    NavItem.Library,
    NavItem.Quran
)
