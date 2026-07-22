import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import Modal from '../components/Modal';
import api from '../api/axios';
import { exportToExcel } from '../utils/exportExcel';

const emptyForm = { className: '', section: '', academicYear: '', classTeacherId: '' };

export default function Classes() {
  const [classes, setClasses] = useState([]);
  const [staff, setStaff] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [saving, setSaving] = useState(false);

  async function loadData() {
    setLoading(true);
    try {
      const [classesRes, staffRes] = await Promise.all([api.get('/classes'), api.get('/staff')]);
      setClasses(classesRes.data);
      setStaff(staffRes.data);
    } catch (err) {
      setError('Failed to load classes.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadData(); }, []);

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setShowModal(true);
  }

  function openEdit(cls) {
    setForm({
      className: cls.className,
      section: cls.section,
      academicYear: cls.academicYear || '',
      classTeacherId: cls.classTeacher?.id || '',
    });
    setEditingId(cls.id);
    setShowModal(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    const payload = {
      className: form.className,
      section: form.section,
      academicYear: form.academicYear,
      classTeacher: form.classTeacherId ? { id: Number(form.classTeacherId) } : null,
    };
    try {
      if (editingId) {
        await api.put(`/classes/${editingId}`, payload);
      } else {
        await api.post('/classes', payload);
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save class.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this class? Students assigned to it will be unlinked.')) return;
    try {
      await api.delete(`/classes/${id}`);
      loadData();
    } catch (err) {
      setError('Failed to delete class.');
    }
  }

  function handleExport() {
    exportToExcel('classes', [
      { label: 'Class', key: 'className' },
      { label: 'Section', key: 'section' },
      { label: 'Academic Year', key: 'academicYear' },
      { label: 'Class Teacher', key: (c) => (c.classTeacher ? `${c.classTeacher.firstName} ${c.classTeacher.lastName}` : '') },
      { label: 'Students', key: (c) => c.students?.length ?? 0 },
    ], classes);
  }

  return (
    <Layout eyebrow="Academics" title="Classes & Sections">
      {error && <div className="error-banner">{error}</div>}

      <div className="page-header">
        <div />
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost" onClick={handleExport} disabled={classes.length === 0}>Export to Excel</button>
          <button className="btn btn-primary" onClick={openCreate}>+ New Class</button>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="empty-state">Loading classes…</div>
        ) : classes.length === 0 ? (
          <div className="empty-state">No classes yet. Create one to start admitting students.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Class</th>
                <th>Section</th>
                <th>Academic Year</th>
                <th>Class Teacher</th>
                <th>Students</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {classes.map((c) => (
                <tr key={c.id}>
                  <td>{c.className}</td>
                  <td>{c.section}</td>
                  <td>{c.academicYear || '—'}</td>
                  <td>{c.classTeacher ? `${c.classTeacher.firstName} ${c.classTeacher.lastName}` : '—'}</td>
                  <td>{c.students?.length ?? 0}</td>
                  <td style={{ textAlign: 'right' }}>
                    <button className="btn btn-ghost" onClick={() => openEdit(c)}>Edit</button>{' '}
                    <button className="btn btn-danger" onClick={() => handleDelete(c.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <Modal title={editingId ? 'Edit Class' : 'New Class'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div className="form-grid">
              <div className="field">
                <label>Class Name</label>
                <input required value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} placeholder="Grade 5" />
              </div>
              <div className="field">
                <label>Section</label>
                <input required value={form.section} onChange={(e) => setForm({ ...form, section: e.target.value })} placeholder="A" />
              </div>
              <div className="field">
                <label>Academic Year</label>
                <input value={form.academicYear} onChange={(e) => setForm({ ...form, academicYear: e.target.value })} placeholder="2026-2027" />
              </div>
              <div className="field">
                <label>Class Teacher</label>
                <select value={form.classTeacherId} onChange={(e) => setForm({ ...form, classTeacherId: e.target.value })}>
                  <option value="">— None —</option>
                  {staff.map((s) => (
                    <option key={s.id} value={s.id}>{s.firstName} {s.lastName}</option>
                  ))}
                </select>
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Class'}</button>
            </div>
          </form>
        </Modal>
      )}
    </Layout>
  );
}
