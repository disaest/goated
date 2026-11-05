package com.example.goated.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val submit = { focus.clearFocus(); vm.submit() }

    Box(Modifier.fillMaxSize().background(Black)) {
        Image(
            painter = painterResource(R.drawable.registration_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Text("Goated", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(18.dp))
            Text(
                "Начни оценивать игры, писать рецензии и пополнять свой аккаунт пройденными играми",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            val shape = RoundedCornerShape(18.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Brush.verticalGradient(listOf(Black.copy(alpha = 0.92f), GradientTop.copy(alpha = 0.92f))))
                    .border(1.dp, SecondaryText.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Переключатель «Вход / Регистрация»
                ModeSwitch(isSignUp = state.isSignUp, onToggle = { if (!state.loading) vm.toggleMode() })
                Spacer(Modifier.height(14.dp))

                if (state.isSignUp) {
                    AuthField("email", state.email, vm::onEmailChange, KeyboardType.Email)
                }
                AuthField(
                    label = if (state.isSignUp) "логин" else "логин или email",
                    value = state.login,
                    onValueChange = vm::onLoginChange
                )
                AuthField(
                    label = "пароль",
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    keyboardType = KeyboardType.Password,
                    isLast = !state.isSignUp,
                    onDone = submit
                )
                if (state.isSignUp) {
                    AuthField(
                        "повторите пароль", state.confirmPassword, vm::onConfirmChange,
                        KeyboardType.Password, isLast = true, onDone = submit
                    )
                }

                state.error?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = ErrorRed, modifier = Modifier.padding(bottom = 8.dp))
                }

                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = submit,
                    enabled = !state.loading,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = White,
                        disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(40.dp)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Text(if (state.isSignUp) "Зарегистрироваться" else "Войти", style = MaterialTheme.typography.labelLarge)
                    }
                }
                TextButton(
                    onClick = { if (!state.loading) vm.toggleMode() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (state.isSignUp) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ModeSwitch(isSignUp: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        listOf(false to "Вход", true to "Регистрация").forEach { (signUp, label) ->
            val selected = signUp == isSignUp
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = { if (!selected) onToggle() }, contentPadding = PaddingValues(4.dp)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, color = if (selected) White else MutedText)
                }
                Box(
                    Modifier.fillMaxWidth(0.6f).height(2.dp)
                        .background(if (selected) AccentBlue else androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLast: Boolean = false,
    onDone: () -> Unit = {}
) {
    val focus = LocalFocusManager.current
    val isPassword = keyboardType == KeyboardType.Password
    Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText, modifier = Modifier.padding(start = 2.dp, bottom = 4.dp))
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = White),
        cursorBrush = SolidColor(AccentBlue),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) },
            onDone = { onDone() }
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Black.copy(alpha = 0.55f))
                    .border(1.dp, FieldBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) { inner() }
        }
    )
    Spacer(Modifier.height(10.dp))
}
