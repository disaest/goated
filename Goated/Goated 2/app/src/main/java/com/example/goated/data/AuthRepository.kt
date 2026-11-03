package com.example.goated.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

object AuthRepository {

    suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Пользователь не создан"))

            supabase.from("profiles").insert(
                Profile(
                    id = userId,
                    username = username,
                    avatarUrl = null,
                    bio = null,
                    email = email
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithLogin(login: String, password: String): Result<Unit> {
        return try {
            val profile = supabase.from("profiles")
                .select { filter { eq("username", login) } }
                .decodeSingleOrNull<Profile>()
                ?: return Result.failure(Exception("Пользователь с таким логином не найден"))

            val email = profile.email
                ?: return Result.failure(Exception("У профиля нет email"))

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (_: Exception) { }
    }

    fun currentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    val isLoggedIn: Boolean
        get() = supabase.auth.currentUserOrNull() != null
}