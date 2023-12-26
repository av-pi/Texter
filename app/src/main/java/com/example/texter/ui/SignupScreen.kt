package com.example.texter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.texter.CheckSignedIn
import com.example.texter.DestinationScreen
import com.example.texter.R
import com.example.texter.TexterProgressSpinner
import com.example.texter.TexterViewModel
import com.example.texter.navigateTo
import com.example.texter.ui.theme.TexterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController, viewModel: TexterViewModel
) {

    TexterTheme {

        CheckSignedIn(viewModel = viewModel, navController = navController)

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val nameState = remember { mutableStateOf(TextFieldValue()) }
                val numberState = remember { mutableStateOf(TextFieldValue()) }
                val emailState = remember { mutableStateOf(TextFieldValue()) }
                val passwordState = remember { mutableStateOf(TextFieldValue()) }

                val focus = LocalFocusManager.current

                Image(
                    painter = painterResource(id = R.drawable.chat),
                    contentDescription = null,
                    modifier = Modifier
                        .width(200.dp)
                        .padding(top = 16.dp)
                        .padding(8.dp)
                )

                Text(
                    text = "Signup",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 30.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    modifier = Modifier
                        .padding(8.dp),
                    label = { Text(text = "Name", color = MaterialTheme.colorScheme.onPrimary) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor =  MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = numberState.value,
                    onValueChange = { numberState.value = it },
                    modifier = Modifier
                        .padding(8.dp),
                    label = { Text(text = "Number", color = MaterialTheme.colorScheme.onPrimary) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor =  MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = emailState.value,
                    onValueChange = { emailState.value = it },
                    modifier = Modifier
                        .padding(8.dp),
                    label = { Text(text = "Email", color = MaterialTheme.colorScheme.onPrimary) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor =  MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = passwordState.value,
                    onValueChange = { passwordState.value = it },
                    modifier = Modifier
                        .padding(8.dp),
                    label = { Text(text = "Password", color = MaterialTheme.colorScheme.onPrimary) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor =  MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )

                Button(
                    onClick = {
                        focus.clearFocus(force = true)
                        viewModel.onSignup(
                            nameState.value.text,
                            numberState.value.text,
                            emailState.value.text,
                            passwordState.value.text
                        )
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                ) {
                    Text(text = "SIGN UP", color = MaterialTheme.colorScheme.onPrimary)
                }

                Text(
                    text = "Already registered? Go to login ->",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            navigateTo(navController, DestinationScreen.Login.route)
                        }
                )

            }

            val isLoading = viewModel.inProgress.value

            if (isLoading) TexterProgressSpinner()
        }
    }

}