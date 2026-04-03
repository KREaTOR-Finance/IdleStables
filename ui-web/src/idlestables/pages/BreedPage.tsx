import { useMemo, useState } from 'react';
import { demoApi } from '../state/demoApi';

function hashStr(s: string) {
  let h = 2166136261;
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i);
    h = Math.imul(h, 16777619);
  }
  return Math.abs(h);
}

export function BreedPage() {
  const horses = demoApi.listHorses();
  const [sire, setSire] = useState(horses[0]?.id ?? '');
  const [dam, setDam] = useState(horses[1]?.id ?? horses[0]?.id ?? '');

  const preview = useMemo(() => {
    const a = demoApi.getHorse(sire);
    const b = demoApi.getHorse(dam);
    if (!a || !b) return null;

    const seed = hashStr(`${a.seed}-${b.seed}-${Date.now()}`) % 1_000_000_000;
    const speed = Math.round((a.speed * 0.6 + b.speed * 0.4) + (seed % 7) - 3);
    const stamina = Math.round((a.stamina * 0.4 + b.stamina * 0.6) + ((seed >> 3) % 7) - 3);
    const focus = Math.round((a.focus * 0.5 + b.focus * 0.5) + ((seed >> 6) % 7) - 3);
    const temperament = Math.round((a.temperament * 0.5 + b.temperament * 0.5) + ((seed >> 9) % 7) - 3);

    return {
      name: 'New Yearling',
      seed,
      speed,
      stamina,
      focus,
      temperament,
    };
  }, [sire, dam]);

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Breeding</div>

      <div style={{ display: 'grid', gap: 10, borderRadius: 16, border: '1px solid rgba(0,0,0,0.08)', padding: 14, background: '#fff' }}>
        <div style={{ fontFamily: 'serif', fontWeight: 900 }}>Select sire and dam</div>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <label style={{ display: 'grid', gap: 6 }}>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Sire</div>
            <select value={sire} onChange={(e) => setSire(e.target.value)} style={{ padding: '10px 12px', borderRadius: 12 }}>
              {horses.map((h) => (
                <option key={h.id} value={h.id}>
                  {h.name}
                </option>
              ))}
            </select>
          </label>

          <label style={{ display: 'grid', gap: 6 }}>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Dam</div>
            <select value={dam} onChange={(e) => setDam(e.target.value)} style={{ padding: '10px 12px', borderRadius: 12 }}>
              {horses.map((h) => (
                <option key={h.id} value={h.id}>
                  {h.name}
                </option>
              ))}
            </select>
          </label>

          <button type="button" style={{ alignSelf: 'end', borderRadius: 12, padding: '10px 12px', border: 'none', background: '#154212', color: 'white', fontWeight: 900 }}>
            Pay & Breed
          </button>
        </div>

        <div style={{ marginTop: 8, color: 'rgba(0,0,0,0.65)', fontSize: 12 }}>
          Demo preview (breeding will mint a new Yearling on-chain later):
        </div>

        {preview && (
          <div style={{ borderRadius: 14, background: 'rgba(21,66,18,0.06)', padding: 12 }}>
            <div style={{ fontWeight: 900 }}>{preview.name}</div>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.7)', marginTop: 6 }}>Seed: {preview.seed}</div>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.7)', marginTop: 6 }}>
              SPD {preview.speed} • STA {preview.stamina} • FOC {preview.focus} • TEMP {preview.temperament}
            </div>
          </div>
        )}
      </div>
    </section>
  );
}
