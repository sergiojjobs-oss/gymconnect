/**
 * backend-wakeup.js
 * Detecta cuando el servidor de Render está durmiendo y muestra
 * un overlay amigable mientras arranca (~30 s en el tier gratuito).
 */
(function () {
  const CHECK_INTERVAL = 4000; // ms entre reintentos
  const MAX_WAIT = 90000;      // tiempo máximo de espera (90 s)
  const OVERLAY_ID = 'gc-wakeup-overlay';

  // Obtener la URL base del backend desde config.js (variable global API)
  function getApiBase() {
    return typeof API !== 'undefined' ? API : 'https://gymconnect-0td0.onrender.com';
  }

  function injectOverlay() {
    if (document.getElementById(OVERLAY_ID)) return;

    const style = document.createElement('style');
    style.textContent = `
      #${OVERLAY_ID} {
        position: fixed; inset: 0; z-index: 9999;
        background: rgba(10,10,10,0.92);
        backdrop-filter: blur(8px);
        display: flex; align-items: center; justify-content: center;
        padding: 1.5rem;
        animation: gcFadeIn 0.3s ease;
      }
      @keyframes gcFadeIn { from { opacity: 0; } to { opacity: 1; } }
      #${OVERLAY_ID} .gc-box {
        background: #111111;
        border: 1px solid #262626;
        border-radius: 10px;
        padding: 2.5rem 2rem;
        max-width: 360px;
        width: 100%;
        text-align: center;
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
        color: #ededed;
      }
      #${OVERLAY_ID} .gc-icon {
        font-size: 2.5rem;
        display: block;
        margin-bottom: 1.25rem;
        animation: gcPulse 2s ease infinite;
      }
      @keyframes gcPulse {
        0%,100% { opacity: 1; transform: scale(1); }
        50%      { opacity: 0.6; transform: scale(0.9); }
      }
      #${OVERLAY_ID} h2 {
        font-size: 1rem; font-weight: 700;
        letter-spacing: -0.3px; margin-bottom: 0.5rem;
        color: #ededed;
      }
      #${OVERLAY_ID} p {
        font-size: 0.82rem; color: #a3a3a3;
        line-height: 1.65; margin-bottom: 1.5rem;
      }
      #${OVERLAY_ID} .gc-bar-wrap {
        height: 3px; background: #1c1c1c;
        border-radius: 99px; overflow: hidden; margin-bottom: 1rem;
      }
      #${OVERLAY_ID} .gc-bar {
        height: 100%; background: #22c55e;
        border-radius: 99px; width: 0%;
        transition: width 0.4s ease;
      }
      #${OVERLAY_ID} .gc-status {
        font-size: 0.72rem; color: #525252; margin-top: 0.25rem;
      }
    `;
    document.head.appendChild(style);

    const overlay = document.createElement('div');
    overlay.id = OVERLAY_ID;
    overlay.innerHTML = `
      <div class="gc-box">
        <span class="gc-icon">☕</span>
        <h2>Despertando el servidor…</h2>
        <p>El servidor está en reposo para ahorrar recursos.<br>
           Tardará unos <strong>30 segundos</strong> en arrancar.</p>
        <div class="gc-bar-wrap"><div class="gc-bar" id="gc-bar-fill"></div></div>
        <div class="gc-status" id="gc-status-txt">Conectando…</div>
      </div>
    `;
    document.body.appendChild(overlay);
  }

  function removeOverlay() {
    const el = document.getElementById(OVERLAY_ID);
    if (el) {
      el.style.transition = 'opacity 0.4s';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 400);
    }
  }

  function setProgress(pct, msg) {
    const bar = document.getElementById('gc-bar-fill');
    const txt = document.getElementById('gc-status-txt');
    if (bar) bar.style.width = Math.min(pct, 98) + '%';
    if (txt) txt.textContent = msg || '';
  }

  async function pingBackend() {
    const url = getApiBase() + '/api/ping';
    const res = await fetch(url, { method: 'GET', cache: 'no-store', signal: AbortSignal.timeout(5000) });
    return res.ok;
  }

  async function waitForBackend() {
    const startTime = Date.now();
    let attempt = 0;

    while (Date.now() - startTime < MAX_WAIT) {
      attempt++;
      const elapsed = Date.now() - startTime;
      const pct = Math.min((elapsed / MAX_WAIT) * 100, 95);
      const secsLeft = Math.max(0, Math.round((MAX_WAIT - elapsed) / 1000));
      setProgress(pct, `Intento ${attempt} — arrancando (~${secsLeft} s restantes)…`);

      try {
        const ok = await pingBackend();
        if (ok) {
          setProgress(100, '¡Servidor listo!');
          await new Promise(r => setTimeout(r, 500));
          removeOverlay();
          return;
        }
      } catch (_) {
        // servidor aún durmiendo
      }
      await new Promise(r => setTimeout(r, CHECK_INTERVAL));
    }

    // Tiempo agotado — dejar el overlay pero cambiar mensaje
    const txt = document.getElementById('gc-status-txt');
    if (txt) txt.textContent = 'El servidor tarda más de lo normal. Recarga la página en un momento.';
  }

  // Intento inicial — si el backend responde rápido, no mostramos nada
  async function init() {
    let firstOk = false;
    try {
      firstOk = await Promise.race([
        pingBackend(),
        new Promise((_, reject) => setTimeout(() => reject(new Error('timeout')), 3500))
      ]);
    } catch (_) {
      firstOk = false;
    }

    if (!firstOk) {
      injectOverlay();
      await waitForBackend();
    }
  }

  // Solo ejecutar en páginas que usan el backend (no en landing pública si no hace fetch)
  // Se inicia cuando el DOM está listo
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
