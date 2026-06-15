// Mobile bottom navigation — injected on dashboard pages
(function () {
  const path = window.location.pathname;
  const isEntrenador = path.includes('/entrenador/');
  const isCliente    = path.includes('/cliente/') || path.endsWith('/buscar.html');
  if (!isEntrenador && !isCliente) return;

  const page = path.split('/').pop();

  const entrenadorItems = [
    { icon: '👤', label: 'Perfil',   href: 'perfil.html',       match: 'perfil.html' },
    { icon: '📋', label: 'Rutinas',  href: 'rutinas.html',      match: 'rutinas.html' },
    { icon: '👥', label: 'Clientes', href: 'clientes.html',     match: 'clientes.html' },
    { icon: '💬', label: 'Chat',     href: '../chat/index.html',match: 'index.html' },
    { icon: '☰',  label: 'Más',     href: '#',                 isMenu: true },
  ];

  const clienteItems = [
    { icon: '🏠', label: 'Inicio',    href: 'dashboard.html',    match: 'dashboard.html' },
    { icon: '📋', label: 'Rutina',    href: 'mi-rutina.html',    match: 'mi-rutina.html' },
    { icon: '🥗', label: 'Nutrición', href: 'mi-nutricion.html', match: 'mi-nutricion.html' },
    { icon: '📈', label: 'Progreso',  href: 'mi-progreso.html',  match: 'mi-progreso.html' },
    { icon: '🔍', label: 'Buscar',    href: 'buscar.html',       match: 'buscar.html' },
  ];

  const items = isEntrenador ? entrenadorItems : clienteItems;

  // Build nav HTML
  const nav = document.createElement('nav');
  nav.className = 'mobile-nav';
  nav.innerHTML = '<div class="mobile-nav-inner">' +
    items.map(item => {
      const active = item.match && page === item.match;
      if (item.isMenu) {
        return `<button class="mobile-menu-btn" onclick="toggleMobileSidebar()">
          <span class="nav-icon">${item.icon}</span><span>${item.label}</span></button>`;
      }
      return `<a href="${item.href}"${active ? ' class="active"' : ''}>
        <span class="nav-icon">${item.icon}</span><span>${item.label}</span></a>`;
    }).join('') + '</div>';

  // Backdrop for sidebar overlay
  const backdrop = document.createElement('div');
  backdrop.className = 'sidebar-backdrop';
  backdrop.addEventListener('click', closeMobileSidebar);

  function applyMobileLayout() {
    if (window.innerWidth > 900) return;

    const header  = document.querySelector('header');
    const layout  = document.querySelector('.dashboard-layout');
    const main    = document.querySelector('.main-content, .main');

    if (!layout) return;

    const NAV_H = 58; // px

    // Body: flex column filling viewport
    document.body.style.cssText =
      'display:flex;flex-direction:column;height:100vh;height:100dvh;overflow:hidden;margin:0;';

    // Header: fixed height, no sticky needed
    if (header) {
      header.style.cssText = 'flex-shrink:0;position:relative;height:64px;';
    }

    // Dashboard layout: fills remaining space, scrolls internally
    layout.style.cssText =
      'display:grid;grid-template-columns:1fr;flex:1;overflow-y:auto;' +
      'overflow-x:hidden;-webkit-overflow-scrolling:touch;min-height:0;';

    // Hide sidebar
    const sidebar = document.querySelector('aside.sidebar');
    if (sidebar && !sidebar.classList.contains('open')) {
      sidebar.style.display = 'none';
    }

    // Main content: padding bottom so last content isn't hidden behind nav
    if (main) {
      main.style.paddingBottom = (NAV_H + 16) + 'px';
    }

    // Nav: flex item at the bottom, NO position:fixed
    nav.style.cssText =
      'display:flex;flex-direction:column;flex-shrink:0;' +
      'background:#0f2318;border-top:1px solid rgba(255,255,255,0.08);' +
      'height:' + NAV_H + 'px;z-index:200;';
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.body.appendChild(nav);
    document.body.appendChild(backdrop);
    applyMobileLayout();
  });

  window.addEventListener('resize', applyMobileLayout);

  // Sidebar toggle
  window.toggleMobileSidebar = function () {
    const sidebar = document.querySelector('aside.sidebar');
    if (!sidebar) return;
    sidebar.classList.contains('open') ? closeMobileSidebar() : openMobileSidebar();
  };

  function openMobileSidebar() {
    const sidebar = document.querySelector('aside.sidebar');
    if (!sidebar) return;
    sidebar.style.cssText =
      'display:flex;flex-direction:column;position:fixed;top:0;left:0;' +
      'width:270px;height:100vh;z-index:300;padding-top:1.5rem;' +
      'background:#0f2318;border-right:1px solid rgba(255,255,255,0.07);' +
      'overflow-y:auto;box-shadow:6px 0 40px rgba(0,0,0,0.6);' +
      'animation:sidebarIn 0.22s ease;';
    backdrop.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  function closeMobileSidebar() {
    const sidebar = document.querySelector('aside.sidebar');
    if (sidebar) {
      sidebar.style.display = 'none';
      sidebar.classList.remove('open');
    }
    backdrop.classList.remove('open');
    // Restore body overflow for the flex layout
    document.body.style.overflow = 'hidden';
  }
})();
