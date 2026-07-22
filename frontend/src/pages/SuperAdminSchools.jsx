import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

const emptyForm = {
  schoolCode: '',
  schoolName: '',
  dbHost: 'localhost',
  dbPort: 3306,
  dbName: '',
  dbUsername: 'root',
  dbPassword: '',
  initializeSchema: true,
  defaultAdminPassword: 'admin123',
  seedSampleData: false,
};

export default function SuperAdminSchools() {
  const { user, logout } = useAuth();
  const [schools, setSchools] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function loadSchools() {
    setLoading(true);
    try {
      const { data } = await api.get('/schools');
      setSchools(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load schools.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadSchools(); }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await api.post('/schools', form);
      setMessage(`School "${form.schoolName}" created. Staff can now sign in as e.g. admin@${form.schoolCode}.`);
      setForm(emptyForm);
      loadSchools();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create school.');
    } finally {
      setSaving(false);
    }
  }

  async function toggleActive(school) {
    setError('');
    try {
      await api.patch(`/schools/${school.schoolCode}/active`, null, { params: { active: !school.active } });
      loadSchools();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update school.');
    }
  }

  return (
    <div style={{ maxWidth: 900, margin: '40px auto', padding: '0 20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 20 }}>
        <div>
          <div style={{ fontSize: 12, color: 'var(--color-ink-soft)' }}>Platform Administration</div>
          <h1 style={{ margin: '4px 0' }}>Schools</h1>
          <div style={{ fontSize: 13, color: 'var(--color-ink-soft)' }}>Signed in as {user?.username}</div>
        </div>
        <button className="btn btn-ghost" onClick={logout}>Sign out</button>
      </div>

      {error && <div className="error-banner">{error}</div>}
      {message && (
        <div className="error-banner" style={{ background: 'var(--color-primary-soft)', color: 'var(--color-primary-dark)' }}>
          {message}
        </div>
      )}

      <div className="card card-pad" style={{ marginBottom: 24 }}>
        <h3>Register a new school</h3>
        <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 12 }}>
          <div className="form-grid">
            <div className="field">
              <label>School Code (used as username@code)</label>
              <input required value={form.schoolCode}
                onChange={(e) => setForm({ ...form, schoolCode: e.target.value.trim().toLowerCase() })}
                placeholder="svm" />
            </div>
            <div className="field">
              <label>School Name</label>
              <input required value={form.schoolName}
                onChange={(e) => setForm({ ...form, schoolName: e.target.value })}
                placeholder="Sri Vidya Mandir" />
            </div>
            <div className="field">
              <label>DB Host</label>
              <input value={form.dbHost} onChange={(e) => setForm({ ...form, dbHost: e.target.value })} />
            </div>
            <div className="field">
              <label>DB Port</label>
              <input type="number" value={form.dbPort} onChange={(e) => setForm({ ...form, dbPort: Number(e.target.value) })} />
            </div>
            <div className="field">
              <label>DB Name (blank = school_&lt;code&gt;)</label>
              <input value={form.dbName} onChange={(e) => setForm({ ...form, dbName: e.target.value })} placeholder="school_svm" />
            </div>
            <div className="field">
              <label>DB Username</label>
              <input required value={form.dbUsername} onChange={(e) => setForm({ ...form, dbUsername: e.target.value })} />
            </div>
            <div className="field">
              <label>DB Password</label>
              <input required type="password" value={form.dbPassword} onChange={(e) => setForm({ ...form, dbPassword: e.target.value })} />
            </div>
            <div className="field">
              <label>Default admin password</label>
              <input value={form.defaultAdminPassword} onChange={(e) => setForm({ ...form, defaultAdminPassword: e.target.value })} />
            </div>
          </div>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
            <input type="checkbox" checked={form.initializeSchema}
              onChange={(e) => setForm({ ...form, initializeSchema: e.target.checked })} />
            Create tables in this database (turn off only if it's an existing school-erp database)
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
            <input type="checkbox" checked={form.seedSampleData}
              onChange={(e) => setForm({ ...form, seedSampleData: e.target.checked })} />
            Populate with demo data (5 records per module)
          </label>
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Creating…' : 'Create School'}
            </button>
          </div>
        </form>
      </div>

      <div className="card card-pad">
        <h3>Registered schools</h3>
        {loading ? (
          <div className="empty-state">Loading…</div>
        ) : schools.length === 0 ? (
          <div className="empty-state">No schools registered yet.</div>
        ) : (
          <table className="table" style={{ marginTop: 12 }}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {schools.map((school) => (
                <tr key={school.id}>
                  <td>{school.schoolCode}</td>
                  <td>{school.schoolName}</td>
                  <td>{school.active ? 'Active' : 'Disabled'}</td>
                  <td>
                    <button className="btn btn-ghost" onClick={() => toggleActive(school)}>
                      {school.active ? 'Disable' : 'Enable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
