/**
 * GymConnect Chat Widget — floating SaaS-style chat component.
 * Include this script in any authenticated page. Requires:
 *   - config.js (exposes API global)
 *   - auth-guard.js (gc_user / gc_token in localStorage)
 */
(function () {
  'use strict';

  // ── Guard: only mount once ──────────────────────────────────────
  if (document.getElementById('gc-chat-widget')) return;

  const user  = JSON.parse(localStorage.getItem('gc_user') || 'null');
  const token = localStorage.getItem('gc_token');
  if (!user || !token) return;

  // ── Inject CDN deps if not present ─────────────────────────────
  function loadScript(src, cb) {
    if (document.querySelector(`script[src="${src}"]`)) { cb(); return; }
    const s = document.createElement('script');
    s.src = src; s.onload = cb; document.head.appendChild(s);
  }

  // ── Inject CSS ─────────────────────────────────────────────────
  const style = document.createElement('style');
  style.textContent = `
    /* ── Widget toggle ─────────────────────────────────────────── */
    .gc-toggle {
      position: fixed; bottom: 1.5rem; right: 1.5rem;
      width: 52px; height: 52px; border-radius: 50%;
      background: var(--brand, #16a34a); border: none; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      z-index: 9990;
      box-shadow: 0 4px 20px rgba(22,163,74,0.35);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      color: #fff;
    }
    .gc-toggle:hover { transform: scale(1.06); box-shadow: 0 6px 28px rgba(22,163,74,0.5); }
    .gc-toggle svg { width: 22px; height: 22px; pointer-events: none; }
    .gc-toggle .ic-close { display: none; }
    .gc-toggle.open .ic-chat  { display: none; }
    .gc-toggle.open .ic-close { display: block; }
    .gc-badge {
      position: absolute; top: -2px; right: -2px;
      min-width: 17px; height: 17px; padding: 0 4px;
      background: #dc2626; color: #fff; border-radius: 99px;
      font-size: 0.6rem; font-weight: 700;
      display: none; align-items: center; justify-content: center;
      border: 2px solid var(--bg, #0a0a0a); font-family: inherit;
    }
    .gc-badge.show { display: flex; }

    /* ── Widget panel ──────────────────────────────────────────── */
    .gc-panel {
      position: fixed;
      bottom: calc(1.5rem + 52px + 0.85rem);
      right: 1.5rem;
      width: 380px;
      height: 540px;
      background: var(--surface, #111);
      border: 1px solid var(--border, #262626);
      border-radius: 12px;
      overflow: hidden;
      box-shadow:
        0 0 0 1px rgba(255,255,255,0.04),
        0 16px 56px rgba(0,0,0,0.5),
        0 4px 12px rgba(0,0,0,0.3);
      display: flex; flex-direction: column;
      z-index: 9989;
      transform: translateY(10px) scale(0.98);
      opacity: 0; pointer-events: none;
      transition: transform 0.22s cubic-bezier(0.34,1.56,0.64,1), opacity 0.18s ease;
      transform-origin: bottom right;
    }
    .gc-panel.open {
      transform: translateY(0) scale(1);
      opacity: 1; pointer-events: all;
    }

    /* ── Header ───────────────────────────────────────────────── */
    .gc-header {
      display: flex; align-items: center; gap: 0.6rem;
      padding: 0.75rem 1rem;
      background: var(--surface-2, #161616);
      border-bottom: 1px solid var(--border-subtle, #1a1a1a);
      flex-shrink: 0; min-height: 52px;
    }
    .gc-back {
      width: 28px; height: 28px; border-radius: 6px;
      background: none; border: none; color: var(--text-2, #a3a3a3);
      cursor: pointer; display: none; align-items: center; justify-content: center;
      transition: background 0.12s, color 0.12s; flex-shrink: 0;
    }
    .gc-back:hover { background: var(--surface-3, #1c1c1c); color: var(--text, #ededed); }
    .gc-hav {
      width: 32px; height: 32px; border-radius: 50%;
      background: var(--brand, #16a34a); color: #fff;
      display: none; align-items: center; justify-content: center;
      font-size: 0.8rem; font-weight: 600; flex-shrink: 0;
    }
    .gc-hinfo { flex: 1; min-width: 0; }
    .gc-htitle { font-size: 0.875rem; font-weight: 600; color: var(--text, #ededed); line-height: 1.2; font-family: inherit; }
    .gc-hstatus {
      font-size: 0.7rem; color: var(--muted, #525252);
      display: none; align-items: center; gap: 0.3rem; margin-top: 0.1rem;
    }
    .gc-hstatus.vis { display: flex; }
    .gc-hdot { width: 5px; height: 5px; border-radius: 50%; background: var(--muted, #525252); flex-shrink: 0; }
    .gc-hstatus.online .gc-hdot { background: #22c55e; }
    .gc-hright { display: flex; align-items: center; gap: 0.35rem; margin-left: auto; flex-shrink: 0; }
    .gc-wspill {
      font-size: 0.6rem; font-weight: 600; padding: 0.15rem 0.45rem;
      border-radius: 99px; white-space: nowrap;
    }
    .gc-wspill.connected    { background: rgba(34,197,94,0.1);  color: #22c55e; border: 1px solid rgba(34,197,94,0.18); }
    .gc-wspill.disconnected { background: rgba(248,113,113,0.08); color: #f87171; border: 1px solid rgba(248,113,113,0.18); }
    .gc-hclose {
      width: 26px; height: 26px; border-radius: 6px;
      background: none; border: none; color: var(--muted, #525252);
      cursor: pointer; font-size: 0.82rem; line-height: 1;
      display: flex; align-items: center; justify-content: center;
      transition: background 0.12s, color 0.12s;
    }
    .gc-hclose:hover { background: var(--surface-3, #1c1c1c); color: var(--text, #ededed); }

    /* ── Contacts view ─────────────────────────────────────────── */
    .gc-view-contacts { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
    .gc-view-contacts.hidden { display: none; }
    .gc-csearch {
      padding: 0.6rem 0.75rem;
      border-bottom: 1px solid var(--border-subtle, #1a1a1a); flex-shrink: 0;
    }
    .gc-csearch input {
      width: 100%; background: var(--bg, #0a0a0a); border: 1px solid var(--border, #262626);
      border-radius: 6px; padding: 0.45rem 0.75rem;
      font-size: 0.8rem; color: var(--text, #ededed); font-family: inherit; outline: none;
      transition: border-color 0.15s;
    }
    .gc-csearch input::placeholder { color: var(--muted, #525252); }
    .gc-csearch input:focus { border-color: var(--brand, #16a34a); }
    .gc-clist { overflow-y: auto; flex: 1; scrollbar-width: thin; scrollbar-color: var(--border, #262626) transparent; }

    /* Contact item */
    .gc-ci {
      display: flex; align-items: center; gap: 0.75rem;
      padding: 0.7rem 1rem; cursor: pointer;
      border-bottom: 1px solid var(--border-subtle, #1a1a1a);
      transition: background 0.1s; position: relative;
    }
    .gc-ci:last-child { border-bottom: none; }
    .gc-ci:hover { background: rgba(255,255,255,0.025); }
    .gc-ci.active { background: rgba(22,163,74,0.05); }
    .gc-ci:hover .gc-ci-del { display: flex; }
    .gc-cav {
      width: 36px; height: 36px; border-radius: 50%;
      background: var(--brand, #16a34a); color: #fff;
      display: flex; align-items: center; justify-content: center;
      font-size: 0.85rem; font-weight: 600; flex-shrink: 0;
    }
    .gc-cav.grey { background: var(--surface-3, #1c1c1c); color: var(--text-2, #a3a3a3); }
    .gc-ci-info { flex: 1; min-width: 0; }
    .gc-ci-name {
      font-size: 0.83rem; font-weight: 500; color: var(--text, #ededed);
      display: flex; align-items: center; gap: 0.35rem; margin-bottom: 0.15rem;
    }
    .gc-ci-meta { display: flex; align-items: center; gap: 0.4rem; }
    .gc-ci-prev { font-size: 0.74rem; color: var(--muted, #525252); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; }
    .gc-ci-time { font-size: 0.62rem; color: var(--muted, #525252); flex-shrink: 0; }
    .gc-cbadge { background: rgba(22,163,74,0.1); color: #22c55e; border-radius: 99px; font-size: 0.6rem; font-weight: 600; padding: 0.1rem 0.4rem; border: 1px solid rgba(22,163,74,0.15); white-space: nowrap; }
    .gc-cbadge.grey { background: var(--surface-3, #1c1c1c); color: var(--muted, #525252); border-color: var(--border-subtle, #1a1a1a); }
    .gc-udot { width: 7px; height: 7px; border-radius: 50%; background: var(--brand, #16a34a); flex-shrink: 0; }
    .gc-ci-del {
      display: none; background: none; border: none; color: var(--muted, #525252);
      cursor: pointer; font-size: 0.82rem; padding: 0.25rem 0.35rem;
      border-radius: 6px; align-items: center; justify-content: center;
      flex-shrink: 0; transition: background 0.12s, color 0.12s;
    }
    .gc-ci-del:hover { background: rgba(220,38,38,0.1); color: #f87171; }
    .gc-cempty { padding: 2rem 1.25rem; text-align: center; color: var(--muted, #525252); font-size: 0.82rem; line-height: 1.6; font-family: inherit; }

    /* ── Chat view ─────────────────────────────────────────────── */
    .gc-view-chat { display: none; flex-direction: column; flex: 1; overflow: hidden; }
    .gc-view-chat.vis { display: flex; }

    .gc-msgs {
      flex: 1; overflow-y: auto; padding: 1rem 0.9rem;
      display: flex; flex-direction: column; gap: 0.25rem;
      scrollbar-width: thin; scrollbar-color: var(--border, #262626) transparent;
    }
    .gc-msgs::-webkit-scrollbar { width: 3px; }
    .gc-msgs::-webkit-scrollbar-thumb { background: var(--border, #262626); border-radius: 99px; }

    .gc-msg { display: flex; flex-direction: column; max-width: 78%; position: relative; }
    .gc-msg.mine   { align-self: flex-end;   align-items: flex-end; }
    .gc-msg.theirs { align-self: flex-start; align-items: flex-start; }
    .gc-mrow { display: flex; align-items: flex-end; gap: 0.3rem; }
    .gc-msg.mine .gc-mrow { flex-direction: row-reverse; }
    .gc-mbubble {
      padding: 0.55rem 0.85rem; border-radius: 12px;
      font-size: 0.84rem; line-height: 1.5; font-family: inherit;
      word-break: break-word; cursor: pointer; user-select: text;
    }
    .gc-msg.mine .gc-mbubble {
      background: var(--brand, #16a34a); color: #fff;
      border-bottom-right-radius: 4px;
    }
    .gc-msg.theirs .gc-mbubble {
      background: var(--surface-3, #1c1c1c); color: var(--text, #ededed);
      border: 1px solid var(--border-subtle, #1a1a1a);
      border-bottom-left-radius: 4px;
    }
    .gc-mtime {
      font-size: 0.62rem; color: var(--muted, #525252);
      margin-top: 0.2rem; padding: 0 0.1rem;
      display: flex; align-items: center; gap: 0.2rem; font-family: inherit;
    }
    .gc-tick { font-size: 0.68rem; }
    .gc-tick.read { color: #22c55e; }
    .gc-mquote {
      background: rgba(0,0,0,0.18); border-left: 2px solid rgba(255,255,255,0.25);
      border-radius: 4px; padding: 0.28rem 0.55rem; margin-bottom: 0.35rem;
      font-size: 0.75rem; color: rgba(255,255,255,0.6); line-height: 1.4;
    }
    .gc-msg.theirs .gc-mquote { border-left-color: var(--brand, #16a34a); color: var(--text-2, #a3a3a3); background: rgba(0,0,0,0.1); }
    .gc-mquote strong { display: block; font-size: 0.68rem; color: #22c55e; margin-bottom: 0.08rem; }
    .gc-mdel {
      padding: 0.45rem 0.75rem; border-radius: 10px;
      font-size: 0.78rem; color: var(--muted, #525252); font-style: italic;
      border: 1px solid var(--border-subtle, #1a1a1a); background: transparent; font-family: inherit;
    }
    .gc-datesep {
      align-self: center; font-size: 0.63rem; font-weight: 500;
      color: var(--muted, #525252); background: var(--surface-2, #161616);
      padding: 0.15rem 0.6rem; border-radius: 99px;
      margin: 0.5rem 0; border: 1px solid var(--border-subtle, #1a1a1a); font-family: inherit;
    }
    .gc-msys {
      align-self: center; text-align: center; font-size: 0.72rem;
      color: var(--muted, #525252); padding: 0.35rem 0.75rem; font-family: inherit;
    }
    .gc-typing {
      align-self: flex-start; display: none;
      background: var(--surface-3, #1c1c1c); border: 1px solid var(--border-subtle, #1a1a1a);
      border-radius: 12px; border-bottom-left-radius: 4px;
      padding: 0.55rem 0.85rem; margin-bottom: 0.25rem;
    }
    .gc-typing span {
      display: inline-block; width: 6px; height: 6px; border-radius: 50%;
      background: var(--muted, #525252); margin: 0 1.5px;
      animation: gcTyping 1.2s infinite;
    }
    .gc-typing span:nth-child(2) { animation-delay: 0.2s; }
    .gc-typing span:nth-child(3) { animation-delay: 0.4s; }
    @keyframes gcTyping { 0%,60%,100% { transform: translateY(0); } 30% { transform: translateY(-5px); } }

    .gc-mactions { display: none; gap: 0.2rem; align-items: center; flex-shrink: 0; }
    .gc-msg:hover .gc-mactions { display: flex; }
    .gc-mabtn {
      background: var(--surface-3, #1c1c1c); border: 1px solid var(--border, #262626);
      border-radius: 5px; padding: 0.18rem 0.32rem; font-size: 0.68rem;
      cursor: pointer; color: var(--text-2, #a3a3a3); line-height: 1; font-family: inherit;
      transition: all 0.1s;
    }
    .gc-mabtn:hover { border-color: var(--brand, #16a34a); color: var(--text, #ededed); }
    .gc-mabtn.del:hover { background: rgba(220,38,38,0.1); color: #f87171; border-color: rgba(220,38,38,0.2); }

    /* ── Reply bar ─────────────────────────────────────────────── */
    .gc-replybar {
      display: none; align-items: center; gap: 0.65rem;
      background: var(--surface-2, #161616); border-top: 1px solid var(--border-subtle, #1a1a1a);
      padding: 0.5rem 1rem; flex-shrink: 0;
    }
    .gc-replybar.vis { display: flex; }
    .gc-rind { font-size: 0.85rem; color: var(--brand, #16a34a); flex-shrink: 0; }
    .gc-rcont { flex: 1; min-width: 0; }
    .gc-rlabel { font-size: 0.65rem; font-weight: 600; color: #22c55e; margin-bottom: 0.05rem; font-family: inherit; }
    .gc-rtext  { font-size: 0.75rem; color: var(--text-2, #a3a3a3); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; font-family: inherit; }
    .gc-rclose { background: none; border: none; color: var(--muted, #525252); font-size: 0.82rem; cursor: pointer; padding: 0.2rem; line-height: 1; }
    .gc-rclose:hover { color: #f87171; }

    /* ── Input bar ─────────────────────────────────────────────── */
    .gc-inputbar {
      background: var(--surface-2, #161616); border-top: 1px solid var(--border-subtle, #1a1a1a);
      padding: 0.65rem 0.85rem; display: flex; gap: 0.5rem;
      align-items: flex-end; flex-shrink: 0; position: relative;
    }
    .gc-emojibtn {
      background: none; border: none; font-size: 1.05rem; cursor: pointer;
      padding: 0.3rem; border-radius: 6px; line-height: 1;
      color: var(--muted, #525252); flex-shrink: 0; transition: background 0.12s, color 0.12s;
    }
    .gc-emojibtn:hover { background: var(--surface-3, #1c1c1c); color: var(--text, #ededed); }
    .gc-inputbar textarea {
      flex: 1; background: var(--bg, #0a0a0a); border: 1px solid var(--border, #262626);
      border-radius: 6px; padding: 0.55rem 0.75rem;
      font-size: 0.84rem; font-family: inherit; color: var(--text, #ededed);
      resize: none; max-height: 100px; min-height: 36px;
      line-height: 1.45; outline: none; transition: border-color 0.15s;
    }
    .gc-inputbar textarea::placeholder { color: var(--muted, #525252); }
    .gc-inputbar textarea:focus { border-color: var(--brand, #16a34a); }
    .gc-sendbtn {
      background: var(--brand, #16a34a); color: #fff; border: none;
      border-radius: 6px; padding: 0.55rem 0.85rem;
      font-size: 0.9rem; cursor: pointer; line-height: 1;
      font-family: inherit; flex-shrink: 0; transition: background 0.15s;
    }
    .gc-sendbtn:hover { background: var(--brand-hover, #15803d); }
    .gc-sendbtn:disabled { opacity: 0.4; cursor: not-allowed; }

    /* ── Emoji picker ──────────────────────────────────────────── */
    .gc-epicker {
      position: absolute; bottom: calc(100% + 0.5rem); left: 0.85rem;
      background: var(--surface-2, #161616); border: 1px solid var(--border, #262626);
      border-radius: 8px; padding: 0.6rem;
      display: none; flex-wrap: wrap; gap: 0.2rem;
      width: 260px; max-height: 152px; overflow-y: auto; z-index: 100;
      scrollbar-width: thin;
    }
    .gc-epicker.open { display: flex; }
    .gc-epicker button { background: none; border: none; font-size: 1.1rem; cursor: pointer; padding: 0.2rem; border-radius: 5px; transition: background 0.1s; }
    .gc-epicker button:hover { background: var(--surface-3, #1c1c1c); }

    /* ── Toast ─────────────────────────────────────────────────── */
    @keyframes gcToast { from { transform: translateY(10px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

    /* ── Mobile ────────────────────────────────────────────────── */
    @media (max-width: 520px) {
      .gc-panel {
        width: calc(100vw - 2rem); height: 65dvh;
        right: 1rem; bottom: calc(1rem + 52px + 0.75rem);
        border-radius: 10px;
      }
      .gc-toggle { bottom: 1rem; right: 1rem; }
    }
    @media (max-height: 680px) {
      .gc-panel { height: calc(100dvh - 130px); }
    }
  `;
  document.head.appendChild(style);

  // ── Inject HTML ────────────────────────────────────────────────
  const wrap = document.createElement('div');
  wrap.id = 'gc-chat-widget';
  wrap.innerHTML = `
    <button class="gc-toggle" id="gcToggle" aria-label="Chat">
      <svg class="ic-chat" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
      </svg>
      <svg class="ic-close" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
      </svg>
      <span class="gc-badge" id="gcBadge">0</span>
    </button>

    <div class="gc-panel" id="gcPanel">
      <div class="gc-header">
        <button class="gc-back" id="gcBack">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <div class="gc-hav" id="gcHav"></div>
        <div class="gc-hinfo">
          <div class="gc-htitle" id="gcHtitle">Mensajes</div>
          <div class="gc-hstatus" id="gcHstatus">
            <span class="gc-hdot"></span>
            <span id="gcChatStatus">En línea</span>
          </div>
        </div>
        <div class="gc-hright">
          <span id="gcWsPill" class="gc-wspill disconnected">● Desconectado</span>
          <button class="gc-hclose" id="gcHclose">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </div>

      <div class="gc-view-contacts" id="gcViewContacts">
        <div class="gc-csearch">
          <input type="text" id="gcSearch" placeholder="Buscar conversación…" autocomplete="off">
        </div>
        <div class="gc-clist" id="gcCList">
          <div class="gc-cempty">Cargando…</div>
        </div>
      </div>

      <div class="gc-view-chat" id="gcViewChat">
        <div class="gc-msgs" id="gcMsgs">
          <div class="gc-typing" id="gcTyping"><span></span><span></span><span></span></div>
        </div>
        <div class="gc-replybar" id="gcReplyBar">
          <span class="gc-rind">↩</span>
          <div class="gc-rcont">
            <div class="gc-rlabel" id="gcRLabel">Respondiendo</div>
            <div class="gc-rtext"  id="gcRText"></div>
          </div>
          <button class="gc-rclose" id="gcRClose">✕</button>
        </div>
        <div class="gc-inputbar">
          <button class="gc-emojibtn" id="gcEmojiBtn">☺</button>
          <div class="gc-epicker" id="gcEPicker"></div>
          <textarea id="gcInput" placeholder="Escribe un mensaje…" rows="1"></textarea>
          <button class="gc-sendbtn" id="gcSendBtn">↑</button>
        </div>
      </div>
    </div>
  `;
  document.body.appendChild(wrap);

  // ── State ──────────────────────────────────────────────────────
  let stomp = null, contactoActivo = null, lastMsgDate = null;
  let replyingTo = null, typingTimer = null, typingHideTimer = null;
  let unreadTotal = 0, msgIdCounter = 0, wakeAttempted = false;
  let toastTimer = null;

  const EMOJIS = ['😀','😂','🥰','😍','😎','🤩','😅','😭','😡','🥲','👍','👎','❤️','🔥','💪','🏋️','🎯','✅','⭐','🙌','🎉','💯','😤','🙏','🤔','😴','🤣','😊','😇','🥳','💥','🚀','⚡','🌟','💎','🏆','🥇','💚','✨'];

  // ── DOM refs ───────────────────────────────────────────────────
  const $ = id => document.getElementById(id);

  // ── Toggle ─────────────────────────────────────────────────────
  function toggleWidget() {
    const panel = $('gcPanel'), toggle = $('gcToggle');
    const isOpen = panel.classList.toggle('open');
    toggle.classList.toggle('open', isOpen);
    if (isOpen) { unreadTotal = 0; $('gcBadge').classList.remove('show'); $('gcBadge').textContent = '0'; }
  }

  window.openChatWidget = function () {
    if (!$('gcPanel').classList.contains('open')) toggleWidget();
  };

  window.openChatWithContact = function (id, nombre) {
    openChatWidget();
    let intentos = 0;
    function tryOpen() {
      const item = document.querySelector(`.gc-ci[data-id="${id}"]`);
      if (item) {
        abrirChat({ id: +id, nombre });
      } else if (intentos++ < 10) {
        setTimeout(tryOpen, 300);
      } else {
        abrirChat({ id: +id, nombre });
      }
    }
    tryOpen();
  };

  $('gcToggle').onclick = toggleWidget;
  $('gcHclose').onclick = toggleWidget;
  $('gcBack').onclick   = volverLista;
  $('gcRClose').onclick = cancelarRespuesta;
  $('gcSearch').oninput = e => filtrar(e.target.value);
  $('gcEmojiBtn').onclick = toggleEmojis;
  $('gcSendBtn').onclick  = enviar;
  $('gcInput').oninput    = function () { autoResize(this); enviarTyping(); };
  $('gcInput').onkeydown  = function (e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); enviar(); }
  };

  // ── WebSocket ──────────────────────────────────────────────────
  function setWsPill(text, cls) {
    const el = $('gcWsPill');
    if (el) { el.textContent = text; el.className = 'gc-wspill ' + cls; }
  }

  async function wakeBackend() {
    if (wakeAttempted) return; wakeAttempted = true;
    try { await fetch(`${API}/api/chat/contactos`, { headers: { Authorization: 'Bearer ' + token }, signal: AbortSignal.timeout(8000) }); } catch {}
  }

  function conectarWS() {
    setWsPill('● Conectando…', 'disconnected');
    stomp = new StompJs.Client({
      webSocketFactory: () => new SockJS(`${API}/ws`),
      connectHeaders: { Authorization: 'Bearer ' + token },
      reconnectDelay: 4000,
      onConnect: () => {
        wakeAttempted = false;
        setWsPill('● Conectado', 'connected');
        stomp.subscribe(`/user/${user.email}/queue/mensajes`, f => recibirMensaje(JSON.parse(f.body)));
        stomp.subscribe(`/user/${user.email}/queue/typing`,   f => recibirTyping(JSON.parse(f.body)));
        stomp.subscribe(`/user/${user.email}/queue/presencia`, f => recibirPresencia(JSON.parse(f.body)));
      },
      onDisconnect:     () => { setWsPill('● Reconectando…', 'disconnected'); wakeBackend(); },
      onStompError:     () => { setWsPill('● Reconectando…', 'disconnected'); wakeBackend(); },
      onWebSocketError: () => { setWsPill('● Reconectando…', 'disconnected'); wakeBackend(); },
    });
    stomp.activate();
  }

  // ── Contacts ───────────────────────────────────────────────────
  async function cargarContactos() {
    let contactos = [];
    try {
      const res = await fetch(`${API}/api/chat/contactos`, { headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) contactos = await res.json();
    } catch {}

    const lista = $('gcCList');
    lista.innerHTML = '';
    if (!contactos.length) {
      const esE = user?.rol === 'ENTRENADOR';
      lista.innerHTML = `<div class="gc-cempty">${esE ? 'Aún no tienes clientes.' : 'Sin contactos. Contrata un entrenador para chatear.'}</div>`;
      return;
    }
    contactos.forEach(c => {
      const div = document.createElement('div');
      div.className = 'gc-ci';
      div.dataset.id = c.id;
      const ns = escH(c.nombre);
      const badge = c.esPagador ? `<span class="gc-cbadge">Cliente</span>` : `<span class="gc-cbadge grey">Contacto</span>`;
      div.innerHTML = `
        <div class="gc-cav${c.esPagador ? '' : ' grey'}">${ns.charAt(0)}</div>
        <div class="gc-ci-info">
          <div class="gc-ci-name">${ns}${badge}</div>
          <div class="gc-ci-meta">
            <div class="gc-ci-prev">${escH(c.ultimoMensaje || 'Sin mensajes')}</div>
            ${c.ultimoMensajeFecha ? `<span class="gc-ci-time">${formatHora(c.ultimoMensajeFecha)}</span>` : ''}
          </div>
        </div>
        <button class="gc-ci-del" data-del="${c.id}">✕</button>
      `;
      div.addEventListener('click', () => abrirChat(c));
      div.querySelector('.gc-ci-del').addEventListener('click', e => eliminarConversacion(e, c.id));
      lista.appendChild(div);
    });
  }

  // ── Open chat ──────────────────────────────────────────────────
  async function abrirChat(contacto) {
    contactoActivo = contacto; lastMsgDate = null; replyingTo = null;

    $('gcBack').style.display = 'flex';
    $('gcHav').textContent = escH(contacto.nombre.charAt(0));
    $('gcHav').style.display = 'flex';
    $('gcHtitle').textContent = contacto.nombre;
    $('gcHstatus').classList.add('vis');

    const wsOk = stomp?.connected;
    $('gcChatStatus').textContent = wsOk ? 'En línea' : 'Conectando…';
    $('gcHstatus').className = 'gc-hstatus vis' + (wsOk ? ' online' : '');

    $('gcViewContacts').classList.add('hidden');
    $('gcViewChat').classList.add('vis');

    document.querySelectorAll('.gc-ci').forEach(el => {
      el.classList.toggle('active', el.dataset.id == contacto.id);
      if (el.dataset.id == contacto.id) el.querySelector('.gc-udot')?.remove();
    });

    const msgs = $('gcMsgs');
    msgs.innerHTML = '<div class="gc-typing" id="gcTyping"><span></span><span></span><span></span></div>';
    cancelarRespuesta();
    construirEmojis();

    fetch(`${API}/api/chat/${contacto.id}/leer`, { method: 'POST', headers: { Authorization: 'Bearer ' + token } }).catch(() => {});
    fetch(`${API}/api/chat/presencia/${contacto.id}`, { headers: { Authorization: 'Bearer ' + token } })
      .then(r => r.ok ? r.json() : null)
      .then(d => { if (d) actualizarPresencia(d.online); })
      .catch(() => {});

    try {
      const res = await fetch(`${API}/api/chat/${contacto.id}`, { headers: { Authorization: 'Bearer ' + token } });
      if (res.ok) (await res.json()).forEach(m => mostrarMensaje(m, m.remitenteId === user.id));
    } catch { mostrarSistema('Inicia el servidor para cargar el historial'); }

    const ti = $('gcTyping');
    if (ti) msgs.appendChild(ti);
    scrollDown();
    $('gcInput')?.focus();
  }

  function volverLista() {
    contactoActivo = null;
    $('gcBack').style.display = 'none';
    $('gcHav').style.display = 'none';
    $('gcHtitle').textContent = 'Mensajes';
    $('gcHstatus').classList.remove('vis');
    $('gcViewContacts').classList.remove('hidden');
    $('gcViewChat').classList.remove('vis');
    $('gcMsgs').innerHTML = '<div class="gc-typing" id="gcTyping"><span></span><span></span><span></span></div>';
    cancelarRespuesta();
  }

  // ── Emojis ─────────────────────────────────────────────────────
  function construirEmojis() {
    const p = $('gcEPicker');
    if (!p) return;
    p.innerHTML = EMOJIS.map(e => `<button>${e}</button>`).join('');
    p.querySelectorAll('button').forEach((btn, i) => {
      btn.onclick = () => insertarEmoji(EMOJIS[i]);
    });
  }
  function toggleEmojis() { $('gcEPicker')?.classList.toggle('open'); }
  function insertarEmoji(e) {
    const ta = $('gcInput'); if (!ta) return;
    const pos = ta.selectionStart;
    ta.value = ta.value.slice(0, pos) + e + ta.value.slice(pos);
    ta.selectionStart = ta.selectionEnd = pos + e.length;
    ta.focus(); $('gcEPicker')?.classList.remove('open');
  }

  // ── Typing ─────────────────────────────────────────────────────
  function enviarTyping() {
    if (!stomp?.connected || !contactoActivo) return;
    clearTimeout(typingTimer);
    stomp.publish({ destination: '/app/chat.typing', body: JSON.stringify({ destinatarioId: contactoActivo.id, escribiendo: true }) });
    typingTimer = setTimeout(() => {
      stomp.publish({ destination: '/app/chat.typing', body: JSON.stringify({ destinatarioId: contactoActivo.id, escribiendo: false }) });
    }, 2000);
  }

  function recibirPresencia(data) {
    if (!contactoActivo || data.usuarioId != contactoActivo.id) return;
    actualizarPresencia(data.online);
  }
  function actualizarPresencia(online) {
    $('gcChatStatus').textContent = online ? 'En línea' : 'Desconectado';
    $('gcHstatus').className = 'gc-hstatus vis' + (online ? ' online' : '');
  }
  function recibirTyping(data) {
    if (!contactoActivo || data.remitenteId != contactoActivo.id) return;
    const ind = $('gcTyping');
    if (!ind) return;
    if (data.escribiendo) {
      ind.style.display = 'block';
      $('gcChatStatus').textContent = 'escribiendo…';
      clearTimeout(typingHideTimer);
      typingHideTimer = setTimeout(() => { ind.style.display = 'none'; $('gcChatStatus').textContent = 'En línea'; }, 3000);
    } else {
      ind.style.display = 'none'; $('gcChatStatus').textContent = 'En línea';
    }
    scrollDown();
  }

  // ── Send ───────────────────────────────────────────────────────
  function enviar() {
    const input = $('gcInput'), texto = input?.value.trim();
    if (!texto || !contactoActivo) return;
    if (!stomp?.connected) { wakeBackend(); mostrarSistema('Reconectando… espera un momento'); return; }
    const payload = { destinatarioId: contactoActivo.id, contenido: texto };
    if (replyingTo && !String(replyingTo.id).startsWith('tmp-')) payload.replyToId = replyingTo.id;
    stomp.publish({ destination: '/app/chat.enviar', body: JSON.stringify(payload) });
    mostrarMensaje({ contenido: texto, remitenteId: user.id, fechaEnvio: new Date().toISOString(), replyTo: replyingTo ? { autor: 'Tú', texto: replyingTo.texto } : null }, true);
    scrollDown();
    input.value = ''; input.style.height = 'auto';
    cancelarRespuesta(); $('gcEPicker')?.classList.remove('open');
  }

  // ── Receive ────────────────────────────────────────────────────
  function recibirMensaje(msg) {
    if (contactoActivo && (msg.remitenteId == contactoActivo.id || msg.destinatarioId == contactoActivo.id)) {
      mostrarMensaje(msg, msg.remitenteId === user.id);
      scrollDown();
      if (msg.remitenteId != user.id)
        fetch(`${API}/api/chat/${msg.remitenteId}/leer`, { method: 'POST', headers: { Authorization: 'Bearer ' + token } }).catch(() => {});
    } else if (msg.remitenteId != user.id) {
      playBeep();
      const item = document.querySelector(`.gc-ci[data-id="${msg.remitenteId}"]`);
      if (item && !item.querySelector('.gc-udot')) {
        const dot = document.createElement('span'); dot.className = 'gc-udot';
        item.querySelector('.gc-ci-info')?.appendChild(dot);
      }
      unreadTotal++;
      $('gcBadge').textContent = unreadTotal > 9 ? '9+' : unreadTotal;
      if (!$('gcPanel').classList.contains('open')) $('gcBadge').classList.add('show');
      mostrarToast(msg.remitenteNombre || 'Nuevo mensaje', msg.eliminado ? 'Mensaje eliminado' : (msg.contenido || ''));
    }
    actualizarPreview(msg);
  }

  // ── Render message ─────────────────────────────────────────────
  function mostrarMensaje(msg, esMio) {
    const msgs = $('gcMsgs'); if (!msgs) return;
    const fechaMsg = msg.fechaEnvio ? new Date(msg.fechaEnvio) : new Date();
    const diaKey = fechaMsg.toDateString();
    if (diaKey !== lastMsgDate) {
      lastMsgDate = diaKey;
      const sep = document.createElement('div');
      sep.className = 'gc-datesep'; sep.textContent = formatFecha(fechaMsg);
      const ti = $('gcTyping');
      if (ti) msgs.insertBefore(sep, ti); else msgs.appendChild(sep);
    }
    const id = msg.id || ('tmp-' + (++msgIdCounter));
    const hora = fechaMsg.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
    const tick = esMio ? `<span class="gc-tick${msg.leido ? ' read' : ''}">✓✓</span>` : '';
    const quoteHtml = msg.replyTo
      ? `<div class="gc-mquote"><strong>${escH(msg.replyTo.autor)}</strong>${escH(msg.replyTo.texto?.slice(0,70))}${msg.replyTo.texto?.length > 70 ? '…' : ''}</div>`
      : '';
    const div = document.createElement('div');
    div.className = 'gc-msg ' + (esMio ? 'mine' : 'theirs');
    div.dataset.id = id; div.dataset.texto = msg.contenido;
    if (msg.eliminado) {
      div.innerHTML = `<div class="gc-mdel">Mensaje eliminado</div><span class="gc-mtime">${hora}</span>`;
    } else {
      const te = escH(msg.contenido).replace(/"/g, '&quot;');
      div.innerHTML = `
        <div class="gc-mrow">
          <div class="gc-mbubble">${quoteHtml}${escH(msg.contenido)}</div>
          <div class="gc-mactions">
            <button class="gc-mabtn" data-reply="${id}" data-txt="${te}">↩</button>
            <button class="gc-mabtn" data-copy="${te}">⎘</button>
            ${esMio ? `<button class="gc-mabtn del" data-delm="${id}">✕</button>` : ''}
          </div>
        </div>
        <span class="gc-mtime">${hora} ${tick}</span>
      `;
      div.querySelector('[data-reply]')?.addEventListener('click', () => responderMsg(id, msg.contenido));
      div.querySelector('[data-copy]')?.addEventListener('click', () => navigator.clipboard?.writeText(msg.contenido).catch(() => {}));
      div.querySelector('[data-delm]')?.addEventListener('click', function () { eliminarMensaje(this, id); });
    }
    const ti = $('gcTyping');
    if (ti) msgs.insertBefore(div, ti); else msgs.appendChild(div);
  }

  // ── Actions ────────────────────────────────────────────────────
  function responderMsg(id, texto) {
    replyingTo = { id, texto, autor: contactoActivo?.nombre };
    $('gcRLabel').textContent = 'Respondiendo a ' + replyingTo.autor;
    $('gcRText').textContent = texto;
    $('gcReplyBar').classList.add('vis');
    $('gcInput')?.focus();
  }
  function cancelarRespuesta() {
    replyingTo = null; $('gcReplyBar')?.classList.remove('vis');
  }
  async function eliminarMensaje(btn, id) {
    const msgEl = btn.closest('.gc-msg');
    if (!id.startsWith('tmp-')) {
      const r = await fetch(`${API}/api/chat/mensaje/${id}`, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } });
      if (!r.ok && r.status !== 404) return;
    }
    const bubble = msgEl.querySelector('.gc-mbubble');
    if (bubble) bubble.outerHTML = '<div class="gc-mdel">Mensaje eliminado</div>';
    msgEl.querySelector('.gc-mactions')?.remove();
  }
  async function eliminarConversacion(e, contactoId) {
    e.stopPropagation();
    if (!confirm('¿Eliminar esta conversación? Se borrarán todos los mensajes.')) return;
    const r = await fetch(`${API}/api/chat/${contactoId}/conversacion`, { method: 'DELETE', headers: { Authorization: 'Bearer ' + token } });
    if (r.ok) { if (contactoActivo?.id == contactoId) volverLista(); cargarContactos(); }
  }

  // ── Helpers ────────────────────────────────────────────────────
  function actualizarPreview(msg) {
    const id = msg.remitenteId === user.id ? msg.destinatarioId : msg.remitenteId;
    const el = document.querySelector(`.gc-ci[data-id="${id}"] .gc-ci-prev`);
    if (el) el.textContent = msg.contenido;
  }
  function autoResize(ta) { ta.style.height = 'auto'; ta.style.height = Math.min(ta.scrollHeight, 100) + 'px'; }
  function scrollDown() { const m = $('gcMsgs'); if (m) m.scrollTop = m.scrollHeight; }
  function escH(s) {
    if (!s) return '';
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\n/g,'<br>');
  }
  function formatFecha(fecha) {
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    const ayer = new Date(hoy); ayer.setDate(ayer.getDate()-1);
    const d = new Date(fecha); d.setHours(0,0,0,0);
    if (d.getTime() === hoy.getTime()) return 'Hoy';
    if (d.getTime() === ayer.getTime()) return 'Ayer';
    return new Date(fecha).toLocaleDateString('es-ES', { day:'numeric', month:'long', year:'numeric' });
  }
  function formatHora(iso) {
    const d = new Date(iso), hoy = new Date(); hoy.setHours(0,0,0,0);
    if (d >= hoy) return d.toLocaleTimeString('es-ES', { hour:'2-digit', minute:'2-digit' });
    const ayer = new Date(hoy); ayer.setDate(ayer.getDate()-1);
    if (d >= ayer) return 'Ayer';
    return d.toLocaleDateString('es-ES', { day:'numeric', month:'short' });
  }
  function filtrar(q) {
    document.querySelectorAll('.gc-ci').forEach(el => {
      const nombre = el.querySelector('.gc-ci-name')?.textContent?.toLowerCase() || '';
      el.style.display = nombre.includes(q.toLowerCase()) ? '' : 'none';
    });
  }
  function mostrarSistema(texto) {
    const msgs = $('gcMsgs'); if (!msgs) return;
    const div = document.createElement('div');
    div.className = 'gc-msys'; div.textContent = texto;
    const ti = $('gcTyping');
    if (ti) msgs.insertBefore(div, ti); else msgs.appendChild(div);
  }
  function playBeep() {
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)();
      const o = ctx.createOscillator(), g = ctx.createGain();
      o.connect(g); g.connect(ctx.destination);
      o.frequency.value = 880;
      g.gain.setValueAtTime(0.1, ctx.currentTime);
      g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
      o.start(); o.stop(ctx.currentTime + 0.3);
    } catch {}
  }
  function mostrarToast(nombre, texto) {
    let toast = document.getElementById('gcToast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'gcToast';
      toast.style.cssText = `
        position:fixed;bottom:calc(1.5rem + 52px + 0.75rem + 540px + 0.5rem);right:1.5rem;
        z-index:9991;background:var(--surface-2,#161616);border:1px solid var(--border,#262626);
        border-radius:8px;padding:0.75rem 1rem;min-width:220px;max-width:300px;
        display:flex;gap:0.65rem;align-items:flex-start;
        animation:gcToast 0.2s ease;cursor:pointer;
        box-shadow:0 4px 20px rgba(0,0,0,0.4);font-family:inherit;
      `;
      toast.onclick = () => toast.remove();
      document.body.appendChild(toast);
    }
    const prev = texto.length > 55 ? texto.slice(0, 55) + '…' : texto;
    toast.innerHTML = `
      <div style="width:32px;height:32px;border-radius:50%;background:var(--brand,#16a34a);display:flex;align-items:center;justify-content:center;font-weight:600;font-size:0.82rem;color:#fff;flex-shrink:0;">${escH(nombre.charAt(0))}</div>
      <div style="flex:1;min-width:0;">
        <div style="font-size:0.78rem;font-weight:600;color:var(--text,#ededed);margin-bottom:0.15rem;">${escH(nombre)}</div>
        <div style="font-size:0.73rem;color:var(--muted,#525252);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${escH(prev)}</div>
      </div>
    `;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast?.remove(), 4000);
  }

  // ── Init ───────────────────────────────────────────────────────
  function init() {
    loadScript('https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js', () => {
      loadScript('https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js', () => {
        wakeBackend();
        cargarContactos().then(() => {
          const params = new URLSearchParams(window.location.search);
          const uid = params.get('userId') || params.get('contacto');
          const nombre = params.get('nombre');
          if (uid) {
            const item = document.querySelector(`.gc-ci[data-id="${uid}"]`);
            if (item) {
              const n = item.querySelector('.gc-ci-name')?.childNodes[0]?.textContent?.trim() || decodeURIComponent(nombre || uid);
              abrirChat({ id: +uid, nombre: n });
            } else if (nombre) {
              abrirChat({ id: +uid, nombre: decodeURIComponent(nombre) });
            }
          }
        });
        conectarWS();
      });
    });
  }

  init();
})();
