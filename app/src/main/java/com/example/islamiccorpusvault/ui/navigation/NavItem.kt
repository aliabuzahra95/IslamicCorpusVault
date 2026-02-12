package com.example.islamiccorpusvault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : NavItem(Routes.HOME, "Home", Icons.Outlined.Home)
    data object Scholars : NavItem(Routes.SCHOLARS, "Scholars", Icons.Outlined.Person)
    data object Library : NavItem(Routes.LIBRARY, "Library", Icons.Outlined.LibraryBooks)
    data object Settings : NavItem(Routes.SETTINGS, "Settings", Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    NavItem.Home,
    NavItem.Scholars,
    NavItem.Library,
    NavItem.Settings
)