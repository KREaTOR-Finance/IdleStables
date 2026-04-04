package com.idlestables.app.idlestables.demo

import com.idlestables.app.idlestables.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.floor

/**
 * In-memory demo repository mirroring ui-web's demoApi.ts + demoStore.ts.
 */
class DemoRepository {

    // Tokenomics constants (matches ui-web demoApi.ts)
    private val AUTO_FEE_FACTOR = 0.35
    private val MEGA_MULTIPLIER = 2
    private val SPLIT_PURSE = 0.55
    private val TOP3 = doubleArrayOf(0.6, 0.25, 0.15)

    private val _state = MutableStateFlow(DemoStore.state)
    val state: StateFlow<DemoStore.State> = _state

    private fun syncFromStore() {
        _state.value = DemoStore.state
    }

    fun refreshRaces() {
        DemoStore.ensureRacesUpToDate()
        syncFromStore()
    }

    fun listTracks(): List<Track> = _state.value.tracks

    fun getTrack(id: String): Track? = _state.value.tracks.find { it.id == id }

    fun listHorses(): List<Horse> = _state.value.horses

    fun getHorse(id: String): Horse? = _state.value.horses.find { it.id == id }

    fun listRacesForTrack(trackId: String): List<RaceSlot> {
        // Important: don't regenerate every call, or we wipe entrantsCount changes.
        DemoStore.ensureRacesUpToDate()
        syncFromStore()
        return _state.value.racesByTrack[trackId].orEmpty()
    }

    fun listResults(): List<RaceResult> = _state.value.results

    fun updateSilks(profile: SilksProfile) {
        DemoStore.updateSilks(profile)
        syncFromStore()
    }

    private fun pickTop3(entrants: List<Horse>): List<PodiumPlace> {
        fun score(h: Horse): Int {
            val idJitter = h.id.sumOf { it.code } % 13
            return h.speed + h.stamina + floor(h.focus / 2.0).toInt() + floor(h.temperament / 4.0).toInt() + idJitter
        }
        val ordered = entrants.sortedByDescending { score(it) }
        return listOf(
            PodiumPlace(1, ordered[0].id),
            PodiumPlace(2, ordered[1].id),
            PodiumPlace(3, ordered[2].id),
        )
    }

    fun enterRace(trackId: String, raceId: String, horseId: String, mode: EnterRaceMode): Boolean {
        // demo: increment entrantsCount in slot
        val current = _state.value
        val trackRaces = current.racesByTrack[trackId].orEmpty()
        val slot = trackRaces.find { it.id == raceId } ?: return false
        if (slot.status != RaceStatus.OPEN) return false

        val updatedSlot = slot.copy(entrantsCount = minOf(slot.fieldSize, slot.entrantsCount + 1))

        // write back into store/state
        DemoStore.setRaceSlot(trackId, raceId) { updatedSlot }
        syncFromStore()

        // when it fills, auto-resolve a result entry (fake)
        if (updatedSlot.entrantsCount >= updatedSlot.fieldSize) {
            val isMega = updatedSlot.isMegaCup
            val baseFee = 100.0
            val fee = (if (mode == EnterRaceMode.MANUAL) baseFee else baseFee * AUTO_FEE_FACTOR) * (if (isMega) MEGA_MULTIPLIER else 1)
            val pursePool = fee * SPLIT_PURSE

            val horses = _state.value.horses
            val entrants = horses.take(minOf(updatedSlot.fieldSize, horses.size))
            if (entrants.size >= 3) {
                val top3 = pickTop3(entrants)
                val payout = pursePool * TOP3[0]
                DemoStore.addResult(
                    RaceResult(
                        raceId = updatedSlot.id,
                        trackId = trackId,
                        winnerHorseId = top3[0].horseId,
                        top3 = top3,
                        purseWon = payout.toInt(),
                    )
                )
                syncFromStore()
            }
        }

        return true
    }
}
