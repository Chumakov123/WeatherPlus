package com.chumakov123.gismeteoweather.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chumakov123.gismeteoweather.domain.model.WeatherDisplaySettings
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
    val settings by viewModel.settings.collectAsState()

    key(settings) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {

            mainScreen(viewModel, navController, settings)
            settingsScreen(viewModel, navController, settings)
            addCityScreen(viewModel, navController)
        }
    }
}

private fun NavGraphBuilder.mainScreen(
    viewModel: WeatherViewModel,
    navController: NavController,
    settings: WeatherDisplaySettings
) {
    composable(WeatherDestinations.MAIN_ROUTE) {
        Scaffold { inner ->
            WeatherMainScreen(
                modifier = Modifier.padding(inner),
                viewModel = viewModel,
                settings = settings,
                onSettingsClick = { navController.navigate(WeatherDestinations.SETTINGS_ROUTE) },
                onAddCityClick = { navController.navigate(WeatherDestinations.CITIES_ROUTE) }
            )
        }
    }
}

private fun NavGraphBuilder.settingsScreen(
    viewModel: WeatherViewModel,
    navController: NavController,
    settings: WeatherDisplaySettings
) {

    composable(WeatherDestinations.SETTINGS_ROUTE) {
        SettingsScreen(
            onBackClick = { navController.popBackStack() },
            settings = settings,
            onSettingsChanged = { newSettings ->
                viewModel.onSettingsChanged(newSettings)
            },
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
            onBackClick = { navController.popBackStack() }
        )
    }
}