// Aplica el color del plan del entrenador al dashboard
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

    // Inyectar estilos dinámicos de sidebar y acentos
    const style = document.createElement('style');
    style.textContent = `
      .sidebar a.active { background: ${t.accentBg} !important; color: ${t.accentText} !important; }
      .sidebar a:hover  { background: ${t.accentBg.replace('0.15','0.08')} !important; color: ${t.accentText} !important; }
      .btn-primary, .btn.btn-primary { background: linear-gradient(135deg, ${t.accent}, ${t.accent}cc) !important; }
      .stat-value, [style*="color:var(--green)"], [style*="color: var(--green)"] { color: ${t.accent} !important; }
      .plan-pill { display:inline-flex;align-items:center;gap:0.4rem;padding:0.25rem 0.75rem;border-radius:99px;font-size:0.72rem;font-weight:800;text-transform:uppercase;letter-spacing:1px;background:${t.accentBg};color:${t.accent};border:1px solid ${t.accent}44; }
    `;
    document.head.appendChild(style);

    // Añadir badge de plan en el sidebar si existe
    const sidebar = document.querySelector('.sidebar');
    if (sidebar && !sidebar.querySelector('.plan-pill')) {
      const label = plan === 'FREE' ? '✓ Free' : plan === 'PRO' ? '⭐ Pro' : '👑 Elite';
      const pill = document.createElement('div');
      pill.className = 'plan-pill';
      pill.style.margin = '1rem 0.75rem 0.25rem';
      pill.textContent = label;
      sidebar.prepend(pill);
    }
  } catch {}
})();
