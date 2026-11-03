package com.example.goated.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://qgljrfbpscryhdfbqfyh.supabase.co",
    supabaseKey = "sb_publishable_xwltOcR2WJcNE6mbPVnToA_UU3mzGID"
) {
    install(Auth)
    install(Postgrest)
}