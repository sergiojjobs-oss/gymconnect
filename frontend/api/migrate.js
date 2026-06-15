module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  try {
    const url = process.env.DATABASE_URL;
    const response = await fetch(`https://${url.match(/@([^/]+)\//)[1]}/sql`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Neon-Connection-String': url },
      body: JSON.stringify({
        query: `ALTER TABLE progreso_cliente ALTER COLUMN entrenador_id DROP NOT NULL`
      })
    });
    const data = await response.json();
    res.json({ ok: true, data });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
};
