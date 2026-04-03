import { Link, useParams } from 'react-router-dom';
import { useMemo, useState } from 'react';
import { TrackSchedule } from '../components/TrackSchedule';
import { demoApi } from '../state/demoApi';

export function TrackDetailPage() {
  const { id } = useParams();
  const track = id ? demoApi.getTrack(id) : undefined;
  const horses = demoApi.listHorses();
  const [horseId, setHorseId] = useState(horses[0]?.id ?? '');
  const horse = useMemo(() => (horseId ? demoApi.getHorse(horseId) : undefined), [horseId]);

  if (!track) {
    return (
      <section>
        <div style={{ color: 'rgba(0,0,0,0.7)' }}>Track not found.</div>
        <Link to="/tracks">Back</Link>
      </section>
    );
  }

  const races = demoApi.listRacesForTrack(track.id);

  return (
    <section style={{ display: 'grid', gap: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
        <div style={{ fontSize: 14, fontWeight: 900, color: '#154212' }}>{track.name}</div>
        <Link to="/tracks" style={{ fontSize: 12 }}>
          Back to tracks
        </Link>
      </div>

      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Enter as:</div>
        <select value={horseId} onChange={(e) => setHorseId(e.target.value)} style={{ padding: '10px 12px', borderRadius: 12 }}>
          {horses.map((h) => (
            <option key={h.id} value={h.id}>
              {h.name}
            </option>
          ))}
        </select>
        <div style={{ fontSize: 12, color: 'rgba(0,0,0,0.6)' }}>Selected: {horse?.name ?? '—'}</div>
      </div>

      <TrackSchedule
        track={track}
        races={races}
        onEnter={(r) => {
          demoApi.enterRace(track.id, r.id, horseId, 'MANUAL');
        }}
      />

      <div style={{ color: 'rgba(0,0,0,0.6)', fontSize: 12 }}>
        Demo: click Enter on an OPEN slot to simulate entry; when a slot fills, it will generate a result visible on the Dashboard.
      </div>
    </section>
  );
}
