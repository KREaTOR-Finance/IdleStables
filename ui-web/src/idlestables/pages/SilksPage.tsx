import { useState } from 'react';
import type { SilksPattern, SilksProfile } from '../types/model';
import { demoApi } from '../state/demoApi';

const patterns: SilksPattern[] = ['SOLID', 'STRIPES', 'QUARTERS', 'DIAGONAL', 'CHEVRON', 'HOOPS'];

export function SilksPage() {
  const current = demoApi.listHorses()[0]?.silks ?? { pattern: 'SOLID', primaryColor: '#154212' };
  const [profile, setProfile] = useState<SilksProfile>(current);

  const set = (patch: Partial<SilksProfile>) => setProfile((p) => ({ ...p, ...patch }));

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Silks</div>

      <div style={{ display: 'grid', gap: 12, borderRadius: 16, border: '1px solid rgba(0,0,0,0.08)', padding: 14, background: '#fff' }}>
        <div style={{ fontFamily: 'serif', fontWeight: 900 }}>Owner silks (wallet-wide)</div>

        <label style={{ display: 'grid', gap: 6 }}>
          <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Pattern</div>
          <select value={profile.pattern} onChange={(e) => set({ pattern: e.target.value as SilksPattern })} style={{ padding: '10px 12px', borderRadius: 12 }}>
            {patterns.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </label>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <label style={{ display: 'grid', gap: 6 }}>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Primary</div>
            <input type="color" value={profile.primaryColor} onChange={(e) => set({ primaryColor: e.target.value })} />
          </label>

          <label style={{ display: 'grid', gap: 6 }}>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Secondary</div>
            <input type="color" value={profile.secondaryColor ?? '#E8D9A8'} onChange={(e) => set({ secondaryColor: e.target.value })} />
          </label>

          <label style={{ display: 'grid', gap: 6 }}>
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Accent</div>
            <input type="color" value={profile.accentColor ?? '#111111'} onChange={(e) => set({ accentColor: e.target.value })} />
          </label>
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
          <button
            type="button"
            onClick={() => demoApi.updateSilks(profile)}
            style={{ borderRadius: 12, padding: '10px 12px', border: 'none', background: '#154212', color: 'white', fontWeight: 900 }}
          >
            Save silks
          </button>
          <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Applies to all horses (demo)</div>
        </div>

        <div style={{ borderRadius: 14, padding: 12, background: 'rgba(21,66,18,0.06)' }}>
          <div style={{ fontWeight: 900, marginBottom: 6 }}>Preview (placeholder)</div>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <div style={{ width: 24, height: 24, borderRadius: 6, background: profile.primaryColor }} />
            <div style={{ width: 24, height: 24, borderRadius: 6, background: profile.secondaryColor ?? '#E8D9A8' }} />
            <div style={{ width: 24, height: 24, borderRadius: 6, background: profile.accentColor ?? '#111111' }} />
            <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.7)' }}>{profile.pattern}</div>
          </div>
        </div>
      </div>
    </section>
  );
}
