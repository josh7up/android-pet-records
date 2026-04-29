package com.joshfeldman.petrecords.core.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joshfeldman.petrecords.feature.pets.PetsRoute
import com.joshfeldman.petrecords.feature.search.SearchRoute
import com.joshfeldman.petrecords.feature.settings.SettingsRoute
import com.joshfeldman.petrecords.feature.upload.UploadRoute
import com.joshfeldman.petrecords.feature.weights.WeightRoute
import java.io.File
import kotlinx.coroutines.launch

private enum class TopLevelDestination(val route: String, val label: String) {
    Pets("pets", "Pets"),
    Search("search", "Search"),
    Upload("upload", "Upload"),
    Settings("settings", "Settings"),
}

@Composable
fun PetRecordsApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            when (destination) {
                                TopLevelDestination.Pets -> Icon(Icons.Outlined.Pets, contentDescription = null)
                                TopLevelDestination.Search -> Icon(Icons.Outlined.Search, contentDescription = null)
                                TopLevelDestination.Upload -> Icon(Icons.Outlined.CloudUpload, contentDescription = null)
                                TopLevelDestination.Settings -> Icon(Icons.Outlined.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Pets.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Pets.route) {
                PetsRoute(onOpenWeightTrend = { petId -> navController.navigate("weights/$petId") })
            }
            composable(TopLevelDestination.Search.route) {
                SearchRoute()
            }
            composable(TopLevelDestination.Upload.route) {
                UploadRoute(onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } })
            }
            composable(TopLevelDestination.Settings.route) {
                SettingsRoute()
            }
            composable(
                route = "weights/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.StringType }),
            ) {
                WeightRoute(onBack = { navController.popBackStack() })
            }
        }
    }
}

fun createTempImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("receipt_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
