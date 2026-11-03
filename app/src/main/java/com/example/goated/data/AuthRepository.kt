package com.example.goated.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Регистрация, вход и выход через Supabase Auth.
 * Пароль хранит только Supabase Auth, в таблицах приложения его нет.
 */
object AuthRepository {

    /** Текущее состояние сессии: загрузка / авторизован / не авторизован. */
    val sessionStatus: StateFlow<SessionStatus> get() = supabase.auth.sessionStatus

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    /**
     * Регистрация: создаёт пользователя в auth.users и профиль в profiles.
     * Профиль создаёт триггер в БД (по username из metadata), а приложение
     * дополнительно проверяет, что он появился, и при необходимости создаёт сам.
     */
    suspend fun signUp(email: String, username: String, password: String) {
        val name = username.trim()
        val mail = email.trim()
        if (isUsernameTaken(name)) throw AppException("Этот логин уже занят")

        supabase.auth.signUpWith(Email) {
            this.email = mail
            this.password = password
            data = buildJsonObject { put("username", name) }
        }
        val userId = currentUserId()
            ?: throw AppException("Аккаунт создан. Подтвердите email по ссылке из письма, затем войдите")
        ensureProfile(userId, name, mail)
    }

    /** Вход по логину (username) или по email и паролю. */
    suspend fun signIn(login: String, password: String) {
        val value = login.trim()
        val email = if ('@' in value) value else findEmailByUsername(value)
            ?: throw AppException("Пользователь с таким логином не найден")

        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    private suspend fun isUsernameTaken(username: String): Boolean {
        val pattern = username.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%")
        return supabase.from("profiles")
            .select(Columns.list("id")) { filter { ilike("username", pattern) } }
            .decodeList<IdOnly>()
            .isNotEmpty()
    }

    /** Email по username через функцию get_email_by_username (см. SQL-миграцию). */
    private suspend fun findEmailByUsername(username: String): String? {
        return try {
            val result = supabase.postgrest.rpc(
                "get_email_by_username",
                buildJsonObject { put("p_username", username) }
            )
            Json.parseToJsonElement(result.data).jsonPrimitive.contentOrNull
        } catch (e: RestException) {
            if ("get_email_by_username" in (e.message ?: "")) {
                throw AppException("Вход по логину не настроен: выполните SQL-миграцию в Supabase или войдите по email")
            }
            throw e
        }
    }

    private suspend fun ensureProfile(userId: String, username: String, email: String) {
        val exists = supabase.from("profiles")
            .select(Columns.list("id")) { filter { eq("id", userId) } }
            .decodeList<IdOnly>()
            .isNotEmpty()
        if (!exists) {
            supabase.from("profiles").insert(NewProfile(id = userId, username = username, email = email))
        }
    }

    @Serializable
    private data class IdOnly(val id: String)
}
