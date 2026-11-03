package com.example.goated.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.goated.R
import com.example.goated.components.BottomBar
import com.example.goated.data.AuthRepository
import com.example.goated.navigation.Screen
import com.example.goated.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(navController: NavHostController) {
    var isSignUp by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Black,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.registration_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (isSignUp) "Регистрация" else "Вход",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = White
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Начни оценивать игры, писать\nрецензии и пополнять свой\nаккаунт пройдеными играми",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(26.dp),
                    color = CardBorder,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                Column(
                    Modifier
                        .padding(bottom = 80.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Black, PrimaryBlue)
                            )
                        )
                        .border(
                            width = 2.dp,color = CardBorder,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .padding(horizontal = 22.dp, vertical = 22.dp,),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSignUp) {
                        FieldLabel("@email")
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors()
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    FieldLabel("login")
                    OutlinedTextField(
                        value = login,
                        onValueChange = { login = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(10.dp))

                    FieldLabel("password")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error!!,
                            color = AccentBlue,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        enabled = !loading,
                        onClick = {
                            loading = true
                            error = null
                            scope.launch {
                                val result = if (isSignUp) {
                                    AuthRepository.signUp(email, password, login)
                                } else {
                                    AuthRepository.signInWithLogin(login, password)
                                }
                                loading = false
                                result.onSuccess {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Main.route)
                                    }
                                }.onFailure {
                                    error = it.message
                                }
                            }
                        },
                        modifier = Modifier
                            .width(250.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = White
                        )
                    ) {
                        Text(
                            if (isSignUp) "Зарегистрироваться" else "Войти",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    TextButton(onClick = { isSignUp = !isSignUp }) {
                        Text(
                            if (isSignUp) "Уже есть аккаунт? Войти"
                            else "Нет аккаунта? Зарегистрироваться",color = CardBorder,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 6.dp)
    ) {
        Text(
            text = text,
            color = CardBorder,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GrayText,
    unfocusedTextColor = GrayText,
    focusedBorderColor = CardBorder,
    unfocusedBorderColor = CardBorder,
    cursorColor = AccentBlue
)