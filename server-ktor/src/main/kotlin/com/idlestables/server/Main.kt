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
import java.net.URI

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

    fun redactUrl(url: String): String {
        return runCatching {
            val u = URI(url)
            val base = buildString {
                append(u.scheme)
                append("://")
                append(u.host ?: "")
                if (u.port != -1) append(":").append(u.port)
                if (!u.path.isNullOrBlank()) append(u.path)
            }
            if (u.query.isNullOrBlank()) base else "$base?REDACTED"
        }.getOrElse {
            // fallback: if it contains query params, redact them.
            url.substringBefore('?') + if (url.contains('?')) "?REDACTED" else ""
        }
    }

    val cfg = AppConfig.fromEnv()
    val steward = installSteward(cfg)

    routing {
        get("/health") {
            call.respond(
                HealthResponse(
                    ok = true,
                    rpcPrimary = redactUrl(cfg.rpcUrlPrimary),
                    rpcFallbackSet = (cfg.rpcUrlFallback != null),
                    programIdSet = cfg.programId.isNotBlank(),
                    supabaseConfigured = (cfg.supabaseUrl != null && cfg.supabaseServiceRoleKey != null),
                    stewardKeyConfigured = (cfg.stewardKeypairJsonBase64Set || cfg.stewardKeypairPath != null),
                )
            )
        }

        // MVP placeholders — will be backed by RPC + Supabase index.
        // IMPORTANT: respond with a statically-serializable type (not `emptyList<Any>()`),
        // otherwise Ktor/Kotlinx may try to serialize the runtime `EmptyList` polymorphically.
        get("/tracks") { call.respond(emptyList<TrackDto>()) }
        get("/track/{id}/races") { call.respond(emptyList<RaceDto>()) }
        get("/wallet/{pubkey}/horses") { call.respond(emptyList<HorseDto>()) }
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

@Serializable
data class TrackDto(
    val track_pubkey: String? = null,
)

@Serializable
data class RaceDto(
    val race_pubkey: String? = null,
)

@Serializable
data class HorseDto(
    val horse_pubkey: String? = null,
)
