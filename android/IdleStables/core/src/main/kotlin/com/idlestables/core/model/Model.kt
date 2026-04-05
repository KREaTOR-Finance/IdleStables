package com.idlestables.core.model

import kotlinx.serialization.Serializable

enum class HorseTier { YEARLING, GRADED, LEGENDARY }

enum class SilksPattern { SOLID, QUARTERS, DIAGONAL }

@Serializable
data class SilksProfile(
    val pattern: SilksPattern,
    val primaryColor: String,
    val secondaryColor: String? = null,
)

@Serializable
data class HorseLayers(
    val poseId: Int,
    val coatId: Int,
    val markingsId: Int,
    val maneId: Int,
    val backgroundId: Int,
)

@Serializable
data class Horse(
    val id: String,
    val name: String,
    val tier: HorseTier,
    val seed: String,
    val layers: HorseLayers,
    val silks: SilksProfile,
    val dailyPurse: Double,
    val speed: Int,
    val stamina: Int,
    val focus: Int,
    val temperament: Int,
    val breedsAsSireLeft: Int,
    val breedsAsDamLeft: Int,
    val yieldDecayBps: Int,
)

@Serializable
data class Track(
    val id: String,
    val name: String,
    val cadenceMinutes: Int,
    val fieldSize: Int,
    val distanceLabel: String,
)

enum class RaceStatus { OPEN, LOCKED, RUNNING, FINISHED }

@Serializable
data class RaceSlot(
    val id: String,
    val trackId: String,
    val scheduledTs: Long,
    val entryClosesTs: Long,
    val status: RaceStatus,
    val entrantsCount: Int,
    val fieldSize: Int,
    val isMegaCup: Boolean,
    val entryFeeMultiplier: Int,
)

@Serializable
data class PodiumPlace(val position: Int, val horseId: String)

@Serializable
data class RaceResult(
    val raceId: String,
    val trackId: String,
    val winnerHorseId: String,
    val top3: List<PodiumPlace>,
    val purseWon: Int,
)

enum class EnterRaceMode { MANUAL, AUTO }
