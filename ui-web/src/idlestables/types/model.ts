export type HorseTier = 'YEARLING' | 'RACEHORSE' | 'GRADED' | 'LEGENDARY';

export type SilksPattern =
  | 'SOLID'
  | 'STRIPES'
  | 'QUARTERS'
  | 'DIAGONAL'
  | 'CHEVRON'
  | 'HOOPS';

export interface SilksProfile {
  pattern: SilksPattern;
  primaryColor: string; // hex
  secondaryColor?: string; // hex
  accentColor?: string; // hex
}

export interface HorseVisualLayers {
  poseId: number;
  coatId: number;
  markingsId: number;
  maneId: number;
  backgroundId: number;
}

export interface Horse {
  id: string;
  name: string;
  tier: HorseTier;
  seed: string; // u64 as string
  layers: HorseVisualLayers;
  silks: SilksProfile;

  dailyPurse: number;

  // Racing + breeding traits (do NOT affect emissions)
  speed: number;
  stamina: number;
  focus: number;
  temperament: number;

  breedsAsSireLeft: number;
  breedsAsDamLeft: number;
  yieldDecayBps: number; // 0..10000

  trained?: {
    speed?: boolean;
    stamina?: boolean;
    focus?: boolean;
    temperament?: boolean;
  };
}

export type RaceStatus = 'OPEN' | 'CLOSED' | 'RESOLVED';

export interface Track {
  id: string;
  name: string;
  cadenceMinutes: 30 | 60;
  fieldSize: 12 | 18;
  // race type is implied by distance; affects Speed/Stamina weighting
  distanceLabel: '6F' | '1.5M' | 'MEGA';
}

export interface RaceSlot {
  id: string;
  trackId: string;
  scheduledTs: number; // unix seconds
  entryClosesTs: number; // scheduledTs - 60
  status: RaceStatus;
  entrantsCount: number;
  fieldSize: 12 | 18;
  isMegaCup?: boolean;
  entryFeeMultiplier?: number; // e.g. 2 for Mega Cup
}

export interface RaceResult {
  raceId: string;
  trackId: string;
  winnerHorseId: string;
  top3: Array<{ position: 1 | 2 | 3; horseId: string }>;
  purseWon: number;
}
