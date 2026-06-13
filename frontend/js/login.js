function showToast(msg, type) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast toast-' + type + ' show';
  setTimeout(() => t.classList.remove('show'), 3500);
}

document.getElementById('loginForm').addEventListener('submit', function (e) {
  e.preventDefault();
  // TODO: conectar con POST /api/auth/login
  showToast('Próximamente: inicio de sesión con el servidor.', 'info');
});
