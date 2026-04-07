package com.idlestables.server

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.programs.SystemProgram
import java.util.Base64

class TxSender(private val rpc: SolanaRpc) {

    /**
     * Smoke-test transaction: a 0-lamport transfer to self.
     * This proves signing + sendTransaction pipeline works.
     */
    suspend fun sendNoopTransfer(feePayer: Account): String {
        val blockhash = rpc.getLatestBlockhash()

        val tx = Transaction()
        tx.addInstruction(
            SystemProgram.transfer(
                feePayer.publicKey,
                feePayer.publicKey,
                0L
            )
        )
        tx.setRecentBlockHash(blockhash)
        // solanaj uses the first signer as fee payer in most flows
        tx.sign(feePayer)

        val serialized = tx.serialize()
        val b64 = Base64.getEncoder().encodeToString(serialized)

        val res = rpc.call(
            method = "sendTransaction",
            params = buildJsonArray {
                add(JsonPrimitive(b64))
                add(
                    buildJsonObject {
                        put("encoding", "base64")
                        put("skipPreflight", true)
                        put("preflightCommitment", "confirmed")
                    }
                )
            }
        )

        // sendTransaction returns signature string
        return res.jsonPrimitive.content
    }
}
