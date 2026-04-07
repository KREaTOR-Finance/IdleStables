package com.idlestables.server

data class AppConfig(
    val rpcUrlPrimary: String,
    val rpcUrlFallback: String?,
    val programId: String,
    val supabaseUrl: String?,
    val supabaseAnonKey: String?,
    val supabaseServiceRoleKey: String?,
    val stewardKeypairPath: String?,
    val stewardKeypairJsonBase64Set: Boolean,
) {
    companion object {
        fun fromEnv(): AppConfig {
            fun env(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

            return AppConfig(
                rpcUrlPrimary = env("RPC_URL_PRIMARY") ?: "https://api.devnet.solana.com",
                rpcUrlFallback = env("RPC_URL_FALLBACK"),
                programId = env("PROGRAM_ID") ?: "",
                supabaseUrl = env("SUPABASE_URL"),
                supabaseAnonKey = env("SUPABASE_ANON_KEY"),
                supabaseServiceRoleKey = env("SUPABASE_SERVICE_ROLE_KEY"),
                stewardKeypairPath = env("STEWARD_KEYPAIR_PATH"),
                stewardKeypairJsonBase64Set = (env("STEWARD_KEYPAIR_JSON_BASE64") != null),
            )
        }
    }
}
