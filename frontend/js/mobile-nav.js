// Mobile bottom navigation — auto-injected on dashboard pages
(function () {
  const path = window.location.pathname;
  const isEntrenador = path.includes('/entrenador/');
  const isCliente = path.includes('/cliente/') || path.endsWith('/buscar.html');
  if (!isEntrenador && !isCliente) return;

  const page = path.split('/').pop();

  const entrenadorItems = [
    { icon: '👤', label: 'Perfil',    href: 'perfil.html',    match: 'perfil.html' },
    { icon: '📋', label: 'Rutinas',   href: 'rutinas.html',   match: 'rutinas.html' },
    { icon: '👥', label: 'Clientes',  href: 'clientes.html',  match: 'clientes.html' },
    { icon: '💬', label: 'Chat',      href: '../chat/index.html', match: 'index.html' },
    { icon: '☰',  label: 'Más',      href: '#',              isMenu: true },
  ];

  const clienteItems = [
    { icon: '🏠', label: 'Inicio',    href: 'dashboard.html',   match: 'dashboard.html' },
    { icon: '📋', label: 'Rutina',    href: 'mi-rutina.html',   match: 'mi-rutina.html' },
    { icon: '🥗', label: 'Nutrición', href: 'mi-nutricion.html',match: 'mi-nutricion.html' },
    { icon: '📈', label: 'Progreso',  href: 'mi-progreso.html', match: 'mi-progreso.html' },
    { icon: '🔍', label: 'Buscar',    href: 'buscar.html',      match: 'buscar.html' },
  ];

  const items = isEntrenador ? entrenadorItems : clienteItems;

  const nav = document.createElement('nav');
  nav.className = 'mobile-nav';
  nav.innerHTML = '<div class="mobile-nav-inner">' +
    items.map(item => {
      const active = item.match && page === item.match;
      if (item.isMenu) {
        return `<button class="mobile-menu-btn${active ? ' active' : ''}" onclick="toggleMobileSidebar()"><span class="nav-icon">${item.icon}</span><span>${item.label}</span></button>`;
      }
      return `<a href="${item.href}"${active ? ' class="active"' : ''}><span class="nav-icon">${item.icon}</span><span>${item.label}</span></a>`;
    }).join('') +
    '</div>';

  const backdrop = document.createElement('div');
  backdrop.className = 'sidebar-backdrop';
  backdrop.addEventListener('click', closeMobileSidebar);

  document.addEventListener('DOMContentLoaded', function () {
    document.body.classList.add('has-mobile-nav');
    document.body.appendChild(nav);
    document.body.appendChild(backdrop);
  });

  window.toggleMobileSidebar = function () {
    const sidebar = document.querySelector('aside.sidebar');
    if (!sidebar) return;
    sidebar.classList.contains('open') ? closeMobileSidebar() : openMobileSidebar();
  };

  function openMobileSidebar() {
    const sidebar = document.querySelector('aside.sidebar');
    if (sidebar) sidebar.classList.add('open');
    backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  function closeMobileSidebar() {
    const sidebar = document.querySelector('aside.sidebar');
    if (sidebar) sidebar.classList.remove('open');
    backdrop.classList.remove('open');
    document.body.style.overflow = '';
  }
})();
