package com.idlestables.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SolanaRpc(
    private val primaryUrl: String,
    private val fallbackUrl: String?,
    private val timeout: Duration = 20.seconds,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val http: HttpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(java.time.Duration.ofMillis(timeout.inWholeMilliseconds))
        .build()

    suspend fun call(method: String, params: JsonArray = buildJsonArray {}): JsonElement {
        // Try primary, then fallback once.
        return try {
            callOnce(primaryUrl, method, params)
        } catch (e: Exception) {
            if (fallbackUrl == null) throw e
            // brief backoff
            delay(250)
            callOnce(fallbackUrl, method, params)
        }
    }

    private suspend fun callOnce(url: String, method: String, params: JsonArray): JsonElement {
        val req = RpcRequest(method = method, params = params)

        val body = json.encodeToString(req)

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(java.time.Duration.ofMillis(timeout.inWholeMilliseconds))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Accept-Encoding", "identity")
            .header("User-Agent", "IdleStables/1.0")
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build()

        val (status, headers, text) = withContext(Dispatchers.IO) {
            val response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            Triple(
                response.statusCode(),
                response.headers().map(),
                response.body() ?: ""
            )
        }

        if (status !in 200..299) {
            throw RpcException("RPC HTTP $status; body=${text.take(300)}")
        }

        if (text.isBlank()) {
            val host = runCatching { URI(url).host }.getOrNull() ?: "(unknown)"
            val clen = headers["content-length"]?.firstOrNull()
            val ctype = headers["content-type"]?.firstOrNull()
            throw RpcException("RPC empty response body (host=$host, content-type=$ctype, content-length=$clen)")
        }

        val resp = runCatching { json.decodeFromString<RpcResponse>(text) }.getOrElse { e ->
            throw RpcException("RPC decode failed: ${e.message}; body=${text.take(300)}")
        }

        if (resp.error != null) {
            throw RpcException("RPC error ${resp.error.code}: ${resp.error.message}")
        }
        return resp.result ?: JsonNull
    }

    suspend fun getHealth(): Boolean {
        return runCatching {
            call("getHealth")
        }.isSuccess
    }

    suspend fun getLatestBlockhash(): String {
        val res = call(
            method = "getLatestBlockhash",
            params = buildJsonArray {
                add(buildJsonObject { put("commitment", "confirmed") })
            }
        )
        // { context: {slot}, value: { blockhash, lastValidBlockHeight } }
        return res.jsonObject["value"]!!.jsonObject["blockhash"]!!.jsonPrimitive.content
    }

    suspend fun getProgramAccounts(programId: String): ProgramAccountsResult {
        val res = call(
            method = "getProgramAccounts",
            params = buildJsonArray {
                add(programId)
                add(
                    buildJsonObject {
                        put("encoding", "base64")
                        put("commitment", "confirmed")
                    }
                )
            }
        )

        // getProgramAccounts returns array; slot is not included, so we fetch slot separately.
        val slotRes = call("getSlot", buildJsonArray { add(buildJsonObject { put("commitment", "confirmed") }) })
        val slot = slotRes.jsonPrimitive.long

        val accounts = res.jsonArray.map { el ->
            val obj = el.jsonObject
            val pubkey = obj["pubkey"]!!.jsonPrimitive.content
            val acc = obj["account"]!!.jsonObject
            val lamports = acc["lamports"]!!.jsonPrimitive.long
            val owner = acc["owner"]!!.jsonPrimitive.content
            val dataArr = acc["data"]!!.jsonArray
            val dataB64 = dataArr[0].jsonPrimitive.content
            ProgramAccount(pubkey, owner, lamports, dataB64)
        }

        return ProgramAccountsResult(slot = slot, accounts = accounts)
    }
}

@Serializable
data class ProgramAccountsResult(
    val slot: Long,
    val accounts: List<ProgramAccount>,
)

@Serializable
data class ProgramAccount(
    val pubkey: String,
    val owner: String,
    val lamports: Long,
    val dataBase64: String,
)

class RpcException(message: String) : RuntimeException(message)

@Serializable
data class RpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int = 1,
    val method: String,
    val params: JsonArray = buildJsonArray { },
)

@Serializable
data class RpcResponse(
    val jsonrpc: String? = null,
    val id: Int? = null,
    val result: JsonElement? = null,
    val error: RpcError? = null,
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)
