import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const baseLinks = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/students', label: 'Students' },
  { to: '/staff', label: 'Staff' },
  { to: '/classes', label: 'Classes' },
  { to: '/attendance', label: 'Attendance' },
  { to: '/fees', label: 'Fees' },
];

const API_ORIGIN = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/api\/?$/, '');

export default function Sidebar() {
  const { logout, user } = useAuth();
  const [settings, setSettings] = useState(null);

  useEffect(() => {
    api.get('/settings').then((res) => setSettings(res.data)).catch(() => {});
  }, []);

  const links = user?.role === 'ADMIN'
    ? [...baseLinks, { to: '/settings', label: 'System Manager' }]
    : baseLinks;

  return (
    <aside className="sidebar">
      <div className="sidebar-brand" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8, textAlign: 'center' }}>
        {settings?.logoUrl && (
          <img
            src={`${API_ORIGIN}${settings.logoUrl}`}
            alt="School logo"
            style={{ width: 100, height: 100, objectFit: 'contain', borderRadius: 4, background: '#fff', flexShrink: 0 }}
          />
        )}
        <div style={{ whiteSpace: 'nowrap' }}>
          {settings?.schoolName || 'Greenfield ERP'}
        </div>
        <span>{settings?.tagline || 'Academic Records'}</span>
      </div>
      <nav className="sidebar-nav">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={link.end}
            className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}
          >
            {link.label}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-footer">
        <div style={{ fontSize: 12, opacity: 0.8, marginBottom: 8 }}>
          Signed in as<br />
          <strong>{user?.fullName}</strong>
        </div>
        <button className="btn btn-ghost" style={{ width: '100%', color: '#fff', borderColor: 'rgba(255,255,255,0.25)' }} onClick={logout}>
          Log out
        </button>
      </div>
    </aside>
  );
}
