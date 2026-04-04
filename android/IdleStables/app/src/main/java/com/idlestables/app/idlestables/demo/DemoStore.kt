package com.idlestables.app.idlestables.demo

import com.idlestables.app.idlestables.model.*

object DemoStore {

    data class State(
        val tracks: List<Track> = DemoData.tracks,
        val horses: List<Horse> = DemoData.horses,
        val racesByTrack: Map<String, List<RaceSlot>> = DemoData.makeDemoRaces(),
        val results: List<RaceResult> = makeInitialResults(),
        val silks: SilksProfile = DemoData.horses.firstOrNull()?.silks
            ?: SilksProfile(pattern = SilksPattern.SOLID, primaryColor = "#154212"),
        val racesGeneratedDayStartSec: Long = dayStartSec(nowSec()),
    )

    private fun nowSec(): Long = System.currentTimeMillis() / 1000

    private fun dayStartSec(tSec: Long): Long = tSec - (tSec % 86_400L)

    private fun makeInitialResults(): List<RaceResult> {
        val t = nowSec()
        return listOf(
            RaceResult(
                raceId = "t1-${t - 3600}",
                trackId = "t1",
                winnerHorseId = "h1",
                top3 = listOf(
                    PodiumPlace(position = 1, horseId = "h1"),
                    PodiumPlace(position = 2, horseId = "h3"),
                    PodiumPlace(position = 3, horseId = "h2"),
                ),
                purseWon = 420,
            )
        )
    }

    @Volatile
    var state: State = State()
        private set

    fun setRacesForTrack(trackId: String, races: List<RaceSlot>) {
        val m = state.racesByTrack.toMutableMap()
        m[trackId] = races
        state = state.copy(racesByTrack = m)
    }

    fun setRaceSlot(trackId: String, raceId: String, update: (RaceSlot) -> RaceSlot) {
        val races = state.racesByTrack[trackId].orEmpty()
        val next = races.map { if (it.id == raceId) update(it) else it }
        setRacesForTrack(trackId, next)
    }

    fun ensureRacesUpToDate(nowSec: Long = nowSec()) {
        val dayStart = dayStartSec(nowSec)
        if (state.racesGeneratedDayStartSec != dayStart) {
            // New race day: regenerate schedule.
            state = state.copy(
                racesByTrack = DemoData.makeDemoRaces(nowSec = nowSec),
                racesGeneratedDayStartSec = dayStart,
            )
        }
    }

    fun updateSilks(profile: SilksProfile) {
        state = state.copy(
            silks = profile,
            horses = state.horses.map { it.copy(silks = profile) },
        )
    }

    fun addResult(result: RaceResult) {
        state = state.copy(results = listOf(result) + state.results)
    }
}
