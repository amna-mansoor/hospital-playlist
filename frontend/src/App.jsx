import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import PatientBooking from './pages/PatientBooking.jsx';
import DoctorDashboard from './pages/DoctorDashboard.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';

function getRole() {
  return localStorage.getItem('role');
}

function ProtectedRoute({ role, children }) {
  const currentRole = getRole();
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  if (role && currentRole !== role) return <Navigate to="/" replace />;
  return children;
}

function Home() {
  const role = getRole();
  if (role === 'PATIENT') return <Navigate to="/book" replace />;
  if (role === 'DOCTOR') return <Navigate to="/doctor" replace />;
  if (role === 'ADMIN') return <Navigate to="/admin" replace />;
  return <Navigate to="/login" replace />;
}

function logout() {
  localStorage.clear();
  window.location.href = '/login';
}

export default function App() {
  const role = getRole();

  return (
    <BrowserRouter>
      <nav>
        <strong>🏥 Hospital System</strong>
        {role === 'PATIENT' && <Link to="/book">Book Appointment</Link>}
        {role === 'DOCTOR' && <Link to="/doctor">My Schedule</Link>}
        {role === 'ADMIN' && <Link to="/admin">Admin Dashboard</Link>}
        {role ? (
          <a href="#" onClick={logout} style={{ marginLeft: 'auto' }}>Log out</a>
        ) : (
          <>
            <Link to="/login" style={{ marginLeft: 'auto' }}>Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>

      <div className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/book" element={<ProtectedRoute role="PATIENT"><PatientBooking /></ProtectedRoute>} />
          <Route path="/doctor" element={<ProtectedRoute role="DOCTOR"><DoctorDashboard /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminDashboard /></ProtectedRoute>} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}
