import type { RaceSlot, Track } from '../types/model';

function fmtTime(tsSec: number) {
  const d = new Date(tsSec * 1000);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function TrackSchedule({ track, races, onEnter }: { track: Track; races: RaceSlot[]; onEnter?: (race: RaceSlot) => void }) {
  return (
    <div
      style={{
        borderRadius: 20,
        border: '1px solid rgba(0,0,0,0.06)',
        background: '#ffffff',
        padding: 16,
        boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 12 }}>
        <div style={{ fontFamily: 'serif', fontSize: 20, fontWeight: 800 }}>{track.name}</div>
        <div style={{ color: 'rgba(0,0,0,0.55)', fontSize: 12 }}>
          {track.distanceLabel} • resolves :00 / :30 • entry closes 60s prior • field {track.fieldSize}
        </div>
      </div>

      <div style={{ display: 'grid', gap: 10 }}>
        {races.map((r) => (
          <div
            key={r.id}
            style={{
              display: 'grid',
              gridTemplateColumns: '100px 1fr 120px',
              gap: 12,
              alignItems: 'center',
              padding: 12,
              borderRadius: 14,
              background: 'rgba(21,66,18,0.06)',
            }}
          >
            <div style={{ fontWeight: 800, color: '#154212' }}>{fmtTime(r.scheduledTs)}</div>
            <div style={{ color: 'rgba(0,0,0,0.7)' }}>
              {r.isMegaCup ? 'Mega Cup • ' : ''}
              {r.entrantsCount}/{r.fieldSize} entered • {r.status}
              {r.entryFeeMultiplier && r.entryFeeMultiplier !== 1 ? ` • ${r.entryFeeMultiplier}x fee` : ''}
            </div>
            <button
              type="button"
              disabled={r.status !== 'OPEN'}
              onClick={() => onEnter?.(r)}
              style={{
                borderRadius: 12,
                padding: '10px 12px',
                border: 'none',
                background: r.isMegaCup ? '#8B6B1F' : r.status === 'OPEN' ? '#154212' : 'rgba(0,0,0,0.15)',
                color: 'white',
                fontWeight: 800,
                cursor: r.status === 'OPEN' ? 'pointer' : 'not-allowed',
              }}
            >
              Enter
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
