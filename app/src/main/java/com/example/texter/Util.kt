package com.example.texter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

/**
 * Function to check if a user is currently signed in with Firebase
 */
@Composable
fun CheckSignedIn(
    viewModel: TexterViewModel,
    navController: NavController
) {
    val alreadySignedIn = remember { mutableStateOf(false) }
    val signedIn = viewModel.signedIn.value

    if (signedIn && !alreadySignedIn.value) {
        alreadySignedIn.value = true
        navController.navigate(DestinationScreen.Profile.route) {
            popUpTo(0)
        }
    }
}

/**
 * Custom divider used throughout the project screens
 */
@Composable
fun TexterDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier.alpha(0.3f).padding(top = 8.dp, bottom = 8.dp)
    )
}




















