import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// requireRole, if set, restricts the route to that exact role (e.g. "SUPER_ADMIN").
// Tenant-scoped routes (the default, requireRole omitted) are additionally off-limits to
// super-admins, since they have no school/database context to show data from.
export default function ProtectedRoute({ children, requireRole }) {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (requireRole && user.role !== requireRole) {
    return <Navigate to="/" replace />;
  }
  if (!requireRole && user.role === 'SUPER_ADMIN') {
    return <Navigate to="/schools" replace />;
  }
  return children;
}
