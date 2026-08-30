package com.egbe.surveillance.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.egbe.surveillance.ui.screens.*
import com.egbe.surveillance.viewmodel.SurveillanceViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dash", "DASH", Icons.Filled.Home)
    object Map : Screen("map", "EYE", Icons.Filled.LocationOn)
    object Trace : Screen("trace", "TRACE", Icons.Filled.Phone)
    object Phish : Screen("phish", "LURE", Icons.Filled.Warning)
    object Tools : Screen("tools", "TOOLS", Icons.Filled.Build)
}

@Composable
fun EGBENavHost() {
    val navController = rememberNavController()
    val vm: SurveillanceViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color(0xFF111827)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                listOf(Screen.Dashboard, Screen.Map, Screen.Trace, Screen.Phish, Screen.Tools).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(Screen.Dashboard.route) { DashboardScreen(vm) }
            composable(Screen.Map.route) { MapScreen(vm) }
            composable(Screen.Trace.route) { PhoneTraceScreen(vm) }
            composable(Screen.Phish.route) { PhishingScreen(vm) }
            composable(Screen.Tools.route) { ToolsScreen(vm) }
        }
    }
}
