package com.example.texter

import androidx.navigation.NavController

/**
 * Helper function to separate the navigation
 * code from the composables in the project
 *
 * @param navController The NavController instance
 * @param route The route to navigate to
 */
fun navigateTo(
    navController: NavController,
    route: String
) {
    navController.navigate(route) {

        // Pop all screens from the backstack till we reach the screen we want to go to
        // Saves memory
        popUpTo(route)

        // Prevent multiple instances of the bottom nav screens stacking onto each other
        // Saves memory
        launchSingleTop = true
    }
}