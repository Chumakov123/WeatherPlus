package com.chumakov123.gismeteoweather.presentation.ui.navigation

import WeatherAppTheme
import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chumakov123.gismeteoweather.presentation.ui.screens.CitiesScreen
import com.chumakov123.gismeteoweather.presentation.ui.screens.SettingsScreen
import com.chumakov123.gismeteoweather.presentation.ui.screens.WeatherMainScreen
import com.chumakov123.gismeteoweather.presentation.ui.viewModel.WeatherViewModel

object WeatherDestinations {
    const val MAIN_ROUTE = "main"
    const val SETTINGS_ROUTE = "settings"
    const val CITIES_ROUTE = "cities"
}

@Composable
fun WeatherNavHost(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = WeatherDestinations.MAIN_ROUTE
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val view = LocalView.current
    val window = (view.context as Activity).window

    LaunchedEffect(currentRoute) {
        val controller = WindowCompat.getInsetsController(window, view)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        when (currentRoute) {
            WeatherDestinations.MAIN_ROUTE -> {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color(0xFF073042).toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }

            WeatherDestinations.SETTINGS_ROUTE,
            WeatherDestinations.CITIES_ROUTE -> {
                window.statusBarColor = Color(0xFF17629F).toArgb()
                window.navigationBarColor = Color(0xFF2196F3).toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        mainScreen(viewModel, navController)
        settingsScreen(viewModel, navController)
        addCityScreen(viewModel, navController)
    }
}

private fun NavGraphBuilder.mainScreen(
    viewModel: WeatherViewModel,
    navController: NavController,
) {
    composable(WeatherDestinations.MAIN_ROUTE) {
        WeatherAppTheme(isMainScreen = true) {
            Scaffold { inner ->
                WeatherMainScreen(
                    modifier = Modifier.padding(inner),
                    viewModel = viewModel,
                    onSettingsClick = {
                        safeNavigate(
                            navController,
                            WeatherDestinations.SETTINGS_ROUTE
                        )
                    },
                    onAddCityClick = {
                        safeNavigate(
                            navController,
                            WeatherDestinations.CITIES_ROUTE
                        )
                    },
                )
            }
        }
    }
}

private fun NavGraphBuilder.settingsScreen(
    viewModel: WeatherViewModel,
    navController: NavController,
) {

    composable(WeatherDestinations.SETTINGS_ROUTE) {
        WeatherAppTheme(isMainScreen = false) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { safePopBack(navController) },
            )
        }
    }
}

private fun NavGraphBuilder.addCityScreen(
    viewModel: WeatherViewModel,
    navController: NavController
) {
    composable(WeatherDestinations.CITIES_ROUTE) {
        WeatherAppTheme(isMainScreen = false) {
            CitiesScreen(
                viewModel = viewModel,
                onBackClick = { safePopBack(navController) },
                onSettingsClick = {
                    safeNavigate(
                        navController,
                        WeatherDestinations.SETTINGS_ROUTE
                    )
                }
            )
        }
    }
}

private fun safeNavigate(
    navController: NavController,
    route: String,
    popUpToRoute: String? = null
) {
    if (navController.currentDestination?.route != route) {
        navController.navigate(route) {
            popUpToRoute?.let {
                popUpTo(it) { saveState = true }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

private fun safePopBack(navController: NavController) {
    if (navController.previousBackStackEntry != null) {
        navController.popBackStack()
    }
}