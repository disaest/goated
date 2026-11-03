package com.example.goated.data

import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException

/** Ошибка с уже готовым текстом для пользователя. */
class AppException(message: String) : Exception(message)

/** Переводит любую ошибку Supabase/сети в понятное сообщение на русском. */
fun Throwable.toUserMessage(): String {
    if (this is AppException) return message ?: "Ошибка"
    val text = (message ?: "").lowercase()
    return when {
        this is HttpRequestTimeoutException || this is ConnectTimeoutException ->
            "Сервер не отвечает. Проверьте подключение к интернету"
        this is HttpRequestException || this is IOException ->
            "Нет подключения к интернету"
        "invalid login credentials" in text -> "Неверный логин или пароль"
        "user already registered" in text || "already been registered" in text ->
            "Этот email уже зарегистрирован"
        "email not confirmed" in text -> "Email не подтверждён. Перейдите по ссылке из письма"
        "password should be at least" in text || "weak" in text ->
            "Пароль слишком простой: минимум 6 символов"
        "unable to validate email" in text || "invalid format" in text || "email address" in text && "invalid" in text ->
            "Некорректный email"
        "rate limit" in text || "too many" in text -> "Слишком много попыток. Попробуйте позже"
        "database error saving new user" in text -> "Не удалось создать профиль. Возможно, логин уже занят"
        "не более четырёх" in text -> "В избранном может быть не более четырёх игр"
        "row-level security" in text || "permission denied" in text ->
            "Нет доступа к данным. Выполните SQL-миграцию Supabase (supabase/goated_fix_auth_reviews.sql)"
        "duplicate key" in text && "username" in text -> "Этот логин уже занят"
        else -> "Ошибка: ${message ?: javaClass.simpleName}"
    }
}
