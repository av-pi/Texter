package com.example.texter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.texter.DestinationScreen
import com.example.texter.R
import com.example.texter.navigateTo

/**
 * Enum class storing the menu items
 * for the bottom navigation menu
 *
 * @param icon The icon from the resources directory
 * @param navDestination The destination page for the menu button
 */
enum class BottomNavigationItem(
    val icon: Int,
    val navDestination: DestinationScreen
) {
    CHATLIST(R.drawable.baseline_chat, DestinationScreen.ChatList),
    STATUSLIST(R.drawable.baseline_status, DestinationScreen.StatusList),
    PROFILE(R.drawable.baseline_profile, DestinationScreen.Profile)
}

@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        for (item in BottomNavigationItem.entries) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .padding(4.dp)
                    .weight(1f)
                    .clickable {
                               navigateTo(navController, item.navDestination.route)
                    },
                colorFilter =
                if (item == selectedItem) ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                else ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}