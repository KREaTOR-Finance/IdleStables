import './App.css';
import { MyStableGrid } from './idlestables/components/MyStableGrid';
import { TrackSchedule } from './idlestables/components/TrackSchedule';
import { demoHorses, makeDemoRaces, tracks } from './idlestables/demoData';

export default function App() {
  const racesByTrack = makeDemoRaces();

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: 24, display: 'grid', gap: 18 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <div style={{ fontFamily: 'serif', fontSize: 28, fontWeight: 900 }}>IdleStables</div>
        <div style={{ color: 'rgba(0,0,0,0.6)', fontSize: 12 }}>UI spec (React) → port 1:1 to Kotlin/Compose</div>
      </header>

      <section style={{ display: 'grid', gap: 12 }}>
        <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>My Stable</div>
        <MyStableGrid horses={demoHorses} />
      </section>

      <section style={{ display: 'grid', gap: 12 }}>
        <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>Tracks</div>
        <div style={{ display: 'grid', gap: 16 }}>
          {tracks.map((t) => (
            <TrackSchedule key={t.id} track={t} races={racesByTrack[t.id] ?? []} />
          ))}
        </div>
      </section>
    </div>
  );
}
