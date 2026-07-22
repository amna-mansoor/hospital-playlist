import { useState } from 'react';
import api from '../api/api.js';
import { useNavigate } from 'react-router-dom';

export default function Register() {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('PATIENT');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      const res = await api.post('/api/auth/register', { fullName, email, password, role });
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('role', res.data.role);
      localStorage.setItem('fullName', res.data.fullName);
      navigate('/');
      window.location.reload();
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed.');
    }
  }

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2>Create an account</h2>
      <form onSubmit={handleSubmit}>
        <div><input placeholder="Full name" value={fullName} onChange={e => setFullName(e.target.value)} required style={{ width: '100%' }} /></div>
        <div><input placeholder="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} required style={{ width: '100%' }} /></div>
        <div><input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} required style={{ width: '100%' }} /></div>
        <div>
          <select value={role} onChange={e => setRole(e.target.value)} style={{ width: '100%' }}>
            <option value="PATIENT">I am a Patient</option>
            <option value="DOCTOR">I am a Doctor</option>
          </select>
        </div>
        {error && <p className="error">{error}</p>}
        <button type="submit">Register</button>
      </form>
      <p style={{ fontSize: 13, color: '#666' }}>
        Note: doctor accounts still need an admin to assign their department and
        working hours before they show up in the booking calendar.
      </p>
    </div>
  );
}
