import axios from 'axios';

// All backend calls go through this one shared axios instance so we only
// have to configure the base URL and the auth token attachment once.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
});

// Attach the JWT token (saved after login) to every outgoing request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
