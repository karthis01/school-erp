import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import Modal from '../components/Modal';
import api from '../api/axios';
import { exportToExcel } from '../utils/exportExcel';

const emptyForm = {
  employeeCode: '', firstName: '', lastName: '', designation: '', department: '',
  phone: '', email: '', dateOfJoining: '', salary: '', status: 'ACTIVE',
};

export default function Staff() {
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);

  async function loadStaff() {
    setLoading(true);
    try {
      const { data } = await api.get('/staff');
      setStaff(data);
    } catch (err) {
      setError('Failed to load staff.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadStaff(); }, []);

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setShowModal(true);
  }

  function openEdit(s) {
    setForm({
      employeeCode: s.employeeCode, firstName: s.firstName, lastName: s.lastName,
      designation: s.designation || '', department: s.department || '', phone: s.phone || '',
      email: s.email || '', dateOfJoining: s.dateOfJoining || '', salary: s.salary ?? '', status: s.status,
    });
    setEditingId(s.id);
    setShowModal(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = { ...form, salary: form.salary === '' ? null : Number(form.salary) };
    try {
      if (editingId) {
        await api.put(`/staff/${editingId}`, payload);
      } else {
        await api.post('/staff', payload);
      }
      setShowModal(false);
      loadStaff();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save staff member.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    if (!confirm('Remove this staff member?')) return;
    try {
      await api.delete(`/staff/${id}`);
      loadStaff();
    } catch (err) {
      setError('Failed to delete staff member.');
    }
  }

  function handleExport() {
    exportToExcel('staff', [
      { label: 'Code', key: 'employeeCode' },
      { label: 'First Name', key: 'firstName' },
      { label: 'Last Name', key: 'lastName' },
      { label: 'Designation', key: 'designation' },
      { label: 'Department', key: 'department' },
      { label: 'Phone', key: 'phone' },
      { label: 'Email', key: 'email' },
      { label: 'Date of Joining', key: 'dateOfJoining' },
      { label: 'Salary', key: 'salary' },
      { label: 'Status', key: 'status' },
    ], staff);
  }

  return (
    <Layout eyebrow="Personnel" title="Staff Directory">
      {error && <div className="error-banner">{error}</div>}

      <div className="page-header">
        <div />
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost" onClick={handleExport} disabled={staff.length === 0}>Export to Excel</button>
          <button className="btn btn-primary" onClick={openCreate}>+ Add Staff</button>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="empty-state">Loading staff…</div>
        ) : staff.length === 0 ? (
          <div className="empty-state">No staff records yet.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Code</th><th>Name</th><th>Designation</th><th>Department</th><th>Phone</th><th>Status</th><th></th>
              </tr>
            </thead>
            <tbody>
              {staff.map((s) => (
                <tr key={s.id}>
                  <td style={{ fontFamily: 'var(--font-mono)' }}>{s.employeeCode}</td>
                  <td>{s.firstName} {s.lastName}</td>
                  <td>{s.designation || '—'}</td>
                  <td>{s.department || '—'}</td>
                  <td>{s.phone || '—'}</td>
                  <td><span className={`pill ${s.status === 'ACTIVE' ? 'pill-active' : 'pill-warn'}`}>{s.status}</span></td>
                  <td style={{ textAlign: 'right' }}>
                    <button className="btn btn-ghost" onClick={() => openEdit(s)}>Edit</button>{' '}
                    <button className="btn btn-danger" onClick={() => handleDelete(s.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <Modal title={editingId ? 'Edit Staff Member' : 'Add Staff Member'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div className="form-grid">
              <div className="field">
                <label>Employee Code</label>
                <input required value={form.employeeCode} onChange={(e) => setForm({ ...form, employeeCode: e.target.value })} placeholder="EMP-001" />
              </div>
              <div className="field">
                <label>Status</label>
                <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value="ACTIVE">Active</option>
                  <option value="ON_LEAVE">On Leave</option>
                  <option value="RESIGNED">Resigned</option>
                  <option value="TERMINATED">Terminated</option>
                </select>
              </div>
              <div className="field">
                <label>First Name</label>
                <input required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
              </div>
              <div className="field">
                <label>Last Name</label>
                <input required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
              </div>
              <div className="field">
                <label>Designation</label>
                <input value={form.designation} onChange={(e) => setForm({ ...form, designation: e.target.value })} placeholder="Teacher" />
              </div>
              <div className="field">
                <label>Department</label>
                <input value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} placeholder="Science" />
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
                <label>Date of Joining</label>
                <input type="date" value={form.dateOfJoining} onChange={(e) => setForm({ ...form, dateOfJoining: e.target.value })} />
              </div>
              <div className="field">
                <label>Salary</label>
                <input type="number" value={form.salary} onChange={(e) => setForm({ ...form, salary: e.target.value })} />
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
            </div>
          </form>
        </Modal>
      )}
    </Layout>
  );
}
