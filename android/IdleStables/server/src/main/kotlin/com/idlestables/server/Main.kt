package com.idlestables.server

import com.idlestables.core.demo.DemoRepository
import com.idlestables.core.model.EnterRaceMode
import com.idlestables.core.model.SilksProfile
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("IDLESTABLES_PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val repo = DemoRepository()

    routing {
        get("/health") {
            call.respond(mapOf("ok" to true))
        }

        get("/state") {
            repo.refreshRaces()
            call.respond(repo.state.value)
        }

        get("/tracks") {
            call.respond(repo.listTracks())
        }

        get("/tracks/{id}/races") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(repo.listRacesForTrack(id))
        }

        get("/horses") {
            call.respond(repo.listHorses())
        }

        get("/results") {
            call.respond(repo.listResults())
        }

        post("/enterRace") {
            val body = call.receive<EnterRaceRequest>()
            val ok = repo.enterRace(
                trackId = body.trackId,
                raceId = body.raceId,
                horseId = body.horseId,
                mode = body.mode
            )
            if (!ok) return@post call.respond(HttpStatusCode.BadRequest, mapOf("ok" to false))
            call.respond(mapOf("ok" to true))
        }

        post("/silks") {
            val profile = call.receive<SilksProfile>()
            repo.updateSilks(profile)
            call.respond(mapOf("ok" to true))
        }
    }
}

@Serializable
data class EnterRaceRequest(
    val trackId: String,
    val raceId: String,
    val horseId: String,
    val mode: EnterRaceMode,
)
