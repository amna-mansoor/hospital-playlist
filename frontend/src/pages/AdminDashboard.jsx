import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, CartesianGrid } from 'recharts';
import api from '../api/api.js';

export default function AdminDashboard() {
  const [deptLoad, setDeptLoad] = useState([]);
  const [utilization, setUtilization] = useState([]);
  const [peakHours, setPeakHours] = useState([]);
  const [occupancy, setOccupancy] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [a, b, c, d] = await Promise.all([
          api.get('/api/admin/analytics/department-load'),
          api.get('/api/admin/analytics/doctor-utilization'),
          api.get('/api/admin/analytics/peak-hours'),
          api.get('/api/admin/analytics/bed-occupancy'),
        ]);
        setDeptLoad(a.data);
        setUtilization(b.data);
        setPeakHours(c.data);
        setOccupancy(d.data);
      } catch (err) {
        setError(err.response?.data?.error || 'Could not load analytics.');
      }
    }
    load();
  }, []);

  return (
    <div>
      <h2>Admin Dashboard</h2>
      {error && <p className="error">{error}</p>}

      <div className="card">
        <h3>Bed Occupancy</h3>
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          {occupancy.map(o => (
            <div key={o.department} className="card" style={{ minWidth: 160 }}>
              <strong>{o.department}</strong>
              <p style={{ fontSize: 22, margin: '4px 0' }}>{o.occupied} / {o.total}</p>
              <span>occupied</span>
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <h3>Department Load (appointments, last 30 days)</h3>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={deptLoad}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="department" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="appointmentsLast30Days" fill="#2563eb" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="card">
        <h3>Doctor Utilization %  (last 7 days)</h3>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={utilization}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="doctor" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="utilizationPct" fill="#16a34a" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="card">
        <h3>Peak Booking Hours (last 30 days)</h3>
        <ResponsiveContainer width="100%" height={260}>
          <LineChart data={peakHours}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="hour" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="bookings" stroke="#b91c1c" />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
