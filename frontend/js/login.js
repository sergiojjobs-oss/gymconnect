const API = 'http://localhost:8080';

function showToast(msg, type) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = 'toast toast-' + type + ' show';
  setTimeout(() => t.classList.remove('show'), 3500);
}

document.getElementById('loginForm').addEventListener('submit', async function (e) {
  e.preventDefault();

  const btn = this.querySelector('button[type="submit"]');
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value;

  if (!email || !password) {
    showToast('Rellena todos los campos', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Entrando…';

  try {
    const res = await fetch(`${API}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    const data = await res.json();

    if (!res.ok) {
      showToast(data.error || 'Credenciales incorrectas', 'error');
      return;
    }

    // Guardar sesión en localStorage
    localStorage.setItem('gc_token', data.token);
    localStorage.setItem('gc_user', JSON.stringify({
      id: data.id, nombre: data.nombre, email: data.email,
      rol: data.rol, plan: data.plan
    }));

    showToast('¡Bienvenido, ' + data.nombre + '! 👋', 'success');

    // Redirigir según rol
    setTimeout(() => {
      if (data.rol === 'ENTRENADOR') {
        window.location.href = 'entrenador/resultados.html';
      } else {
        window.location.href = 'cliente/buscar.html';
      }
    }, 1200);

  } catch (err) {
    showToast('No se puede conectar con el servidor', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Entrar';
  }
});
