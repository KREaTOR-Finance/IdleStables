import { Link, NavLink, Outlet } from 'react-router-dom';

const navItemStyle = ({ isActive }: { isActive: boolean }) => ({
  padding: '10px 12px',
  borderRadius: 12,
  textDecoration: 'none',
  color: isActive ? 'white' : 'rgba(0,0,0,0.7)',
  background: isActive ? '#154212' : 'rgba(21,66,18,0.08)',
  fontWeight: 900,
  fontSize: 13,
});

export function AppShell() {
  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: 24, display: 'grid', gap: 18 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 16 }}>
        <Link to="/" style={{ fontFamily: 'serif', fontSize: 28, fontWeight: 900, color: '#111', textDecoration: 'none' }}>
          IdleStables
        </Link>
        <nav style={{ display: 'flex', gap: 10, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
          <NavLink to="/dashboard" style={navItemStyle}>
            Dashboard
          </NavLink>
          <NavLink to="/stable" style={navItemStyle}>
            Stable
          </NavLink>
          <NavLink to="/tracks" style={navItemStyle}>
            Tracks
          </NavLink>
          <NavLink to="/breed" style={navItemStyle}>
            Breed
          </NavLink>
          <NavLink to="/silks" style={navItemStyle}>
            Silks
          </NavLink>
        </nav>
      </header>

      <Outlet />

      <footer style={{ color: 'rgba(0,0,0,0.55)', fontSize: 12, paddingTop: 10 }}>
        UI spec app (React) — routes + demo data. Port 1:1 to Kotlin/Compose.
      </footer>
    </div>
  );
}
