import { useState } from 'react';
import api from '../api/api.js';
import { useNavigate, Link } from 'react-router-dom';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      const res = await api.post('/api/auth/login', { email, password });
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('role', res.data.role);
      localStorage.setItem('fullName', res.data.fullName);
      navigate('/');
      window.location.reload(); // refresh nav links based on new role
    } catch (err) {
      setError(err.response?.data?.error || 'Login failed. Check your email and password.');
    }
  }

  return (
    <div className="card" style={{ maxWidth: 400, margin: '40px auto' }}>
      <h2>Log in</h2>
      <form onSubmit={handleSubmit}>
        <div><input placeholder="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} required style={{ width: '100%' }} /></div>
        <div><input placeholder="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} required style={{ width: '100%' }} /></div>
        {error && <p className="error">{error}</p>}
        <button type="submit">Log In</button>
      </form>
      <p>No account? <Link to="/register">Register here</Link></p>
    </div>
  );
}
