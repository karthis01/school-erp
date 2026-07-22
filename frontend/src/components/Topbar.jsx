import { useAuth } from '../context/AuthContext';

export default function Topbar({ title, eyebrow }) {
  const { user } = useAuth();

  return (
    <div className="topbar">
      <div>
        {eyebrow && <div className="page-eyebrow">{eyebrow}</div>}
        <h2 style={{ margin: 0 }}>{title}</h2>
      </div>
      <div className="topbar-user">
        <span>{user?.fullName}</span>
        <span className="role-badge">{user?.role}</span>
      </div>
    </div>
  );
}
