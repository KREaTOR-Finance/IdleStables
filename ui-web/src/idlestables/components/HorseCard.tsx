import type { Horse } from '../types/model';

export function HorseCard({ horse, onClick }: { horse: Horse; onClick?: () => void }) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        width: '100%',
        textAlign: 'left',
        borderRadius: 20,
        border: '1px solid rgba(0,0,0,0.06)',
        background: '#fbf9f5',
        boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
        overflow: 'hidden',
        cursor: onClick ? 'pointer' : 'default',
      }}
    >
      <div style={{ padding: 14, display: 'flex', gap: 12, alignItems: 'baseline' }}>
        <div
          style={{
            padding: '6px 10px',
            borderRadius: 999,
            background: 'rgba(21,66,18,0.12)',
            color: '#154212',
            fontSize: 12,
            fontWeight: 700,
            letterSpacing: 0.6,
          }}
        >
          {horse.tier}
        </div>
        <div style={{ fontFamily: 'serif', fontSize: 18, fontWeight: 700, color: '#1c1c1c' }}>{horse.name}</div>
      </div>

      <div style={{ height: 220, background: 'linear-gradient(180deg, rgba(21,66,18,0.08), rgba(21,66,18,0))' }}>
        {/* Placeholder: in production this renders the deterministic horse portrait from layers + silks */}
        <div
          style={{
            height: '100%',
            display: 'grid',
            placeItems: 'center',
            color: 'rgba(0,0,0,0.45)',
            fontSize: 12,
          }}
        >
          horse portrait (seed {horse.seed})
        </div>
      </div>

      <div style={{ padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ color: '#154212', fontSize: 18, fontWeight: 800 }}>+{horse.dailyPurse.toFixed(1)} P/day</div>
        <div style={{ display: 'flex', gap: 12, color: 'rgba(0,0,0,0.65)', fontSize: 12, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
          <div>SPD {horse.speed}</div>
          <div>STA {horse.stamina}</div>
          <div>FOC {horse.focus}</div>
          <div>TEMP {horse.temperament}</div>
        </div>
      </div>
    </button>
  );
}
