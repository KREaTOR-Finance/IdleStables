package com.idlestables.server

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.p2p.solanaj.core.Account
import java.util.Base64

object StewardKeypair {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Expects env var STEWARD_KEYPAIR_JSON_BASE64 which is base64-encoded JSON array of 64 ints
     * (standard Solana keypair file content).
     */
    fun loadFromEnv(): Account {
        val b64 = System.getenv("STEWARD_KEYPAIR_JSON_BASE64")?.takeIf { it.isNotBlank() }
            ?: error("Missing STEWARD_KEYPAIR_JSON_BASE64")

        val raw = String(Base64.getDecoder().decode(b64))
        val arr = json.parseToJsonElement(raw).jsonArray
        require(arr.size == 64) { "Expected 64-byte secret key array, got ${arr.size}" }
        val secret = ByteArray(64)
        for (i in 0 until 64) {
            secret[i] = arr[i].jsonPrimitive.content.toInt().toByte()
        }
        return Account(secret)
    }
}
