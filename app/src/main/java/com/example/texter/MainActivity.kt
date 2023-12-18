package com.example.texter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.texter.ui.ChatListScreen
import com.example.texter.ui.LoginScreen
import com.example.texter.ui.ProfileScreen
import com.example.texter.ui.SignupScreen
import com.example.texter.ui.SingleChatScreen
import com.example.texter.ui.SingleStatusScreen
import com.example.texter.ui.StatusListScreen
import com.example.texter.ui.theme.TexterTheme

/**
 * Sealed class holding routes for each screen in the app.
 * Adds consistency and prevents errors in the navigation code.
 */
sealed class DestinationScreen(val route: String) {
    object Signup: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Profile: DestinationScreen("profile")
    object ChatList: DestinationScreen("chat_list")
    object SingleChat: DestinationScreen("single_chat/{chatId}") {
        fun createRoute(id: String) = "single_chat/$id"
    }
    object StatusList: DestinationScreen("status_list")
    object SingleStatus: DestinationScreen("single_status/{statusId}") {
        fun createRoute(id: String) = "single_status/$id"
    }

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TexterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TexterNavigation()
                }
            }
        }
    }
}

@Composable
fun TexterNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = DestinationScreen.Profile.route) {

        composable(route = DestinationScreen.Signup.route) {
            SignupScreen()
        }

        composable(route = DestinationScreen.Login.route) {
            LoginScreen()
        }

        composable(route = DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(route = DestinationScreen.StatusList.route) {
            StatusListScreen(navController = navController)
        }

        composable(route = DestinationScreen.SingleStatus.route) {
            SingleStatusScreen(statusId = "123")
        }

        composable(route = DestinationScreen.ChatList.route) {
            ChatListScreen(navController = navController)
        }

        composable(route = DestinationScreen.SingleChat.route) {
            SingleChatScreen(chatId = "123")
        }

    }
}
