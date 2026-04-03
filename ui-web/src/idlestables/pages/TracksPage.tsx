import { Link } from 'react-router-dom';
import { demoApi } from '../state/demoApi';

export function TracksPage() {
  const tracks = demoApi.listTracks();

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Tracks</div>

      <div style={{ display: 'grid', gap: 12 }}>
        {tracks.map((t) => (
          <Link
            key={t.id}
            to={`/tracks/${t.id}`}
            style={{
              textDecoration: 'none',
              borderRadius: 16,
              border: '1px solid rgba(0,0,0,0.08)',
              padding: 14,
              background: 'white',
              boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
              color: '#111',
            }}
          >
            <div style={{ fontFamily: 'serif', fontWeight: 900 }}>{t.name}</div>
            <div style={{ marginTop: 6, color: 'rgba(0,0,0,0.65)', fontSize: 12 }}>
              Distance: {t.distanceLabel} • Cadence: {t.cadenceMinutes}m • Normal field: 12 • Mega Cup: 18 (2x fee)
            </div>
          </Link>
        ))}
      </div>
    </section>
  );
}
