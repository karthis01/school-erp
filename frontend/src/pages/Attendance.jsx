import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import api from '../api/axios';
import { exportToExcel } from '../utils/exportExcel';

function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

const STATUS_OPTIONS = ['PRESENT', 'ABSENT', 'LATE', 'HALF_DAY', 'EXCUSED'];

export default function Attendance() {
  const [classes, setClasses] = useState([]);
  const [classId, setClassId] = useState('');
  const [date, setDate] = useState(todayISO());
  const [students, setStudents] = useState([]);
  const [records, setRecords] = useState({}); // studentId -> { status, remarks }
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.get('/classes').then((res) => {
      setClasses(res.data);
      if (res.data.length > 0) setClassId(res.data[0].id);
    }).catch(() => setError('Failed to load classes.'));
  }, []);

  useEffect(() => {
    if (!classId) return;
    loadRoster();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [classId, date]);

  async function loadRoster() {
    setLoading(true);
    setMessage('');
    setError('');
    try {
      const [studentsRes, attendanceRes] = await Promise.all([
        api.get('/students', { params: { classId } }),
        api.get('/attendance', { params: { date, classId } }),
      ]);
      setStudents(studentsRes.data);

      const initial = {};
      studentsRes.data.forEach((s) => {
        initial[s.id] = { status: 'PRESENT', remarks: '' };
      });
      attendanceRes.data.forEach((a) => {
        initial[a.student.id] = { status: a.status, remarks: a.remarks || '' };
      });
      setRecords(initial);
    } catch (err) {
      setError('Failed to load attendance roster.');
    } finally {
      setLoading(false);
    }
  }

  function setStatus(studentId, status) {
    setRecords((prev) => ({ ...prev, [studentId]: { ...prev[studentId], status } }));
  }

  async function handleSaveAll() {
    setSaving(true);
    setError('');
    setMessage('');
    try {
      const payload = students.map((s) => ({
        student: { id: s.id },
        date,
        status: records[s.id]?.status || 'PRESENT',
        remarks: records[s.id]?.remarks || '',
      }));
      await api.post('/attendance/bulk', payload);
      setMessage('Attendance saved successfully.');
    } catch (err) {
      setError('Failed to save attendance.');
    } finally {
      setSaving(false);
    }
  }

  const pillClass = { PRESENT: 'pill-active', ABSENT: 'pill-danger', LATE: 'pill-warn', HALF_DAY: 'pill-warn', EXCUSED: 'pill-active' };

  function handleExport() {
    exportToExcel(`attendance_${date}`, [
      { label: 'Admission #', key: 'admissionNumber' },
      { label: 'Name', key: (s) => `${s.firstName} ${s.lastName}` },
      { label: 'Date', key: () => date },
      { label: 'Status', key: (s) => records[s.id]?.status || 'PRESENT' },
    ], students);
  }

  return (
    <Layout eyebrow="Daily Register" title="Attendance">
      {error && <div className="error-banner">{error}</div>}
      {message && <div className="error-banner" style={{ background: 'var(--color-primary-soft)', color: 'var(--color-primary-dark)' }}>{message}</div>}

      <div className="page-header">
        <div style={{ display: 'flex', gap: 8 }}>
          <select value={classId} onChange={(e) => setClassId(e.target.value)} style={{ padding: '8px 10px', border: '1px solid var(--color-ledger-line)', borderRadius: 4 }}>
            {classes.map((c) => (
              <option key={c.id} value={c.id}>{c.className} - {c.section}</option>
            ))}
          </select>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ padding: '8px 10px', border: '1px solid var(--color-ledger-line)', borderRadius: 4 }} />
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost" onClick={handleExport} disabled={students.length === 0}>Export to Excel</button>
          <button className="btn btn-primary" onClick={handleSaveAll} disabled={saving || students.length === 0}>
            {saving ? 'Saving…' : 'Save Attendance'}
          </button>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="empty-state">Loading roster…</div>
        ) : students.length === 0 ? (
          <div className="empty-state">No students in this class yet.</div>
        ) : (
          <table>
            <thead>
              <tr><th>Admission #</th><th>Name</th><th>Status</th></tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.id}>
                  <td style={{ fontFamily: 'var(--font-mono)' }}>{s.admissionNumber}</td>
                  <td>{s.firstName} {s.lastName}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                      {STATUS_OPTIONS.map((opt) => (
                        <button
                          key={opt}
                          type="button"
                          onClick={() => setStatus(s.id, opt)}
                          className={`pill ${records[s.id]?.status === opt ? pillClass[opt] : ''}`}
                          style={{
                            cursor: 'pointer',
                            border: '1px solid var(--color-ledger-line)',
                            background: records[s.id]?.status === opt ? undefined : '#fff',
                            opacity: records[s.id]?.status === opt ? 1 : 0.55,
                          }}
                        >
                          {opt.replace('_', ' ')}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
