import { Navigate, Route, Routes } from 'react-router-dom';
import {
  AppShell,
  BreedPage,
  DashboardPage,
  RacePage,
  SilksPage,
  StablePage,
  TrackDetailPage,
  TracksPage,
} from './idlestables/pages';

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/stable" element={<StablePage />} />
        <Route path="/tracks" element={<TracksPage />} />
        <Route path="/tracks/:id" element={<TrackDetailPage />} />
        <Route path="/race/:id" element={<RacePage />} />
        <Route path="/breed" element={<BreedPage />} />
        <Route path="/silks" element={<SilksPage />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Route>
    </Routes>
  );
}
