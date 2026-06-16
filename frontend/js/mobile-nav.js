// Mobile navigation — hamburger button in header that opens the sidebar overlay
(function () {
  const path = window.location.pathname;
  const isEntrenador = path.includes('/entrenador/');
  const isCliente    = path.includes('/cliente/') || path.endsWith('/buscar.html');
  if (!isEntrenador && !isCliente) return;

  // Backdrop for sidebar overlay
  const backdrop = document.createElement('div');
  backdrop.className = 'sidebar-backdrop';
  backdrop.addEventListener('click', closeMobileSidebar);

  function applyMobileLayout() {
    if (window.innerWidth > 900) {
      // Desktop: restore sidebar, remove backdrop
      const sidebar = document.querySelector('aside.sidebar');
      if (sidebar) { sidebar.style.cssText = ''; }
      backdrop.classList.remove('open');
      return;
    }

    const layout  = document.querySelector('.dashboard-layout');
    if (!layout) return;

    // Body: flex column filling viewport
    document.body.style.cssText =
      'display:flex;flex-direction:column;height:100vh;height:100dvh;overflow:hidden;margin:0;';

    const header = document.querySelector('header');
    if (header) header.style.cssText = 'flex-shrink:0;position:relative;height:64px;';

    // Dashboard layout: fills remaining space, scrolls internally
    layout.style.cssText =
      'display:grid;grid-template-columns:1fr;flex:1;overflow-y:auto;' +
      'overflow-x:hidden;-webkit-overflow-scrolling:touch;min-height:0;';

    // Hide sidebar by default
    const sidebar = document.querySelector('aside.sidebar');
    if (sidebar && !sidebar.classList.contains('open')) {
      sidebar.style.display = 'none';
    }
  }

  function injectHamburger() {
    const header = document.querySelector('header .container, header');
    if (!header || header.querySelector('.mobile-hamburger')) return;

    const btn = document.createElement('button');
    btn.className = 'mobile-hamburger';
    btn.setAttribute('aria-label', 'Menú');
    btn.innerHTML = '<span></span><span></span><span></span>';
    btn.addEventListener('click', toggleMobileSidebar);

    // Style injected button
    const style = document.createElement('style');
    style.textContent = `
      .mobile-hamburger {
        display: none;
        flex-direction: column; gap: 5px;
        background: none; border: none; cursor: pointer;
        padding: 0.4rem; border-radius: 6px;
        margin-left: auto;
      }
      .mobile-hamburger span {
        display: block; width: 24px; height: 2.5px;
        background: #ffffff; border-radius: 3px;
        transition: all 0.18s ease;
      }
      .mobile-hamburger:hover { background: rgba(74,222,128,0.12); }
      @media (max-width: 900px) {
        .mobile-hamburger { display: flex; }
        /* hide desktop nav links inside dashboard header if any */
        header nav { display: none !important; }
      }
      @keyframes sidebarIn {
        from { transform: translateX(-100%); opacity: 0; }
        to   { transform: translateX(0);     opacity: 1; }
      }
    `;
    document.head.appendChild(style);
    header.appendChild(btn);
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.body.appendChild(backdrop);
    injectHamburger();
    applyMobileLayout();
  });

  window.addEventListener('resize', applyMobileLayout);

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
    sidebar.classList.add('open');
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
    document.body.style.overflow = 'hidden';
  }
})();
