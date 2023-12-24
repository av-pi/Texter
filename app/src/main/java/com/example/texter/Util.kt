package com.example.texter

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

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
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}


@Composable
fun TexterImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {

    val painter = rememberAsyncImagePainter(model = data)

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = contentScale,
        modifier = modifier
    )

    if (painter.state is AsyncImagePainter.State.Loading) TexterProgressSpinner()


}

@Composable
fun TexterListItem(
    imageUrl: String?,
    name: String?,
    onItemClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clickable { onItemClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        TexterImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(
                    CircleShape
                )
                .background(Color.Red)
        )
        Text(
            text = name ?: "---",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun TitleText(title: String) {
    Text(text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        modifier = Modifier.padding(8.dp)
    )
}




















