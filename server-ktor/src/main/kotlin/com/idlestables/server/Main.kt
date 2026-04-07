package com.idlestables.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = (System.getenv("PORT") ?: "8080").toInt(), host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
            }
        )
    }

    val cfg = AppConfig.fromEnv()
    val steward = installSteward(cfg)

    routing {
        get("/health") {
            call.respond(
                HealthResponse(
                    ok = true,
                    rpcPrimary = cfg.rpcUrlPrimary,
                    rpcFallbackSet = (cfg.rpcUrlFallback != null),
                    programIdSet = cfg.programId.isNotBlank(),
                    supabaseConfigured = (cfg.supabaseUrl != null && cfg.supabaseServiceRoleKey != null),
                    stewardKeyConfigured = (cfg.stewardKeypairJsonBase64Set || cfg.stewardKeypairPath != null),
                )
            )
        }

        // MVP placeholders — will be backed by RPC + Supabase index.
        get("/tracks") { call.respond(emptyList<Any>()) }
        get("/track/{id}/races") { call.respond(emptyList<Any>()) }
        get("/wallet/{pubkey}/horses") { call.respond(emptyList<Any>()) }
    }
}

@Serializable
data class HealthResponse(
    val ok: Boolean,
    val rpcPrimary: String,
    val rpcFallbackSet: Boolean,
    val programIdSet: Boolean,
    val supabaseConfigured: Boolean,
    val stewardKeyConfigured: Boolean,
)
