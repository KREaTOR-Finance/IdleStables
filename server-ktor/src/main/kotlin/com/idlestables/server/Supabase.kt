package com.idlestables.server

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SupabaseRest(private val url: String, private val serviceRoleKey: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) { json(this@SupabaseRest.json) }
        expectSuccess = false
    }

    suspend fun upsertProgramAccounts(rows: List<ProgramAccountRow>) {
        if (rows.isEmpty()) return

        val endpoint = "$url/rest/v1/program_accounts?on_conflict=pubkey"
        val resp = http.post(endpoint) {
            header("apikey", serviceRoleKey)
            header("Authorization", "Bearer $serviceRoleKey")
            header("Prefer", "resolution=merge-duplicates")
            contentType(ContentType.Application.Json)
            setBody(rows)
        }

        if (!resp.status.isSuccess()) {
            val body = runCatching { resp.body<String>() }.getOrNull()
            throw RuntimeException("Supabase upsert failed (${resp.status}): ${body ?: ""}")
        }
    }
}

@Serializable
data class ProgramAccountRow(
    val pubkey: String,
    val owner_program: String,
    val lamports: Long,
    val data_base64: String,
    val data_encoding: String = "base64",
    val rpc_slot: Long? = null,
)
