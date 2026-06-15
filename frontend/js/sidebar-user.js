(function() {
  const u = JSON.parse(localStorage.getItem('gc_user') || 'null');
  if (!u) return;
  const sidebar = document.querySelector('.sidebar');
  if (!sidebar) return;
  const ini = ((u.nombre||'?').charAt(0) + (u.apellido||'?').charAt(0)).toUpperCase();
  const role = u.rol === 'ENTRENADOR' ? 'Entrenador' : 'Cliente';
  const chip = document.createElement('div');
  chip.className = 'sidebar-user';
  chip.innerHTML = `
    <div class="sidebar-user-avatar" id="su-avatar">${ini}</div>
    <div style="flex:1;min-width:0;">
      <div class="sidebar-user-name">${u.nombre||''} ${u.apellido||''}</div>
      <div class="sidebar-user-role">${role}</div>
    </div>
    <span title="Cerrar sesión" style="font-size:1rem;opacity:0.45;cursor:pointer;" onclick="localStorage.clear();location.href='/login.html'">⏻</span>`;
  sidebar.appendChild(chip);

  // Load avatar photo if available
  const token = localStorage.getItem('gc_token');
  if (token && u.rol === 'ENTRENADOR') {
    fetch(`${window.API || ''}/api/entrenadores/mi-perfil`, {
      headers: { Authorization: 'Bearer ' + token }
    }).then(r => r.json()).then(data => {
      if (data.fotoUrl) {
        const av = document.getElementById('su-avatar');
        if (av) av.innerHTML = `<img src="${data.fotoUrl}" alt="" style="width:100%;height:100%;object-fit:cover;border-radius:8px;">`;
      }
    }).catch(() => {});
  }
})();
