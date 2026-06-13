// Protege páginas que requieren login
// Uso: <script src="../js/auth-guard.js" data-rol="ENTRENADOR"></script>
(function () {
  const token = localStorage.getItem('gc_token');
  const user  = JSON.parse(localStorage.getItem('gc_user') || 'null');

  if (!token || !user) {
    window.location.href = (window.location.pathname.includes('/cliente/') ||
                            window.location.pathname.includes('/entrenador/') ||
                            window.location.pathname.includes('/chat/'))
      ? '../login.html' : 'login.html';
    return;
  }

  // Verificar rol si se especifica data-rol
  const script = document.currentScript;
  const rolRequerido = script && script.getAttribute('data-rol');
  if (rolRequerido && user.rol !== rolRequerido) {
    window.location.href = '../login.html';
  }
})();
