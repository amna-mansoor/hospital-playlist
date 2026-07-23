import { BrowserRouter, Routes, Route, Link, Navigate, useLocation } from 'react-router-dom';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import PatientBooking from './pages/PatientBooking.jsx';
import DoctorDashboard from './pages/DoctorDashboard.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';

function getRole() { return localStorage.getItem('role'); }
function getFullName() { return localStorage.getItem('fullName'); }

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

function TopNav() {
  const role = getRole();
  const fullName = getFullName();
  const location = useLocation();
  const isActive = (path) => location.pathname === path;

  return (
    <nav>
      <span className="brand">🏥 Hospital System</span>
      {role === 'PATIENT' && <Link to="/book" className={isActive('/book') ? 'active' : ''}>Book Appointment</Link>}
      {role === 'DOCTOR' && <Link to="/doctor" className={isActive('/doctor') ? 'active' : ''}>My Schedule</Link>}
      {role === 'ADMIN' && <Link to="/admin" className={isActive('/admin') ? 'active' : ''}>Admin Dashboard</Link>}
      <div className="greeting">
        {fullName ? (
          <>
            <span>Hi, <strong>{fullName}</strong> <span style={{ opacity: 0.6 }}>({role?.toLowerCase()})</span></span>
            <a className="logout-link" onClick={logout}>Log out</a>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <TopNav />
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