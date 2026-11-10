
package com.example.goated.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goated.R
import com.example.goated.ui.theme.*
import com.example.goated.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(vm: AuthViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val submit = {
        focus.clearFocus()
        vm.submit()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Black)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Image(
            painter = painterResource(R.drawable.registration_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 68.dp, start = 28.dp, end = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Goated",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Начни оценивать игры, писать рецензии и пополнять свой аккаунт пройденными играми",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            val shape = RoundedCornerShape(18.dp)

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Black.copy(alpha = 0.94f),
                                GradientTop.copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(1.dp, SecondaryText.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                ModeSwitch(
                    isSignUp = state.isSignUp,
                    onToggle = {
                        if (!state.loading) vm.toggleMode()
                    }
                )

                Spacer(Modifier.height(24.dp))

                if (state.isSignUp) {
                    AuthField(
                        placeholder = "example@mail.ru",
                        value = state.email,
                        onValueChange = vm::onEmailChange,
                        keyboardType = KeyboardType.Email
                    )
                }

                AuthField(
                    placeholder = if (state.isSignUp)
                        "Придумайте логин (3-20 символов)"
                    else
                        "Логин или email",
                    value = state.login,
                    onValueChange = vm::onLoginChange,
                    keyboardType = if (!state.isSignUp)
                        KeyboardType.Email
                    else
                        KeyboardType.Text
                )

                AuthField(
                    placeholder = if (state.isSignUp)
                        "Пароль (от 6 символов)"
                    else
                        "Пароль",
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    keyboardType = KeyboardType.Password,
                    isLast = !state.isSignUp,
                    onDone = submit
                )

                if (state.isSignUp) {
                    AuthField(
                        placeholder = "Повторите пароль",
                        value = state.confirmPassword,
                        onValueChange = vm::onConfirmChange,
                        keyboardType = KeyboardType.Password,
                        isLast = true,
                        onDone = submit
                    )
                }

                state.error?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = submit,
                    enabled = !state.loading,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = White,
                        disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 32.dp,
                        vertical = 8.dp
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(40.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            color = White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            if (state.isSignUp) "Зарегистрироваться" else "Войти",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                TextButton(
                    onClick = {
                        if (!state.loading) vm.toggleMode()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (state.isSignUp)
                            "Уже есть аккаунт? Войти"
                        else
                            "Нет аккаунта? Зарегистрироваться",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }

            Spacer(Modifier.weight(0.8f))
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModeSwitch(isSignUp: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        listOf(
            false to "Вход",
            true to "Регистрация"
        ).forEach { (signUp, label) ->
            val selected = signUp == isSignUp

            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {
                        if (!selected) onToggle()
                    },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (selected) White else MutedText
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .background(
                            if (selected) AccentBlue
                            else androidx.compose.ui.graphics.Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLast: Boolean = false,
    onDone: () -> Unit = {}
) {
    val focus = LocalFocusManager.current
    val isPassword = keyboardType == KeyboardType.Password
    var passwordVisible by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = White),
        cursorBrush = SolidColor(AccentBlue),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focus.moveFocus(
                    androidx.compose.ui.focus.FocusDirection.Down
                )
            },
            onDone = {
                onDone()
            }
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Black.copy(alpha = 0.55f))
                    .border(
                        1.dp,
                        if (isFocused) AccentBlue else FieldBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(
                        start = 10.dp,
                        end = 4.dp,
                        top = 3.dp,
                        bottom = 3.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    if (value.isEmpty() && !isFocused) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }
                    inner()
                }

                if (isPassword) {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                if (passwordVisible)
                                    R.drawable.ic_visibility_off
                                else
                                    R.drawable.ic_visibility
                            ),
                            contentDescription = if (passwordVisible)
                                "Скрыть пароль"
                            else
                                "Показать пароль",
                            tint = SecondaryText
                        )
                    }
                }
            }
        }
    )

    Spacer(Modifier.height(10.dp))
}
