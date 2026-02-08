package com.sz.fileman.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sz.fileman.presentation.local.LocalFilesScreen

/**
 * Main navigation graph with bottom navigation.
 */
@Composable
fun FileManNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavScreens = listOf(
        BottomNavItem(
            screen = Screen.LocalFiles,
            icon = Icons.Default.Folder,
            label = "Local"
        ),
        BottomNavItem(
            screen = Screen.NasFiles,
            icon = Icons.Default.Cloud,
            label = "NAS"
        )
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.screen.route
                    } == true
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.LocalFiles.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Local Files Screen
            composable(Screen.LocalFiles.route) {
                LocalFilesScreen()
            }
            
            // NAS Files Screen
            composable(Screen.NasFiles.route) {
                // TODO: Implement NasFilesScreen
                PlaceholderScreen("NAS Files")
            }
            
            // NAS Connections Screen
            composable(Screen.NasConnections.route) {
                // TODO: Implement NasConnectionListScreen
                PlaceholderScreen("NAS Connections")
            }
            
            // NAS Connection Edit Screen
            composable(Screen.NasConnectionEdit.route) { backStackEntry ->
                val connectionId = backStackEntry.arguments?.getString("connectionId") ?: "new"
                // TODO: Implement NasConnectionEditScreen
                PlaceholderScreen("Edit Connection: $connectionId")
            }
        }
    }
}

/**
 * Placeholder screen for unimplemented screens.
 */
@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.material3.Text(
        text = "$title - Coming Soon",
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
    )
}

/**
 * Data class representing a bottom navigation item.
 */
data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)
