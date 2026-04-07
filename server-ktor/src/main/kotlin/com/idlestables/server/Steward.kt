package com.idlestables.server

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class RaceSteward(
    private val cfg: AppConfig,
    private val rpc: SolanaRpc,
    private val supabase: SupabaseRest?,
) {
    private val log = LoggerFactory.getLogger("RaceSteward")

    private val loopMs: Long = (System.getenv("STEWARD_LOOP_MS") ?: "15000").toLong()
    private var didNoopTx = false

    fun start(scope: CoroutineScope) {
        scope.launch {
            log.info("Steward loop starting (interval={}ms)", loopMs)
            while (isActive) {
                try {
                    tick()
                } catch (e: Exception) {
                    log.warn("Steward tick failed: {}", e.message)
                }
                delay(loopMs)
            }
        }
    }

    private suspend fun tick() {
        if (cfg.programId.isBlank()) {
            log.info("PROGRAM_ID not set yet; steward idle")
            return
        }
        if (!cfg.stewardKeypairJsonBase64Set && cfg.stewardKeypairPath == null) {
            log.info("No steward key configured yet; set STEWARD_KEYPAIR_JSON_BASE64; steward idle")
            return
        }

        // MVP: sanity check RPC.
        val ok = rpc.getHealth()
        if (!ok) {
            log.warn("RPC health check failed")
            return
        }

        // Step 1: prove we can sign + send a tx from Railway (noop self-transfer) once.
        if (cfg.stewardKeypairJsonBase64Set && !didNoopTx) {
            val acct = runCatching { StewardKeypair.loadFromEnv() }.getOrElse {
                log.warn("Failed to load steward keypair: {}", it.message)
                return
            }
            val sig = TxSender(rpc).sendNoopTransfer(acct)
            didNoopTx = true
            log.info("Steward noop tx sent: {}", sig)
        }

        // Step 2: discover program accounts (tracks/races/horses are all owned by the program)
        val pa = rpc.getProgramAccounts(cfg.programId)
        log.info("Discovered {} program accounts at slot {}", pa.accounts.size, pa.slot)

        // Step 4 (MVP indexing): upsert raw program accounts into Supabase.
        // This gives the mobile app fast reads even before we decode layouts.
        if (supabase != null) {
            val rows = pa.accounts.map {
                ProgramAccountRow(
                    pubkey = it.pubkey,
                    owner_program = it.owner,
                    lamports = it.lamports,
                    data_base64 = it.dataBase64,
                    rpc_slot = pa.slot,
                )
            }
            supabase.upsertProgramAccounts(rows)
            log.info("Upserted {} program_accounts rows", rows.size)
        } else {
            log.info("Supabase not configured; skipping indexing")
        }

        // Step 3 TODO (next): open/resolve race instructions.
        // Once programId + key are live and we have PDA + instruction encoding, we crank races here.
    }
}

fun Application.installSteward(cfg: AppConfig): RaceSteward {
    val rpc = SolanaRpc(primaryUrl = cfg.rpcUrlPrimary, fallbackUrl = cfg.rpcUrlFallback)

    val supabase = if (cfg.supabaseUrl != null && cfg.supabaseServiceRoleKey != null) {
        SupabaseRest(cfg.supabaseUrl, cfg.supabaseServiceRoleKey)
    } else {
        null
    }

    val steward = RaceSteward(cfg, rpc, supabase)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    steward.start(scope)

    environment.monitor.subscribe(ApplicationStopping) {
        scope.launch {
            // no-op; scope will be cancelled by JVM shutdown
        }
    }

    return steward
}
