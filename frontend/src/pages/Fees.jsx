import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import Modal from '../components/Modal';
import api from '../api/axios';
import { printFeeReceipt } from '../utils/receipt';
import { exportToExcel } from '../utils/exportExcel';

const emptyStructureForm = { feeType: '', classId: '', amount: '', frequency: 'MONTHLY', academicYear: '' };
const emptyPaymentForm = { studentId: '', feeStructureId: '', amountPaid: '', paymentDate: '', paymentMode: 'CASH', transactionRef: '', remarks: '' };

export default function Fees() {
  const [tab, setTab] = useState('payments'); // payments | structures | lookup
  const [structures, setStructures] = useState([]);
  const [payments, setPayments] = useState([]);
  const [students, setStudents] = useState([]);
  const [classes, setClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showStructureModal, setShowStructureModal] = useState(false);
  const [structureForm, setStructureForm] = useState(emptyStructureForm);

  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [paymentForm, setPaymentForm] = useState(emptyPaymentForm);
  const [saving, setSaving] = useState(false);

  const [lookupStudentId, setLookupStudentId] = useState('');
  const [summary, setSummary] = useState(null);

  async function loadAll() {
    setLoading(true);
    try {
      const [structuresRes, paymentsRes, studentsRes, classesRes] = await Promise.all([
        api.get('/fees/structures'),
        api.get('/fees/payments'),
        api.get('/students'),
        api.get('/classes'),
      ]);
      setStructures(structuresRes.data);
      setPayments(paymentsRes.data);
      setStudents(studentsRes.data);
      setClasses(classesRes.data);
    } catch (err) {
      setError('Failed to load fee data.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadAll(); }, []);

  async function handleCreateStructure(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await api.post('/fees/structures', {
        feeType: structureForm.feeType,
        amount: Number(structureForm.amount),
        frequency: structureForm.frequency,
        academicYear: structureForm.academicYear,
        schoolClass: structureForm.classId ? { id: Number(structureForm.classId) } : null,
      });
      setShowStructureModal(false);
      setStructureForm(emptyStructureForm);
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save fee structure.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteStructure(id) {
    if (!confirm('Delete this fee structure?')) return;
    try {
      await api.delete(`/fees/structures/${id}`);
      loadAll();
    } catch (err) {
      setError('Failed to delete fee structure.');
    }
  }

  async function handleRecordPayment(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await api.post('/fees/payments', {
        student: { id: Number(paymentForm.studentId) },
        feeStructure: paymentForm.feeStructureId ? { id: Number(paymentForm.feeStructureId) } : null,
        amountPaid: Number(paymentForm.amountPaid),
        paymentDate: paymentForm.paymentDate || new Date().toISOString().slice(0, 10),
        paymentMode: paymentForm.paymentMode,
        transactionRef: paymentForm.transactionRef,
        remarks: paymentForm.remarks,
        status: 'PAID',
      });
      setShowPaymentModal(false);
      setPaymentForm(emptyPaymentForm);
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDeletePayment(id) {
    if (!confirm('Delete this payment record?')) return;
    try {
      await api.delete(`/fees/payments/${id}`);
      loadAll();
    } catch (err) {
      setError('Failed to delete payment.');
    }
  }

  async function handleLookup() {
    if (!lookupStudentId) return;
    try {
      const { data } = await api.get(`/fees/summary/student/${lookupStudentId}`);
      setSummary(data);
    } catch (err) {
      setError('Failed to fetch fee summary.');
    }
  }

  const studentName = (id) => {
    const s = students.find((x) => x.id === id);
    return s ? `${s.firstName} ${s.lastName}` : `#${id}`;
  };

  function handleExportPayments() {
    exportToExcel('fee_payments', [
      { label: 'Date', key: 'paymentDate' },
      { label: 'Student', key: (p) => (p.student ? `${p.student.firstName} ${p.student.lastName}` : '') },
      { label: 'Amount', key: 'amountPaid' },
      { label: 'Mode', key: 'paymentMode' },
      { label: 'Reference', key: 'transactionRef' },
    ], payments);
  }

  function handleExportStructures() {
    exportToExcel('fee_structures', [
      { label: 'Fee Type', key: 'feeType' },
      { label: 'Class', key: (f) => (f.schoolClass ? `${f.schoolClass.className} - ${f.schoolClass.section}` : 'All Classes') },
      { label: 'Amount', key: 'amount' },
      { label: 'Frequency', key: 'frequency' },
      { label: 'Year', key: 'academicYear' },
    ], structures);
  }

  return (
    <Layout eyebrow="Finance" title="Fees & Payments">
      {error && <div className="error-banner">{error}</div>}

      <div style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
        {['payments', 'structures', 'lookup'].map((t) => (
          <button
            key={t}
            className={`btn ${tab === t ? 'btn-primary' : 'btn-ghost'}`}
            onClick={() => setTab(t)}
          >
            {t === 'payments' ? 'Payments' : t === 'structures' ? 'Fee Structures' : 'Student Balance'}
          </button>
        ))}
      </div>

      {tab === 'payments' && (
        <>
          <div className="page-header">
            <div />
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost" onClick={handleExportPayments} disabled={payments.length === 0}>Export to Excel</button>
              <button className="btn btn-primary" onClick={() => setShowPaymentModal(true)}>+ Record Payment</button>
            </div>
          </div>
          <div className="card">
            {loading ? (
              <div className="empty-state">Loading payments…</div>
            ) : payments.length === 0 ? (
              <div className="empty-state">No payments recorded yet.</div>
            ) : (
              <table>
                <thead>
                  <tr><th>Date</th><th>Student</th><th>Amount</th><th>Mode</th><th>Reference</th><th></th></tr>
                </thead>
                <tbody>
                  {payments.map((p) => (
                    <tr key={p.id}>
                      <td>{p.paymentDate}</td>
                      <td>{p.student ? `${p.student.firstName} ${p.student.lastName}` : '—'}</td>
                      <td>₹{p.amountPaid?.toLocaleString()}</td>
                      <td>{p.paymentMode || '—'}</td>
                      <td style={{ fontFamily: 'var(--font-mono)' }}>{p.transactionRef || '—'}</td>
                      <td style={{ textAlign: 'right' }}>
                        <button className="btn btn-ghost" onClick={() => printFeeReceipt(p)}>Print Receipt</button>{' '}
                        <button className="btn btn-danger" onClick={() => handleDeletePayment(p.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {tab === 'structures' && (
        <>
          <div className="page-header">
            <div />
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-ghost" onClick={handleExportStructures} disabled={structures.length === 0}>Export to Excel</button>
              <button className="btn btn-primary" onClick={() => setShowStructureModal(true)}>+ New Fee Structure</button>
            </div>
          </div>
          <div className="card">
            {loading ? (
              <div className="empty-state">Loading fee structures…</div>
            ) : structures.length === 0 ? (
              <div className="empty-state">No fee structures defined yet.</div>
            ) : (
              <table>
                <thead>
                  <tr><th>Fee Type</th><th>Class</th><th>Amount</th><th>Frequency</th><th>Year</th><th></th></tr>
                </thead>
                <tbody>
                  {structures.map((f) => (
                    <tr key={f.id}>
                      <td>{f.feeType}</td>
                      <td>{f.schoolClass ? `${f.schoolClass.className} - ${f.schoolClass.section}` : 'All Classes'}</td>
                      <td>₹{f.amount?.toLocaleString()}</td>
                      <td>{f.frequency}</td>
                      <td>{f.academicYear || '—'}</td>
                      <td style={{ textAlign: 'right' }}>
                        <button className="btn btn-danger" onClick={() => handleDeleteStructure(f.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {tab === 'lookup' && (
        <div className="card card-pad">
          <div style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
            <select value={lookupStudentId} onChange={(e) => setLookupStudentId(e.target.value)} style={{ padding: '8px 10px', border: '1px solid var(--color-ledger-line)', borderRadius: 4, flex: 1 }}>
              <option value="">Select a student…</option>
              {students.map((s) => (
                <option key={s.id} value={s.id}>{s.firstName} {s.lastName} ({s.admissionNumber})</option>
              ))}
            </select>
            <button className="btn btn-primary" onClick={handleLookup}>Check Balance</button>
          </div>

          {summary && (
            <div>
              <div className="stat-grid">
                <div className="stat-card">
                  <div className="stat-label">Total Due</div>
                  <div className="stat-value">₹{summary.totalDue?.toLocaleString()}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Total Paid</div>
                  <div className="stat-value">₹{summary.totalPaid?.toLocaleString()}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Balance</div>
                  <div className="stat-value" style={{ color: summary.balance > 0 ? 'var(--color-danger)' : 'var(--color-primary)' }}>
                    ₹{summary.balance?.toLocaleString()}
                  </div>
                </div>
              </div>
              <h3>Payment History</h3>
              {summary.payments?.length === 0 ? (
                <p style={{ color: 'var(--color-ink-soft)' }}>No payments recorded for this student yet.</p>
              ) : (
                <table>
                  <thead><tr><th>Date</th><th>Amount</th><th>Mode</th><th>Reference</th><th></th></tr></thead>
                  <tbody>
                    {summary.payments?.map((p) => (
                      <tr key={p.id}>
                        <td>{p.paymentDate}</td>
                        <td>₹{p.amountPaid?.toLocaleString()}</td>
                        <td>{p.paymentMode || '—'}</td>
                        <td style={{ fontFamily: 'var(--font-mono)' }}>{p.transactionRef || '—'}</td>
                        <td style={{ textAlign: 'right' }}>
                          <button className="btn btn-ghost" onClick={() => printFeeReceipt(p)}>Print Receipt</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}
        </div>
      )}

      {showStructureModal && (
        <Modal title="New Fee Structure" onClose={() => setShowStructureModal(false)}>
          <form onSubmit={handleCreateStructure} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div className="form-grid">
              <div className="field">
                <label>Fee Type</label>
                <input required value={structureForm.feeType} onChange={(e) => setStructureForm({ ...structureForm, feeType: e.target.value })} placeholder="Tuition Fee" />
              </div>
              <div className="field">
                <label>Applies To</label>
                <select value={structureForm.classId} onChange={(e) => setStructureForm({ ...structureForm, classId: e.target.value })}>
                  <option value="">All Classes</option>
                  {classes.map((c) => (
                    <option key={c.id} value={c.id}>{c.className} - {c.section}</option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Amount (₹)</label>
                <input required type="number" value={structureForm.amount} onChange={(e) => setStructureForm({ ...structureForm, amount: e.target.value })} />
              </div>
              <div className="field">
                <label>Frequency</label>
                <select value={structureForm.frequency} onChange={(e) => setStructureForm({ ...structureForm, frequency: e.target.value })}>
                  <option value="MONTHLY">Monthly</option>
                  <option value="QUARTERLY">Quarterly</option>
                  <option value="ANNUAL">Annual</option>
                  <option value="ONE_TIME">One-time</option>
                </select>
              </div>
              <div className="field">
                <label>Academic Year</label>
                <input value={structureForm.academicYear} onChange={(e) => setStructureForm({ ...structureForm, academicYear: e.target.value })} placeholder="2026-2027" />
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowStructureModal(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button>
            </div>
          </form>
        </Modal>
      )}

      {showPaymentModal && (
        <Modal title="Record Payment" onClose={() => setShowPaymentModal(false)}>
          <form onSubmit={handleRecordPayment} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div className="form-grid">
              <div className="field" style={{ gridColumn: 'span 2' }}>
                <label>Student</label>
                <select required value={paymentForm.studentId} onChange={(e) => setPaymentForm({ ...paymentForm, studentId: e.target.value })}>
                  <option value="">Select student…</option>
                  {students.map((s) => (
                    <option key={s.id} value={s.id}>{s.firstName} {s.lastName} ({s.admissionNumber})</option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Fee Structure (optional)</label>
                <select value={paymentForm.feeStructureId} onChange={(e) => setPaymentForm({ ...paymentForm, feeStructureId: e.target.value })}>
                  <option value="">— General Payment —</option>
                  {structures.map((f) => (
                    <option key={f.id} value={f.id}>{f.feeType} (₹{f.amount})</option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Amount Paid (₹)</label>
                <input required type="number" value={paymentForm.amountPaid} onChange={(e) => setPaymentForm({ ...paymentForm, amountPaid: e.target.value })} />
              </div>
              <div className="field">
                <label>Payment Date</label>
                <input type="date" value={paymentForm.paymentDate} onChange={(e) => setPaymentForm({ ...paymentForm, paymentDate: e.target.value })} />
              </div>
              <div className="field">
                <label>Payment Mode</label>
                <select value={paymentForm.paymentMode} onChange={(e) => setPaymentForm({ ...paymentForm, paymentMode: e.target.value })}>
                  <option value="CASH">Cash</option>
                  <option value="CARD">Card</option>
                  <option value="UPI">UPI</option>
                  <option value="BANK_TRANSFER">Bank Transfer</option>
                  <option value="CHEQUE">Cheque</option>
                </select>
              </div>
              <div className="field">
                <label>Transaction Ref</label>
                <input value={paymentForm.transactionRef} onChange={(e) => setPaymentForm({ ...paymentForm, transactionRef: e.target.value })} />
              </div>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 8 }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowPaymentModal(false)}>Cancel</button>
              <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Payment'}</button>
            </div>
          </form>
        </Modal>
      )}
    </Layout>
  );
}
