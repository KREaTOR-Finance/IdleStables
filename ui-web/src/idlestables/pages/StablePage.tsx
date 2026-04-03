import { useMemo, useState } from 'react';
import { MyStableGrid } from '../components/MyStableGrid';
import { demoApi } from '../state/demoApi';

export function StablePage() {
  const horses = demoApi.listHorses();
  const [selected, setSelected] = useState<string | null>(horses[0]?.id ?? null);
  const horse = useMemo(() => (selected ? demoApi.getHorse(selected) : undefined), [selected]);

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>My Stable</div>

      <MyStableGrid
        horses={horses.map((h) => ({
          ...h,
        }))}
      />

      <div
        style={{
          borderRadius: 16,
          border: '1px solid rgba(0,0,0,0.08)',
          background: 'white',
          padding: 14,
          display: 'grid',
          gap: 10,
        }}
      >
        <div style={{ fontFamily: 'serif', fontWeight: 900 }}>Actions (demo)</div>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
          <select
            value={selected ?? ''}
            onChange={(e) => setSelected(e.target.value)}
            style={{ padding: '10px 12px', borderRadius: 12, border: '1px solid rgba(0,0,0,0.12)' }}
          >
            {horses.map((h) => (
              <option key={h.id} value={h.id}>
                {h.name}
              </option>
            ))}
          </select>

          <button
            type="button"
            style={{ borderRadius: 12, padding: '10px 12px', border: 'none', background: '#154212', color: 'white', fontWeight: 900 }}
          >
            Train +4 (pick trait)
          </button>

          <button
            type="button"
            style={{
              borderRadius: 12,
              padding: '10px 12px',
              border: '1px solid rgba(0,0,0,0.12)',
              background: 'white',
              color: 'rgba(0,0,0,0.8)',
              fontWeight: 900,
            }}
          >
            Season lock (Circuit)
          </button>
        </div>

        <div style={{ color: 'rgba(0,0,0,0.65)', fontSize: 12 }}>
          Selected: <strong>{horse?.name ?? '—'}</strong> • Traits: SPD {horse?.speed} / STA {horse?.stamina} / FOC {horse?.focus} / TEMP{' '}
          {horse?.temperament}
        </div>
      </div>
    </section>
  );
}
