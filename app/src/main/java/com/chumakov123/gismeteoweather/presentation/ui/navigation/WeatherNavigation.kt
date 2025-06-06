package com.chumakov123.gismeteoweather.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        Scaffold { inner ->
            WeatherMainScreen(
                modifier = Modifier.padding(inner),
                viewModel = viewModel,
                onSettingsClick = { safeNavigate(navController, WeatherDestinations.SETTINGS_ROUTE) },
                onAddCityClick = { safeNavigate(navController, WeatherDestinations.CITIES_ROUTE) },
            )
        }
    }
}

private fun NavGraphBuilder.settingsScreen(
    viewModel: WeatherViewModel,
    navController: NavController,
) {

    composable(WeatherDestinations.SETTINGS_ROUTE) {
        SettingsScreen(
            viewModel = viewModel,
            onBackClick = { safePopBack(navController) },
        )
    }
}

private fun NavGraphBuilder.addCityScreen(
    viewModel: WeatherViewModel,
    navController: NavController
) {
    composable(WeatherDestinations.CITIES_ROUTE) {
    CitiesScreen(
            viewModel = viewModel,
            onBackClick = { safePopBack(navController) }
        )
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