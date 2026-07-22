import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import api from '../api/axios';

export default function Dashboard() {
  const [stats, setStats] = useState({ students: 0, staff: 0, classes: 0, feesCollected: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function loadStats() {
      try {
        const [studentsRes, staffRes, classesRes, paymentsRes] = await Promise.all([
          api.get('/students'),
          api.get('/staff'),
          api.get('/classes'),
          api.get('/fees/payments'),
        ]);
        const totalCollected = paymentsRes.data.reduce((sum, p) => sum + (p.amountPaid || 0), 0);
        setStats({
          students: studentsRes.data.length,
          staff: staffRes.data.length,
          classes: classesRes.data.length,
          feesCollected: totalCollected,
        });
      } catch (err) {
        setError('Could not load dashboard data. Is the backend running?');
      } finally {
        setLoading(false);
      }
    }
    loadStats();
  }, []);

  return (
    <Layout eyebrow="Overview" title="Dashboard">
      {error && <div className="error-banner">{error}</div>}

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-label">Enrolled Students</div>
          <div className="stat-value">{loading ? '—' : stats.students}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Staff Members</div>
          <div className="stat-value">{loading ? '—' : stats.staff}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Active Classes</div>
          <div className="stat-value">{loading ? '—' : stats.classes}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Fees Collected</div>
          <div className="stat-value">{loading ? '—' : `₹${stats.feesCollected.toLocaleString()}`}</div>
        </div>
      </div>

      <div className="card card-pad">
        <h3>Getting started</h3>
        <p style={{ color: 'var(--color-ink-soft)', fontSize: 13.5 }}>
          Use the sidebar to manage student admissions, staff records, class sections, daily attendance,
          and the fee ledger. New here? Start by adding a class under <strong>Classes</strong>, then admit
          students into it.
        </p>
      </div>
    </Layout>
  );
}
