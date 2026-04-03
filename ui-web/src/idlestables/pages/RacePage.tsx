import { Link, useParams } from 'react-router-dom';
import { demoApi } from '../state/demoApi';

export function RacePage() {
  const { id } = useParams();
  const results = demoApi.listResults();
  const r = results.find((x) => x.raceId === id);
  const horses = demoApi.listHorses();
  const horseName = (hid: string) => horses.find((h) => h.id === hid)?.name ?? hid;

  if (!id) return null;

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Race Result</div>
        <Link to="/dashboard" style={{ fontSize: 12 }}>
          Back to dashboard
        </Link>
      </div>

      {!r && <div style={{ color: 'rgba(0,0,0,0.65)' }}>No result found for {id}. Enter races to generate demo results.</div>}

      {r && (
        <div
          style={{
            borderRadius: 20,
            border: '1px solid rgba(0,0,0,0.08)',
            background: '#fff',
            overflow: 'hidden',
            boxShadow: '0 10px 30px rgba(0,0,0,0.06)',
          }}
        >
          <div style={{ padding: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <div style={{ fontFamily: 'serif', fontSize: 18, fontWeight: 900 }}>Photo Finish (static)</div>
            <div style={{ color: 'rgba(0,0,0,0.55)', fontSize: 12 }}>{id}</div>
          </div>
          <div style={{ height: 280, background: 'linear-gradient(180deg, rgba(21,66,18,0.12), rgba(21,66,18,0))', display: 'grid', placeItems: 'center' }}>
            Winner: <strong>{horseName(r.winnerHorseId)}</strong>
          </div>
          <div style={{ padding: 16, display: 'grid', gap: 8 }}>
            <div style={{ fontWeight: 900 }}>Top 3</div>
            <ol style={{ margin: 0, paddingLeft: 18, color: 'rgba(0,0,0,0.75)' }}>
              {r.top3.map((p) => (
                <li key={p.position}>
                  {horseName(p.horseId)}
                </li>
              ))}
            </ol>
            <div style={{ marginTop: 8, color: '#154212', fontWeight: 900 }}>Winner payout (demo): {r.purseWon} PURSE</div>
          </div>
        </div>
      )}
    </section>
  );
}
