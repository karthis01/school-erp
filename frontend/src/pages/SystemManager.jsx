import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import api from '../api/axios';

const emptyForm = {
  schoolName: '', tagline: '', address: '', phone: '', email: '',
  website: '', establishedYear: '', principalName: '',
};

// Strip the trailing /api so we can build a direct URL to static files like /uploads/logos/xyz.png
const API_ORIGIN = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/api\/?$/, '');

export default function SystemManager() {
  const [form, setForm] = useState(emptyForm);
  const [logoUrl, setLogoUrl] = useState('');
  const [logoFile, setLogoFile] = useState(null);
  const [logoPreview, setLogoPreview] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function loadSettings() {
    setLoading(true);
    try {
      const { data } = await api.get('/settings');
      setForm({
        schoolName: data.schoolName || '',
        tagline: data.tagline || '',
        address: data.address || '',
        phone: data.phone || '',
        email: data.email || '',
        website: data.website || '',
        establishedYear: data.establishedYear || '',
        principalName: data.principalName || '',
      });
      setLogoUrl(data.logoUrl || '');
    } catch (err) {
      setError('Failed to load school settings.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadSettings(); }, []);

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const payload = { ...form, establishedYear: form.establishedYear ? Number(form.establishedYear) : null };
      await api.put('/settings', payload);
      setMessage('School details saved successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save school details. (Only Admin accounts can edit this.)');
    } finally {
      setSaving(false);
    }
  }

  function handleFileChange(e) {
    const file = e.target.files[0];
    if (!file) return;
    setLogoFile(file);
    setLogoPreview(URL.createObjectURL(file));
  }

  async function handleUploadLogo() {
    if (!logoFile) return;
    setUploading(true);
    setError('');
    setMessage('');
    try {
      const formData = new FormData();
      formData.append('file', logoFile);
      const { data } = await api.post('/settings/logo', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setLogoUrl(data.logoUrl || '');
      setLogoFile(null);
      setLogoPreview('');
      setMessage('Logo uploaded successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload logo. (Only Admin accounts can upload.)');
    } finally {
      setUploading(false);
    }
  }

  return (
    <Layout eyebrow="Administration" title="System Manager">
      {error && <div className="error-banner">{error}</div>}
      {message && (
        <div className="error-banner" style={{ background: 'var(--color-primary-soft)', color: 'var(--color-primary-dark)' }}>
          {message}
        </div>
      )}

      <div className="card card-pad" style={{ marginBottom: 20 }}>
        <h3>School Logo</h3>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, marginTop: 12 }}>
          <div
            style={{
              width: 96, height: 96, borderRadius: 8, border: '1px solid var(--color-ledger-line)',
              display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', background: '#fff', flexShrink: 0,
            }}
          >
            {logoPreview || logoUrl ? (
              <img
                src={logoPreview || `${API_ORIGIN}${logoUrl}`}
                alt="School logo"
                style={{ width: '100%', height: '100%', objectFit: 'contain' }}
              />
            ) : (
              <span style={{ fontSize: 11, color: 'var(--color-ink-soft)' }}>No logo</span>
            )}
          </div>
          <div>
            <input type="file" accept="image/*" onChange={handleFileChange} />
            <div style={{ marginTop: 10 }}>
              <button className="btn btn-primary" onClick={handleUploadLogo} disabled={!logoFile || uploading}>
                {uploading ? 'Uploading…' : 'Upload Logo'}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="card card-pad">
        <h3>School Details</h3>
        {loading ? (
          <div className="empty-state">Loading…</div>
        ) : (
          <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 12 }}>
            <div className="form-grid">
              <div className="field">
                <label>School Name</label>
                <input required value={form.schoolName} onChange={(e) => setForm({ ...form, schoolName: e.target.value })} />
              </div>
              <div className="field">
                <label>Tagline</label>
                <input value={form.tagline} onChange={(e) => setForm({ ...form, tagline: e.target.value })} placeholder="Academic Records" />
              </div>
              <div className="field">
                <label>Principal Name</label>
                <input value={form.principalName} onChange={(e) => setForm({ ...form, principalName: e.target.value })} />
              </div>
              <div className="field">
                <label>Established Year</label>
                <input type="number" value={form.establishedYear} onChange={(e) => setForm({ ...form, establishedYear: e.target.value })} />
              </div>
              <div className="field">
                <label>Phone</label>
                <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
              </div>
              <div className="field">
                <label>Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
              </div>
              <div className="field">
                <label>Website</label>
                <input value={form.website} onChange={(e) => setForm({ ...form, website: e.target.value })} placeholder="https://" />
              </div>
              <div className="field" style={{ gridColumn: 'span 2' }}>
                <label>Address</label>
                <textarea rows={2} value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Details'}</button>
            </div>
          </form>
        )}
      </div>
    </Layout>
  );
}
