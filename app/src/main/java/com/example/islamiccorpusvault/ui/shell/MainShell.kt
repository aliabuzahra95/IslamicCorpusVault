package com.example.islamiccorpusvault.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islamiccorpusvault.ui.navigation.AppNavHost
import com.example.islamiccorpusvault.ui.navigation.Routes
import com.example.islamiccorpusvault.ui.navigation.bottomNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME

    val topLevelRoutes = remember { bottomNavItems.map { it.route }.toSet() }
    val isTopLevel = currentRoute in topLevelRoutes

    val canGoBack = navController.previousBackStackEntry != null
    val hideShellTopBar = currentRoute == Routes.NOTE_EDITOR
    val hideShellBottomBar = currentRoute == Routes.NOTE_EDITOR

    val scholarFlowRoutes = setOf(
        Routes.SCHOLARS,
        Routes.CATEGORY,
        Routes.SUBCATEGORY
    )
    val inScholarFlow = (currentRoute in scholarFlowRoutes) ||
        (currentRoute?.startsWith(Routes.SCHOLAR_DETAIL) == true)

    val titleText = when {
        currentRoute == Routes.HOME -> "Islamic Corpus Vault"
        currentRoute == Routes.GENERAL_NOTES -> "General Notes"
        currentRoute == Routes.SCHOLARS -> "Scholars"
        currentRoute == Routes.LIBRARY -> "Library"
        currentRoute == Routes.SETTINGS -> "Settings"
        currentRoute == Routes.CATEGORY -> "Category"
        currentRoute == Routes.SUBCATEGORY -> "Subcategory"
        currentRoute == Routes.NOTE_DETAIL -> ""
        currentRoute == Routes.NOTE_EDITOR -> ""
        currentRoute?.startsWith(Routes.SCHOLAR_DETAIL) == true -> "Scholar"
        else -> "Details"
    }

    var showQuickCreate by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (!hideShellTopBar) {
                    CenterAlignedTopAppBar(
                        windowInsets = WindowInsets.statusBars,
                        navigationIcon = {
                            if (!isTopLevel && canGoBack) {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                            ) {
                                if (currentRoute == Routes.HOME) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoStories,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .padding(6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                if (titleText.isNotBlank()) {
                                    Text(
                                        text = titleText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        },
                        actions = {
                            if (isTopLevel) {
                                IconButton(
                                    onClick = {
                                        navController.navigate(Routes.SETTINGS) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            floatingActionButton = {
                if (currentRoute == Routes.HOME) {
                    FloatingActionButton(onClick = { showQuickCreate = true }) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Quick create")
                    }
                }
            },
            bottomBar = {
                if (!hideShellBottomBar) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .navigationBarsPadding()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .height(60.dp)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = when (item.route) {
                                    Routes.SCHOLARS -> inScholarFlow
                                    else -> currentRoute == item.route
                                }
                                val interaction = remember { MutableInteractionSource() }

                                val itemBg = if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color.Transparent
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(itemBg)
                                        .clickable(
                                            interactionSource = interaction,
                                            indication = null
                                        ) {
                                            val poppedToTabRoot = navController.popBackStack(item.route, false)
                                            if (!poppedToTabRoot) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = false
                                                }
                                            }
                                        }
                                        .padding(top = 6.dp, bottom = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = item.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showQuickCreate) {
        ModalBottomSheet(
            onDismissRequest = { showQuickCreate = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick create",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Choose what you want to add.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        showQuickCreate = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("New note") }

                Button(
                    onClick = {
                        showQuickCreate = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("New quote") }

                Button(
                    onClick = {
                        showQuickCreate = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("New reference") }

                TextButton(
                    onClick = { showQuickCreate = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancel") }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
