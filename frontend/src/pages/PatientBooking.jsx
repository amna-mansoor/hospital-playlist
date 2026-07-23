import { useEffect, useState } from 'react';
import api from '../api/api.js';

export default function PatientBooking() {
  const [doctors, setDoctors] = useState([]);
  const [doctorId, setDoctorId] = useState('');
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [slots, setSlots] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/doctors').then(res => {
      setDoctors(res.data);
      if (res.data.length > 0) setDoctorId(String(res.data[0].id));
    }).catch(err => setError(err.response?.data?.error || 'Could not load doctors.'));
  }, []);

  useEffect(() => {
    setSelectedDoctor(doctors.find(d => String(d.id) === String(doctorId)) || null);
  }, [doctorId, doctors]);

  async function loadSlots() {
    if (!doctorId) return;
    setError('');
    try {
      const res = await api.get('/api/appointments/slots', { params: { doctorId, date } });
      setSlots(res.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Could not load slots.');
    }
  }

  useEffect(() => { if (doctorId) loadSlots(); }, [date, doctorId]);

  async function book(slotStart) {
    setMessage(''); setError('');
    try {
      await api.post('/api/appointments/book', { doctorId, slotStart });
      setMessage('Appointment booked! Check your email for confirmation.');
      loadSlots();
    } catch (err) {
      setError(err.response?.data?.error || 'Booking failed.');
      loadSlots();
    }
  }

  async function joinWaitlist() {
    try {
      await api.post('/api/waitlist/join', null, { params: { doctorId, date } });
      setMessage("You're on the waitlist. We'll email you if a slot opens up.");
    } catch (err) {
      setError(err.response?.data?.error || 'Could not join waitlist.');
    }
  }

  const allTaken = slots.length > 0 && slots.every(s => s.taken);
  const dayNames = ['', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

  return (
    <div>
      <h2>Book an Appointment</h2>

      <div className="card">
        <label>Doctor: </label>
        <select value={doctorId} onChange={e => setDoctorId(e.target.value)} style={{ minWidth: 320 }}>
          {doctors.map(d => (
            <option key={d.id} value={d.id}>
              Dr. {d.fullName} — {d.departmentName} ({d.specialization})
            </option>
          ))}
        </select>
        <label style={{ marginLeft: 16 }}>Date: </label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} />
        <button style={{ marginLeft: 12 }} onClick={loadSlots}>Refresh Slots</button>
      </div>

      {selectedDoctor && (
        <div className="doctor-info-card">
          <div className="name">Dr. {selectedDoctor.fullName}</div>
          <div className="meta">
            {selectedDoctor.specialization} &middot; {selectedDoctor.departmentName} Department
            &middot; Works {dayNames[selectedDoctor.dayOfWeek]}s, {selectedDoctor.shiftStart?.slice(0,5)}–{selectedDoctor.shiftEnd?.slice(0,5)}
            {selectedDoctor.onLeave && ' · Currently on leave'}
          </div>
        </div>
      )}

      {message && <p className="success-msg">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="card">
        <h3>Available Slots</h3>
        {slots.length === 0 && <p style={{ color: 'var(--text-muted)' }}>No slots for this doctor on this day (they may not work this day, or be on leave).</p>}
        <div className="slot-grid">
          {slots.map(s => (
            <button
              key={s.start}
              disabled={s.taken}
              onClick={() => book(s.start)}
              className={`slot-btn ${s.taken ? 'taken' : ''}`}
            >
              {new Date(s.start).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
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