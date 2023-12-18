package com.example.texter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

/**
 * Common progress spinner used across the application
 */
@Composable
fun TexterProgressSpinner() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun NotificationErrorMessage(viewModel: TexterViewModel) {
    val notificationState = viewModel.popupNotification.value

    val notificationMessage = notificationState?.getContentOrNull()

    if (!notificationMessage.isNullOrEmpty()) {
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_SHORT).show()
    }
}




















