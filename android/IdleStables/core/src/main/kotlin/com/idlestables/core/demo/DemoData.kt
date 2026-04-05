package com.idlestables.core.demo

import com.idlestables.core.model.*
import kotlin.math.absoluteValue

object DemoData {

    val horses: List<Horse> = listOf(
        Horse(
            id = "h1",
            name = "Velvet Thunder",
            tier = HorseTier.GRADED,
            seed = "184467440737095516",
            layers = HorseLayers(poseId = 1, coatId = 2, markingsId = 1, maneId = 2, backgroundId = 1),
            silks = SilksProfile(pattern = SilksPattern.QUARTERS, primaryColor = "#154212", secondaryColor = "#E8D9A8"),
            dailyPurse = 2.4,
            speed = 98,
            stamina = 94,
            focus = 88,
            temperament = 90,
            breedsAsSireLeft = 12,
            breedsAsDamLeft = 6,
            yieldDecayBps = 250,
        ),
        Horse(
            id = "h2",
            name = "Morning Mist",
            tier = HorseTier.YEARLING,
            seed = "998877665544332211",
            layers = HorseLayers(poseId = 2, coatId = 1, markingsId = 2, maneId = 1, backgroundId = 2),
            silks = SilksProfile(pattern = SilksPattern.SOLID, primaryColor = "#7A3B2E"),
            dailyPurse = 0.9,
            speed = 70,
            stamina = 85,
            focus = 76,
            temperament = 82,
            breedsAsSireLeft = 12,
            breedsAsDamLeft = 6,
            yieldDecayBps = 0,
        ),
        Horse(
            id = "h3",
            name = "Onyx Legacy",
            tier = HorseTier.LEGENDARY,
            seed = "123456789012345678",
            layers = HorseLayers(poseId = 3, coatId = 3, markingsId = 0, maneId = 3, backgroundId = 1),
            silks = SilksProfile(pattern = SilksPattern.DIAGONAL, primaryColor = "#0A0A0A", secondaryColor = "#154212"),
            dailyPurse = 4.8,
            speed = 105,
            stamina = 102,
            focus = 96,
            temperament = 92,
            breedsAsSireLeft = 6,
            breedsAsDamLeft = 3,
            yieldDecayBps = 500,
        ),
    )

    val tracks: List<Track> = listOf(
        Track(id = "t1", name = "Vedauwoo Park (6f)", cadenceMinutes = 60, fieldSize = 12, distanceLabel = "6F"),
        Track(id = "t2", name = "Canoli Downs (1.5m)", cadenceMinutes = 30, fieldSize = 12, distanceLabel = "1.5M"),
    )

    /**
     * Demo schedule rules:
     * - Generate the next [slotsAhead] races aligned to cadence.
     * - "Mega Cup" shows **exactly once per track race day**.
     *   In this demo we define race day as UTC day (00:00..23:59 UTC) for determinism.
     * - Mega Cup has: 18 field + 2x fee.
     */
    fun makeDemoRaces(
        nowSec: Long = System.currentTimeMillis() / 1000,
        slotsAhead: Int = 12,
    ): Map<String, List<RaceSlot>> {
        fun dayStartSec(tSec: Long): Long = tSec - (tSec % 86_400L)

        fun mk(trackId: String, fieldSize: Int, cadenceMinutes: Int): List<RaceSlot> {
            val res = ArrayList<RaceSlot>()
            val cadenceSec = cadenceMinutes * 60L
            val todayStart = dayStartSec(nowSec)
            val todayEndExclusive = todayStart + 86_400L

            // Mega Cup time = last cadence-aligned slot of the day.
            val lastMoment = todayEndExclusive - 1
            val megaScheduledTs = lastMoment - (lastMoment % cadenceSec)

            // Build next N slots.
            var i = 1
            while (res.size < slotsAhead) {
                val scheduledTs = nowSec - (nowSec % cadenceSec) + i * cadenceSec
                val isMegaCup = (scheduledTs == megaScheduledTs)

                val slotFieldSize = if (isMegaCup) 18 else fieldSize
                val entrantsCount = ((scheduledTs + trackId.hashCode()).toInt().absoluteValue % slotFieldSize)

                res += RaceSlot(
                    id = "$trackId-$scheduledTs",
                    trackId = trackId,
                    scheduledTs = scheduledTs,
                    entryClosesTs = scheduledTs - 60,
                    status = RaceStatus.OPEN,
                    entrantsCount = entrantsCount,
                    fieldSize = slotFieldSize,
                    isMegaCup = isMegaCup,
                    entryFeeMultiplier = if (isMegaCup) 2 else 1,
                )

                i++
            }

            return res
        }

        return mapOf(
            "t1" to mk("t1", 12, 60),
            "t2" to mk("t2", 12, 30),
        )
    }
}
