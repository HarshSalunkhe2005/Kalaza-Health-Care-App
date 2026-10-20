package com.kalazacare.app.data.remote

import com.kalazacare.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

// Supplied at build time from local.properties (see local.properties.example). The anon key is safe to
// ship in the client: every request it makes is still subject to the project's Row Level Security.
private val SUPABASE_URL = BuildConfig.SUPABASE_URL
private val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY

object SupabaseClients {
    val main: SupabaseClient by lazy {
        createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            // Functions — only used to call admin-reset-password (Super Admin
            // setting a staff member's, or their own, password). Everything else
            // stays plain Postgrest/RLS; this is the one action that genuinely
            // needs the service-role key, which only an Edge Function can hold.
            install(Functions)
        }
    }

    /**
     * A second, independent client used only for creating new staff accounts.
     * Supabase Auth's signUpWith(Email) signs the caller in as whichever user
     * just signed up, on that client instance — running staff creation here
     * keeps the Super Admin signed in on [main] throughout (mirrors the old
     * secondary-FirebaseAuth-instance trick).
     */
    val staffCreation: SupabaseClient by lazy {
        createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_ANON_KEY) {
            install(Auth)
        }
    }
}
