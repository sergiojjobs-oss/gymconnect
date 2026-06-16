// Aplica el color del plan del entrenador al dashboard y bloquea funciones no disponibles
(async function() {
  const token = localStorage.getItem('gc_token');
  if (!token) return;

  try {
    const res = await fetch(`${API}/api/suscripcion/estado`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    if (!res.ok) return;
    const { plan } = await res.json();

    const themes = {
      FREE:  { h: '142', s: '72%', accent: '#16a34a', accentBg: 'rgba(22,163,74,0.15)',  accentText: '#4ade80' },
      PRO:   { h: '217', s: '91%', accent: '#2563eb', accentBg: 'rgba(37,99,235,0.15)',  accentText: '#60a5fa' },
      ELITE: { h: '263', s: '70%', accent: '#7c3aed', accentBg: 'rgba(139,92,246,0.15)', accentText: '#a78bfa' }
    };

    const t = themes[plan] || themes.FREE;
    const root = document.documentElement;
    root.style.setProperty('--plan-accent',    t.accent);
    root.style.setProperty('--plan-accent-bg', t.accentBg);
    root.style.setProperty('--plan-accent-txt',t.accentText);

    // Estilos dinámicos de sidebar y acentos
    const style = document.createElement('style');
    style.textContent = `
      .sidebar a.active { background: ${t.accentBg} !important; color: ${t.accentText} !important; }
      .sidebar a:hover:not(.locked) { background: ${t.accentBg.replace('0.15','0.08')} !important; color: ${t.accentText} !important; }
      .btn-primary, .btn.btn-primary { background: linear-gradient(135deg, ${t.accent}, ${t.accent}cc) !important; }
      .stat-value, [style*="color:var(--green)"], [style*="color: var(--green)"] { color: ${t.accent} !important; }
      .plan-pill { display:inline-flex;align-items:center;gap:0.4rem;padding:0.25rem 0.75rem;border-radius:99px;font-size:0.72rem;font-weight:800;text-transform:uppercase;letter-spacing:1px;background:${t.accentBg};color:${t.accent};border:1px solid ${t.accent}44; }
      .sidebar a.locked { opacity:0.4;cursor:not-allowed;pointer-events:none;position:relative; }
      .sidebar a.locked .lock-badge { display:inline-flex;align-items:center;font-size:0.6rem;font-weight:800;text-transform:uppercase;letter-spacing:0.5px;background:rgba(124,58,237,0.15);color:#a78bfa;border:1px solid rgba(124,58,237,0.3);border-radius:99px;padding:0.1rem 0.4rem;margin-left:auto;flex-shrink:0; }
      .sidebar a.locked-tooltip:hover::after { content:attr(data-tooltip);position:fixed;background:#1e293b;color:#f8fafc;font-size:0.75rem;padding:0.4rem 0.75rem;border-radius:8px;white-space:nowrap;z-index:9999;box-shadow:0 4px 16px rgba(0,0,0,0.3);border:1px solid rgba(255,255,255,0.1); }
    `;
    document.head.appendChild(style);

    // Badge de plan en sidebar
    const sidebar = document.querySelector('.sidebar');
    if (sidebar && !sidebar.querySelector('.plan-pill')) {
      const label = plan === 'FREE' ? '✓ Free' : plan === 'PRO' ? '⭐ Pro' : '👑 Elite';
      const pill = document.createElement('div');
      pill.className = 'plan-pill';
      pill.style.margin = '1rem 0.75rem 0.25rem';
      pill.textContent = label;
      sidebar.prepend(pill);
    }

    // Definir qué páginas requieren qué plan
    // FREE puede acceder a: perfil, rutinas, chat, clientes, ajustes, planes
    // PRO añade: fichas-cliente, agenda, ejercicios, resultados/progreso, nutricion
    // ELITE añade: estadisticas
    const lockConfig = {
      FREE: [
        { href: 'ficha-cliente.html',  requiere: 'PRO'  },
        { href: 'agenda.html',         requiere: 'PRO'  },
        { href: 'ejercicios.html',     requiere: 'PRO'  },
        { href: 'resultados.html',     requiere: 'PRO'  },
        { href: 'nutricion.html',      requiere: 'PRO'  },
        { href: 'estadisticas.html',   requiere: 'ELITE'},
      ],
      PRO: [
        { href: 'estadisticas.html',   requiere: 'ELITE'},
      ],
      ELITE: []
    };

    const toLock = lockConfig[plan] || [];
    if (sidebar && toLock.length > 0) {
      sidebar.querySelectorAll('a').forEach(a => {
        const href = a.getAttribute('href') || '';
        const match = toLock.find(l => href.includes(l.href));
        if (match) {
          a.classList.add('locked');
          a.removeAttribute('href');
          const badge = document.createElement('span');
          badge.className = 'lock-badge';
          badge.textContent = match.requiere === 'ELITE' ? '👑 Elite' : '⭐ Pro';
          a.appendChild(badge);
          a.title = `Requiere plan ${match.requiere}`;
        }
      });
    }

  } catch {}
})();
