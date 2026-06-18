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
    return;
  }

  // Inyectar chat widget en todas las páginas autenticadas
  const src = script ? script.src.replace('auth-guard.js', 'chat-widget.js') : null;
  if (src && !document.getElementById('gc-chat-widget')) {
    const s = document.createElement('script');
    s.src = src;
    document.addEventListener('DOMContentLoaded', () => document.body.appendChild(s));
  }
})();
