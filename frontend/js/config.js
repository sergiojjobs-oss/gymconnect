// URL del backend — cambia aquí y se aplica en toda la app
const API = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
  ? 'http://localhost:8080'
  : 'https://gymconnect-0td0.onrender.com';

const FRONTEND_URL = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
  ? 'http://localhost:3000'
  : 'https://momentfitapp.vercel.app';
