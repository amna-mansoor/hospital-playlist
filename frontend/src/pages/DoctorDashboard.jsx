import { useEffect, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import api from '../api/api.js';

export default function DoctorDashboard() {
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState('');

  async function load() {
    try {
      const res = await api.get('/api/doctors/dashboard/today');
      setAppointments(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Could not load schedule.');
    }
  }

  useEffect(() => { load(); }, []);

  async function markStatus(id, status) {
    try {
      await api.post(`/api/appointments/${id}/status`, null, { params: { status } });
      load();
    } catch (err) {
      setError(err.response?.data?.error || 'Could not update status.');
    }
  }

  // Convert appointments into FullCalendar's event format
  const events = appointments.map(a => ({
    id: a.id,
    title: `${a.patient?.user?.fullName || 'Patient'} (${a.status})`,
    start: a.slotStart,
    end: a.slotEnd,
    backgroundColor: a.status === 'BOOKED' ? '#2563eb' : a.status === 'COMPLETED' ? '#16a34a' : '#9ca3af',
  }));

  return (
    <div>
      <h2>Today's Schedule</h2>
      {error && <p className="error">{error}</p>}

      <div className="card">
        <FullCalendar
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          initialView="timeGridDay"
          height="auto"
          events={events}
          headerToolbar={false}
        />
      </div>

      <div className="card">
        <h3>Mark Appointments</h3>
        <table width="100%" cellPadding="8">
          <thead><tr><th align="left">Patient</th><th align="left">Time</th><th align="left">Status</th><th></th></tr></thead>
          <tbody>
            {appointments.map(a => (
              <tr key={a.id}>
                <td>{a.patient?.user?.fullName}</td>
                <td>{new Date(a.slotStart).toLocaleTimeString()}</td>
                <td>{a.status}</td>
                <td>
                  <button onClick={() => markStatus(a.id, 'COMPLETED')}>Completed</button>{' '}
                  <button onClick={() => markStatus(a.id, 'NO_SHOW')} style={{ background: '#b91c1c' }}>No-show</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
