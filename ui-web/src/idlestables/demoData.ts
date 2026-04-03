import type { Horse, RaceSlot, Track } from './types/model';

export const demoHorses: Horse[] = [
  {
    id: 'h1',
    name: 'Velvet Thunder',
    tier: 'GRADED',
    seed: '184467440737095516',
    layers: { poseId: 1, coatId: 2, markingsId: 1, maneId: 2, backgroundId: 1 },
    silks: { pattern: 'QUARTERS', primaryColor: '#154212', secondaryColor: '#E8D9A8' },
    dailyPurse: 2.4,
    speed: 98,
    stamina: 94,
    focus: 88,
    temperament: 90,
    breedsAsSireLeft: 12,
    breedsAsDamLeft: 6,
    yieldDecayBps: 250,
  },
  {
    id: 'h2',
    name: 'Morning Mist',
    tier: 'YEARLING',
    seed: '998877665544332211',
    layers: { poseId: 2, coatId: 1, markingsId: 2, maneId: 1, backgroundId: 2 },
    silks: { pattern: 'SOLID', primaryColor: '#7A3B2E' },
    dailyPurse: 0.9,
    speed: 70,
    stamina: 85,
    focus: 76,
    temperament: 82,
    breedsAsSireLeft: 12,
    breedsAsDamLeft: 6,
    yieldDecayBps: 0,
  },
  {
    id: 'h3',
    name: 'Onyx Legacy',
    tier: 'LEGENDARY',
    seed: '123456789012345678',
    layers: { poseId: 3, coatId: 3, markingsId: 0, maneId: 3, backgroundId: 1 },
    silks: { pattern: 'DIAGONAL', primaryColor: '#0A0A0A', secondaryColor: '#154212' },
    dailyPurse: 4.8,
    speed: 105,
    stamina: 102,
    focus: 96,
    temperament: 92,
    breedsAsSireLeft: 6,
    breedsAsDamLeft: 3,
    yieldDecayBps: 500,
  },
];

export const tracks: Track[] = [
  { id: 't1', name: 'Vedauwoo Park (6f)', cadenceMinutes: 60, fieldSize: 12, distanceLabel: '6F' },
  { id: 't2', name: 'Canoli Downs (1.5m)', cadenceMinutes: 30, fieldSize: 12, distanceLabel: '1.5M' },
];

export function makeDemoRaces(nowSec = Math.floor(Date.now() / 1000)): Record<string, RaceSlot[]> {
  const mk = (trackId: string, fieldSize: 12 | 18, cadenceMinutes: 30 | 60) => {
    const res: RaceSlot[] = [];
    const cadenceSec = cadenceMinutes * 60;
    // Next 6 slots
    for (let i = 1; i <= 6; i++) {
      const scheduledTs = nowSec - (nowSec % cadenceSec) + i * cadenceSec;
      const isMegaCup = i === 6; // demo: last slot is the featured Mega Cup
      const slotFieldSize = isMegaCup ? (18 as const) : fieldSize;
      res.push({
        id: `${trackId}-${scheduledTs}`,
        trackId,
        scheduledTs,
        entryClosesTs: scheduledTs - 60,
        status: 'OPEN',
        entrantsCount: Math.floor(Math.random() * slotFieldSize),
        fieldSize: slotFieldSize,
        isMegaCup,
        entryFeeMultiplier: isMegaCup ? 2 : 1,
      });
    }
    return res;
  };

  return {
    t1: mk('t1', 12, 60),
    t2: mk('t2', 12, 30),
  };
}
