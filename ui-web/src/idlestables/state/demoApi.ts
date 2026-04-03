import type { Horse, RaceResult, RaceSlot, SilksProfile, Track } from '../types/model';
import { DemoStore, refreshRaces, updateSilks } from './demoStore';

// Tokenomics constants (matches TOKENOMICS_RACING_v0.1.md)
const AUTO_FEE_FACTOR = 0.35;
const MEGA_MULTIPLIER = 2;
const SPLIT = { purse: 0.55, track: 0.25, treasury: 0.1, burn: 0.1 };
const TOP3 = [0.6, 0.25, 0.15] as const;

export type EnterRaceMode = 'MANUAL' | 'AUTO';

function pickTop3(entrants: Horse[]): Array<{ position: 1 | 2 | 3; horseId: string }> {
  // Deterministic-ish: sort by (speed/stamina edge) + a tiny seeded jitter from id
  const score = (h: Horse) => {
    const idJitter = Array.from(h.id).reduce((a, c) => a + c.charCodeAt(0), 0) % 13;
    return h.speed + h.stamina + Math.floor(h.focus / 2) + Math.floor(h.temperament / 4) + idJitter;
  };
  const ordered = [...entrants].sort((a, b) => score(b) - score(a));
  return [
    { position: 1, horseId: ordered[0]!.id },
    { position: 2, horseId: ordered[1]!.id },
    { position: 3, horseId: ordered[2]!.id },
  ];
}

export const demoApi = {
  listTracks(): Track[] {
    return DemoStore.tracks;
  },

  getTrack(id: string): Track | undefined {
    return DemoStore.tracks.find((t) => t.id === id);
  },

  listHorses(): Horse[] {
    return DemoStore.horses;
  },

  getHorse(id: string): Horse | undefined {
    return DemoStore.horses.find((h) => h.id === id);
  },

  listRacesForTrack(trackId: string): RaceSlot[] {
    refreshRaces();
    return DemoStore.racesByTrack[trackId] ?? [];
  },

  listResults(): RaceResult[] {
    return DemoStore.results;
  },

  enterRace(trackId: string, raceId: string, _horseId: string, mode: EnterRaceMode): { ok: true } {
    // purely demo: increment entrantsCount
    const slot = (DemoStore.racesByTrack[trackId] ?? []).find((r) => r.id === raceId);
    if (slot && slot.status === 'OPEN') slot.entrantsCount = Math.min(slot.fieldSize, slot.entrantsCount + 1);

    // when it fills, auto-resolve a result entry (fake)
    if (slot && slot.entrantsCount >= slot.fieldSize) {
      const isMega = !!slot.isMegaCup;
      const baseFee = 100; // low fee for demo
      const fee = (mode === 'MANUAL' ? baseFee : baseFee * AUTO_FEE_FACTOR) * (isMega ? MEGA_MULTIPLIER : 1);
      const pursePool = fee * SPLIT.purse;
      const horses = DemoStore.horses;
      const entrants = horses.slice(0, Math.min(slot.fieldSize, horses.length));
      const top3 = pickTop3(entrants);
      const payout = pursePool * TOP3[0];
      DemoStore.results.unshift({
        raceId: slot.id,
        trackId,
        winnerHorseId: top3[0].horseId,
        top3,
        purseWon: Math.round(payout),
      });
    }

    return { ok: true };
  },

  updateSilks(profile: SilksProfile) {
    updateSilks(profile);
    return { ok: true };
  },
};
