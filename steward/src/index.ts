import 'dotenv/config';
import cron from 'node-cron';

// IdleStables Race Steward (MVP)
// This service is intended to run on Railway.
// It will:
// - Open scheduled races for each track
// - Close entries at T-60s
// - Fill from Circuit pool, then House pool
// - Resolve at :00 / :30
//
// NOTE: On-chain integration (Anchor client) is stubbed for now.

type TrackCfg = {
  id: string;
  name: string;
  cadenceMinutes: 30 | 60;
  distance: '6F' | '1.5M';
  baseEntryFee: number; // in PURSE smallest units
  megaCupAt?: string; // cron-like HH:MM, optional
};

const TRACKS: TrackCfg[] = [
  {
    id: 't1',
    name: 'Vedauwoo Park',
    cadenceMinutes: 60,
    distance: '6F',
    baseEntryFee: Number(process.env.TRACK1_BASE_FEE ?? 1000),
  },
  {
    id: 't2',
    name: 'Canoli Downs',
    cadenceMinutes: 30,
    distance: '1.5M',
    baseEntryFee: Number(process.env.TRACK2_BASE_FEE ?? 1000),
  },
];

function nowSec() {
  return Math.floor(Date.now() / 1000);
}

function nextSlotTs(cadenceMinutes: number, offsetSlots = 1) {
  const cadenceSec = cadenceMinutes * 60;
  const n = nowSec();
  return n - (n % cadenceSec) + offsetSlots * cadenceSec;
}

async function openRacesTick() {
  // Open the next slot for each track (idempotent on-chain)
  for (const t of TRACKS) {
    const scheduledTs = nextSlotTs(t.cadenceMinutes, 1);
    const isMegaCup = false; // later: once per race day
    console.log(`[open] ${t.name} @ ${new Date(scheduledTs * 1000).toISOString()} mega=${isMegaCup}`);
    // TODO: call idlestables::open_race(track, scheduledTs, isMegaCup)
  }
}

async function closeAndFillTick() {
  // Close races that are within 60s of schedule, fill remaining seats
  console.log(`[close] tick ${new Date().toISOString()}`);
  // TODO: find races where now in [entry_closes_ts, scheduled_ts) and call close_and_fill
}

async function resolveTick() {
  // Resolve races that hit scheduled time
  console.log(`[resolve] tick ${new Date().toISOString()}`);
  // TODO: find races where now >= scheduled_ts and status=open/closed and resolve
}

// Every minute: open future slots + close/fill
cron.schedule('* * * * *', async () => {
  try {
    await openRacesTick();
    await closeAndFillTick();
  } catch (e) {
    console.error('minute tick error', e);
  }
});

// Every 30 seconds: resolve cadence; align with :00/:30 in practice (Railway cron is minute-based; use loop)
setInterval(() => {
  resolveTick().catch((e) => console.error('resolve tick error', e));
}, 30_000);

console.log('IdleStables steward running', { tracks: TRACKS.map((t) => ({ id: t.id, cadence: t.cadenceMinutes, distance: t.distance })) });
