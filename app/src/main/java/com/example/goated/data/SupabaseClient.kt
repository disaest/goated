package com.example.goated.data

import com.example.goated.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlin.time.Duration.Companion.seconds

/**
 * Единственный клиент Supabase на всё приложение.
 * URL и publishable-ключ берутся из BuildConfig (см. local.properties).
 * Auth сам сохраняет сессию на устройстве, поэтому после перезапуска
 * пользователь остаётся авторизованным.
 */
val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    requestTimeout = 15.seconds
    install(Auth)
    install(Postgrest)
}
