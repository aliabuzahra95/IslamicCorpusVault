package com.example.islamiccorpusvault.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings

val bottomNavItems = listOf(
    NavItem(route = Routes.HOME, label = "Home", icon = Icons.Outlined.Home),
    NavItem(route = Routes.LIBRARY, label = "Library", icon = Icons.Outlined.LibraryBooks),
    NavItem(route = Routes.SETTINGS, label = "Settings", icon = Icons.Outlined.Settings),
)