import { useEffect, useState } from 'react';
import api from '../api/api.js';

export default function PatientBooking() {
  const [doctorId, setDoctorId] = useState('');
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [slots, setSlots] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // NOTE: in a fuller build this list would come from GET /api/departments + doctors
  // per department. Wire it up once you've seeded some doctors (see SETUP_GUIDE.md,
  // Part 6, "Seeding your first data").
  const [doctorIdInput, setDoctorIdInput] = useState('1');

  async function loadSlots() {
    setError('');
    try {
      const res = await api.get('/api/appointments/slots', { params: { doctorId: doctorIdInput, date } });
      setSlots(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Could not load slots.');
    }
  }

  useEffect(() => { loadSlots(); }, [date]);

  async function book(slotStart) {
    setMessage(''); setError('');
    try {
      await api.post('/api/appointments/book', { doctorId: doctorIdInput, slotStart });
      setMessage('Appointment booked! Check your email for confirmation.');
      loadSlots(); // refresh so the just-booked slot now shows as taken
    } catch (err) {
      // This is the friendly message our backend sends when two patients
      // race for the same slot - see AppointmentService.bookAppointment
      setError(err.response?.data?.error || 'Booking failed.');
      loadSlots();
    }
  }

  async function joinWaitlist() {
    try {
      await api.post('/api/waitlist/join', null, { params: { doctorId: doctorIdInput, date } });
      setMessage("You're on the waitlist. We'll email you if a slot opens up.");
    } catch (err) {
      setError(err.response?.data?.error || 'Could not join waitlist.');
    }
  }

  const allTaken = slots.length > 0 && slots.every(s => s.taken);

  return (
    <div>
      <h2>Book an Appointment</h2>
      <div className="card">
        <label>Doctor ID: </label>
        <input value={doctorIdInput} onChange={e => setDoctorIdInput(e.target.value)} style={{ width: 80 }} />
        <label style={{ marginLeft: 16 }}>Date: </label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} />
        <button style={{ marginLeft: 12 }} onClick={loadSlots}>Refresh Slots</button>
      </div>

      {message && <p style={{ color: '#166534' }}>{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="card">
        <h3>Available Slots</h3>
        {slots.length === 0 && <p>No slots for this doctor on this day (they may not work this day, or be on leave).</p>}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
          {slots.map(s => (
            <button
              key={s.start}
              disabled={s.taken}
              onClick={() => book(s.start)}
              style={{ background: s.taken ? '#9ca3af' : '#2563eb' }}
            >
              {new Date(s.start).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              {' '}
              <span className={`badge ${s.taken ? 'taken' : 'free'}`}>{s.taken ? 'Booked' : 'Free'}</span>
            </button>
          ))}
        </div>

        {allTaken && (
          <div style={{ marginTop: 16 }}>
            <p>All slots are full for this day.</p>
            <button onClick={joinWaitlist}>Join Waitlist</button>
          </div>
        )}
      </div>
    </div>
  );
}
