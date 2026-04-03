import type { Horse } from '../types/model';
import { HorseCard } from './HorseCard';

export function MyStableGrid({ horses }: { horses: Horse[] }) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: 16 }}>
      {horses.map((h) => (
        <HorseCard key={h.id} horse={h} />
      ))}
    </div>
  );
}
