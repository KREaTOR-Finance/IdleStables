import { Link } from 'react-router-dom';
import { demoApi } from '../state/demoApi';

export function DashboardPage() {
  const results = demoApi.listResults();
  const horses = demoApi.listHorses();
  const horseName = (id: string) => horses.find((h) => h.id === id)?.name ?? id;

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Announcements</div>

      <div style={{ display: 'grid', gap: 12 }}>
        {results.map((r) => (
          <div
            key={r.raceId}
            style={{
              borderRadius: 16,
              border: '1px solid rgba(0,0,0,0.08)',
              padding: 14,
              background: 'white',
              boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, alignItems: 'baseline' }}>
              <div style={{ fontFamily: 'serif', fontWeight: 900 }}>Race Resolved</div>
              <Link to={`/race/${encodeURIComponent(r.raceId)}`} style={{ fontSize: 12 }}>
                View photo finish
              </Link>
            </div>
            <div style={{ marginTop: 8, color: 'rgba(0,0,0,0.75)' }}>
              Winner: <strong>{horseName(r.winnerHorseId)}</strong> • Purse won: <strong>{r.purseWon} PURSE</strong>
            </div>
            <div style={{ marginTop: 10, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
              <button
                type="button"
                style={{
                  borderRadius: 12,
                  padding: '10px 12px',
                  border: 'none',
                  background: '#154212',
                  color: 'white',
                  fontWeight: 900,
                }}
              >
                Claim winnings
              </button>
              <button
                type="button"
                style={{
                  borderRadius: 12,
                  padding: '10px 12px',
                  border: '1px solid rgba(0,0,0,0.1)',
                  background: 'white',
                  color: 'rgba(0,0,0,0.8)',
                  fontWeight: 900,
                }}
              >
                Claim yield
              </button>
            </div>
          </div>
        ))}

        {results.length === 0 && (
          <div style={{ color: 'rgba(0,0,0,0.6)' }}>No results yet. Enter a race to generate a demo result.</div>
        )}
      </div>
    </section>
  );
}
