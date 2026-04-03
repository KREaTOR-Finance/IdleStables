import type { Horse, RaceResult, RaceSlot, SilksProfile, Track } from '../types/model';
import { demoHorses, makeDemoRaces, tracks as demoTracks } from '../demoData';

export type DemoState = {
  tracks: Track[];
  horses: Horse[];
  racesByTrack: Record<string, RaceSlot[]>;
  results: RaceResult[];
  silks: SilksProfile;
};

const now = () => Math.floor(Date.now() / 1000);

function makeInitialResults(): RaceResult[] {
  const t = now();
  return [
    {
      raceId: `t1-${t - 3600}`,
      trackId: 't1',
      winnerHorseId: 'h1',
      top3: [
        { position: 1, horseId: 'h1' },
        { position: 2, horseId: 'h3' },
        { position: 3, horseId: 'h2' },
      ],
      purseWon: 420,
    },
  ];
}

export const DemoStore: DemoState = {
  tracks: demoTracks,
  horses: demoHorses,
  racesByTrack: makeDemoRaces(),
  results: makeInitialResults(),
  silks: demoHorses[0]?.silks ?? { pattern: 'SOLID', primaryColor: '#154212' },
};

export function refreshRaces() {
  DemoStore.racesByTrack = makeDemoRaces();
}

export function updateSilks(profile: SilksProfile) {
  DemoStore.silks = profile;
  DemoStore.horses = DemoStore.horses.map((h) => ({ ...h, silks: profile }));
}
