import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Modal from '../components/Modal';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [conflictMessage, setConflictMessage] = useState(''); // set -> shows the confirm modal
  const { login } = useAuth();
  const navigate = useNavigate();

  async function attemptLogin(force) {
    setError('');
    setLoading(true);
    try {
      await login(username, password, force);
      setConflictMessage('');
      navigate('/');
    } catch (err) {
      if (err.response?.status === 409 && err.response?.data?.conflict) {
        // Already logged in elsewhere - ask the user whether to kick out that session.
        setConflictMessage(err.response.data.message || 'This account is already signed in elsewhere.');
      } else {
        setError(err.response?.data?.message || 'Invalid username or password');
      }
    } finally {
      setLoading(false);
    }
  }

  function handleSubmit(e) {
    e.preventDefault();
    attemptLogin(false);
  }

  function handleConfirmForceLogin() {
    attemptLogin(true);
  }

  function handleCancelForceLogin() {
    setConflictMessage('');
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-brand">
          <div className="stamp">Est. Ledger System</div>
          <h1 style={{ fontSize: 22 }}>Greenfield ERP</h1>
          <div style={{ fontSize: 12.5, color: 'var(--color-ink-soft)' }}>Sign in to the school records office</div>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="admin@svm"
              autoFocus
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ justifyContent: 'center', marginTop: 6 }}>
            {loading ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <div style={{ marginTop: 18, fontSize: 12, color: 'var(--color-ink-soft)', textAlign: 'center' }}>
          School staff sign in as <strong>username@schoolcode</strong>, e.g. <strong>admin@svm</strong>
        </div>
      </div>

      {conflictMessage && (
        <Modal title="Already signed in" onClose={handleCancelForceLogin}>
          <p style={{ marginTop: 0 }}>{conflictMessage}</p>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 16 }}>
            <button className="btn btn-ghost" onClick={handleCancelForceLogin} disabled={loading}>
              Cancel
            </button>
            <button className="btn btn-primary" onClick={handleConfirmForceLogin} disabled={loading}>
              {loading ? 'Signing in…' : 'Log out other session & continue'}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
